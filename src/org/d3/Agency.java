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
package org.d3;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.d3.actor.LocalActor;
import org.d3.agency.AgencyListener;
import org.d3.agency.ApplicationExecutor;
import org.d3.agency.Feature;
import org.d3.agency.FeatureManager;
import org.d3.agency.ActorManager;
import org.d3.agency.IpTables;
import org.d3.agency.RemoteAgency;
import org.d3.agency.ActorManager.RegistrationStatus;
import org.d3.annotation.ActorDescription;
import org.d3.annotation.ActorPath;
import org.d3.annotation.RequestCallable;
//import org.d3.atlas.internal.D3Atlas;
import org.d3.protocol.Protocols;
import org.d3.request.RequestListener;
//import org.d3.request.RequestService;
import org.d3.security.D3SecurityManager;

/**
 * Agencies are the base of the distribution environment.
 * 
 * @author gsavin
 * 
 */
@ActorDescription("Agency object.")
@ActorPath("/")
public class Agency extends LocalActor implements RequestListener {
	public static enum Argument {
		PROTOCOLS("protocols"), FEATURES("features"), DEFAULT_CHARSET(
				"system.cs.default"), REQUEST_SERVICE("request.service");

		public final String key;

		Argument(String key) {
			this.key = key;
		}
	}

	private static Agency localAgency;
	private static Args localArgs;

	public static String getArg(String key) {
		return localArgs.get(key);
	}

