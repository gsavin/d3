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
package org.ri2c.d3;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.ri2c.d3.agency.AgencyListener;
import org.ri2c.d3.agency.ApplicationExecutor;
import org.ri2c.d3.agency.Feature;
import org.ri2c.d3.agency.FeatureManager;
import org.ri2c.d3.agency.IdentifiableObjectManager;
import org.ri2c.d3.agency.IpTables;
import org.ri2c.d3.agency.RemoteAgency;
import org.ri2c.d3.agency.IdentifiableObjectManager.RegistrationStatus;
import org.ri2c.d3.annotation.IdentifiableObjectDescription;
import org.ri2c.d3.annotation.IdentifiableObjectPath;
import org.ri2c.d3.annotation.RequestCallable;
import org.ri2c.d3.atlas.internal.D3Atlas;
import org.ri2c.d3.protocol.Protocols;
import org.ri2c.d3.request.RequestListener;
import org.ri2c.d3.request.RequestService;

import static org.ri2c.d3.IdentifiableObject.Tools.getURI;

@IdentifiableObjectDescription("Agency object.")
@IdentifiableObjectPath("/")
public class Agency implements IdentifiableObject, RequestListener {
	private static Agency localAgency;
	private static Args localArgs;

	public static String getArg(String key) {
		return localArgs.get(key);
	}

	public static void enableAgency(Args args) {
		if (localAgency == null) {
			localArgs = args;
			localAgency = new Agency();

			System.out.printf("[agency] create agency%n");

			if (localArgs.has("l2d.protocols")) {
				String[] protocols = localArgs.get("l2d.protocols").split(",");

				for (String protocol : protocols)
					Protocols.initProtocol(protocol);
			}

			if (localArgs.has("l2d.features")) {
				String[] features = localArgs.get("l2d.features").split(",");

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
			String classname = "org.ri2c.d3.agency.feature." + name.trim();
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

	public static Agency getLocalAgency() {
		return localAgency;
	}

	// ExtendableRequestInterpreter requestInterpreter;
	private String agencyId;
	private ConcurrentHashMap<String, RemoteAgency> remoteAgencies;
	private FeatureManager featureManager;
	private IpTables ipTables;
	private RequestService requestService;
	private Atlas atlas;
	private ConcurrentLinkedQueue<AgencyListener> agencyListeners;
	private IdentifiableObjectManager identifiableObjects;

	private Agency() {
		try {
			agencyId = InetAddress.getLocalHost().getHostName();

			if (agencyId.equals("localhost"))
				throw new UnknownHostException();

		} catch (UnknownHostException e) {
			agencyId = String.format("%X:%X", System.nanoTime(),
					(long) (Math.random() * Long.MAX_VALUE));
		}

		identifiableObjects = new IdentifiableObjectManager();

		remoteAgencies = new ConcurrentHashMap<String, RemoteAgency>();
		featureManager = new FeatureManager(this);
		ipTables = new IpTables();

		agencyListeners = new ConcurrentLinkedQueue<AgencyListener>();

		// requestInterpreter = new DefaultRequestInterpreter();
		requestService = new RequestService();
		requestService.init(
				Collections.unmodifiableCollection(agencyListeners),
				localArgs.getArgs("l2d.request.service"));

		atlas = new D3Atlas();
		atlas.init(this);

		registerIdentifiableObject(this);
		registerIdentifiableObject(atlas);

		identifiableObjects.alias(atlas, "default");
	}

	public final String getId() {
		return agencyId;
	}

	public final IdentifiableType getType() {
		return IdentifiableType.agency;
	}

	public Args getArgs() {
		return localArgs;
	}

	public IpTables getIpTables() {
		return ipTables;
	}

	public Atlas getAtlas() {
		return atlas;
	}

	public void requestReceived(Request r) {
		requestService.executeRequest(r);
	}

	/*
	 * public void addRequestInterpreter( String requestName, RequestInterpreter
	 * ri ) { requestInterpreter.addInterpreter(requestName,ri); }
	 */

	public void launch(Application app) {
		registerIdentifiableObject(app);
		addAgencyListener(app);

		new ApplicationExecutor(app);
	}

	public void registerAgency(String remoteId, String address,
			String protocols, String digest) {
		boolean blacklisted = ipTables.isBlacklisted(address);

		if (!remoteAgencies.containsKey(remoteId) && !blacklisted) {
			try {
				InetAddress inet = InetAddress.getByName(address);
				if (!inet.isReachable(400)) {
					ipTables.declareErrorOn(address);
					System.err.printf(
							"[agency] remote agency %s not reachable%n",
							remoteId);
					return;
				}
			} catch (UnknownHostException e) {
				ipTables.declareErrorOn(address);
				System.err.printf("[agency] remote agency %s not reachable%n",
						remoteId);
				return;
			} catch (IOException e) {
				ipTables.declareErrorOn(address);
				System.err.printf("[agency] remote agency %s not reachable%n",
						remoteId);
				return;
			}

			Console.info("register new agency: %s %s@%s", remoteId, address,
					protocols);

			RemoteAgency rad = new RemoteAgency(remoteId, address, protocols, digest);
			remoteAgencies.put(remoteId, rad);
			ipTables.registerId(remoteId, address);

			for (AgencyListener l : agencyListeners)
				l.newAgencyRegistered(rad);
		} else if (!blacklisted) {
			RemoteAgency remote = remoteAgencies.get(remoteId);
			
			if(!remote.getDigest().equals(digest)) {
				remote.updateDigest(digest);
				
				for( AgencyListener l: agencyListeners)
					l.remoteAgencyDescriptionUpdated(remote);
			}
		}
	}

	public void unregisterAgency(RemoteAgency rad) {
		remoteAgencies.remove(rad.getRemoteAgencyId());
		Console.info("unregister agency %s", rad.getRemoteAgencyId());
	}

	public boolean registerIdentifiableObject(IdentifiableObject idObject) {
		if (identifiableObjects.register(idObject) != RegistrationStatus.accepted) {
			System.err.printf("[agency] object not registered%n");
			return false;
		}

		for (AgencyListener l : agencyListeners)
			l.identifiableObjectRegistered(idObject);

		return true;
	}

	public void unregisterIdentifiableObject(IdentifiableObject idObject) {
		identifiableObjects.unregister(idObject);

		for (AgencyListener l : agencyListeners)
			l.identifiableObjectUnregistered(idObject);
	}

	public IdentifiableObject getIdentifiableObject(URI uri) {
		return identifiableObjects.get(uri);
	}
	
	public IdentifiableObject getIdentifiableObject(IdentifiableType type, String path) {
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
		IdentifiableObject[] objects = identifiableObjects.get(type);
		URI[] uris = new URI[objects.length];

		for (int i = 0; i < objects.length; i++)
			uris[i] = getURI(objects[i]);

		return uris;
	}

	@RequestCallable("ping")
	public String ping() {
		return "pong";
	}
}
