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

public class XMLParseException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5766479332500566441L;

	public XMLParseException() {
		super();
	}

	public XMLParseException(String msg, Object... args) {
		super(String.format(msg, args));
	}

	public XMLParseException(Throwable t) {
		super(t);
	}
}
