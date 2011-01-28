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

import org.d3.Actor;
import org.d3.actor.body.BodyMap;
import org.d3.agency.AgencyThread;
import org.d3.entity.EntityThread;
import org.d3.feature.FeatureThread;

/**
 * Local actors are these actors hosted on the local agency. They have a
 * dedicated thread, called body. This body is used to handle requests received
 * by the actor.
 * 
 * Actor threads, like the body thread, are special threads associated to a
 * local actor. So, when such thread is currently running, the actor owning the
 * thread can be easily determined.
 * 
 * Local actors needs to be registered on the agency. This is done by the body
 * when it is started. When the body thread stop, the associated actor is
 * unregistered.
 * 
 * @author Guilhelm Savin
 * 
 */
public abstract class LocalActor extends Actor {

	protected static final ThreadGroup actorsThreads = new ThreadGroup("/");

	private final ThreadGroup threadGroup;
	private final BodyThread bodyThread;
	private final BodyMap bodyMap;

	public LocalActor(String id) {
		super(Agency.getLocalHost(), Agency.getLocalAgencyId(), null, id);

		if (this instanceof Agency)
			bodyThread = new AgencyThread((Agency) this);
		else if (this instanceof Feature)
			bodyThread = new FeatureThread((Feature) this);
		else if ((this instanceof Entity))
			bodyThread = new EntityThread((Entity) this);
		else
			bodyThread = new BodyThread(this);

		threadGroup = new ThreadGroup(actorsThreads, getFullPath());
		bodyMap = BodyMap.getBodyMap(getClass());
	}

	public void init() {
		if (!bodyThread.isAlive())
			bodyThread.start();
	}

	public void terminate() {

	}

	void migrate() {
		if (!(this instanceof Entity))
			throw new SecurityException();
		
		bodyThread.migrate();
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
		if (Thread.currentThread() instanceof ActorThread) {
			if (((ActorThread) Thread.currentThread()).getOwner() != this)
				throw new SecurityException();
		} else
			throw new SecurityException();
	}

	/**
	 * Enqueue a call in the body queue.
	 * 
	 * @param c
	 */
	public void call(Call c) {
		bodyThread.enqueue(c);
	}

	/**
	 * This method can have two issue depending of the thread invoking it. If
	 * the thread is the body thread of the actor, then the callable is invoked
	 * and the result directly returned. Else, the call is enqueued as a request
	 * in the body and a future is returned. This future will be initialized
	 * once the associated request will be executed.
	 * 
	 * @param name
	 *            name of the callable.
	 * @param args
	 *            arguments of the invocation.
	 * @return result of the invocation if in body thread, a future else.
	 */
	public Object call(String name, Object... args) {
		if (bodyThread.isOwner()) {
			bodyThread.checkIsOwner();

			if (!bodyMap.has(name))
				return new CallableNotFoundException(name);

			try {
				return bodyMap.invoke(this, name, args);
			} catch (Exception e) {
				return new CallException(e);
			}
		} else {
			Future f = bodyThread.enqueue(name, args);
			return f;
		}
	}
}
