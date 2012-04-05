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
package org.d3.protocol.raw;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.d3.protocol.FutureRequest;
import org.d3.protocol.Request;
import org.d3.protocol.TransmissionException;
import org.d3.protocol.Transmitter;
import org.d3.protocol.request.ObjectCoder;

public class RAWTCPTransmitter extends Transmitter {
	private ServerSocketChannel channel;

	public RAWTCPTransmitter(String id, InetSocketAddress socketAddress)
			throws IOException {
		super("raw", id, socketAddress);

		channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.socket().bind(socketAddress);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.d3.protocol.Transmitter#close(java.nio.channels.Channel)
	 */
	public void close(Channel ch) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.d3.protocol.Transmitter#getChannel()
	 */
	public SelectableChannel getChannel() {
		return channel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.d3.protocol.Transmitter#read(java.nio.channels.ReadableByteChannel)
	 */
	public int read(ReadableByteChannel ch) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.d3.protocol.Transmitter#write(org.d3.protocol.Request)
	 */
	public void write(Request r) throws TransmissionException {
		URI target;
		ByteBuffer data;
		SocketChannel out;
		InetSocketAddress socket;

		target = r.getTargetURI();
		data = requestToBytes(r);
		socket = new InetSocketAddress(target.getHost(), target.getPort());

		try {
			out = SocketChannel.open();

			out.connect(socket);
			out.finishConnect();
			out.write(data);
			out.close();
		} catch (IOException e) {
			throw new TransmissionException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.d3.protocol.Transmitter#write(org.d3.protocol.FutureRequest)
	 */
	public void write(FutureRequest fr) throws TransmissionException {
		// TODO Auto-generated method stub

	}

	protected ByteBuffer requestToBytes(Request r) {
		byte[] dataSource, dataTarget, dataFuture;

		dataSource = r.getSourceURI().toString().getBytes();
		dataTarget = r.getTargetURI().toString().getBytes();

		if (r.getFutureId() != null)
			dataFuture = r.getFutureId().getBytes();
		else
			dataFuture = new byte[0];
		
		

		return null;
	}
}
