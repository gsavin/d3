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
import org.d3.Console;
import org.d3.protocol.NotRemoteActorCallException;
import org.d3.protocol.ProtocolThread;

/**
 * Thread owned by an actor. This kind of thread are owned by a local actor so
 * it is possible to retrieve which actor is owning the current thread.
 * 
 * These threads are in daemon mode, so they can not block the shutdown of the
 * global runtime.
 * 
 * @author Guilhelm Savin
 * @see java.lang.Thread
 * @see org.d3.actor.BodyThread
 */
public class ActorThread extends Thread implements
		Thread.UncaughtExceptionHandler {

	/**
	 * As actor threads are owned by a local actor, it is possible to get the
	 * owner of the current thread.
	 * 
	 * Protocols have a special thread which can be temporarily owned by a
	 * remote actor. The method test if the protocol thread is actually
	 * endorsing the identity of a remote actor, in this case the remote actor
	 * is return.
	 * 
	 * @return the actor owning the current thread or null is the current thread
	 *         is not a thread owned by an actor.
	 */
	public static final Actor getCurrentActor() {
		Thread t = Thread.currentThread();

		if (t instanceof ProtocolThread) {
			try {
				Actor a = ((ProtocolThread) t).getCurrentRemoteActor();
				return a;
			} catch (NotRemoteActorCallException e) {
				// Not a remote call
			}
		}

		if (t instanceof ActorThread) {
			ActorThread rt = (ActorThread) t;
			return rt.getOwner();
		}

		return null;
	}

	/**
	 * The actor owning the thread. Only local actors can owned a thread
	 * directly. Remote actors can owned a thread through a protocol.
	 */
	protected final LocalActor owner;

	/**
	 * Sole constructor. The actor owning the thread and an id are needed. Id is
	 * used to create the id of the thread which will be the full path of the
	 * actor concatenate with "/threads/" and the id passed as parameter.
	 * 
	 * @param owner
	 *            the local actor which will owned the thread.
	 * @param threadId
	 *            the suffix of the thread id.
	 */
	public ActorThread(LocalActor owner, String threadId) {
		super(owner.getThreadGroup(), owner.getFullPath() + "/threads/"
				+ threadId);
		this.owner = owner;

		setDaemon(true);
		setUncaughtExceptionHandler(this);
	}

	/**
	 * Access to the owner of the thread.
	 * 
	 * @return the local actor owning the thread.
	 */
	public final LocalActor getOwner() {
		return this.owner;
	}

	/**
	 * Test if the current thread is this thread.
	 * 
	 * @return true if the current is this thread.
	 */
	public final boolean isOwner() {
		return Thread.currentThread() == this;
	}

	/**
	 * This is a security hook to check is the current thread is this thread. If
	 * not, a SecurityException is thrown.
	 */
	public final void checkIsOwner() {
		if (Thread.currentThread() != this)
			throw new SecurityException();
	}

	/**
	 * When the thread terminate cause to an uncaught exception, this method is
	 * called, passing the throwable which causes the termination as parameter.
	 * 
	 * @param t
	 *            thread which terminated abnormally.
	 * @param e
	 *            the throwable causing the termination.
	 * @see java.lang.Thread.UncaughtExceptionHandler
	 */
	public void uncaughtException(Thread t, Throwable e) {
		// TODO Handle unexpected end of actor thread
		Console.error("unexpected end of actor thread:\n\t%s: %s",
				e.getClass(), e.getMessage());
		e.printStackTrace();
		terminate();
	}

	/**
	 * Called when the thread terminates. It can be override by extended classes
	 * to perform special action.
	 */
	protected void terminate() {
		checkIsOwner();
	}
}
