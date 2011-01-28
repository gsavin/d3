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
package org.d3.entity.migration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.d3.Console;

abstract class Negociation {

	private static class Data {
		int req;
		int flag;
		int size;

		ByteBuffer data;

		Data(int req, int flag, ByteBuffer data) {
			this.req = req;
			this.flag = flag;
			this.size = data.limit();
			this.data = data;
		}
	}

	protected static final int HEADER_REQUEST = 0x10;
	protected static final int HEADER_REQUEST_RESPONSE = 0x1A;
	protected static final int HEADER_SEND = 0x20;
	protected static final int HEADER_SEND_RESPONSE = 0x2A;

	public static enum Response {
		MIGRATION_ACCEPTED, MIGRATION_REJECTED, MIGRATION_SUCCEED, MIGRATION_FAILED
	}

	public static enum Status {
		RECEIVER, SENDER
	}

	protected Charset charset;
	protected SocketChannel channel;
	protected ByteBuffer header;
	protected Throwable cause;
	protected ConcurrentLinkedQueue<Data> toWrite;
	protected InetSocketAddress address;

	public Negociation(SocketChannel channel,
			InetSocketAddress address) {
		this(channel);
		this.address = address;
	}

	public Negociation(SocketChannel channel) {
		this.channel = channel;
		this.charset = Charset.forName("UTF-8");
		this.header = ByteBuffer.allocate(3 * Integer.SIZE);
		this.toWrite = new ConcurrentLinkedQueue<Data>();
		this.address = null;
	}

	public InetSocketAddress getAddress() {
		return address;
	}
	/*
	public boolean waitTheResult() {
		if (future == null)
			return false;

		try {
			return (Boolean) future.getValue();
		} catch (CallException e) {
			if (cause == null)
				cause = e;
			return false;
		}
	}
	*/
	protected void write(int req, String message) {
		ByteBuffer buffer = charset.encode(message);
		toWrite.add(new Data(req, 0x00, buffer));
	}

	protected void write() throws IOException {
		while (toWrite.size() > 0) {
			Data d = toWrite.poll();

			header.clear();
			header.putInt(d.req);
			header.putInt(d.flag);
			header.putInt(d.size);
			header.reset();

			channel.write(header);
			channel.write(d.data);
		}
	}

	protected abstract void handle(int req, String[] data);
	
	protected void read() throws IOException {
		header.clear();
		channel.read(header);
		header.reset();

		int req = header.getInt();
		// int flag =
		header.getInt();
		int size = header.getInt();

		ByteBuffer buffer = ByteBuffer.allocate(size);
		channel.read(buffer);
		buffer.reset();

		String[] data = buffer.toString().split("\\s*;\\s*");
		handle(req, data);
	}

	protected void close() {
		try {
			channel.close();
		} catch (Exception e) {
			Console.exception(e);
		}
	}
}
