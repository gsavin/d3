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
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.concurrent.Semaphore;

import org.d3.Actor;
import org.d3.Args;
import org.d3.Console;
import org.d3.FaultManager;
import org.d3.HostAddress;
import org.d3.RegistrationException;
import org.d3.FaultManager.FaultPolicy;
import org.d3.agency.AgencyEvents;
import org.d3.agency.AgencyExitThread;
import org.d3.agency.IpTables;
import org.d3.annotation.ActorDescription;
import org.d3.annotation.ActorPath;
import org.d3.annotation.Callable;
import org.d3.entity.Traveller;
import org.d3.entity.TravellerChecker;
import org.d3.entity.migration.MigrationProtocol;
import org.d3.events.ActorEventDispatcher;
import org.d3.events.EventDispatchable;
import org.d3.events.EventDispatcher;
import org.d3.fault.DefaultFaultManager;
import org.d3.feature.Features;
import org.d3.protocol.BadProtocolException;
import org.d3.protocol.Protocols;
import org.d3.remote.HostNotFoundException;
import org.d3.remote.RemoteActors;
import org.d3.remote.RemoteAgency;
import org.d3.remote.RemoteHost;
import org.d3.remote.RemoteHosts;
import org.d3.remote.UnknownAgencyException;
import org.d3.security.D3SecurityManager;
import org.d3.tools.Utils;

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

	public static final String CALLABLE_REGISTER_NEW_HOST = "register.host";
	public static final String CALLABLE_REGISTER_NEW_AGENCY = "register.agency";
	public static final String CALLABLE_UNREGISTER_AGENCY = "unregister.agency";
	public static final String CALLABLE_GET_DIGEST = "agency.digest";
	public static final String CALLABLE_ACTORS_LIST = "actors.list";

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
	private static HostAddress localHost;
	private static FaultManager faultManager;

	public static String getArg(String key) {
		return localArgs.get(key);
	}

	public static Args getActorArgs(Actor actor) {
		return localArgs.getArgs(actor);
	}

	public static Args getArgs() {
		return localArgs;
	}

	public static FaultManager getFaultManager() {
		return faultManager;
	}

	private static void initFaultManager(Args args) {
		Throwable initException = null;

		if (args.has("class")) {
			try {
				Class<?> cls = Class.forName(args.get("class"));
				Object obj = cls.newInstance();

				if (obj instanceof FaultManager)
					faultManager = (FaultManager) obj;
				else
					throw new ClassCastException("not a fault manager");
			} catch (Exception e) {
				faultManager = new DefaultFaultManager();
				initException = e;
			}
		} else
			faultManager = new DefaultFaultManager();

		if (args.has("policy")) {
			try {
				FaultPolicy policy = FaultPolicy.valueOf(args.get("policy")
						.toUpperCase());
				faultManager.setFaultPolicy(policy);
			} catch (IllegalArgumentException e) {
				Console.exception(e);
			}
		}

		if (initException != null)
			faultManager.handle(initException, null);
	}

	public static void enableAgency(Args args) {
		if (localAgency == null) {
			Console.init(args.getArgs("console"));
			initFaultManager(args.getArgs("system.fault"));

			if (!(System.getSecurityManager() instanceof D3SecurityManager))
				System.setSecurityManager(new D3SecurityManager());

			SecureRandom random = new SecureRandom();

			localAgencyId = String.format("%x%x", System.nanoTime(), random
					.nextLong());

			localArgs = args;

			try {
				String ifname = localArgs.get("system.net.interface", "etho");
				InetAddress address;
				boolean inet6 = localArgs.getBoolean("system.net.inet6", true);

				if (ifname == null)
					address = InetAddress.getLocalHost();
				else
					try {
						address = Utils.getAddressForInterface(ifname, inet6);
					} catch (Exception e) {
						address = InetAddress.getLocalHost();
					}

				localHost = HostAddress.getByInetAddress(address);
				Console.info("localhost is %s", localHost);
			} catch (UnknownHostException e) {
				throw new RegistrationException(e);
			}

			localAgency = new Agency(localAgencyId);
			localAgency.init();
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

	public static HostAddress getLocalHost() {
		return localHost;
	}

	public static void shutdown() {
		// TODO
	}

	private IpTables ipTables;
	private final EventDispatcher<AgencyEvents> eventDispatcher;
	// private ActorManager identifiableObjects;
	private final RemoteHosts remoteHosts;
	private final Protocols protocols;
	private final Features features;
	private final Actors actors;
	private final RemoteActors remoteActors;
	private final Semaphore actorThreadSemaphore;

	private Agency(String id) {
		super(id);

		int concurrentActorThreads = 10;

		if (localArgs.has("actors.threads.concurrent"))
			concurrentActorThreads = localArgs
					.getInteger("actors.threads.concurrent");

		int remoteActorsCapacity = 1000;

		if (localArgs.has("actors.remote.cache"))
			concurrentActorThreads = localArgs
					.getInteger("actors.remote.cache");

		ipTables = new IpTables();

		this.eventDispatcher = new ActorEventDispatcher<AgencyEvents>(
				AgencyEvents.class, this);
		this.remoteHosts = new RemoteHosts();
		this.protocols = new Protocols();
		this.features = new Features();
		this.actors = new Actors();
		this.remoteActors = new RemoteActors(remoteActorsCapacity);
		this.actorThreadSemaphore = new Semaphore(concurrentActorThreads);
	}

	public final void initAgency() {
		checkBodyThreadAccess();

		Runtime.getRuntime().addShutdownHook(new AgencyExitThread());

		Protocols.init();
		Features.init();

		if (localArgs.getBoolean("system.entity.migration", false)) {
			int port;

			if (localArgs.has("system.entity.migration.port"))
				port = localArgs.getInteger("system.entity.migration.port");
			else
				port = MigrationProtocol.DEFAULT_PORT;

			String ifname;

			if (localArgs.has("system.net.interface"))
				ifname = localArgs.get("system.net.interface");
			else
				ifname = null;

			try {
				Protocols.enableProtocol(MigrationProtocol.class.getName(),
						ifname, port);
			} catch (BadProtocolException e) {
				Console.error("unable to enable migration");
				Agency.getFaultManager().handle(e, null);
			}
		}

		if (localArgs.has("test.traveller")) {
			Traveller t = new Traveller();
			t.init();
			TravellerChecker tc = new TravellerChecker(t.getId() + "_checker");
			tc.init(t.getReference());
		}

		Console.info("agency enable");
	}

	public final IdentifiableType getType() {
		return IdentifiableType.AGENCY;
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

	public Actors getActors() {
		return actors;
	}

	public RemoteActors getRemoteActors() {
		return remoteActors;
	}

	public Protocol getDefaultProtocol() {
		// TODO
		return null;
	}

	public IpTables getIpTables() {
		return ipTables;
	}

	public Semaphore getActorThreadSemaphore() {
		if (Thread.currentThread() instanceof ActorThread)
			return actorThreadSemaphore;

		throw new NotActorThreadException();
	}

	public void register(LocalActor actor) throws RegistrationException {
		actors.register(actor);

		try {
			switch (actor.getType()) {
			case FEATURE:
				features.register((Feature) actor);
				break;
			}
		} catch (Exception e) {
			throw new RegistrationException(e);
		}
	}

	public void unregister(LocalActor actor) {
		actors.unregister(actor);

		try {
			switch (actor.getType()) {
			case FEATURE:

				break;
			}
		} catch (Exception e) {
			throw new RegistrationException(e);
		}
	}

	public EventDispatcher<AgencyEvents> getEventDispatcher() {
		return eventDispatcher;
	}

	@Callable(CALLABLE_REGISTER_NEW_HOST)
	public RemoteHost registerNewHost(HostAddress host) {
		RemoteHost remoteHost;

		try {
			remoteHost = remoteHosts.get(host);
		} catch (HostNotFoundException e) {
			remoteHost = remoteHosts.registerHost(host);
		}

		return remoteHost;
	}

	@Callable(CALLABLE_REGISTER_NEW_AGENCY)
	public RemoteAgency registerNewAgency(RemoteHost host, String id) {
		RemoteAgency remoteAgency;

		try {
			remoteAgency = host.getRemoteAgency(id);
		} catch (UnknownAgencyException e) {
			remoteAgency = remoteHosts.registerAgency(host, id);
		}

		return remoteAgency;
	}

	@Callable(CALLABLE_UNREGISTER_AGENCY)
	public void unregisterAgency(RemoteAgency remoteAgency) {
		remoteHosts.unregisterAgency(remoteAgency);
	}

	@Callable(CALLABLE_GET_DIGEST)
	public String getDigest() {
		return actors.getDigest();
	}

	@Callable(CALLABLE_ACTORS_LIST)
	public String[] getActorsList() {
		return actors.exportActorsPath();
	}
}
