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
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.d3.Console;
import org.d3.actor.Protocol;
import org.d3.annotation.ActorPath;
import org.d3.protocol.InetProtocol;
import org.d3.remote.RemoteAgency;
import org.d3.remote.RemotePort;

@ActorPath("/protocols/migration")
@InetProtocol
public class MigrationProtocol extends Protocol {

	public static final String SCHEME = "emp";
	public static final int DEFAULT_PORT = 10111;
	
	// private final HashMap<SocketChannel, Negociation> negociations;
	private Selector selector;

	public MigrationProtocol() {
		this(new InetSocketAddress(DEFAULT_PORT));
	}
	
	public MigrationProtocol(InetSocketAddress address) {
		super(SCHEME, Integer.toString(address.getPort()), address);
		// negociations = new HashMap<SocketChannel, Negociation>();
	}

	public void listen() {
		try {
			selector = Selector.open();
		} catch(IOException e) {
			Console.exception(e);
			return;
		}
		
		while (selector.isOpen()) {
			try {
				selector.select();
			} catch (IOException e) {
				// TODO
				e.printStackTrace();
			} catch (ClosedSelectorException e) {
				// TODO
				e.printStackTrace();
			}

			Iterator<SelectionKey> it = selector.selectedKeys().iterator();

			while (it.hasNext()) {
				SelectionKey sk = it.next();

				try {
					processSelectionKey(sk);
				} catch (IOException e) {
					// TODO
					Console.exception(e);
				}
			}
		}
	}

	public boolean migrate(RemoteAgency agency, MigrationData data)
			throws MigrationException {
		try {
			RemotePort rp = agency.getCompatibleRemotePort(SCHEME);
			InetSocketAddress address = new InetSocketAddress(agency
					.getRemoteHost().getAddress().asInetAddress(), rp.getPort());

			SocketChannel client = SocketChannel.open();
			Negociation negociation = new Negociation(client, data, address);
			client.register(selector, SelectionKey.OP_CONNECT, negociation);

			boolean result = negociation.waitTheResult();

			if (!result)
				throw new MigrationException();

			return true;
		} catch (Exception e) {
			Console.exception(e);
		}

		return false;
	}

	protected void processSelectionKey(SelectionKey sk) throws IOException {
		if (sk.isValid() && sk.isAcceptable()) {
			ServerSocketChannel ch = (ServerSocketChannel) sk.channel();

			try {
				SocketChannel sc = ch.accept();

				if (sc != null) {
					sc.configureBlocking(false);
					sc.register(sk.selector(), SelectionKey.OP_READ,
							new Negociation(sc));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (sk.isValid() && sk.isConnectable()) {
			SocketChannel sChannel = (SocketChannel) sk.channel();

			Object attach = sk.attachment();

			if (attach instanceof Negociation) {
				Negociation negociation = (Negociation) attach;

				sChannel.connect(negociation.getAddress());

				if (!sChannel.finishConnect())
					sk.cancel();

				sk.interestOps(SelectionKey.OP_READ);

				negociation.begin();
			} else {
				Console.error("not a negociation");
				sk.channel().close();
			}
		}

		if (sk.isValid() && sk.isReadable()) {
			Object attach = sk.attachment();

			if (attach instanceof Negociation) {
				Negociation negociation = (Negociation) attach;
				negociation.read();
			} else {
				Console.error("not a negociation");
				sk.channel().close();
			}
		}

		if (sk.isValid() && sk.isWritable()) {
			Object attach = sk.attachment();

			if (attach instanceof Negociation) {
				Negociation negociation = (Negociation) attach;
				negociation.write();

				sk.interestOps(SelectionKey.OP_READ);
			} else {
				Console.error("not a negociation");
				sk.channel().close();
			}
		}
	}
}
