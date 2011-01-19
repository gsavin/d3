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

//import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;

import org.d3.actor.ActorInternalException;
import org.d3.actor.Agency;
import org.d3.actor.Protocol;
import org.d3.annotation.ActorPath;
import org.d3.remote.HostNotFoundException;
import org.d3.remote.NoRemotePortAvailableException;
import org.d3.remote.RemotePort;
import org.d3.remote.UnknownAgencyException;
import org.d3.tools.Utils;

@ActorPath("/")
public abstract class Actor {
	public static enum IdentifiableType {
		FEATURE, ENTITY, AGENCY, PROTOCOL, APPLICATION, migration, REMOTE
	}

	public static final String VALID_ID_PATTERN = "^[\\w\\d]([\\w\\d_-[.]]*[\\w\\d])?$";

	public static String getTypePath(Class<? extends Actor> cls) {
		return getTypePath(cls, null);
	}

	public static String getTypePath(Class<? extends Actor> idCls, String id) {
		ActorPath idPath = null;
		Class<?> cls = idCls;

		while (Actor.class.isAssignableFrom(cls) && idPath == null) {
			idPath = cls.getAnnotation(ActorPath.class);

			if (idPath == null) {
				for (Class<?> i : cls.getInterfaces()) {
					idPath = i.getAnnotation(ActorPath.class);
					if (idPath != null)
						break;
				}
			}

			cls = cls.getSuperclass();
		}

		String path = idPath == null ? "/" : idPath.value();

		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		if (!path.endsWith("/") && id != null) {
			path = path + "/";
		}

		return path + (id == null ? "" : id);
	}

	protected final String id;
	protected final InetAddress host;
	protected final String agencyId;
	private final String path;
	private transient URI uri;

	protected Actor(InetAddress host, String agencyId, String path, String id) {
		if (id == null || agencyId == null)
			throw new NullPointerException();

		if (!id.matches(VALID_ID_PATTERN))
			throw new InvalidIdException(id);

		if (path == null)
			path = getTypePath(getClass());

		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		if (!path.endsWith("/")) {
			path = path + "/";
		}

		this.host = host;
		this.id = id;
		this.agencyId = agencyId;
		this.path = path;
		this.uri = null;
	}

	public final InetAddress getHost() {
		return host;
	}

	/**
	 * Get the id which identify this object.
	 * 
	 * @return object id
	 */
	public final String getId() {
		return id;
	}

	/**
	 * 
	 * @return
	 */
	public abstract IdentifiableType getType();

	public abstract void init();

	public final String getPath() {
		return path;
	}

	public final String getFullPath() {
		return path + id;
	}

	public final String getAgencyId() {
		return agencyId;
	}
	
	public final String getAgencyFullPath() {
		return agencyId + getFullPath();
	}
	
	public final URI getURI() {
		if (uri == null) {
			String uriString;
			String scheme = null;
			int port = -1;
			String host = this.host.getHostAddress();
			String path = getFullPath();

			if (this.host instanceof Inet6Address)
				host = String.format("[%s]", host);

			if (isRemote()) {
				try {
					RemotePort rp = Utils.getRandomRemotePortFromRemoteAgency(
							this.host, agencyId);

					scheme = rp.getScheme();
					port = rp.getPort();
				} catch (HostNotFoundException e) {
					throw new ActorInternalException(e);
				} catch (UnknownAgencyException e) {
					throw new ActorInternalException(e);
				} catch (NoRemotePortAvailableException e) {
					throw new ActorInternalException(e);
				}
			} else {
				Protocol p = Agency.getLocalAgency().getDefaultProtocol();

				scheme = p.getScheme();
				port = p.getPort();
			}

			uriString = String.format("%s://%s:%d/%s%s%s", scheme, host, port,
					agencyId, path);

			try {
				uri = new URI(uriString);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return uri;
	}

	public String getArgsPrefix() {
		String path = getPath();

		if (path == null || path.length() == 0 || path.equals("/"))
			return null;

		if (path.startsWith("/"))
			path = path.substring(1);

		if (path.endsWith("/"))
			path = path.substring(0, path.length() - 1);

		return path.replace("/", ".");
	}

	public String toString() {
		return String.format("//%s/%s%s", host.getHostAddress(), agencyId,
				getFullPath());
	}

	public abstract boolean isRemote();

	public abstract Object call(String name, Object... args);
}
