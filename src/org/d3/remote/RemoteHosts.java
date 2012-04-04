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

import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.d3.Console;
import org.d3.HostAddress;
import org.d3.actor.Agency;
import org.d3.events.EventDispatchable;
import org.d3.events.EventDispatcher;

public class RemoteHosts implements Iterable<RemoteHost>,
		EventDispatchable<RemoteEvent> {
	private final ConcurrentHashMap<HostAddress, RemoteHost> hosts;
	private final ConcurrentHashMap<String, RemoteAgency> agencies;
	private final EventDispatcher<RemoteEvent> eventDispatcher;

	public RemoteHosts() {
		hosts = new ConcurrentHashMap<HostAddress, RemoteHost>();
		agencies = new ConcurrentHashMap<String, RemoteAgency>();
		eventDispatcher = new EventDispatcher<RemoteEvent>(RemoteEvent.class);
	}

	public Iterator<RemoteHost> iterator() {
		return hosts.values().iterator();
	}

	public RemoteAgency getRemoteAgency(String id)
			throws UnknownAgencyException {
		RemoteAgency remote = agencies.get(id);

		if (remote == null)
			throw new UnknownAgencyException(id);

		return remote;
	}

	public RemoteHost registerHost(HostAddress address) {
		Agency.getLocalAgency().checkBodyThreadAccess();
		RemoteHost host = hosts.get(address);

		if (host == null) {
			host = new RemoteHost(address);
			hosts.put(address, host);
			Console.info("register host \"%s\"", address);
			eventDispatcher.trigger(RemoteEvent.REMOTE_HOST_REGISTERED, host);
		}

		return host;
	}

	public void unregisterHost(RemoteHost host) {
		Agency.getLocalAgency().checkBodyThreadAccess();

		HostAddress address = host.getAddress();

		if (address != null) {
			eventDispatcher.trigger(RemoteEvent.REMOTE_HOST_UNREGISTERED, host);
			hosts.remove(address);
		}
	}

	public RemoteAgency registerAgency(RemoteHost host, String id) {
		Agency.getLocalAgency().checkBodyThreadAccess();

		RemoteAgency remote = host.registerAgency(id);
		agencies.put(id, remote);
		
		Console.info("register agency \"%s\" @ %s", id, host.getAddress()
				.getHost());
		eventDispatcher.trigger(RemoteEvent.REMOTE_AGENCY_REGISTERED, remote);

		return remote;
	}

	public void unregisterAgency(RemoteAgency remote) {
		Agency.getLocalAgency().checkBodyThreadAccess();

		eventDispatcher.trigger(RemoteEvent.REMOTE_AGENCY_UNREGISTERED, remote);
		agencies.remove(remote.getId());
		remote.getRemoteHost().unregisterAgency(remote);
	}

	public RemoteHost get(String host) throws HostNotFoundException {
		HostAddress address;

		Console.info("try to get host '%s'", host);
		
		try {
			address = HostAddress.getByName(host);
		} catch (UnknownHostException e) {
			throw new HostNotFoundException(e);
		}

		return get(address);
	}

	public RemoteHost get(HostAddress address) throws HostNotFoundException {
		RemoteHost rh = hosts.get(address);

		if (rh == null)
			throw new HostNotFoundException(address.toString());

		return rh;
	}

	public EventDispatcher<RemoteEvent> getEventDispatcher() {
		return eventDispatcher;
	}
}
