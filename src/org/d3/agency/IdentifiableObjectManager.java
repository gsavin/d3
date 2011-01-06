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
package org.d3.agency;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.d3.Agency;
//import org.d3.Console;
import org.d3.IdentifiableObject;
import org.d3.RemoteIdentifiableObject;
import org.d3.IdentifiableObject.IdentifiableType;
import org.d3.request.ObjectCoder;

import static org.d3.IdentifiableObject.Tools.getTypePath;

public class IdentifiableObjectManager {
	private static class Pool extends
			ConcurrentHashMap<String, IdentifiableObject> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7397149209003613470L;

	}

	public static enum RegistrationStatus {
		accepted, refused, alreadyRegistered, error
	}

	//private ConcurrentHashMap<IdentifiableType, Pool> pools;
	private final Pool objects;
	private final ReentrantLock registrationLock;
	private MessageDigest digest;
	private long lastChange;

	public IdentifiableObjectManager() {
		//pools = new ConcurrentHashMap<IdentifiableType, Pool>();

		//for (IdentifiableType t : IdentifiableType.values())
		//	pools.put(t, new Pool());

		objects = new Pool();
		registrationLock = new ReentrantLock();

		try {
			digest = MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		lastChange = System.nanoTime();
	}
	/*
	public void alias(String id, IdentifiableType type, String alias) {
		registrationLock.lock();

		Pool pool = pools.get(type);
		alias(pool.get(id), alias);

		registrationLock.unlock();
	}

	public void alias(IdentifiableObject idObject, String alias) {
		if (idObject == null)
			return;

		registrationLock.lock();

		Pool pool = pools.get(idObject.getType());

		if (!pool.containsKey(alias) && pool.containsKey(idObject.getId())) {
			pool.put(alias, idObject);

			Console.info("%s/%s --> %s/%s", alias, idObject.getType(),
					idObject.getId(), idObject.getType());
		}

		registrationLock.unlock();
	}
	*/
	public RegistrationStatus register(IdentifiableObject obj) {
		if (obj == null || obj.getId() == null || obj.getType() == null)
			return RegistrationStatus.error;

		String path = obj.getFullPath();

		registrationLock.lock();
		/*
		Pool p = pools.get(obj.getType());

		if (p == null) {
			registrationLock.unlock();
			return RegistrationStatus.error;
		}
		 */
		if (objects.containsKey(path)) {
			if (objects.get(path) == obj) {
				registrationLock.unlock();
				return RegistrationStatus.alreadyRegistered;
			} else {
				registrationLock.unlock();
				return RegistrationStatus.refused;
			}
		}

		objects.put(path, obj);
		needUpdate(obj);
		// Console.info("register %s", path);

		registrationLock.unlock();
		return RegistrationStatus.accepted;
	}

	public void unregister(IdentifiableObject obj) {
		if (obj == null)
			return;

		registrationLock.lock();
		objects.remove(obj.getFullPath());
		needUpdate(obj);
		registrationLock.unlock();

		// Console.info("unregister %s", obj.getId());
	}

	public IdentifiableObject get(IdentifiableType type,
			Class<? extends IdentifiableObject> cls, String id) {
		return objects.get(getTypePath(cls, id));
	}

	public IdentifiableObject get(IdentifiableType type, String path) {
		return objects.get(path);
	}

	public IdentifiableObject get(URI uri) {
		if (uri == null)
			throw new NullPointerException("uri is null");

		if (uri.getHost().equals(Agency.getLocalAgency().getId())) {
			return objects.get(uri.getPath());
		} else {
			return new RemoteIdentifiableObject(uri.getHost(), uri.getPath(),
					IdentifiableType.valueOf(uri.getScheme()));
		}
	}

	public IdentifiableObject[] get(IdentifiableType type) {
		IdentifiableObject[] objects = new IdentifiableObject[0];
		return this.objects.values().toArray(objects);
	}

	public String getDigest() {
		digest.update(Long.toString(lastChange).getBytes());
		return ObjectCoder.byte2hexa(digest.digest());
	}
	
	protected void needUpdate(IdentifiableObject cause) {
		switch (cause.getType()) {
		case entity:
		case feature:
		case protocol:
		case application:
			lastChange = System.currentTimeMillis();
			break;
		}
	}
}