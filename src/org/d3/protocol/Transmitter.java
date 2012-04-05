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
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.d3.ActorNotFoundException;
import org.d3.Console;
import org.d3.HostAddress;
import org.d3.actor.Agency;
import org.d3.actor.Call;
import org.d3.actor.CallException;
import org.d3.actor.LocalActor;
import org.d3.actor.Protocol;
import org.d3.actor.RemoteActor;
import org.d3.actor.UnregisteredActorException;
import org.d3.remote.HostNotFoundException;
import org.d3.remote.RemotePort;
import org.d3.remote.UnknownAgencyException;
import org.d3.tools.CacheCreationException;

public abstract class Transmitter extends Protocol {

	protected Transmitter(String scheme, String id,
			InetSocketAddress socketAddress) {
		super(scheme, id, socketAddress);
	}

	public void listen() {
		checkProtocolThreadAccess();

		Selector selector = null;
		SelectableChannel channel = getChannel();

		try {
			selector = Selector.open();
		} catch (IOException e) {
			Agency.getFaultManager().handle(e, this);
		}

		try {
			channel
					.register(
							selector,
							channel.validOps()
									& (SelectionKey.OP_ACCEPT
											| SelectionKey.OP_READ | SelectionKey.OP_CONNECT));

		} catch (ClosedChannelException e) {
			Agency.getFaultManager().handle(e, this);
		}

		while (selector.isOpen()) {
			try {
				selector.select();
			} catch (IOException e) {
				Agency.getFaultManager().handle(e, this);
			} catch (ClosedSelectorException e) {
				Agency.getFaultManager().handle(e, this);
			}

			Iterator<SelectionKey> it = selector.selectedKeys().iterator();

			while (it.hasNext()) {
				SelectionKey sk = it.next();

				try {
					processSelectionKey(sk);
				} catch (IOException e) {
					// TODO
					Agency.getFaultManager().handle(e, this);
				}
			}
		}

		Console.error("protocol end");
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

			int r = read(ch);

			if (r < 0) {
				close(ch);
				ch.close();
			}
		}
	}

	/**
	 * Send a request to an identifiable object.
	 * 
	 * @param target
	 * @param r
	 */
	public void transmit(RemotePort port, Call c) throws TransmissionException {
		if (c.getTarget().isRemote()) {
			// Future f = c.getFuture();
			// Agency.getLocalAgency().getProtocols().getFutures().register(f);
			write(new Request(c, this, port));
		} else {
			// TODO
			Console.error("local call on a transmitter");
		}
	}

	public void transmitFuture(RemotePort remote, String futureId, Object value)
			throws TransmissionException {
		FutureRequest fr = new FutureRequest(futureId, value, remote);
		write(fr);
	}

	protected void dispatch(Request r) throws HostNotFoundException,
			UnknownAgencyException {
		URI target = r.getTargetURI();
		HostAddress address;

		try {
			address = HostAddress.getByName(target.getHost());
			// address = InetAddress.getByName(target.getHost());
		} catch (UnknownHostException e) {
			Agency.getFaultManager().handle(e, null);
			return;
		}

		if (address.isLocal()) {
			RemoteActor source;

			try {
				source = Agency.getLocalAgency().getRemoteActors().get(
						r.getSourceURI());
			} catch (CacheCreationException e) {
				source = null;
				Agency.getFaultManager().handle(e, this);
			}

			startAssuming(source);

			String path = target.getPath();
			String agencyId = path.substring(1, path.indexOf('/', 1));

			if (Agency.getLocalAgencyId().equals(agencyId)) {
				String fullPath = path.substring(path.indexOf('/', 1));

				RemoteFuture future = new RemoteFuture(
						source.getRemoteAgency(), r.getFutureId());

				try {
					LocalActor targetActor = Agency.getLocalAgency()
							.getActors().get(fullPath);

					if (targetActor != null) {
						targetActor.call(r.getCall(), future, r
								.getDecodedArgs());
					} else {
						Console.error(path);
						future.init(new CallException(
								new ActorNotFoundException()));
					}
				} catch (ActorNotFoundException e) {
					Console.error("actor not found '%s'", fullPath);
					future.init(new CallException(e));
				} catch (UnregisteredActorException e) {
					future.init(new CallException(e));
				}
			} else
				Console.error("not local agency : %s", agencyId);
			// writeRequest(r);
		} else
			Console.error("not local address : %s", address);
		// writeRequest(r);

		stopAssuming();
	}

	protected void dispatch(FutureRequest fr) {
		Agency.getLocalAgency().getProtocols().getFutures().initFuture(
				fr.getFutureId(), fr.getDecodedValue());
	}

	public abstract SelectableChannel getChannel();

	public abstract int read(ReadableByteChannel ch);

	public abstract void close(Channel ch);

	public abstract void write(Request r) throws TransmissionException;

	public abstract void write(FutureRequest fr) throws TransmissionException;
}
