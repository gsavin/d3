/*
 * This file is part of d3.
 * 
 * d3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * d3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with d3.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2010 Guilhelm Savin
 */
package org.d3.protocol.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.modelmbean.XMLParseException;

public class XMLStanzaBuilder {
	private static final Pattern ATTR = Pattern
			.compile("(\\w[\\w:[-]\\d]*)='([^']*)'");

	private static final Pattern stanzaSimple = Pattern
			.compile("<(\\w+[\\w:[-]\\d]*)\\s*((?:\\s+\\w[\\w:[-]\\d]*='[^']*')*)\\s*/>");
	private static final Pattern stanzaBegin = Pattern
			.compile("<(\\w+[\\w:[-]\\d]*)\\s*((?:\\s+\\w[\\w:[-]\\d]*='[^']*')*)\\s*>");

	public static int stanzaEndPosition(String s) {
		int count = 0;
		int i;

		if (s.charAt(0) == '<') {
			count++;
			i = s.indexOf('>');

			if (i == -1)
				return -1;

			if (s.charAt(i - 1) == '/')
				return i + 1;

			while (count > 0) {
				i++;

				if (i >= s.length())
					return -1;

				if (s.charAt(i) == '<') {
					if (s.charAt(i + 1) == '/')
						count--;
					else
						count++;

					while (s.charAt(i) != '>') {
						i++;
						if (i >= s.length())
							return -1;
					}

					if (s.charAt(i - 1) == '/')
						count--;
				} else if (s.charAt(i) == '>' && s.charAt(i - 1) == '/') {
					count--;
				}
			}

			return i + 1;
		}

		return -1;
	}

	public static XMLStanza string2stanza(String xml) throws XMLParseException {
		return string2stanza(XMLStanzaFactory.DEFAULT, xml);
	}

	public static XMLStanza string2stanza(XMLStanzaFactory factory, String xml)
			throws XMLParseException {
		XMLStanza current = null;
		xml = xml.trim();
		int end = stanzaEndPosition(xml);

		if (end < 0)
			throw new XMLParseException(String.format("invalid xml: '%s'", xml));

		xml = xml.substring(0, end);

		if (xml.charAt(0) != '<')
			throw new XMLParseException(String.format("invalid xml: '%s'", xml));

		int i = 1;
		while (xml.charAt(i) != '>')
			i++;

		if (xml.charAt(i - 1) == '/') {
			Matcher m = stanzaSimple.matcher(xml.substring(0, i + 1));

			if (!m.find())
				throw new XMLParseException(String.format("invalid xml: '%s'",
						xml));

			current = factory.newXMLStanza(m.group(1));

			if (m.group(2) != null) {
				Matcher a = ATTR.matcher(m.group(2));

				while (a.find())
					current.addAttribute(a.group(1), a.group(2));
			}
		} else {
			Matcher m = stanzaBegin.matcher(xml.substring(0, i + 1));

			if (!m.find())
				throw new XMLParseException(String.format("invalid xml: '%s'",
						xml));

			current = factory.newXMLStanza(m.group(1));

			if (m.group(2) != null) {
				Matcher a = ATTR.matcher(m.group(2));

				while (a.find())
					current.addAttribute(a.group(1), a.group(2));
			}

			i++;

			while (i < xml.lastIndexOf('<')) {
				int j = i;
				while (xml.charAt(i) != '<')
					i++;

				if (i > j)
					current.appendContent(xml.substring(j, i));

				if (i < xml.lastIndexOf('<')) {
					String xmlChild = xml.substring(i);
					j = stanzaEndPosition(xmlChild);
					xmlChild = xmlChild.substring(0, j);
					current.addChild(string2stanza(factory, xmlChild));
					i += j;
				}
			}
		}

		return current;
	}
	/*
	 * public static void main( String [] args ) { String [] xmls = { "<test/>",
	 * "<test attr1='098'/>", "<test>..</test>",
	 * "<test attr1='bn' attr2=''>...</test>",
	 * "<test attr1=''><child/><child attr1='jh'/><child>adn</child></test>",
	 * "<test/", "<test attr1'098'/>", "<test>../test>",
	 * "<test attr1='bn' attr2=>...</test>",
	 * "<test attr1=''><child/><child attr1='jh'><child>adn</child></test>" };
	 * 
	 * for( String xml : xmls ) { try { System.out.printf( "[test] %s\t", xml );
	 * Object o = string2stanza(xml); System.out.printf( "[ok]%n%s%n", o ); }
	 * catch( XMLParseException e ) { System.out.printf( "[bad xml]%n'%s'%n",
	 * e.getMessage() ); } }
	 * 
	 * String benchXML =
	 * "<message from='someone@xmpp-server.org'><content>bonjour, comment ça va ?</content><presence status='online'><show/></presence></message>"
	 * ; int count = 1000000;
	 * 
	 * System.out.printf("[bench] %d times, %d characters%n", count,
	 * benchXML.length() ); System.out.printf("[bench] string2stanza%n" );
	 * 
	 * int i = count; long m1 = System.currentTimeMillis(); while( i-- > 0 ) {
	 * try { string2stanza(benchXML); } catch( Exception e ) {} } long m2 =
	 * System.currentTimeMillis();
	 * 
	 * System.out.printf("[bench] total   : %d ms%n", ( m2 - m1 ) );
	 * System.out.printf("[bench] average : %f ms%n", ( m2 - m1 ) / (float)
	 * count );
	 * 
	 * System.out.printf("[bench] stanzaEndPosition%n" );
	 * 
	 * i = count;
	 * 
	 * m1 = System.currentTimeMillis(); while( i-- > 0 ) {
	 * stanzaEndPosition(benchXML); } m2 = System.currentTimeMillis();
	 * 
	 * System.out.printf("[bench] total   : %d ms%n", ( m2 - m1 ) );
	 * System.out.printf("[bench] average : %f ms%n", ( m2 - m1 ) / (float)
	 * count ); }
	 */
}
