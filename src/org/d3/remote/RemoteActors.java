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

import org.d3.actor.Agency;
import org.d3.actor.RemoteActor;
import org.d3.tools.Cache;
import org.d3.tools.CacheCreationException;

public class RemoteActors extends Cache<URI, RemoteActor> {

	public static URI getRemoteActorURI(RemoteAgency remote, String fullpath)
			throws URISyntaxException {
		String host = remote.getRemoteHost().getAddress().getHost();
		String agencyId = remote.getId();

		if (!fullpath.startsWith("/"))
			fullpath = "/" + fullpath;

		String uri = String.format("//%s/%s%s", host, agencyId, fullpath);

		return new URI(uri);
	}

	public RemoteActors(int capacity) {
		super(capacity);
	}

	protected RemoteActor createObject(URI uri) throws CacheCreationException {
		RemoteHost remoteHost;
		RemoteAgency remoteAgency;
		RemoteActor remoteActor;
		String path, id;

		path = uri.getPath();

		try {
			remoteHost = Agency.getLocalAgency().getRemoteHosts()
					.get(uri.getHost());
			remoteAgency = remoteHost.getRemoteAgency(path.substring(1,
					path.indexOf('/', 1)));
		} catch (Exception e) {
			throw new CacheCreationException(e);
		}

		id = path.substring(path.lastIndexOf('/') + 1);
		path = path.substring(path.indexOf('/', 1), path.lastIndexOf('/'));

		remoteActor = new RemoteActor(remoteAgency, path, id);
		return remoteActor;
	}
}
