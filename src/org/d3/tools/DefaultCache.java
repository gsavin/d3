/*
 * This file is part of d3 <http://d3-project.org>.
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
 * Copyright 2010 - 2011 Guilhelm Savin
 */
package org.d3.tools;

public class DefaultCache<K, V> extends Cache<K, V> {

	public DefaultCache(int capacity) {
		super(capacity);
	}

	public V get(K key) {
		try {
			return super.get(key);
		} catch (CacheCreationException e) {
			return null;
		}
	}
	
	protected V createObject(K key) throws CacheCreationException {
		return null;
	}
}
