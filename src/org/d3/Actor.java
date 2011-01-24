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

import java.net.URI;

import org.d3.annotation.ActorPath;

/**
 * Actors are the base of d3. They are active objects, identified by a unique
 * path on an agency. Agency itself is an actor. Actors provide special methods
 * called "callable" which can be invoked from any accessible agency.
 * 
 * Two kind of actor can be distinguished:
 * <ul>
 * <li>{@link org.d3.actor.LocalActor},</li>
 * <li>{@link org.d3.actor.RemoteActor}.</li>
 * </ul>
 * Local actors are hosted on the local agency. They have a body thread which
 * executed requests (objects modeling the invocation of a callable). Remote
 * actors are only a local representation of a local actor located on another
 * agency. They transmit their requests through the network.
 * 
 * There are several kinds of local actor:
 * <ul>
 * <li>{@link org.d3.actor.Agency},</li>
 * <li>{@link org.d3.actor.Feature},</li>
 * <li>{@link org.d3.actor.Protocol},</li>
 * <li>{@link org.d3.actor.Entity},</li>
 * <li>Application.</li>
 * </ul>
 * 
 * Agency is the main actor. There is only one agency instance by runtime. It is
 * the actor which organize and control the d3 instance on the runtime. Features
 * are extension of the abilities of the agency. Protocols are actors which are
 * allowed to communicate through the network. There is a special king of
 * protocol, called {@link org.d3.protocol.Transmitter}, which are used to
 * transmit requests from one agency to another. Entities aim to be small actors
 * which are able to migrate from one agency to another.
 * 
 * Identifier of actors is composed of four components:
 * <ol>
 * <li>host address,</li>
 * <li>agency id,</li>
 * <li>actor path,</li>
 * <li>actor id.</li>
 * </ol>
 * This identifier is modeled as a Uniform Resource Identifier (URI):
 * <code>//host/agency_id/path/id</code>. Informations about an available
 * transmitter can be added to this URI:
 * <ul>
 * <li>scheme, which identify the type of the protocol,</li>
 * <li>port, which identify the port on which the protocol is listening.</li>
 * </ul>
 * So, an exportable actor's uri can be :
 * <code>protocol://host:port/path/id</code>, this last item contains all
 * informations needed to contact the actor.
 * 
 * @author Guilhelm Savin
 * @see org.d3.actor.RemoteActor, org.d3.actor.LocalActor
 */
@ActorPath("/")
public abstract class Actor {
	public static enum IdentifiableType {
		FEATURE, ENTITY, AGENCY, PROTOCOL, APPLICATION, REMOTE
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

	/**
	 * Identifier of the actor. It should be unique for the path.
	 */
	protected final String id;
	/**
	 * Address of the remote machine hosting this actor.
	 */
	protected final HostAddress host;
	/**
	 * Id of the agency hosting this actor.
	 */
	protected final String agencyId;
	/**
	 * Path of the actor. This allows to organize actors and to provide more
	 * independence for actor's id. Path can be defined for a class of actors,
	 * using the annotation {@link org.d3.annotation.ActorPath}.
	 */
	private final String path;
	/**
	 * URI modeling the actor identifier.
	 */
	private transient URI uri;

	/**
	 * Sole constructor.
	 * 
	 * @param host
	 *            defines the host of the actor.
	 * @param agencyId
	 *            defines the agency of the actor.
	 * @param path
	 *            defines the path of the actor.
	 * @param id
	 *            defines the id of the actor.
	 */
	protected Actor(HostAddress host, String agencyId, String path, String id) {
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

	/**
	 * Get the address of the machine hosting the actor.
	 * 
	 * @return address of the host
	 */
	public final HostAddress getHost() {
		return host;
	}

	/**
	 * Get the id which identify this object.
	 * 
	 * @return actor id
	 */
	public final String getId() {
		return id;
	}

	/**
	 * Get the type of this actor.
	 * 
	 * @return type of the actor
	 * @see org.d3.Actor.IdentifiableType
	 */
	public abstract IdentifiableType getType();

	/**
	 * Init the actor.
	 */
	public abstract void init();

	/**
	 * Get the path of this actor.
	 * 
	 * @return path of this actor.
	 */
	public final String getPath() {
		return path;
	}

	/**
	 * Full path of an actor is its path concatenated to its id.
	 * 
	 * @return path + id
	 */
	public final String getFullPath() {
		return path + id;
	}

	/**
	 * The id of the agency hosting this actor.
	 * 
	 * @return agency id
	 */
	public final String getAgencyId() {
		return agencyId;
	}

	/**
	 * Agency full path is the id of the agency hosting the actor concatenated
	 * to its full path.
	 * 
	 * @return agency_id + path + id
	 */
	public final String getAgencyFullPath() {
		return agencyId + getFullPath();
	}

	/**
	 * Get the uri of this actor, creating it if does not exists.
	 * 
	 * @return uri of the actor.
	 */
	public final URI getURI() {
		if (uri == null) {
			String uriString;
			String host = this.host.getHost();
			String path = getFullPath();

			uriString = String.format("//%s/%s%s", host, agencyId, path);

			try {
				uri = new URI(uriString);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return uri;
	}

	/**
	 * Return the prefix used to define args for this actor. Prefix is the path
	 * of the actor replacing path separator '/' by a dot '.'.
	 * 
	 * @return prefix used in args for this actor.
	 * @see org.d3.Args
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return String.format("//%s/%s%s", host.getHost(), agencyId,
				getFullPath());
	}

	/**
	 * Convenient method to indicate if the actor is remote or not.
	 * 
	 * @return true if this is a remote actor.
	 */
	public abstract boolean isRemote();

	/**
	 * Main method of actor, defining the way of invoking a callable.
	 * 
	 * @param name
	 *            name of the callable to invoke.
	 * @param args
	 *            arguments of the invocation.
	 * @return depending of local or remote actor.
	 * @see org.d3.actor.LocalActor, org.d3.actor.RemoteActor
	 */
	public abstract Object call(String name, Object... args);
}