	public static void enableAgency(Args args) {
		if (localAgency == null) {
			if (!(System.getSecurityManager() instanceof D3SecurityManager))
				System.setSecurityManager(new D3SecurityManager());

			String agencyId;

			try {
				agencyId = InetAddress.getLocalHost().getHostName();

				if (agencyId.equals("localhost"))
					throw new UnknownHostException();

			} catch (UnknownHostException e) {
				agencyId = String.format("%X:%X", System.nanoTime(),
						(long) (Math.random() * Long.MAX_VALUE));
			}

			localArgs = args;
			localAgency = new Agency();

			System.out.printf("[agency] create agency%n");

			if (localArgs.has(Argument.PROTOCOLS.key)) {
				String[] protocols = localArgs.get(Argument.PROTOCOLS.key)
						.split(",");

				for (String protocol : protocols)
					Protocols.initProtocol(protocol);
			}

			if (localArgs.has(Argument.FEATURES.key)) {
				String[] features = localArgs.get(Argument.FEATURES.key).split(
						",");

				for (String feature : features) {
					String r = loadFeature(feature);

					if (r != null)
						System.err
								.printf("[agency] error while loading feature \"%s\": %s%n",
										feature, r);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static String loadFeature(String name) {
		try {
			String classname = "org.d3.agency.feature." + name.trim();
			Class<? extends Feature> featureClass = (Class<? extends Feature>) Class
					.forName(classname);
			Feature feature = featureClass.newInstance();
			localAgency.addFeature(feature);
		} catch (ClassNotFoundException e) {
			return e.getMessage();
		} catch (InstantiationException e) {
			return e.getMessage();
		} catch (IllegalAccessException e) {
			return e.getMessage();
		}

		return null;
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

	public static InetAddress getLocalHost() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	// ExtendableRequestInterpreter requestInterpreter;
	// private String agencyId;
	private ConcurrentHashMap<String, RemoteAgency> remoteAgencies;
	private FeatureManager featureManager;
	private IpTables ipTables;
	// private RequestService requestService;
	// private Atlas atlas;
	private ConcurrentLinkedQueue<AgencyListener> agencyListeners;
	private ActorManager identifiableObjects;

	private Agency() {
		super("agency");
		/*
		 * try { agencyId = InetAddress.getLocalHost().getHostName();
		 * 
		 * if (agencyId.equals("localhost")) throw new UnknownHostException();
		 * 
		 * } catch (UnknownHostException e) { agencyId = String.format("%X:%X",
		 * System.nanoTime(), (long) (Math.random() * Long.MAX_VALUE)); }
		 */
		identifiableObjects = new ActorManager();

		remoteAgencies = new ConcurrentHashMap<String, RemoteAgency>();
		featureManager = new FeatureManager(this);
		ipTables = new IpTables();

		agencyListeners = new ConcurrentLinkedQueue<AgencyListener>();

		// requestInterpreter = new DefaultRequestInterpreter();
		// requestService = new RequestService();
		// requestService.init(
		// Collections.unmodifiableCollection(agencyListeners),
		// / localArgs.getArgs(Argument.REQUEST_SERVICE.key));

		// atlas = new D3Atlas();
		// atlas.init(this);

		registerIdentifiableObject(this);
		// /registerIdentifiableObject(atlas);

		// identifiableObjects.alias(atlas, "default");
	}

	/*
	 * public final String getId() { return agencyId; }
	 */
	public final IdentifiableType getType() {
		return IdentifiableType.agency;
	}

	public Args getArgs() {
		return localArgs;
	}

	public IpTables getIpTables() {
		return ipTables;
	}

	/*
	 * public Atlas getAtlas() { return atlas; }
	 */
	public void requestReceived(Request r) {
		// requestService.executeRequest(r);
	}

	public void handle(Request r) {

	}

	public void launch(Application app) {
		registerIdentifiableObject(app);
		addAgencyListener(app);

		new ApplicationExecutor(app);
	}

	public void registerAgency(String remoteId, String address,
			String protocols, String digest) {
		boolean blacklisted = ipTables.isBlacklisted(address);

		Console.info("agency %s digest: %s", remoteId, digest);

		if (!remoteAgencies.containsKey(remoteId) && !blacklisted) {
			InetAddress inet = null;

			try {
				inet = InetAddress.getByName(address);
				if (!inet.isReachable(400)) {
					ipTables.declareErrorOn(address);
					Console.error("[agency] remote agency %s not reachable%n",
							remoteId);
					return;
				}
			} catch (UnknownHostException e) {
				ipTables.declareErrorOn(address);
				Console.error("[agency] remote agency %s not reachable%n",
						remoteId);
				return;
			} catch (IOException e) {
				ipTables.declareErrorOn(address);
				Console.error("[agency] remote agency %s not reachable%n",
						remoteId);
				return;
			}

			Console.info("register new agency: %s %s@%s", remoteId, address,
					protocols);

			RemoteAgency rad = new RemoteAgency(inet, protocols, digest);
			remoteAgencies.put(remoteId, rad);
			ipTables.registerId(remoteId, address);

			for (AgencyListener l : agencyListeners)
				l.newAgencyRegistered(rad);
		} else if (!blacklisted) {
			RemoteAgency remote = remoteAgencies.get(remoteId);

			if (!remote.getDigest().equals(digest)) {
				remote.updateDigest(digest);

				for (AgencyListener l : agencyListeners)
					l.remoteAgencyDescriptionUpdated(remote);
			}
		}
	}

	public void unregisterAgency(RemoteAgency rad) {
		remoteAgencies.remove(rad.getHost());
		Console.info("unregister agency %s", rad.getHost());
	}

	public boolean registerIdentifiableObject(Actor idObject) {
		if (identifiableObjects.register(idObject) != RegistrationStatus.accepted) {
			System.err.printf("[agency] object not registered%n");
			return false;
		}

		for (AgencyListener l : agencyListeners)
			l.identifiableObjectRegistered(idObject);

		return true;
	}

	public void unregisterIdentifiableObject(Actor idObject) {
		identifiableObjects.unregister(idObject);

		for (AgencyListener l : agencyListeners)
			l.identifiableObjectUnregistered(idObject);
	}

	public Actor getIdentifiableObject(URI uri)
			throws ActorNotFoundException {
		return identifiableObjects.get(uri);
	}

	public Actor getIdentifiableObject(IdentifiableType type,
			String path) throws ActorNotFoundException {
		return identifiableObjects.get(type, path);
	}

	public void addFeature(Feature f) {
		featureManager.addFeature(f);
	}

	public Iterable<RemoteAgency> eachRemoteAgency() {
		return remoteAgencies.values();
	}

	public RemoteAgency getRemoteAgencyDescription(String agencyId) {
		return remoteAgencies.get(agencyId);
	}

	public void remoteAgencyDescriptionChanged(String agencyId) {
		if (remoteAgencies.get(agencyId) != null) {
			RemoteAgency rad = remoteAgencies.get(agencyId);

			for (AgencyListener l : agencyListeners)
				l.remoteAgencyDescriptionUpdated(rad);
		}
	}

	public void addAgencyListener(AgencyListener listener) {
		agencyListeners.add(listener);
	}

	public void removeAgencyListener(AgencyListener listener) {
		agencyListeners.remove(listener);
	}

	/*
	 * public void handleRequest(IdentifiableObject source, IdentifiableObject
	 * target, Request r) { if( r.getName().startsWith("entity:") )
	 * atlas.handleRequest(source, target, r); }
	 */

	public void lazyCheckEntitiesOn(RemoteAgency rad) {
		Request r = new Request(this, rad, "getEntityList", null);
		// Protocols.createRequestTo(this,rad,"entity:getlist");
		Protocols.sendRequest(r);
	}

	@RequestCallable("getDigest")
	public String getDigest() {
		return identifiableObjects.getDigest();
	}

	@RequestCallable("getIdentifiableObjectList")
	public URI[] getIdentifiableObjectList(IdentifiableType type) {
		Actor[] objects = identifiableObjects.get(type);
		URI[] uris = new URI[objects.length];

		for (int i = 0; i < objects.length; i++)
			uris[i] = objects[i].getURI();

		return uris;
	}

	@RequestCallable("ping")
	public String ping() {
		return "pong";
	}
}