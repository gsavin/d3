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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import org.d3.actor.Agency;
import org.d3.actor.RemoteActor;

public class RemoteActors {

	private ReentrantLock lock;
	private HashMap<URI, RemoteActor> remoteActors;
	private LinkedList<URI> availables;
	private final int capacity;

	public RemoteActors(int capacity) {
		this.lock = new ReentrantLock();
		this.remoteActors = new HashMap<URI, RemoteActor>();
		this.availables = new LinkedList<URI>();
		this.capacity = capacity;
	}

	public RemoteActor get(URI uri) throws HostNotFoundException,
			UnknownAgencyException {
		RemoteActor ra = null;

		try {
			lock();

			int index = availables.indexOf(uri);

			if (index < 0)
				index = create(uri);

			moveToTop(index);

			ra = remoteActors.get(uri);
		} finally {
			unlock();
		}

		return ra;
	}

	private void moveToTop(int index) {
		if (index < availables.size() - 1) {
			URI uri = availables.remove(index);
			availables.push(uri);
		}
	}

	private int create(URI uri) throws HostNotFoundException,
			UnknownAgencyException {
		RemoteHost remoteHost;
		RemoteAgency remoteAgency;
		RemoteActor remoteActor;
		String path, id;

		path = uri.getPath();

		remoteHost = Agency.getLocalAgency().getRemoteHosts()
				.get(uri.getHost());
		remoteAgency = remoteHost.getRemoteAgency(path.substring(1,
				path.indexOf('/', 1)));

		id = path.substring(path.lastIndexOf('/') + 1);
		path = path.substring(path.indexOf('/', 1), path.lastIndexOf('/'));

		remoteActor = new RemoteActor(remoteAgency, path, id);

		while (availables.size() >= capacity)
			pop();

		availables.add(uri);
		remoteActors.put(uri, remoteActor);

		return availables.size() - 1;
	}

	private void pop() {
		URI uri = availables.poll();

		if (uri != null)
			remoteActors.remove(uri);
	}

	private void lock() {
		lock.lock();
	}

	private void unlock() {
		lock.unlock();
	}
}
