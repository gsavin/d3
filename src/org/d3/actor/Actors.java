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

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.d3.Actor;
import org.d3.ActorNotFoundException;
import org.d3.Console;
import org.d3.RegistrationException;
import org.d3.events.EventDispatchable;
import org.d3.events.EventDispatcher;
import org.d3.protocol.request.ObjectCoder;
import org.d3.tools.CacheCreationException;

public class Actors implements Iterable<LocalActor>,
		EventDispatchable<ActorsEvent> {

	private final ConcurrentHashMap<String, LocalActor> actors;
	private String digest;
	private MessageDigest digestAlgorithm;
	private final EventDispatcher<ActorsEvent> eventDispatcher;

	public Actors() {
		actors = new ConcurrentHashMap<String, LocalActor>();
		digest = "";
		eventDispatcher = new EventDispatcher<ActorsEvent>(ActorsEvent.class);

		try {
			digestAlgorithm = MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e) {
			digestAlgorithm = null;
		}
	}

	public void register(LocalActor actor) throws RegistrationException {
		actor.checkBodyThreadAccess();

		String fullpath = actor.getFullPath();

		if (actors.putIfAbsent(fullpath, actor) != null)
			throw new RegistrationException();

		updateDigest();
		eventDispatcher.trigger(ActorsEvent.ACTOR_REGISTERED, actor);
	}

	public void unregister(LocalActor actor) {
		actors.remove(actor.getFullPath());
		updateDigest();
		eventDispatcher.trigger(ActorsEvent.ACTOR_UNREGISTERED, actor);

		Console.warning("unregistered");
	}

	public String getDigest() {
		return digest;
	}

	public Actor get(URI uri) throws ActorNotFoundException {
		String agencyId = uri.getPath().substring(1,
				uri.getPath().indexOf('/', 1));

		if (Agency.getLocalAgencyId().equals(agencyId))
			return get(uri.getPath().substring(
					uri.getPath().indexOf('/', 1) + 1));

		try {
			return Agency.getLocalAgency().getRemoteActors().get(uri);
		} catch (CacheCreationException e) {
			throw new ActorNotFoundException(e);
		}
	}

	public LocalActor get(String fullPath) {
		return actors.get(fullPath);
	}

	private void updateDigest() {
		String date = Long.toString(System.currentTimeMillis());
		digestAlgorithm.digest(date.getBytes());
		digest = ObjectCoder.byte2hexa(digestAlgorithm.digest());
	}

	public EventDispatcher<ActorsEvent> getEventDispatcher() {
		return eventDispatcher;
	}

	public Iterator<LocalActor> iterator() {
		return actors.values().iterator();
	}

	public String[] exportActorsPath() {
		String[] a = new String[1];
		return actors.keySet().toArray(a);
	}
}
