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
import java.util.HashMap;

import org.d3.annotation.ActorPath;
import org.d3.protocol.FutureRequest;
import org.d3.protocol.InetProtocol;
import org.d3.protocol.Request;
import org.d3.protocol.TransmissionException;
import org.d3.protocol.Transmitter;
import org.d3.protocol.request.ObjectCoder.CodingMethod;
import org.d3.remote.HostNotFoundException;
import org.d3.remote.UnknownAgencyException;

@ActorPath("/protocols/raw/tcp")
@InetProtocol
public class RAWTCPTransmitter extends Transmitter {
	public static final byte TYPE_REQUEST = 0x01;
	public static final byte TYPE_FUTURE_REQUEST = 0x02;

	private final ServerSocketChannel channel;
	private final HashMap<Channel, ByteBuffer> buffers;

	public RAWTCPTransmitter(InetSocketAddress socketAddress)
			throws IOException {
		super("raw", Integer.toString(socketAddress.getPort()), socketAddress);

		buffers = new HashMap<Channel, ByteBuffer>();

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
		if (buffers.containsKey(ch)) {
			buffers.remove(ch);
		}
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
	public int read(ReadableByteChannel ch) throws TransmissionException {
		int r = -1;
		ByteBuffer data;

		if (!buffers.containsKey(ch)) {
			ByteBuffer header = ByteBuffer.allocate(4);
			int size;

			try {
				r = ch.read(header);
			} catch (IOException e) {
				throw new TransmissionException(e);
			}

			if (r != 4)
				throw new TransmissionException("headers not read");

			header.rewind();
			size = header.getInt();

			if (size > maxBytesPerRequest)
				throw new TransmissionException(
						"request should not exceed %d bytes",
						maxBytesPerRequest);

			data = ByteBuffer.allocate(size);
			data.putInt(size);
			buffers.put(ch, data);
		} else
			data = buffers.get(ch);

		try {
			r = ch.read(data);
		} catch (IOException e) {
			throw new TransmissionException(e);
		}

		if (!data.hasRemaining()) {
			data.rewind();
			dataReceived(data);
			
			return -1;
		}

		return r;
	}

	protected void dataReceived(ByteBuffer data) throws TransmissionException {
		byte type;
		
		//
		// Skip the 4-bytes size
		data.getInt();
		//
		
		type = data.get();
		
		switch (type) {
		case TYPE_REQUEST:
			requestDataReceived(data);
			break;
		case TYPE_FUTURE_REQUEST:
			futureRequestDataReceived(data);
			break;
		}
	}

	protected void requestDataReceived(ByteBuffer data)
			throws TransmissionException {
		Request r;
		byte[] dataSource, dataTarget, dataCall, dataFuture, codingMethod, args;

		dataSource = new byte[data.getInt()];
		data.get(dataSource);

		dataTarget = new byte[data.getInt()];
		data.get(dataTarget);

		dataCall = new byte[data.getInt()];
		data.get(dataCall);

		dataFuture = new byte[data.getInt()];
		data.get(dataFuture);

		codingMethod = new byte[data.getInt()];
		data.get(codingMethod);

		args = new byte[data.getInt()];
		data.get(args);

		URI source, target;
		String call, futureId;
		CodingMethod cm;

		source = URI.create(new String(dataSource));
		target = URI.create(new String(dataTarget));
		call = new String(dataCall);
		
		try {
			cm = CodingMethod.valueOf(new String(codingMethod));
		} catch (IllegalArgumentException e) {
			throw new TransmissionException(e);
		}

		futureId = dataFuture.length == 0 ? null : new String(dataFuture);

		r = new Request(source, target, call, cm, args, futureId);

		try {
			dispatch(r);
		} catch (HostNotFoundException e) {
			throw new TransmissionException(e);
		} catch (UnknownAgencyException e) {
			throw new TransmissionException(e);
		}
	}

	protected void futureRequestDataReceived(ByteBuffer data)
			throws TransmissionException {
		byte[] dataId, dataTarget, codingMethod, dataValue;

		dataId = new byte[data.getInt()];
		data.get(dataId);

		dataTarget = new byte[data.getInt()];
		data.get(dataTarget);

		codingMethod = new byte[data.getInt()];
		data.get(codingMethod);

		dataValue = new byte[data.getInt()];
		data.get(dataValue);

		String id;
		CodingMethod cm;
		URI target;
		FutureRequest r;

		id = new String(dataId);
		target = URI.create(new String(dataTarget));

		try {
			cm = CodingMethod.valueOf(new String(codingMethod));
		} catch (IllegalArgumentException e) {
			throw new TransmissionException(e);
		}

		r = new FutureRequest(id, cm, dataValue, target);

		dispatch(r);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.d3.protocol.Transmitter#write(org.d3.protocol.Request)
	 */
	public void write(Request r) throws TransmissionException {
		URI target;
		ByteBuffer data;

		target = r.getTargetURI();
		data = requestToBytes(r);

		write(target, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.d3.protocol.Transmitter#write(org.d3.protocol.FutureRequest)
	 */
	public void write(FutureRequest fr) throws TransmissionException {
		URI target;
		ByteBuffer data;

		target = fr.getTarget();
		data = requestToBytes(fr);

		write(target, data);
	}

	protected void write(URI target, ByteBuffer data)
			throws TransmissionException {
		SocketChannel out;
		InetSocketAddress socket;
		TransmissionException ex = null;

		socket = new InetSocketAddress(target.getHost(), target.getPort());

		try {
			out = SocketChannel.open();
		} catch (IOException e) {
			throw new TransmissionException(e);
		}

		try {
			out.connect(socket);
			out.finishConnect();
			out.write(data);
		} catch (IOException e) {
			ex = new TransmissionException(e);
		}

		try {
			out.close();
		} catch (IOException e) {
			if (ex != null)
				throw ex;
			throw new TransmissionException(e);
		}

		if (ex != null)
			throw ex;
	}

	/**
	 * <pre>
	 * 4 bytes : full size of the request
	 * 1 byte  : request type
	 * 4 bytes : id size
	 * x bytes : id
	 * 4 bytes : target size
	 * x bytes : target
	 * 4 bytes : coding method size
	 * x bytes : coding method
	 * 4 bytes : value size
	 * x bytes : value
	 * </pre>
	 * 
	 * @param r
	 * @return
	 */
	protected ByteBuffer requestToBytes(FutureRequest r) {
		byte[] dataId, dataTarget, dataValue, codingMethod;
		int size = 1 + 5 * 4;

		dataId = r.getFutureId().getBytes();
		dataTarget = r.getTarget().toString().getBytes();
		dataValue = r.getValue();
		codingMethod = r.getCodingMethod().name().getBytes();

		size += dataId.length;
		size += dataTarget.length;
		size += dataValue.length;
		size += codingMethod.length;

		ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.putInt(size);
		buffer.put(TYPE_FUTURE_REQUEST);

		buffer.putInt(dataId.length);
		buffer.put(dataId);

		buffer.putInt(dataTarget.length);
		buffer.put(dataTarget);

		buffer.putInt(codingMethod.length);
		buffer.put(codingMethod);

		buffer.putInt(dataValue.length);
		buffer.put(dataValue);

		buffer.rewind();

		return buffer;
	}

	/**
	 * <pre>
	 * 4 bytes : full size of the request
	 * 1 byte  : request type
	 * 4 bytes : source size
	 * x bytes : source
	 * 4 bytes : target size
	 * x bytes : target
	 * 4 bytes : call size
	 * x bytes : call
	 * 4 bytes : future id size
	 * x bytes : future id
	 * 4 bytes : coding method size
	 * x bytes : coding method
	 * 4 bytes : args size
	 * x bytes : args
	 * </pre>
	 * 
	 * @param r
	 * @return
	 */
	protected ByteBuffer requestToBytes(Request r) {
		byte[] dataSource, dataTarget, dataCall, dataFuture, args, codingMethod;
		int size = 1 + 7 * Integer.SIZE / 8;

		dataSource = r.getSourceURI().toString().getBytes();
		dataTarget = r.getTargetURI().toString().getBytes();
		dataCall = r.getCall().getBytes();

		if (r.getFutureId() != null)
			dataFuture = r.getFutureId().getBytes();
		else
			dataFuture = new byte[0];

		args = r.getArgs();
		codingMethod = r.getCodingMethod().name().getBytes();

		size += dataSource.length;
		size += dataTarget.length;
		size += dataCall.length;
		size += dataFuture.length;
		size += args.length;
		size += codingMethod.length;

		ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.putInt(size);
		buffer.put(TYPE_REQUEST);

		buffer.putInt(dataSource.length);
		buffer.put(dataSource);

		buffer.putInt(dataTarget.length);
		buffer.put(dataTarget);

		buffer.putInt(dataCall.length);
		buffer.put(dataCall);

		buffer.putInt(dataFuture.length);
		buffer.put(dataFuture);

		buffer.putInt(codingMethod.length);
		buffer.put(codingMethod);

		buffer.putInt(args.length);
		buffer.put(args);

		buffer.rewind();

		return buffer;
	}
}
