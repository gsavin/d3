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
package org.ri2c.d3.protocol.xml;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class XMLStanza {
	protected String name;
	protected Map<String, String> attributes;
	protected List<XMLStanza> children;
	protected StringBuffer content;

	public XMLStanza(String name) {
		this.name = name;

		attributes = new HashMap<String, String>();
		children = new LinkedList<XMLStanza>();
		content = null;
	}

	public XMLStanza(String name, String content) {
		this(name);
		appendContent(content);
	}

	public void addAttribute(String key, String value) {
		attributes.put(key, value);
	}

	public XMLStanza addChild(XMLStanza child) {
		children.add(child);
		return this;
	}

	public XMLStanza appendContent(String newContent) {
		if (content == null)
			content = new StringBuffer();

		content.append(newContent);

		return this;
	}

	public String getContent() {
		return content == null ? "" : content.toString();
	}

	public String getName() {
		return name;
	}

	public boolean is(String name) {
		return this.name.equals(name);
	}

	public boolean hasAttribute(String key) {
		return attributes.containsKey(key);
	}

	public boolean checkAttribute(String key, String expectedValue) {
		if (!hasAttribute(key))
			return expectedValue == null;

		return attributes.get(key).equals(expectedValue);
	}

	public int getChildrenCount() {
		return children.size();
	}

	public XMLStanza getChild(int i) {
		return children.get(i);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<").append(name);

		if (attributes.size() > 0) {
			for (String key : attributes.keySet())
				buffer.append(" ").append(key).append("='")
						.append(attributes.get(key)).append("'");
		}

		if (children.size() == 0 && content == null)
			buffer.append("/>");
		else {
			buffer.append(">");

			for (XMLStanza child : children) {
				String childStr = child.toString();
				childStr = childStr.replace("\n", "\n  ");
				buffer.append("\n  ").append(childStr);
			}

			if (children.size() > 0)
				buffer.append("\n");

			if (content != null)
				buffer.append(content.toString());

			buffer.append("</").append(name).append(">");
		}

		return buffer.toString();
	}

	public Iterable<String> attributeKeySet() {
		return attributes.keySet();
	}

	public String getAttribute(String key) {
		return attributes.get(key);
	}
}
