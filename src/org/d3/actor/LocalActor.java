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
package org.d3.actor;

import java.lang.reflect.Method;

import org.d3.Actor;
import org.d3.Agency;
import org.d3.Request;
import org.d3.annotation.RequestCallable;

public abstract class LocalActor extends Actor {

	protected static final ThreadGroup actorsThreads = new ThreadGroup("/");

	private final ThreadGroup threadGroup;
	private final BodyThread requestThread;
	
	public LocalActor(String id) {
		super(Agency.getLocalHost(), id);
		requestThread = new BodyThread(this);
		threadGroup = new ThreadGroup(actorsThreads, getFullPath());
	}
	
	public void init() {
		requestThread.start();
	}
	
	public final boolean isRemote() {
		return false;
	}
	
	public final ThreadGroup getThreadGroup() {
		return threadGroup;
	}
	
	public void handle(Request r) {
		requestThread.enqueue(r);
	}
	
	public Object call(Actor source,
			String name, Object ... args) {
		requestThread.checkIsOwner();
		
		Class<?> cls = getClass();
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
			return callable.invoke(this, args);
		} catch (Exception e) {
			return e;
		}
	}
}
