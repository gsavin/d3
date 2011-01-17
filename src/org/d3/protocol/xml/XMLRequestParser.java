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

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumMap;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.d3.protocol.Request;
import org.d3.request.ObjectCoder.CodingMethod;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class XMLRequestParser extends DefaultHandler {

	public static enum Component {
		REQUEST, SOURCE, TARGET, CALL, ARGS, FUTURE
	}

	EnumMap<Component, String> env;
	Component current;
	StringBuilder builder;
	Stack<Component> stack;
	CodingMethod codingMethod;

	public XMLRequestParser() {
		env = new EnumMap<Component, String>(Component.class);
		stack = new Stack<Component>();
		builder = new StringBuilder();
	}

	public void characters(char[] ch, int start, int length) {
		builder.append(ch, start, length);
	}

	public void endElement(String uri, String localName, String qName) {
		env.put(stack.pop(), builder.toString());
		builder.delete(0, builder.length());
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) {
		Component c = Component.valueOf(qName.toUpperCase());
		stack.push(c);

		if (c == Component.ARGS)
			codingMethod = CodingMethod.valueOf(attributes.getValue("coding"));
	}

	public URI getSource() {
		String s = env.get(Component.SOURCE);

		try {
			return s == null ? null : new URI(s);
		} catch (URISyntaxException e) {
			return null;
		}
	}

	public URI getTarget() {
		String s = env.get(Component.TARGET);

		try {
			return s == null ? null : new URI(s);
		} catch (URISyntaxException e) {
			return null;
		}
	}

	public String getCall() {
		return env.get(Component.CALL);
	}

	public String getArgs() {
		return env.get(Component.ARGS);
	}

	public String getFutureId() {
		return env.get(Component.FUTURE);
	}

	public Request getRequestAndClear() {
		Request r = new Request(getSource(), getTarget(), getCall(),
				codingMethod, getArgs(), getFutureId());

		env.clear();
		codingMethod = null;
		builder.delete(0, builder.length());

		return r;
	}

	public static void main(String... args) throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<request><source>xml://host:port/agency/path/id</source><target>xml://host:port/agency/path/id</target><call>query</call><future>FUTUREID</future><args coding=\"HEXABYTES\">AEFFFDEFAF36778613FDF</args></request>\n";
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

		long m1, m2, m3, m4 = 0, m5 = 0;
		int size = 100000;

		XMLRequestParser xrp = new XMLRequestParser();

		m1 = System.nanoTime();
		for (int i = 0; i < size; i++) {
			m3 = System.nanoTime();
			ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
			m4 += System.nanoTime() - m3;
			parser.parse(in, xrp);
			m3 = System.nanoTime();
			xrp.getRequestAndClear();
			m5 += System.nanoTime() - m3;
		}
		m2 = System.nanoTime();

		System.out.printf("> average %d ns%n> init : %d ns%n> parse : %d ns%n> request : %d ns%n",
				(m2 - m1) / size, m4 / size, (m2 - m1 - m4 - m5) / size, m5 / size);
	}
}
