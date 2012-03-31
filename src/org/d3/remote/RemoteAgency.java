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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import org.d3.ActorNotFoundException;
import org.d3.Console;
import org.d3.HostAddress;
import org.d3.RegistrationException;
import org.d3.actor.Agency;
import org.d3.actor.RemoteActor;
import org.d3.protocol.Protocols;
import org.d3.tools.CacheCreationException;

public class RemoteAgency {
	protected final ConcurrentHashMap<Integer, RemotePort> ports;
	protected final ConcurrentHashMap<String, RemotePort> schemes;
	protected final RemoteHost remoteHost;
	protected final String id;
	protected long lastPresenceDate;
	protected String digest;
	protected final URI uri;

	public RemoteAgency(RemoteHost remoteHost, String agencyId) {
		this(remoteHost, agencyId, null, "");
	}

	public RemoteAgency(RemoteHost remoteHost, String agencyId,
			String protocols, String digest) {
		this.ports = new ConcurrentHashMap<Integer, RemotePort>();
		this.schemes = new ConcurrentHashMap<String, RemotePort>();
		this.lastPresenceDate = System.currentTimeMillis();
		this.remoteHost = remoteHost;
		this.id = agencyId;

		if (protocols != null)
			updateProtocols(protocols);

		if (digest != null)
			updateDigest(digest);

		HostAddress address = remoteHost.getAddress();
		String uri = String.format("//%s/%s/%s", address.getHost(), agencyId,
				agencyId);

		try {
			this.uri = new URI(uri);
		} catch (URISyntaxException e) {
			throw new RegistrationException(e);
		}
	}

	public RemoteHost getRemoteHost() {
		return remoteHost;
	}

	/**
	 * Get a remote actor hosted by this remote agency.
	 * 
	 * @param path
	 *            path to the actor (with no agencyId).
	 * @return a remote actor if existed
	 */
	public RemoteActor getRemoteActor(String path)
			throws ActorNotFoundException {
		String uriStr = String.format("//%s/%s/%s", remoteHost.getAddress()
				.getHost(), id, path);

		URI uri;

		try {
			uri = new URI(uriStr);
		} catch (URISyntaxException e) {
			throw new ActorNotFoundException(e);
		}

		try {
			return Agency.getLocalAgency().getRemoteActors().get(uri);
		} catch (CacheCreationException e) {
			Console.exception(e);
		}
		
		throw new ActorNotFoundException();
	}

	public String getId() {
		return id;
	}

	public void udpatePresence(long date) {
		this.lastPresenceDate = date;
	}

	public void updateDigest(String digest) {
		this.digest = digest;
	}

	public void updateProtocols(String protocols) {
		Matcher m = Protocols.PROTOCOL_EXPORT_PATTERN.matcher(protocols);

		LinkedList<String> schemes = new LinkedList<String>();
		LinkedList<Integer> ports = new LinkedList<Integer>();
		
		while (m.find()) {
			String scheme = m.group(Protocols.PROTOCOL_EXPORT_PATTERN_SCHEME);
			int port = Integer.parseInt(m
					.group(Protocols.PROTOCOL_EXPORT_PATTERN_PORT));

			schemes.addLast(scheme);
			ports.addLast(port);
		}

		LinkedList<Integer> portsToRemove = new LinkedList<Integer>();

		for (Integer i : this.ports.keySet())
			if (!ports.contains(i))
				portsToRemove.add(i);

		for (int i = 0; i < portsToRemove.size(); i++)
			this.ports.remove(portsToRemove.get(i));

		portsToRemove.clear();

		for (int i = 0; i < ports.size(); i++) {
			if (!this.ports.containsKey(ports.get(i))) {
				try {
					registerPort(ports.get(i), schemes.get(i));
				} catch (RemotePortException rpe) {
					Console.exception(rpe);
				}
			}
		}

		ports.clear();
		schemes.clear();
	}

	public String getDigest() {
		return digest;
	}

	public void registerPort(int port, String scheme)
			throws RemotePortException {
		if (ports.containsKey(port))
			throw new RemotePortException();

		RemotePort rp = new RemotePort(this, scheme, port);
		ports.put(port, rp);
		schemes.putIfAbsent(scheme, rp);
		Console.warning("register %s %s:%d on %s",
				rp.isTransmitter() ? "transmitter" : "protocol", scheme, port,
				id);
	}

	public void unregisterPort(int port) {
		RemotePort rp = ports.remove(port);

		if (rp != null) {
			if (schemes.remove(rp.getScheme(), rp)) {
				for (RemotePort rpn : ports.values()) {
					if (rpn.getScheme().equals(rp.getScheme())) {
						schemes.put(rpn.getScheme(), rpn);
						break;
					}
				}
			}
		}
	}

	public RemotePort getCompatibleRemotePort(String scheme)
			throws NoRemotePortAvailableException {
		RemotePort rp = schemes.get(scheme);

		if (rp == null)
			throw new NoRemotePortAvailableException();

		return rp;
	}

	public RemotePort getRandomRemotePort()
			throws NoRemotePortAvailableException {
		Iterator<RemotePort> it = ports.values().iterator();

		if (it.hasNext())
			return it.next();

		throw new NoRemotePortAvailableException();
	}

	public RemotePort getRandomRemotePortTransmittable()
			throws NoRemotePortAvailableException {
		Iterator<RemotePort> it = ports.values().iterator();

		while (it.hasNext()) {
			RemotePort rp = it.next();

			if (rp.isTransmitter())
				return rp;
		}

		throw new NoRemotePortAvailableException();
	}

	public RemoteActor asRemoteActor() {
		try {
			return Agency.getLocalAgency().getRemoteActors().get(uri);
		} catch (Exception e) {
			Console.exception(e);
			return null;
		}
	}
}
