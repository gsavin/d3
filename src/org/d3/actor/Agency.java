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
package org.d3.actor;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.SecureRandom;

import org.d3.Actor;
import org.d3.Args;
import org.d3.Console;
import org.d3.RegistrationException;
import org.d3.agency.AgencyEvents;
import org.d3.agency.IpTables;
import org.d3.annotation.ActorDescription;
import org.d3.annotation.ActorPath;
import org.d3.annotation.Callable;
import org.d3.events.EventDispatchable;
import org.d3.events.EventDispatcher;
import org.d3.feature.Features;
import org.d3.protocol.Protocols;
import org.d3.remote.HostNotFoundException;
import org.d3.remote.RemoteAgency;
import org.d3.remote.RemoteHost;
import org.d3.remote.RemoteHosts;
import org.d3.remote.UnknownAgencyException;
import org.d3.security.D3SecurityManager;

/**
 * Agencies are the base of the distribution environment.
 * 
 * @author gsavin
 * 
 */
@ActorDescription("Agency object.")
@ActorPath("/")
public class Agency extends LocalActor implements
		EventDispatchable<AgencyEvents> {
	public static enum Argument {
		PROTOCOLS("protocols"), FEATURES("features"), DEFAULT_CHARSET(
				"system.cs.default");

		public final String key;

		Argument(String key) {
			this.key = key;
		}
	}

	private static Agency localAgency;
	private static String localAgencyId;
	private static Args localArgs;

	public static String getArg(String key) {
		return localArgs.get(key);
	}

	public static Args getActorArgs(Actor actor) {
		return localArgs.getArgs(actor);
	}

	public static void enableAgency(Args args) {
		if (localAgency == null) {
			if (!(System.getSecurityManager() instanceof D3SecurityManager))
				System.setSecurityManager(new D3SecurityManager());

			SecureRandom random = new SecureRandom();

			localAgencyId = String.format("%x%x", System.nanoTime(),
					random.nextLong());

			localArgs = args;
			localAgency = new Agency(localAgencyId);
			localAgency.init();

			System.out.printf("[agency] create agency%n");
		}
	}

	/**
	 * Get the local agency. This agency can be accessed from any objects of the
	 * runtime.
	 * 
	 * @return the local agency
	 */
	public static Agency getLocalAgency() {
		return localAgency;
	}

	public static String getLocalAgencyId() {
		return localAgencyId;
	}

	public static InetAddress getLocalHost() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private IpTables ipTables;
	private final EventDispatcher<AgencyEvents> eventDispatcher;
	// private ActorManager identifiableObjects;
	private final RemoteHosts remoteHosts;
	private final Protocols protocols;
	private final Features features;
	private final Actors actors;

	private Agency(String id) {
		super(id);

		// identifiableObjects = new ActorManager();

		// remoteAgencies = new ConcurrentHashMap<String, RemoteAgency>();
		// featureManager = new FeatureManager(this);
		ipTables = new IpTables();

		this.eventDispatcher = new EventDispatcher<AgencyEvents>(
				AgencyEvents.class, this);
		this.remoteHosts = new RemoteHosts();
		this.protocols = new Protocols();
		this.features = new Features();
		this.actors = new Actors();
	}

	public final void initAgency() {
		checkBodyThreadAccess();

		Protocols.init();
		Features.init();

		Console.info("agency \"%s\" operational", id);
	}

	public final IdentifiableType getType() {
		return IdentifiableType.agency;
	}

	public RemoteHosts getRemoteHosts() {
		return remoteHosts;
	}

	public Protocols getProtocols() {
		return protocols;
	}

	public Features getFeatures() {
		return features;
	}

	public Protocol getDefaultProtocol() {
		// TODO
		return null;
	}

	public Args getArgs() {
		return localArgs;
	}

	public IpTables getIpTables() {
		return ipTables;
	}

	public void registerAgency(String remoteId, String address,
			String protocols, String digest) {
		// boolean blacklisted = ipTables.isBlacklisted(address);

		Console.info("agency %s digest: %s", remoteId, digest);

	}

	public void unregisterAgency(RemoteAgency rad) {

	}

	public void register(LocalActor actor) throws RegistrationException {
		actors.register(actor);

		try {
			if (actor instanceof Entity) {
			} else if (actor instanceof Feature)
				features.register((Feature) actor);
		} catch (Exception e) {
			throw new RegistrationException(e);
		}
	}

	public void unregister(LocalActor actor) {

	}

	public EventDispatcher<AgencyEvents> getEventDispatcher() {
		return eventDispatcher;
	}

	@Callable("registerNewHost")
	public RemoteHost registerNewHost(InetAddress host) {
		RemoteHost remoteHost;

		try {
			remoteHost = remoteHosts.get(host);
		} catch (HostNotFoundException e) {
			remoteHost = remoteHosts.registerHost(host);
			eventDispatcher.trigger(AgencyEvents.REMOTE_HOST_REGISTERED,
					remoteHost);
		}

		return remoteHost;
	}

	@Callable("registerNewAgency")
	public RemoteAgency registerNewAgency(RemoteHost host, String id) {
		RemoteAgency remoteAgency;

		try {
			remoteAgency = host.getRemoteAgency(id);
		} catch (UnknownAgencyException e) {
			remoteAgency = host.registerAgency(id);
			eventDispatcher.trigger(AgencyEvents.REMOTE_AGENCY_REGISTERED,
					remoteAgency);
		}

		return remoteAgency;
	}

	@Callable("getDigest")
	public String getDigest() {
		// XXX
		return "";// identifiableObjects.getDigest();
	}

	@Callable("getIdentifiableObjectList")
	public URI[] getIdentifiableObjectList(IdentifiableType type) {
		// XXX
		return null;
	}

	@Callable("ping")
	public Boolean ping() {
		return Boolean.TRUE;
	}
}
