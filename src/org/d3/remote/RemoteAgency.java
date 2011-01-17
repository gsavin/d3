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
package org.d3.remote;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.d3.actor.RemoteActor;
import org.d3.annotation.ActorPath;

@ActorPath("/agencies")
public class RemoteAgency extends RemoteActor {
	// protected String[] protocols;
	protected ConcurrentHashMap<Integer, RemotePort> ports;
	protected long lastPresenceDate;
	protected String digest;

	public RemoteAgency(InetAddress host, String agencyId) {
		this(host, agencyId, null, "");
	}

	public RemoteAgency(InetAddress host, String agencyId, String protocols,
			String digest) {
		super(host, agencyId, "/", agencyId);

		// this.protocols = protocols.trim().split("\\s*,\\s*");
		this.ports = new ConcurrentHashMap<Integer, RemotePort>();
		this.lastPresenceDate = System.currentTimeMillis();
		this.digest = digest;
	}

	public void udpatePresence(long date) {
		this.lastPresenceDate = date;
	}

	public void updateDigest(String digest) {
		this.digest = digest;
	}

	public String getDigest() {
		return digest;
	}

	public void registerPort(int port, String scheme)
			throws RemotePortException {
		if (ports.containsKey(port))
			throw new RemotePortException();

		ports.put(port, new RemotePort(scheme, port));
	}

	public void unregisterPort(int port) {
		ports.remove(port);
	}

	public RemotePort getRandomRemotePort()
			throws NoRemotePortAvailableException {
		Iterator<RemotePort> it = ports.values().iterator();

		if (it.hasNext())
			return it.next();

		throw new NoRemotePortAvailableException();
	}
}
