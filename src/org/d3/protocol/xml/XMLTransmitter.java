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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.d3.actor.Agency;
import org.d3.actor.Protocol;
import org.d3.protocol.FutureRequest;
import org.d3.protocol.Request;
import org.d3.protocol.TransmissionException;
import org.d3.protocol.Transmitter;
import org.d3.protocol.xml.parser.XMLRequestHandler;
import org.d3.protocol.xml.parser.XMLStreamParser;
import org.d3.template.Template;

public abstract class XMLTransmitter extends Transmitter {
	public static final int XML_PROTOCOL_PORT = 10001;

	// "<?xml version=\"1.0\" encoding=\"{%encoding%}\"?>\n"
	protected static final String XML_REQUEST_TEMPLATE = "<request>\n"
			+ " <source>{%source%}</source>\n"
			+ " <target>{%target%}</target>\n" + " <call>{%call%}</call>\n"
			+ " <args coding=\"{%coding_method%}\">{%args%}</args>\n"
			+ " <future>{%future%}</future>\n" + "</request>\n";

	protected static final String XML_FUTURE_TEMPLATE = "<future id=\"{%id%}\" coding=\"{%coding%}\" target=\"{%target%}\">{%value%}</future>";

	private Template xmlRequestTemplate;
	private Template xmlFutureTemplate;
	private Charset charset;
	// private XMLRequestParser handler;
	// private SAXParser parser;
	private HashMap<Channel, XMLStreamParser> parsers;
	private ByteBuffer readBuffer;

	protected XMLTransmitter(String id, InetSocketAddress socketAddress) {
		super("xml", id, socketAddress);

		xmlRequestTemplate = new Template(XML_REQUEST_TEMPLATE);
		xmlFutureTemplate = new Template(XML_FUTURE_TEMPLATE);
		charset = Charset.defaultCharset();
		parsers = new HashMap<Channel, XMLStreamParser>();
		readBuffer = ByteBuffer.allocate(Protocol.REQUEST_MAX_SIZE);
	}

	protected ByteBuffer convert(Request r) {
		HashMap<String, String> env = new HashMap<String, String>();

		env.put("encoding", charset.name());
		env.put("source", r.getSourceURI().toString());
		env.put("target", r.getTargetURI().toString());
		env.put("call", r.getCall());
		env.put("coding_method", r.getCodingMethod().name());
		env.put("args", r.getArgs());
		env.put("future", r.getFutureId());

		return charset.encode(xmlRequestTemplate.toString(env));
	}

	protected ByteBuffer convert(FutureRequest fr) {
		HashMap<String, String> env = new HashMap<String, String>();

		env.put("id", fr.getFutureId());
		env.put("coding", fr.getCodingMethod().name());
		env.put("value", fr.getValue());
		env.put("target", fr.getTarget().toString());

		return charset.encode(xmlFutureTemplate.toString(env));
	}

	protected abstract void write(ByteBuffer data, String host, int port) throws IOException ;

	public void close(Channel ch) {
		if (parsers.containsKey(ch))
			parsers.remove(ch);
	}

	public int read(ReadableByteChannel ch) {
		checkProtocolThreadAccess();

		if (!parsers.containsKey(ch))
			parsers.put(ch, new XMLStreamParser(new XMLRequestHandler()));

		XMLStreamParser stream = parsers.get(ch);
		XMLRequestHandler handler = (XMLRequestHandler) stream.getHandler();

		int r = -1;

		try {
			readBuffer.clear();
			r = ch.read(readBuffer);
			readBuffer.flip();

			Charset cs = Charset.defaultCharset();
			stream.parse(cs.decode(readBuffer));
		} catch (IOException e) {
			Agency.getFaultManager().handle(e, null);
		}

		try {
			while (handler.requestCount() > 0) {
				dispatch(handler.popRequest());
			}

			while (handler.futureRequestCount() > 0) {
				dispatch(handler.popFutureRequest());
			}
		} catch (Exception e) {
			Agency.getFaultManager().handle(e, null);
		}

		return r;
	}
	
	public void write(Request request) throws TransmissionException {
		ByteBuffer data = convert(request);
		URI target = request.getTargetURI();
		
		try {
			write(data, target.getHost(), target.getPort());
		} catch (IOException e) {
			throw new TransmissionException(e);
		}
	}
	
	public void write(FutureRequest request) throws TransmissionException {
		ByteBuffer data = convert(request);
		URI target = request.getTarget();
		
		try {
			write(data, target.getHost(), target.getPort());
		} catch (IOException e) {
			throw new TransmissionException(e);
		}
	}
}
