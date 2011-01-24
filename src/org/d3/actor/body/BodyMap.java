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
package org.d3.actor.body;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.d3.Console;
import org.d3.actor.LocalActor;
import org.d3.annotation.Callable;

public class BodyMap {
	private static final ConcurrentHashMap<Class<? extends LocalActor>, BodyMap> maps = new ConcurrentHashMap<Class<? extends LocalActor>, BodyMap>();

	public static synchronized BodyMap getBodyMap(
			Class<? extends LocalActor> clazz) {
		if (!maps.containsKey(clazz))
			maps.put(clazz, new BodyMap(clazz));

		return maps.get(clazz);
	}

	protected HashMap<String, Method> callables;

	public BodyMap(Class<? extends LocalActor> clazz) {
		callables = new HashMap<String, Method>();

		Class<?> cls = clazz;

		while (LocalActor.class.isAssignableFrom(cls) && cls != Object.class) {
			Method[] methods = cls.getMethods();

			if (methods != null) {
				for (Method m : methods) {
					if (m.getAnnotation(Callable.class) != null) {
						String name = m.getAnnotation(Callable.class).value();

						if (callables.containsKey(name)) {
							Console.warning("duplicate callable \"%s\"", name);
						} else {
							callables.put(name, m);
						}
					}
				}
			}

			cls = cls.getSuperclass();
		}
	}

	public boolean has(String name) {
		return callables.containsKey(name);
	}

	public Object invoke(LocalActor obj, String name, Object... args)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		return callables.get(name).invoke(obj, args);
	}
}
