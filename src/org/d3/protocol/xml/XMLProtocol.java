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
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.d3.InvalidRequestFormatException;
import org.d3.actor.ActorInternalException;
import org.d3.protocol.Request;
import org.d3.protocol.RequestIOProtocol;
import org.d3.template.Template;

public abstract class XMLProtocol extends RequestIOProtocol {
	public static final int XML_PROTOCOL_PORT = 10001;

	protected static final String XML_REQUEST_TEMPLATE = "<?xml version=\"1.0\" encoding=\"{%encoding%}\"?>\n"
			+ "<request>\n"
			+ " <source>{%source%}</source>\n"
			+ " <target>{%target%}</target>\n"
			+ " <call>{%call%}</call>\n"
			+ " <args coding=\"{%coding_method%}\">{%args%}</args>\n"
			+ " <future>{%future%}</future>\n" + "</request>\n";

	private Template xmlRequestTemplate;
	private Charset charset;
	private XMLRequestParser handler;
	private SAXParser parser;

	protected XMLProtocol(String id, SocketAddress socketAddress) {
		super("xml", id, socketAddress);

		xmlRequestTemplate = new Template(XML_REQUEST_TEMPLATE);
		charset = Charset.defaultCharset();
		handler = new XMLRequestParser();

		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
		} catch (Exception e) {
			throw new ActorInternalException(e);
		}
	}

	protected byte[] convert(Request r) {
		HashMap<String, String> env = new HashMap<String, String>();

		env.put("encoding", charset.name());
		env.put("source", r.getSourceURI().toString());
		env.put("target", r.getTargetURI().toString());
		env.put("call", r.getCall());
		env.put("coding_method", r.getCodingMethod().name());
		env.put("args", r.getArgs());
		env.put("future", r.getFutureId());

		return xmlRequestTemplate.toString(env).getBytes(charset);
	}

	public abstract void writeRequest(Request r);

	public void readRequest(ByteBuffer buffer)
			throws InvalidRequestFormatException {
		checkProtocolThreadAccess();

		ByteArrayInputStream in = new ByteArrayInputStream(buffer.array(),
				buffer.arrayOffset(), buffer.limit());

		try {
			parser.parse(in, handler);
			dispatch(handler.getRequestAndClear());
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
	}
}
