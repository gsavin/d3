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
import java.net.InetAddress;
import java.net.URI;

import org.d3.annotation.ActorPath;

@ActorPath("/")
public abstract class Actor {
	public static enum IdentifiableType {
		feature, entity, agency, atlas, protocol, application, future, migration, remote
	}

	public static final String VALID_ID_PATTERN = "^[\\w\\d]([\\w\\d_-[.]]*[\\w\\d])?$";

	public static class Tools {

		public static String getArgsPrefix(Actor idObject) {
			String path = idObject.getPath();// getPath(idObject.getClass());

			if (path == null || path.length() == 0 || path.equals("/"))
				return null;

			if (path.startsWith("/"))
				path = path.substring(1);

			return path.replace("/", ".");
		}
		
		public static String getTypePath(Class<? extends Actor> cls) {
			return getTypePath(cls, null);
		}

		public static String getTypePath(
				Class<? extends Actor> idCls, String id) {
			ActorPath idPath = null;
			Class<?> cls = idCls;

			while (Actor.class.isAssignableFrom(cls)
					&& idPath == null) {
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
	}
	
	protected final String id;
	protected final InetAddress host;
	private final String path;
	private transient URI uri;

	protected Actor(InetAddress host, String id) {
		if (!id.matches(VALID_ID_PATTERN))
			throw new InvalidIdException(
					String.format("invalid id: \"%s\"", id));

		this.host = host;
		this.id = id;
		this.path = Tools.getTypePath(getClass());
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
	
	public final void register() {
		Agency.getLocalAgency().registerIdentifiableObject(this);
	}

	public final void unregister() {
		Agency.getLocalAgency().unregisterIdentifiableObject(this);
	}

	public String getPath() {
		return path;
	}

	public String getFullPath() {
		String path = getPath();

		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		if (!path.endsWith("/")) {
			path = path + "/";
		}

		return path + id;
	}

	public URI getURI(){
		if (uri == null) {
			String path = getFullPath();
			String uriString = String.format("d3://%s%s", host.getHostName(), path);

			try {
				uri = new URI(uriString);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return uri;
	}

	public URI getQueryURI(String query) {
		getURI();

		return URI.create(String.format("d3://%s%s?%s", uri.getHost(),
				uri.getPath(), query));
	}

	/*
	 * public Object synchroneCall(IdentifiableObject source, String name,
	 * Object... args) { return Tools.call(source, this, name, args, false); }
	 * 
	 * public Object asynchroneCall(IdentifiableObject source, String name,
	 * Object... args) { return Tools.call(source, this, name, args, true); }
	 */
	public abstract boolean isRemote();
	
	public abstract void handle(Request r);

	public abstract Object call(Actor source, String name,
			Object... args);
}
