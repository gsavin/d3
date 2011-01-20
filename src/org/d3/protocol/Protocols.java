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

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.d3.Console;
import org.d3.actor.Agency;
import org.d3.actor.Protocol;
import org.d3.actor.Agency.Argument;
import org.d3.events.EventDispatchable;
import org.d3.events.EventDispatcher;
import org.d3.tools.Utils;

public class Protocols implements EventDispatchable<ProtocolsEvent> {
	public static final Pattern PROTOCOL_EXPORT_PATTERN = Pattern.compile("(\\w+):(\\d+)");
	public static final int PROTOCOL_EXPORT_PATTERN_SCHEME = 1;
	public static final int PROTOCOL_EXPORT_PATTERN_PORT = 2;
	
	public static void init() {
		Agency.getLocalAgency().checkBodyThreadAccess();

		String protocolsToLoad = Agency.getArg(Argument.PROTOCOLS.key);

		if (protocolsToLoad != null) {
			Pattern protocol = Pattern
					.compile("(@?[a-zA-Z0-9_]+(?:[.][a-zA-Z0-9_]+)*)\\((?:([^:]+)(?::(\\d+))?)?\\)");
			Matcher protocols = protocol.matcher(protocolsToLoad);

			while (protocols.find()) {
				String cls = protocols.group(1);
				String ifname = protocols.group(2);
				int port = protocols.group(3) == null ? -1 : Integer
						.parseInt(protocols.group(3));

				cls = cls.replace("@", Protocols.class.getPackage().getName()
						+ ".");

				Console.info("load %s", cls);

				try {
					Protocols.enableProtocol(cls, ifname, port);
				} catch (BadProtocolException e) {
					Console.exception(e);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void enableProtocol(String classname, String ifname, int port)
			throws BadProtocolException {
		Class<? extends Protocol> cls;

		try {
			cls = (Class<? extends Protocol>) Class.forName(classname);
		} catch (ClassNotFoundException e) {
			throw new BadProtocolException(e);
		}

		if (cls.getAnnotation(InetProtocol.class) != null) {
			Constructor<? extends Protocol> c;
			Object[] args;

			if (ifname != null) {
				if (port > 0) {
					InetAddress inet;

					try {
						inet = Utils.getAddressForInterface(ifname);
					} catch (SocketException e) {
						throw new BadProtocolException(e);
					}

					InetSocketAddress inetSocket = new InetSocketAddress(inet,
							port);

					try {
						c = cls.getConstructor(InetSocketAddress.class);
					} catch (Exception e) {
						throw new BadProtocolException(e);
					}

					args = new Object[] { inetSocket };
				} else {
					NetworkInterface nif;

					try {
						nif = NetworkInterface.getByName(ifname);
						c = cls.getConstructor(NetworkInterface.class);
					} catch (Exception e) {
						throw new BadProtocolException(e);
					}

					args = new Object[] { nif };
				}
			} else {
				try {
					c = cls.getConstructor();
				} catch (Exception e) {
					throw new BadProtocolException(e);
				}

				args = null;
			}

			try {
				Protocol p = c.newInstance(args);
				p.init();
			} catch (Exception e) {
				throw new BadProtocolException(e);
			}

		}
	}

	private final ReentrantLock lock;
	private final Ports ports;
	private final Schemes schemes;
	private final HashSet<Protocol> protocols;
	private final EventDispatcher<ProtocolsEvent> eventDispatcher;
	private final Futures futures;
	
	public Protocols() {
		this.ports = new Ports();
		this.schemes = new Schemes();
		this.lock = new ReentrantLock();
		this.protocols = new HashSet<Protocol>();
		this.eventDispatcher = new EventDispatcher<ProtocolsEvent>(ProtocolsEvent.class);
		this.futures = new Futures();
	}

	public void register(Protocol protocol) throws ProtocolException {
		protocol.checkProtocolThreadAccess();

		lock();

		if (!protocols.contains(protocol)) {
			ports.register(protocol);
			schemes.register(protocol);
			protocols.add(protocol);
			
			Console.info("protocol enable");
		}
		
		unlock();
		
		eventDispatcher.trigger(ProtocolsEvent.PROTOCOL_REGISTERED, protocol);
	}

	public boolean isLocalPort(int port) {
		Protocol p;
		
		lock();
		p = ports.get(port);
		unlock();
		
		return p != null;
	}
	
	public String exportDescription() {
		StringBuilder builder = new StringBuilder();
		String sep = "";
		
		lock();
		for(Protocol p: protocols) {
			builder.append(sep);
			builder.append(p.getScheme());
			builder.append(":");
			builder.append(p.getPort());
			
			sep = ", ";
		}
		unlock();
		
		return builder.toString();
	}
	
	public EventDispatcher<ProtocolsEvent> getEventDispatcher() {
		return eventDispatcher;
	}
	
	public Futures getFutures() {
		return futures;
	}
	
	public Schemes getSchemes() {
		return schemes;
	}
	
	private void lock() {
		lock.lock();
	}
	
	private void unlock() {
		lock.unlock();
	}
}
