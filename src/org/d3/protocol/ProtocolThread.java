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
package org.d3.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.d3.InvalidRequestFormatException;
import org.d3.Protocol;
import org.d3.actor.ActorThread;

public class ProtocolThread extends ActorThread {

	private ByteBuffer readBuffer = ByteBuffer
			.allocate(Protocol.REQUEST_MAX_SIZE);

	public ProtocolThread(Protocol owner) {
		super(owner, "server");
	}

	public void run() {
		Protocols.register((Protocol) owner);
		
		Selector selector = null;
		SelectableChannel channel = ((Protocol) owner).getChannel();

		try {
			selector = Selector.open();
		} catch (IOException e) {
			// TODO
		}

		try {
			channel.register(
					selector,
					channel.validOps()
							& (SelectionKey.OP_ACCEPT | SelectionKey.OP_READ | SelectionKey.OP_CONNECT));
			
		} catch (ClosedChannelException e) {
			// TODO
		}

		while (selector.isOpen()) {
			try {
				selector.select();
			} catch (IOException e) {
				// TODO
			} catch (ClosedSelectorException e) {
				// TODO
			}

			Iterator<SelectionKey> it = selector.selectedKeys().iterator();

			while (it.hasNext()) {
				SelectionKey sk = it.next();
				System.out.printf("<key %s%s%s%s>%n", sk.isValid() ? "valid,"
						: "", sk.isAcceptable() ? "acceptable," : "", sk
						.isReadable() ? "readable," : "",
						sk.isWritable() ? "writable," : "");
				try {
					processSelectionKey(sk);
				} catch (IOException e) {
					// TODO
				}
			}
		}
	}

	protected void processSelectionKey(SelectionKey sk) throws IOException {
		if (sk.isValid() && sk.isAcceptable()) {
			ServerSocketChannel ch = (ServerSocketChannel) sk.channel();

			try {
				SocketChannel sc = ch.accept();

				if (sc != null) {
					sc.configureBlocking(false);
					sc.register(sk.selector(), SelectionKey.OP_READ);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (sk.isValid() && sk.isConnectable()) {
			SocketChannel sChannel = (SocketChannel) sk.channel();

			if (!sChannel.finishConnect())
				sk.cancel();
		}

		if (sk.isValid() && sk.isReadable()) {
			ReadableByteChannel ch = (ReadableByteChannel) sk.channel();
			int r;

			readBuffer.clear();
			r = ch.read(readBuffer);
			readBuffer.flip();

			switch (r) {
			case -1:
				sk.channel().close();
				sk.cancel();
				break;
			case 0:
				break;
			default:
				try {
					((Protocol) owner).readRequest(readBuffer);
				} catch(InvalidRequestFormatException e) {
					// TODO
					e.printStackTrace();
				}
				break;
			}
		}
	}
}
