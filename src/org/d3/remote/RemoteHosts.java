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
import java.util.concurrent.ConcurrentHashMap;

import org.d3.Console;
import org.d3.actor.Agency;

public class RemoteHosts {
	private final ConcurrentHashMap<InetAddress, RemoteHost> hosts;

	public RemoteHosts() {
		hosts = new ConcurrentHashMap<InetAddress, RemoteHost>();
	}

	public RemoteHost registerHost(InetAddress address) {
		Agency.getLocalAgency().checkBodyThreadAccess();

		if (!hosts.containsKey(address)) {
			hosts.put(address, new RemoteHost(address));
			Console.info("register host \"%s\"", address);
		}

		return hosts.get(address);
	}

	public void unregisterHost(RemoteHost host) {
		Agency.getLocalAgency().checkBodyThreadAccess();

		InetAddress address = host.getAddress();

		if (address != null)
			hosts.remove(address);
	}

	public RemoteHost get(InetAddress address) throws HostNotFoundException {
		RemoteHost rh = hosts.get(address);

		if (rh == null)
			throw new HostNotFoundException();

		return rh;
	}
}
