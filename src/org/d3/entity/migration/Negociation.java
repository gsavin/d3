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
import org.d3.actor.Agency;
import org.d3.actor.CallException;
import org.d3.actor.Future;
import org.d3.protocol.request.ObjectCoder;
import org.d3.protocol.request.ObjectCoder.CodingMethod;

class Negociation {

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
	protected Status status;
	protected MigrationData entity;
	protected Future future;
	protected Throwable cause;
	protected ConcurrentLinkedQueue<Data> toWrite;
	protected InetSocketAddress address;

	public Negociation(SocketChannel channel, MigrationData entity,
			InetSocketAddress address) {
		this(channel);
		this.entity = entity;
		this.status = Status.SENDER;
		this.future = new Future();
		this.address = address;
	}

	public Negociation(SocketChannel channel) {
		this.channel = channel;
		this.charset = Charset.forName("UTF-8");
		this.header = ByteBuffer.allocate(3 * Integer.SIZE);
		this.status = Status.RECEIVER;
		this.toWrite = new ConcurrentLinkedQueue<Data>();
		this.address = null;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

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

	public void begin() {
		String message = String.format("%s;%s;%s;%s",
				Agency.getLocalAgencyId(), entity.getClass().getName(),
				entity.getPath(), entity.getId());

		write(HEADER_REQUEST, message);
	}

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

		switch (status) {
		case RECEIVER:
			switch (req) {
			case HEADER_REQUEST:
				// String agencyId = data[0];
				String className = data[1];
				String entityPath = data[2];
				// String entityId = data[3];

				checkClassName(className);
				checkEntityPath(entityPath);

				write(HEADER_REQUEST_RESPONSE,
						Response.MIGRATION_ACCEPTED.name());
				break;
			case HEADER_SEND:
				CodingMethod coding = CodingMethod.valueOf(data[0]);
				entity = (MigrationData) ObjectCoder.decode(coding, data[1]);

				write(HEADER_SEND_RESPONSE, Response.MIGRATION_SUCCEED.name());

				close();

				break;
			default:
			}
		case SENDER:
			Response r = Response.valueOf(data[0]);

			switch (req) {
			case HEADER_REQUEST_RESPONSE:
				switch (r) {
				case MIGRATION_ACCEPTED:
					CodingMethod coding = CodingMethod.HEXABYTES;
					String encodedData = ObjectCoder.encode(coding, entity);
					String message = String
							.format("%s;%s", coding, encodedData);

					write(HEADER_SEND, message);
					break;
				case MIGRATION_REJECTED:
					future.init(Boolean.FALSE);
					break;
				default:
					future.init(Boolean.FALSE);
				}

				break;
			case HEADER_SEND_RESPONSE:
				switch (r) {
				case MIGRATION_SUCCEED:
					future.init(Boolean.TRUE);
					break;
				case MIGRATION_FAILED:
					future.init(Boolean.FALSE);
					break;
				default:
				}

				close();

				break;
			default:
			}
		}
	}

	protected void close() {
		try {
			channel.close();
		} catch (Exception e) {
			Console.exception(e);
		}
	}

	protected void checkEntityPath(String entityPath) {

	}

	protected void checkClassName(String className) {
	}
}
