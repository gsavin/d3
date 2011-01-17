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
package org.d3.template;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Very fast template system.
 * 
 * @author Guilhelm Savin
 * 
 */
public class Template {

	private LinkedList<Node> strings;
	private LinkedList<ReplaceNode> replaces;
	private StringBuilder buffer;

	public Template(String t) {
		strings = new LinkedList<Node>();
		replaces = new LinkedList<ReplaceNode>();
		buffer = new StringBuilder();

		build(t);
	}

	protected void build(String template) {
		strings.clear();
		replaces.clear();

		Pattern rep = Pattern.compile("(\\{%([^%]*|%[^\\}]+)%\\})");
		Matcher m = rep.matcher(template);
		int s = 0;

		while (m.find()) {
			if (s < m.start(1))
				strings.addLast(new Node(template.substring(s, m.start(1))));

			ReplaceNode rn = new ReplaceNode(m.group(2));
			strings.addLast(rn);
			replaces.add(rn);

			s = m.end(1);
		}

		if (s < template.length() - 1)
			strings.addLast(new Node(template.substring(s)));
	}

	public synchronized String toString(Map<String, String> env) {
		buffer.delete(0, buffer.length());
		String str;
		ReplaceNode rn;

		for (int i = 0; i < replaces.size(); i++) {
			rn = replaces.get(i);
			str = env.get(rn.getName());

			rn.setText(str == null ? "" : str);
		}

		for (int i = 0; i < strings.size(); i++)
			buffer.append(strings.get(i).toString());

		return buffer.toString();
	}

	public Map<String, String> reverse(String str) throws ReverseException {
		HashMap<String, String> env = new HashMap<String, String>();
		int s;

		s = 0;

		for (int i = 0; i < strings.size(); i++) {
			Node n = strings.get(i);

			if (n instanceof ReplaceNode) {
				ReplaceNode rn = (ReplaceNode) n;

				if (i < strings.size() - 1) {
					int e = str.indexOf(strings.get(i + 1).text, s);

					if (e < s)
						throw new ReverseException();

					env.put(rn.name, str.substring(s, e));
					s = e;
				} else {
					env.put(rn.name, str.substring(s));
				}
			} else {
				if (!str.startsWith(n.text, s))
					throw new ReverseException();

				s += n.text.length();
			}
		}

		return env;
	}
}
