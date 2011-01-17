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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.d3.Console;
import org.d3.actor.Agency;
import org.d3.actor.Protocol;
import org.d3.actor.Agency.Argument;
import org.d3.tools.Utils;

public class Protocols {
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

				Console.warning("load %s %s%d", cls, ifname, port);
				
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
					} catch(Exception e) {
						throw new BadProtocolException(e);
					}
					
					args = new Object[] {nif};
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

	private final Ports ports;
	private final Schemes schemes;

	public Protocols() {
		this.ports = new Ports();
		this.schemes = new Schemes();
	}

	public void register(Protocol protocol) throws ProtocolException {
		protocol.checkProtocolThreadAccess();

		ports.register(protocol);
		schemes.register(protocol);

		Console.info("protocol \"%s\" enable", protocol.getFullPath());
	}

	public boolean isLocalPort(int port) {
		Protocol p = ports.get(port);
		return p != null;
	}
}
