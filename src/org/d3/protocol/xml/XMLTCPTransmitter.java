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
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import org.d3.Console;
import org.d3.annotation.ActorPath;
import org.d3.protocol.InetProtocol;

@ActorPath("/protocols/xml/tcp")
@InetProtocol
public class XMLTCPTransmitter extends XMLTransmitter {
	private ServerSocketChannel channel;

	public XMLTCPTransmitter(InetSocketAddress socketAddress)
			throws IOException {
		super(Integer.toString(socketAddress.getPort()), socketAddress);

		channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.socket().bind(socketAddress);

		Console.info("channel is open ? %s", channel.isOpen());
	}

	protected void write(ByteBuffer data, String host, int port)
			throws IOException {
		InetSocketAddress socket = new InetSocketAddress(host, port);
		SocketChannel out = SocketChannel.open();

		out.connect(socket);
		out.finishConnect();
		out.write(data);
		out.close();
	}

	public final SelectableChannel getChannel() {
		return channel;
	}
}
