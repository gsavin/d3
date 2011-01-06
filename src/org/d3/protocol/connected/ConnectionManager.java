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
package org.d3.protocol.connected;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A generic connection manager. It uses SocketChannel and Selector to handle
 * data reception.
 * 
 * @author Guilhelm Savin
 * 
 */
public class ConnectionManager {
	public static ConnectionFactory getDefaultConnectionFactory() {
		return new ConnectionFactory() {
			public Connection createConnection(SocketChannel channel) {
				return new Connection(channel);
			}
		};
	}

	class ConnectionEntry {
		Connection connection;
		SelectionKey selectionKey;
		long bytes = 0;
		SocketAddress socketAddress;
	}

	class ConnectionSelector implements Runnable {
		Selector selector;
		ConcurrentLinkedQueue<Connection> waitingForRegistration;
		AtomicBoolean running;
		ByteBuffer buffer;

		public ConnectionSelector() throws IOException {
			selector = Selector.open();
			waitingForRegistration = new ConcurrentLinkedQueue<Connection>();
			running = new AtomicBoolean(false);
			buffer = ByteBuffer.allocate(bufferStepSize);
		}

		public void enqueueConnection(Connection conn) {
			waitingForRegistration.add(conn);

			if (running.compareAndSet(false, true)) {
				Thread t = threadFactory.newThread(this);
				t.setDaemon(true);
				t.start();
			}
		}

		public void register(Connection connection) throws IOException {
			SocketChannel channel = connection.getChannel();

			if (channel.isConnectionPending())
				channel.finishConnect();

			channel.configureBlocking(false);

			ConnectionEntry entry = new ConnectionEntry();
			entry.connection = connection;
			entry.selectionKey = channel.register(selector,
					SelectionKey.OP_READ, entry);
			entry.socketAddress = channel.socket().getRemoteSocketAddress();

			entries.add(entry);

			System.out.printf("[+] %d connections%n", entries.size());
		}

		public void run() {
			while (true) {
				while (waitingForRegistration.size() > 0) {
					try {
						register(waitingForRegistration.poll());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				try {
					checkData();
				} catch (IOException ioe) {

				}
			}
		}

		protected void checkData() throws IOException {
			selector.select(1000);

			{
				Set<SelectionKey> keys = selector.selectedKeys();

				for (SelectionKey key : keys) {
					if (key.isValid() && key.isReadable()
							&& !lostConnections.contains(key.attachment())) {
						ReadableByteChannel in = (ReadableByteChannel) key
								.channel();

						int read = 0;

						while ((read = in.read(buffer)) >= 0) {
							if (buffer.remaining() <= 0) {
								buffer.flip();
								ByteBuffer tmp = ByteBuffer.allocate(buffer
										.capacity() + bufferStepSize);
								tmp.put(buffer);
								buffer = tmp;
							} else if (read == 0) {
								break;
							}
						}

						buffer.flip();

						if (buffer.limit() > 0) {
							byte[] data = new byte[buffer.limit()];
							int i = 0;

							while (buffer.position() < buffer.limit())
								data[i++] = buffer.get();

							buffer.clear();

							ConnectionEntry entry = (ConnectionEntry) key
									.attachment();
							entry.bytes += data.length;

							System.out.printf("%s: %dbytes%n", entry,
									entry.bytes);// new String(data));

							if (connectedInterface != null)
								connectedInterface.receiveData(
										entry.connection, data);
						}

						if (read < 0) {
							System.out.printf("connection lost%n");
							connectionLost((ConnectionEntry) key.attachment());
						}
					}
				}
			}
		}
	}

	class ConnectionPurge implements Runnable {
		Thread thread;

		public void run() {
			if (thread == null)
				thread = Thread.currentThread();
			else
				return;

			while (true) {
				for (ConnectionEntry entry : entries) {
					if (!entry.connection.getChannel().isConnected()) {
						System.out.printf("channel not connected anymore%n");
						lostConnections.add(entry);
					}
				}

				while (lostConnections.size() > 0) {
					ConnectionEntry entry = lostConnections.poll();

					try {
						mappingInetConn.remove(entry.socketAddress);

						entry.selectionKey.cancel();
						entry.connection.getChannel().close();

						entries.remove(entry);

						System.out.printf("[-] %d connections%n",
								entries.size());
					} catch (IOException e) {
						e.printStackTrace();
						// Nothing to do
					}
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {

				}
			}
		}
	}

	/**
	 * Active connections.
	 */
	ConcurrentLinkedQueue<ConnectionEntry> entries;
	/**
	 * Connections which have been closed or lost.
	 */
	ConcurrentLinkedQueue<ConnectionEntry> lostConnections;

	ConcurrentHashMap<SocketAddress, Connection> mappingInetConn;

	ReentrantLock connectionLock;

	int bufferStepSize = 64;

	ConnectionSelector[] selectors;

	ConnectionPurge connectionPurge;

	int nextSelector;

	ThreadFactory threadFactory;

	ConnectedInterface connectedInterface;

	int port;

	ConnectionServer server;

	ConnectionFactory connectionFactory;

	public ConnectionManager() {
		this(1);
	}

	public ConnectionManager(int port) {
		this(getDefaultConnectionFactory(), Executors.defaultThreadFactory(),
				port, 1);
	}

	public ConnectionManager(int port, int selectorCount) {
		this(getDefaultConnectionFactory(), Executors.defaultThreadFactory(),
				port, selectorCount);
	}

	public ConnectionManager(ConnectionFactory connectionFactory,
			ThreadFactory threadFactory, int port, int selectorCount) {
		entries = new ConcurrentLinkedQueue<ConnectionEntry>();
		lostConnections = new ConcurrentLinkedQueue<ConnectionEntry>();
		mappingInetConn = new ConcurrentHashMap<SocketAddress, Connection>();
		nextSelector = 0;

		connectionPurge = new ConnectionPurge();
		selectors = new ConnectionSelector[selectorCount];

		this.connectionFactory = connectionFactory;

		this.port = port;

		connectionLock = new ReentrantLock();

		this.threadFactory = threadFactory;

		for (int i = 0; i < selectorCount; i++) {
			try {
				selectors[i] = new ConnectionSelector();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		Thread t = threadFactory.newThread(connectionPurge);
		t.setDaemon(true);
		t.start();

		server = new ConnectionServer(this, connectionFactory, port);

		t = threadFactory.newThread(server);
		t.setDaemon(true);
		t.start();
	}

	void register(Connection connection) {
		connectionLock.lock();

		if (!mappingInetConn.containsKey(connection.channel.socket()
				.getRemoteSocketAddress())) {
			mappingInetConn.put(connection.channel.socket()
					.getRemoteSocketAddress(), connection);
			selectors[nextSelector].enqueueConnection(connection);
			nextSelector = (nextSelector + 1) % selectors.length;
		} else {
			try {
				connection.channel.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		connectionLock.unlock();
	}

	private void connectionLost(ConnectionEntry entry) {
		lostConnections.add(entry);

		if (connectionPurge.thread != null)
			connectionPurge.thread.interrupt();
	}

	public void send(SocketAddress address, byte[] data) throws IOException {
		connectionLock.lock();

		Connection conn = mappingInetConn.get(address);

		if (conn == null) {
			SocketChannel sc = SocketChannel.open(address);
			conn = connectionFactory.createConnection(sc);

			register(conn);
		}

		connectionLock.unlock();

		conn.channel.write(ByteBuffer.wrap(data));
	}

	public static void main(String... args) throws InterruptedException {
		ConnectionManager cm = new ConnectionManager(6010);
		synchronized (cm) {
			cm.wait();
		}
	}
}
