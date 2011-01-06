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

import java.lang.reflect.Method;
import java.util.HashMap;

import org.d3.Agency;
import org.d3.Console;
import org.d3.Protocol;
import org.d3.Request;
import org.d3.agency.RemoteAgency;

@SuppressWarnings("unchecked")
public class Protocols {
	private static final HashMap<String, Protocol> knownProtocols = new HashMap<String, Protocol>();
	private static final HashMap<String, Protocol> protocols = new HashMap<String, Protocol>();

	static {
		String[] map = { "org.d3.protocol.XMLProtocol" };

		for (String entry : map)
			enableProtocol(entry);
	}

	public static void enableProtocol(String classname) {
		try {
			Class<? extends Protocol> cls = (Class<? extends Protocol>) Class
					.forName(classname);
			Method m = cls.getMethod("getDefault");
			Protocol p = (Protocol) m.invoke(null);

			if (p != null) {
				System.out.printf("[protocols] enable %s --> %s%n",
						p.getFullPath(), classname);
				knownProtocols.put(p.getFullPath(), p);
			} else
				System.err.printf("[protocols] error getting protocol %s%n",
						classname);
		} catch (Exception e) {
			System.err.printf("[protocols] error while loading \"%s\"%n",
					classname);
			e.printStackTrace();
		}
	}

	public static void initProtocol(String path) {
		if (knownProtocols.containsKey(path)) {
			Protocol p = knownProtocols.get(path);

			if (Agency.getLocalAgency().registerIdentifiableObject(p)) {
				p.init();
				knownProtocols.remove(path);
				protocols.put(path, p);

				System.out.printf("[protocols] %s ready%n", path);
			}
		}
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

			if( protocol == null ) {
				Console.error("no protocol to %s",rad.getId());
			}

			protocol.sendRequest(r);
		} else {
			InternalProtocol.getInternalProtocol().sendRequest(r);
		}
	}
}
