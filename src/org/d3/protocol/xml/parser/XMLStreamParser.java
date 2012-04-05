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

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Stack;

import org.d3.protocol.xml.XMLParseException;

public class XMLStreamParser {

	public static enum State {
		S01, S02, S03, S04, S05, S06, S07, S08, S09, S10, S11
	}

	protected static final String ID = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	protected Handler handler;
	protected int stack;
	protected State state;

	protected StringBuilder element_name_builder;
	protected StringBuilder attribute_name_builder;
	protected StringBuilder attribute_value_builder;
	protected StringBuilder element_text_builder;

	protected Attributes attributes;

	protected Stack<String> names;

	public XMLStreamParser(Handler handler) {
		this.handler = handler;

		stack = 0;
		state = State.S01;
		element_name_builder = new StringBuilder();
		element_text_builder = new StringBuilder();
		attribute_name_builder = new StringBuilder();
		attribute_value_builder = new StringBuilder();
		attributes = new Attributes();
		names = new Stack<String>();
	}

	public Handler getHandler() {
		return handler;
	}

	public void parse(Reader in) throws IOException, XMLParseException {
		int c;

		while (in.ready() && (c = in.read()) > 0)
			nextChar(c);
	}

	public void parse(CharBuffer buffer) throws XMLParseException {
		while (buffer.hasRemaining())
			nextChar(buffer.get());
	}

	protected void nextChar(int c) throws XMLParseException {
		switch (state) {
		case S01:
			s01(c);
			break;
		case S02:
			s02(c);
			break;
		case S03:
			s03(c);
			break;
		case S04:
			s04(c);
			break;
		case S05:
			s05(c);
			break;
		case S06:
			s06(c);
			break;
		case S07:
			s07(c);
			break;
		case S08:
			s08(c);
			break;
		case S09:
			s09(c);
			break;
		case S10:
			s10(c);
			break;
		case S11:
			s11(c);
			break;
		}
	}

	protected String getAndClear(StringBuilder b) {
		String s = b.toString();
		b.delete(0, b.length());

		return s;
	}

	protected void s01(int c) throws XMLParseException {
		switch (c) {
		case '<':
			state = State.S02;
			break;
		case ' ':
		case '\n':
		case '\r':
			break;
		default:
			throw new XMLParseException("invalid character '%c'", c);
		}
	}

	protected void s02(int c) throws XMLParseException {
		switch (c) {
		case '>':
			elementStart();
			state = State.S04;
			break;
		case '/':
			state = State.S05;
			break;
		case ' ':
			state = State.S03;
			break;
		default:
			if (ID.indexOf(c) < 0)
				throw new XMLParseException("invalid character '%c'", c);
			element_name_builder.appendCodePoint(c);
			break;
		}
	}

	protected void s03(int c) throws XMLParseException {
		switch (c) {
		case ' ':
			break;
		case '>':
			elementStart();
			state = State.S04;
			break;
		default:
			if (ID.indexOf(c) < 0)
				throw new XMLParseException("invalid character '%c'", c);
			attribute_name_builder.appendCodePoint(c);
			state = State.S06;
			break;
		}
	}

	protected void s04(int c) throws XMLParseException {
		switch (c) {
		case '<':
			elementText();
			state = State.S10;
			break;
		default:
			element_text_builder.appendCodePoint(c);
			break;
		}
	}

	protected void s05(int c) throws XMLParseException {
		switch (c) {
		case '>':
			elementStartAndEnd();
			if (stack == 0)
				state = State.S01;
			else {
				stack--;
				state = State.S04;
			}
			break;
		default:
			throw new XMLParseException("invalid character '%c'", c);
		}
	}

	protected void s06(int c) throws XMLParseException {
		switch (c) {
		case '=':
			state = State.S07;
			break;
		default:
			if (ID.indexOf(c) < 0)
				throw new XMLParseException("invalid character '%c'", c);
			attribute_name_builder.appendCodePoint(c);
			break;
		}
	}

	protected void s07(int c) throws XMLParseException {
		switch (c) {
		case '"':
			state = State.S08;
			break;
		default:
			throw new XMLParseException("invalid character '%c'", c);
		}
	}

	protected void s08(int c) throws XMLParseException {
		switch (c) {
		case '"':
			newAttribute();
			state = State.S09;
			break;
		default:
			attribute_value_builder.appendCodePoint(c);
			break;
		}
	}

	protected void s09(int c) throws XMLParseException {
		switch (c) {
		case ' ':
			state = State.S03;
			break;
		case '>':
			elementStart();
			state = State.S04;
			break;
		case '/':
			state = State.S05;
			break;
		default:
			throw new XMLParseException("invalid character '%c'", c);
		}
	}

	protected void s10(int c) throws XMLParseException {
		switch (c) {
		case '/':
			state = State.S11;
			break;
		default:
			if (ID.indexOf(c) < 0)
				throw new XMLParseException("invalid character '%c'", c);
			stack++;
			state = State.S02;
			element_name_builder.appendCodePoint(c);
			break;
		}
	}

	protected void s11(int c) throws XMLParseException {
		switch (c) {
		case '>':
			elementEnd();
			if (stack == 0)
				state = State.S01;
			else {
				stack--;
				state = State.S04;
			}
			break;
		default:
			if (ID.indexOf(c) < 0)
				throw new XMLParseException("invalid character '%c'", c);
			element_name_builder.appendCodePoint(c);
			break;
		}
	}

	protected void elementStart() {
		String name = getAndClear(element_name_builder);

		handler.elementStart(name, attributes.cloneAndClear());
		names.push(name);
	}

	protected void elementStartAndEnd() {
		String name = getAndClear(element_name_builder);

		handler.elementStart(name, attributes.cloneAndClear());
		handler.elementEnd(name);
	}

	protected void elementEnd() throws XMLParseException {
		String name = getAndClear(element_name_builder);

		if (!name.equals(names.peek()))
			throw new XMLParseException("expect end of '%s', got '%s'", names
					.peek(), name);

		names.pop();
		handler.elementEnd(name);
	}

	protected void elementText() {
		String text = getAndClear(element_text_builder);
		handler.elementText(text);
	}

	protected void newAttribute() {
		String key = getAndClear(attribute_name_builder);
		String value = getAndClear(attribute_value_builder);

		attributes.add(key, value);
	}
}
