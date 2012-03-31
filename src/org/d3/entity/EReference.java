/*
 * This file is part of d3 <http://d3-project.org>.
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
 * Copyright 2010 - 2011 Guilhelm Savin
 */
package org.d3.entity;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.d3.Actor;
import org.d3.ActorNotFoundException;
import org.d3.Console;
import org.d3.actor.Agency;
import org.d3.actor.CallException;
import org.d3.actor.Future;
import org.d3.actor.UnregisteredActorException;
import org.d3.tools.Cache;
import org.d3.tools.CacheCreationException;

/**
 * Reference is a proxy to call an actor. It handles migration of the referenced
 * actor
 * 
 * @author Guilhelm Savin
 * 
 */
public class EReference {

	private Actor ref;

	private EReference(Actor ref) {
		if (ref == null)
			throw new NullPointerException();

		this.ref = ref;
	}

	public Future call(String name, Object... args)
			throws ActorNotFoundException {
		if (ref == null)
			throw new ActorNotFoundException();

		ERefFuture f = new ERefFuture(name, args);
		call(name, f, args);
		return f;
	}

	private void call(String name, ERefFuture f, Object... args) {
		ref.call(name, f, args);
	}

	private void redirect(Redirection r) {
		Console.warning("redirect to \"%s\"", r.getNewLocation());
		redirect(r, new HashSet<URI>());
	}

	private void redirect(Redirection r, Set<URI> uris) {
		URI loc = r.getNewLocation();

		if (uris.contains(loc)) {
			return;
		}

		uris.add(loc);

		try {
			ref = Agency.getLocalAgency().getActors().get(r.getNewLocation());
		} catch (ActorNotFoundException e) {
			Console.error("redirection failed");
			ref = null;
		} catch (UnregisteredActorException e) {
			if (e.getCause() != null && e.getCause() instanceof Redirection)
				redirect((Redirection) e.getCause(), uris);
			else {
				Console.error("redirection failed");
				ref = null;
			}
		}
	}

	private class ERefFuture extends Future {
		String name;
		Object[] args;

		ERefFuture(String name, Object[] args) {
			super();

			this.name = name;
			this.args = args;
		}

		public void init(Object value) {
			Redirection r = null;

			if (value instanceof Redirection)
				r = (Redirection) value;
			else if (value instanceof CallException) {
				Throwable cause = ((CallException) value).getCause();

				while (cause != null && r == null) {
					if (cause instanceof Redirection)
						r = (Redirection) cause;

					cause = cause.getCause();
				}
			}

			if (r != null) {
				redirect(r);

				if (ref == null) {
					super.init(new CallException(new ActorNotFoundException()));
				} else {
					Console.info("redirect call");
					call(name, this, args);
				}
			} else {
				super.init(value);
			}
		}
	}

	private static class ERefCache extends Cache<URI, EReference> {

		public ERefCache(int capacity) {
			super(capacity);
		}

		protected EReference createObject(URI key)
				throws CacheCreationException {
			try {
				Actor ref = Agency.getLocalAgency().getActors().get(key);
				return new EReference(ref);
			} catch (ActorNotFoundException e) {
				throw new CacheCreationException(e);
			} catch (UnregisteredActorException e) {

				throw new CacheCreationException(e);
			}
		}
	}

	private static ERefCache cache;
	public static final String E_REF_CACHE_CAPACITY_ARG = "system.entity.ref.cache";

	public static synchronized EReference get(URI uri) {
		if (cache == null) {
			int cap = 1000;

			if (Agency.getArgs().has(E_REF_CACHE_CAPACITY_ARG))
				cap = Agency.getArgs().getInteger(E_REF_CACHE_CAPACITY_ARG);

			cache = new ERefCache(cap);
		}

		try {
			return cache.get(uri);
		} catch (CacheCreationException e) {
			return null;
		}
	}

	public static EReference get(Actor actor) {
		return get(actor.getURI());
	}
}
