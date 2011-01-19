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
import org.d3.agency.AgencyThread;
import org.d3.annotation.Callable;
import org.d3.feature.FeatureThread;

public abstract class LocalActor extends Actor {

	protected static final ThreadGroup actorsThreads = new ThreadGroup("/");

	private final ThreadGroup threadGroup;
	private final BodyThread bodyThread;

	public LocalActor(String id) {
		super(Agency.getLocalHost(), Agency.getLocalAgencyId(), null, id);

		if (this instanceof Agency)
			bodyThread = new AgencyThread((Agency) this);
		else if( this instanceof Feature)
			bodyThread = new FeatureThread((Feature) this);
		else
			bodyThread = new BodyThread(this);
		threadGroup = new ThreadGroup(actorsThreads, getFullPath());
	}

	public void init() {
		if (!bodyThread.isAlive())
			bodyThread.start();
	}

	public final void register() {
		Agency.getLocalAgency().register(this);
	}

	public final void unregister() {
		Agency.getLocalAgency().unregister(this);
	}

	public final boolean isRemote() {
		return false;
	}

	public void join() throws InterruptedException {
		bodyThread.join();
	}
	
	public final ThreadGroup getThreadGroup() {
		return threadGroup;
	}

	public final void checkBodyThreadAccess() {
		bodyThread.checkIsOwner();
	}

	public final void checkActorThreadAccess() {
		if(Thread.currentThread() instanceof ActorThread) {
			if(((ActorThread) Thread.currentThread()).getOwner() != this)
				throw new SecurityException();
		} else throw new SecurityException();
	}
	
	public Object call(String name, Object... args) {
		if (bodyThread.isOwner()) {
			bodyThread.checkIsOwner();

			Class<?> cls = getClass();
			Method callable = null;

			while (callable == null && cls != Object.class) {
				Method[] methods = cls.getMethods();

				if (methods != null) {
					for (Method m : methods) {
						if (m.getAnnotation(Callable.class) != null
								&& m.getAnnotation(Callable.class)
										.value().equals(name)) {
							callable = m;
							break;
						}
					}
				}

				cls = cls.getSuperclass();
			}

			if (callable == null)
				return new CallableNotFoundException(name);

			try {
				return callable.invoke(this, args);
			} catch (Exception e) {
				return e;
			}
		} else {
			Future f = bodyThread.enqueue(name, args);
			return f;
		}
	}
}
