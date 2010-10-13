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

import java.lang.reflect.Method;
import java.net.URI;

import org.ri2c.d3.annotation.IdentifiableObjectPath;
import org.ri2c.d3.annotation.RequestCallable;
import org.ri2c.d3.protocol.Protocols;

@IdentifiableObjectPath("/d3")
public abstract class IdentifiableObject {
	public static enum IdentifiableType {
		feature, entity, agency, atlas, protocol, application, future, migration
	}

	public static class Tools {

		public static String getArgsPrefix(IdentifiableObject idObject) {
			String path = getPath(idObject.getClass());

			if (path == null || path.length() == 0 || path.equals("/"))
				return null;

			if (path.startsWith("/"))
				path = path.substring(1);

			return path.replace("/", ".");
		}

		public static String getPath(IdentifiableObject idObject) {
			return getPath(idObject.getClass());
		}

		public static String getPath(Class<? extends IdentifiableObject> idCls) {
			IdentifiableObjectPath path = null;
			Class<?> cls = idCls;

			while (cls != Object.class && path == null) {
				path = cls.getAnnotation(IdentifiableObjectPath.class);

				if (path == null) {
					for (Class<?> i : cls.getInterfaces()) {
						path = i.getAnnotation(IdentifiableObjectPath.class);
						if (path != null)
							break;
					}
				}

				cls = cls.getSuperclass();
			}

			if (path != null)
				return path.value();

			return "/";
		}

		public static String getFullPath(
				Class<? extends IdentifiableObject> cls, String id) {
			String path = getPath(cls);

			if (!path.startsWith("/")) {
				path = "/" + path;
			}

			if (!path.endsWith("/")) {
				path = path + "/";
			}

			return path + id;
		}

		public static String getFullPath(IdentifiableObject idObject) {
			return getFullPath(idObject.getClass(), idObject.getId());
		}

		public static URI getURI(IdentifiableObject idObject) {
			return getURI(idObject, null);
		}

		public static URI getURI(IdentifiableObject idObject, String query) {
			String path = getFullPath(idObject);
			String host;

			if (idObject instanceof RemoteIdentifiableObject) {
				host = ((RemoteIdentifiableObject) idObject)
						.getRemoteAgencyId();
			} else {
				host = Agency.getLocalAgency().getId();
			}

			String uriString = String.format("%s://%s%s", idObject.getType()
					.toString(), host, path);

			if (query != null) {
				uriString = String.format("%s?%s", uriString, query);
			}

			try {
				return new URI(uriString);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		public static Object call(IdentifiableObject source,
				IdentifiableObject target, String name, Object[] args) {
			return call(source, target, name, args, false);
		}

		public static Object call(IdentifiableObject source,
				IdentifiableObject target, String name, Object[] args,
				boolean async) {
			if (target instanceof RemoteIdentifiableObject) {
				Future f = new Future();
				Request r = new Request(source, target, name, args, f);

				Protocols.sendRequest(r);

				if (async) {
					return f;
				} else {
					f.waitForValue();

					if (f.getValue() == Future.SpecialReturn.NULL)
						return null;

					return f.getValue();
				}
			} else {
				Class<?> cls = target.getClass();
				Method callable = null;

				while (callable == null && cls != Object.class) {
					Method[] methods = cls.getMethods();

					if (methods != null) {
						for (Method m : methods) {
							if (m.getAnnotation(RequestCallable.class) != null
									&& m.getAnnotation(RequestCallable.class)
											.value().equals(name)) {
								callable = m;
								break;
							}
						}
					}

					cls = cls.getSuperclass();
				}

				if (callable == null)
					return new NullPointerException("callable is null");

				try {
					return callable.invoke(target, args);
				} catch (Exception e) {
					return e;
				}
			}
		}

		public static void handleRequest(Request r) {
			IdentifiableObject source = Agency.getLocalAgency()
					.getIdentifiableObject(r.getSourceURI());
			IdentifiableObject target = Agency.getLocalAgency()
					.getIdentifiableObject(r.getTargetURI());

			Object ret = call(source, target, r.getCallable(),
					r.getCallableArguments());

			if (r.hasFuture()) {
				URI future = r.getFutureURI();

				Object[] args = new Object[] { ret == null ? Future.SpecialReturn.NULL
						: ret };

				Request back = new Request(target, Agency.getLocalAgency()
						.getIdentifiableObject(future), "init", args);

				Protocols.sendRequest(back);
			}
		}
	}

	protected final String id;
	
	protected IdentifiableObject( String id ) {
		this.id = id;
	}
	
	/**
	 * 
	 * @return
	 */
	public final String getId() {
		return id;
	}

	/**
	 * 
	 * @return
	 */
	public abstract IdentifiableType getType();
	
	public void register() {
		Agency.getLocalAgency().registerIdentifiableObject(this);
	}
	
	public void unregister() {
		Agency.getLocalAgency().unregisterIdentifiableObject(this);
	}
}
