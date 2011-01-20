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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Stack;

import org.d3.Console;
import org.d3.protocol.FutureRequest;
import org.d3.protocol.Request;
import org.d3.protocol.request.ObjectCoder.CodingMethod;

public class XMLRequestHandler implements Handler {

	public static enum RequestType {
		REQUEST, FUTURE, NONE
	}

	public static enum Component {
		REQUEST, SOURCE, TARGET, CALL, ARGS, FUTURE, CODING, ID, _TEXT_
	}

	protected static class RequestData {
		URI source;
		URI target;
		String name;
		CodingMethod cm;
		String args;
		String futureId;

		void add(Component parent, Component c, String value) {
			switch (parent) {
			case REQUEST:
				switch (c) {
				case SOURCE:
					try {
						source = new URI(value.trim());
					} catch (URISyntaxException e) {
						Console.exception(e);
					}
					break;
				case TARGET:
					try {
						target = new URI(value.trim());
					} catch (URISyntaxException e) {
						Console.exception(e);
					}
					break;
				case FUTURE:
					futureId = value.trim();
					break;
				case CALL:
					name = value.trim();
					break;
				case ARGS:
					args = value.trim();
					break;
				}
			case SOURCE:
			case TARGET:
			case FUTURE:
			case CALL:
				if (c == Component._TEXT_)
					add(Component.REQUEST, parent, value);
				break;
			case ARGS:
				if (c == Component._TEXT_)
					add(Component.REQUEST, parent, value);
				else if (c == Component.CODING)
					cm = CodingMethod.valueOf(value);
				break;
			}
		}

		Request get() {
			return new Request(source, target, name, cm, args, futureId);
		}

		void clear() {
			source = target = null;
			name = args = futureId = null;
			cm = null;
		}
	}

	protected static class FutureData {
		String futureId;
		CodingMethod coding;
		String value;
		URI target;
		
		void add(Component parent, Component c, String value) {
			switch (parent) {
			case FUTURE:
				switch (c) {
				case ID:
					futureId = value;
					break;
				case CODING:
					coding = CodingMethod.valueOf(value.trim());
					break;
				case TARGET:
					try {
						target = new URI(value.trim());
					} catch (URISyntaxException e) {
						Console.exception(e);
					}
					break;
				case _TEXT_:
					this.value = value.trim();
					break;
				}
			case ID:
			case TARGET:
			case CODING:
				if (c == Component._TEXT_)
					add(Component.FUTURE, parent, value);
				break;
			}
		}

		FutureRequest get() {
			return new FutureRequest(futureId, coding, value, target);
		}

		void clear() {
			futureId = value = null;
			coding = null;
			target = null;
		}
	}

	StringBuilder builder;
	Stack<Component> stack;
	RequestType requestType;
	RequestData requestData;
	FutureData futureData;
	LinkedList<Request> requests;
	LinkedList<FutureRequest> futureRequests;

	public XMLRequestHandler() {
		stack = new Stack<Component>();
		builder = new StringBuilder();
		requestData = new RequestData();
		futureData = new FutureData();
		requestType = RequestType.NONE;
		requests = new LinkedList<Request>();
		futureRequests = new LinkedList<FutureRequest>();
	}

	public void elementText(String text) {
		builder.append(text);
	}

	public void elementEnd(String name) {
		switch (requestType) {
		case REQUEST:
			requestData.add(stack.peek(), Component._TEXT_, builder.toString());
			break;
		case FUTURE:
			futureData.add(stack.peek(), Component._TEXT_, builder.toString());
			break;
		}

		builder.delete(0, builder.length());
		stack.pop();

		if (stack.size() == 0) {
			switch (requestType) {
			case REQUEST:
				requests.add(requestData.get());
				requestData.clear();
				break;
			case FUTURE:
				futureRequests.add(futureData.get());
				futureData.clear();
				break;
			}

			requestType = RequestType.NONE;
		}
	}

	public void elementStart(String name, Attributes attributes) {
		Component c = Component.valueOf(name.toUpperCase());
		stack.push(c);

		if (requestType == RequestType.NONE) {
			switch (c) {
			case REQUEST:
				requestType = RequestType.REQUEST;
				break;
			case FUTURE:
				requestType = RequestType.FUTURE;
				break;
			default:
				
			}
		}

		switch (requestType) {
		case REQUEST:
			for (int i = 0; i < attributes.getAttributeCount(); i++) {
				Component d = Component.valueOf(attributes.getAttributeName(i)
						.toUpperCase());
				requestData.add(c, d, attributes.getAttributeValue(i));
			}
			break;
		case FUTURE:
			for (int i = 0; i < attributes.getAttributeCount(); i++) {
				Component d = Component.valueOf(attributes.getAttributeName(i)
						.toUpperCase());
				futureData.add(c, d, attributes.getAttributeValue(i));
			}
			break;
		}
	}

	public int requestCount() {
		return requests.size();
	}

	public Request popRequest() {
		return requests.poll();
	}

	public int futureRequestCount() {
		return futureRequests.size();
	}

	public FutureRequest popFutureRequest() {
		return futureRequests.poll();
	}

	protected void clear() {
		requestData.clear();
		builder.delete(0, builder.length());
		requestType = RequestType.NONE;
	}
}
