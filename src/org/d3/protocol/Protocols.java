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
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.d3.Actor;
import org.d3.Agency;
import org.d3.Console;
import org.d3.Protocol;
import org.d3.Request;
import org.d3.agency.RemoteAgency;

@SuppressWarnings("unchecked")
public class Protocols {
	public static void enableProtocol(String classname)
			throws BadProtocolException {
		try {
			Class<? extends Protocol> cls = (Class<? extends Protocol>) Class
					.forName(classname);

			if (cls.getAnnotation(InetProtocol.class) != null) {
				try {
					Constructor<? extends Protocol> c = cls
							.getConstructor(InetSocketAddress.class);

					Protocol p = c.newInstance();
					enableProtocol(p);
				} catch (NoSuchMethodException e) {
					throw new BadProtocolException(e.getMessage());
				}

			}

		} catch (Exception e) {
			System.err.printf("[protocols] error while loading \"%s\"%n",
					classname);
			e.printStackTrace();
		}
	}

	public static void register(Protocol protocol) throws ProtocolException {
		protocol.checkProtocolThreadAccess();

		int port = protocol.getPort();
		
		if (port > 0) {
			if (ports.containsKey(port))
				throw new ProtocolException();
			
			ports.put(port, protocol);
		}

	}

	public static void enableProtocol(Protocol protocol) {

	}

	private static final Protocol getProtocol(String id) {
		if (knownProtocols.containsKey(id))
			initProtocol(id);

		return protocols.get(id);
	}

	private static final Protocol getProtocolTo(RemoteAgency rad) {
		return getProtocol(rad.getFirstProtocol());
	}

	public static final void sendRequest(Request r) {
		if (!r.isLocalTarget()) {
			RemoteAgency rad = Agency.getLocalAgency()
					.getRemoteAgencyDescription(r.getTargetAgency());

			Protocol protocol = getProtocolTo(rad);

			if (protocol == null) {
				Console.error("no protocol to %s", rad.getId());
			}

			protocol.sendRequest(r);
		} else {
			InternalProtocol.getInternalProtocol().sendRequest(r);
		}
	}

	public static final boolean isLocalPort(int port) {
		Protocol p = ports.get(port);
		return p == null ? false : protocols.containsValue(p);
	}

	public static final Protocol getProtocolTo(Actor actor)
			throws ProtocolNotFoundException {
		return null;
	}
	
	private final Ports ports;
	private final Schemes schemes;
	
	public Protocols() {
		this.ports = new Ports();
		this.schemes = new Schemes();
	}
	
	public void register(Protocol protocol) throws ProtocolException {
		ports.register(protocol);
		schemes.register(protocol);
	}
}
