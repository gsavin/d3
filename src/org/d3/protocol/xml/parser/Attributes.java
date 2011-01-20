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
package org.d3.protocol.xml.parser;

import java.util.LinkedList;

public class Attributes {
	protected LinkedList<String> keys;
	protected LinkedList<String> values;
	
	public Attributes() {
		keys = new LinkedList<String>();
		values = new LinkedList<String>();
	}
	
	public Attributes(Attributes attributes) {
		keys = new LinkedList<String>(attributes.keys);
		values = new LinkedList<String>(attributes.values);
	}
	
	public int getAttributeCount() {
		return keys.size();
	}
	
	public String getAttributeName(int index) {
		return keys.get(index);
	}
	
	public String getAttributeValue(int index) {
		return values.get(index);
	}
	
	void add(String key, String value) {
		keys.add(key);
		values.add(value);
	}
	
	Attributes cloneAndClear() {
		Attributes clone = new Attributes(this);
		keys.clear();
		values.clear();
		
		return clone;
	}
}
