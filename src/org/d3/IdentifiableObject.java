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

import org.d3.annotation.IdentifiableObjectPath;

@IdentifiableObjectPath("/")
public abstract class IdentifiableObject {
	public static enum IdentifiableType {
		feature, entity, agency, atlas, protocol, application, future, migration
	}

	public static final String VALID_ID_PATTERN = "^[\\w\\d]([\\w\\d_-[.]]*[\\w\\d])?$";

	public static class Tools {

		public static String getArgsPrefix(IdentifiableObject idObject) {
			String path = idObject.getPath();// getPath(idObject.getClass());

			if (path == null || path.length() == 0 || path.equals("/"))
				return null;

			if (path.startsWith("/"))
				path = path.substring(1);

			return path.replace("/", ".");
		}

		/*
		 * public static String getPath(IdentifiableObject idObject) { return
		 * getPath(idObject.getClass()); }
		 * 
		 * public static String getPath(Class<? extends IdentifiableObject>
		 * idCls) { IdentifiableObjectPath path = null; Class<?> cls = idCls;
		 * 
		 * while (cls != Object.class && path == null) { path =
		 * cls.getAnnotation(IdentifiableObjectPath.class);
		 * 
		 * if (path == null) { for (Class<?> i : cls.getInterfaces()) { path =
		 * i.getAnnotation(IdentifiableObjectPath.class); if (path != null)
		 * break; } }
		 * 
		 * cls = cls.getSuperclass(); }
		 * 
		 * if (path != null) return path.value();
		 * 
		 * return "/"; }
		 * 
		 * public static String getFullPath( Class<? extends IdentifiableObject>
		 * cls, String id) { String path = getPath(cls);
		 * 
		 * if (!path.startsWith("/")) { path = "/" + path; }
		 * 
		 * if (!path.endsWith("/")) { path = path + "/"; }
		 * 
		 * return path + id; }
		 * 
		 * public static String getFullPath(IdentifiableObject idObject) {
		 * return getFullPath(idObject.getClass(), idObject.getId()); }
		 * 
		 * public static URI getURI(IdentifiableObject idObject) { return
		 * getURI(idObject, null); }
		 * 
		 * public static URI getURI(IdentifiableObject idObject, String query) {
		 * String path = getFullPath(idObject); String host;
		 * 
		 * if (idObject instanceof RemoteIdentifiableObject) { host =
		 * ((RemoteIdentifiableObject) idObject) .getRemoteAgencyId(); } else {
		 * host = Agency.getLocalAgency().getId(); }
		 * 
		 * String uriString = String.format("%s://%s%s", idObject.getType()
		 * .toString(), host, path);
		 * 
		 * if (query != null) { uriString = String.format("%s?%s", uriString,
		 * query); }
		 * 
		 * try { return new URI(uriString); } catch (Exception e) {
		 * e.printStackTrace(); }
		 * 
		 * return null; }
		 */
		public static String getTypePath(Class<? extends IdentifiableObject> cls) {
			return getTypePath(cls, null);
		}

		public static String getTypePath(
				Class<? extends IdentifiableObject> idCls, String id) {
			IdentifiableObjectPath idPath = null;
			Class<?> cls = idCls;

			while (IdentifiableObject.class.isAssignableFrom(cls)
					&& idPath == null) {
				idPath = cls.getAnnotation(IdentifiableObjectPath.class);

				if (idPath == null) {
					for (Class<?> i : cls.getInterfaces()) {
						idPath = i.getAnnotation(IdentifiableObjectPath.class);
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
		/*
		 * public static Object call(IdentifiableObject source,
		 * IdentifiableObject target, String name, Object[] args) { return
		 * call(source, target, name, args, false); }
		 * 
		 * public static Object call(IdentifiableObject source,
		 * IdentifiableObject target, String name, Object[] args, boolean async)
		 * { if (target instanceof RemoteIdentifiableObject) { Future f = new
		 * Future(); Request r = new Request(source, target, name, args, f);
		 * 
		 * Protocols.sendRequest(r);
		 * 
		 * if (async) { return f; } else { f.waitForValue();
		 * 
		 * if (f.getValue() == Future.SpecialReturn.NULL) return null;
		 * 
		 * return f.getValue(); } } else { Class<?> cls = target.getClass();
		 * Method callable = null;
		 * 
		 * while (callable == null && cls != Object.class) { Method[] methods =
		 * cls.getMethods();
		 * 
		 * if (methods != null) { for (Method m : methods) { if
		 * (m.getAnnotation(RequestCallable.class) != null &&
		 * m.getAnnotation(RequestCallable.class) .value().equals(name)) {
		 * callable = m; break; } } }
		 * 
		 * cls = cls.getSuperclass(); }
		 * 
		 * if (callable == null) return new
		 * NullPointerException("callable is null");
		 * 
		 * try { return callable.invoke(target, args); } catch (Exception e) {
		 * return e; } } }
		 * 
		 * public static void handleRequest(Request r) { IdentifiableObject
		 * source = Agency.getLocalAgency()
		 * .getIdentifiableObject(r.getSourceURI()); IdentifiableObject target =
		 * Agency.getLocalAgency() .getIdentifiableObject(r.getTargetURI());
		 * 
		 * Object ret = call(source, target, r.getCallable(),
		 * r.getCallableArguments());
		 * 
		 * if (r.hasFuture()) { URI future = r.getFutureURI();
		 * 
		 * Object[] args = new Object[] { ret == null ?
		 * Future.SpecialReturn.NULL : ret };
		 * 
		 * Request back = new Request(target, Agency.getLocalAgency()
		 * .getIdentifiableObject(future), "init", args);
		 * 
		 * Protocols.sendRequest(back); } }
		 */
	}

	protected final String id;
	protected final InetAddress host;
	private final String path;
	private transient URI uri;

	protected IdentifiableObject(InetAddress host, String id) {
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

	public abstract Object call(IdentifiableObject source, String name,
			Object... args);
}
