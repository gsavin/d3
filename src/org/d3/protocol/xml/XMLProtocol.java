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

import java.net.SocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.d3.ActorNotFoundException;
import org.d3.InvalidRequestFormatException;
import org.d3.Protocol;
import org.d3.Request;

public abstract class XMLProtocol extends Protocol {
	public static final int XML_PROTOCOL_PORT = 10001;

	protected static final String XML_REQUEST_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<request>\n"
			+ "<source>%source%</source>\n"
			+ "<target>%target%</target>\n" + "</request>\n";

	protected static final Pattern XML_REQUEST_PATTERN = Pattern
			.compile("^\\s*(?:<\\?xml\\s+version=\"[^\"]+\"\\s+encoding=\"[^\"]+\"\\?>)?\\s*"
					+ "<request>\\s*((?:<source>.*</source>\\s*|<target>.*</target>\\s*){2})</request>\\s*$");

	protected static final Pattern XML_REQUEST_SOURCE_PATTERN = Pattern
			.compile("<source>(.*)</source>");
	protected static final Pattern XML_REQUEST_TARGET_PATTERN = Pattern
			.compile("<target>(.*)</target>");

	protected XMLProtocol(String id, SocketAddress socketAddress) {
		super("xml", id, socketAddress);
	}

	protected byte[] convert(Request r) {
		String req = XML_REQUEST_TEMPLATE;

		req.replace("%source%", r.getSourceURI().toString());
		req.replace("%target%", r.getTargetURI().toString());

		return req.getBytes();
	}

	protected Request convert(byte[] bytes, int offset, int length)
			throws InvalidRequestFormatException {
		String req = new String(bytes,offset,length);
		
		Matcher m = XML_REQUEST_PATTERN.matcher(req);
		if (m.matches()) {
			Matcher s = XML_REQUEST_SOURCE_PATTERN.matcher(m.group(1));
			Matcher t = XML_REQUEST_TARGET_PATTERN.matcher(m.group(1));

			if (s.find() && t.find()) {
				URI source = URI.create(s.group(1));
				URI target = URI.create(t.group(1));

				return new Request(source, target);
			}
		}

		throw new InvalidRequestFormatException();
	}

	public abstract void sendRequest(Request r);

	public void readRequest(ByteBuffer buffer)
		throws InvalidRequestFormatException {
		checkProtocolThreadAccess();

		Request req = convert(buffer.array(), buffer.arrayOffset(),
				buffer.limit());

		try {
			dispatch(req);
		} catch (ActorNotFoundException e) {
			// TODO
			e.printStackTrace();
		}
	}
}
