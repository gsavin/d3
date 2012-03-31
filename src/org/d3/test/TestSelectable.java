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
package org.d3.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class TestSelectable implements Runnable {

	ServerSocketChannel channel;

	public TestSelectable(int port) throws Exception {
		channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.socket().bind(new InetSocketAddress(port));
	}

	public void run() {
		Selector selector = null;

		try {
			selector = Selector.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			channel.register(selector, channel.validOps());
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (selector.isOpen()) {
			try {
				selector.select();
			} catch (IOException e) {
				e.printStackTrace();
			}

			for (SelectionKey sk : selector.selectedKeys()) {
				System.out
						.printf("- new selection key ( %s, %s, %s, %s, %s )%n",
								sk.isAcceptable(), sk.isConnectable(),
								sk.isReadable(), sk.isWritable(), sk.isValid());

				if (sk.isAcceptable()) {
					ServerSocketChannel ch = (ServerSocketChannel) sk.channel();

					try {
						SocketChannel sc = ch.accept();

						if (sc != null) {
							sc.configureBlocking(false);
							sc.register(selector, SelectionKey.OP_READ);
							
							sc.write(ByteBuffer.wrap("Welcome !\n".getBytes()));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (sk.isValid() && sk.isReadable()) {
					ReadableByteChannel ch = (ReadableByteChannel) sk.channel();
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					try {
						int r = ch.read(buffer);
						
						switch(r) {
						case -1:
							sk.channel().close();
							sk.cancel();
							System.out.printf("<< end of stream >>%n");
							break;
						case 0:
							System.out.printf("<< nothing to read >>%n");
							break;
						default:
							buffer.flip();
							String str = new String(buffer.array());
							System.out.printf("> %s%n",str);
							break;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		TestSelectable ts = new TestSelectable(10000);
		Thread t = new Thread(ts);
		t.start();
	}

}
