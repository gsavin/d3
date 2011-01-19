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

public class ActorThread extends Thread implements Thread.UncaughtExceptionHandler {

	public static final Actor getCurrentActor() {
		Thread t = Thread.currentThread();

		if (t instanceof ProtocolThread) {
			try {
				Actor a = ((ProtocolThread) t).getCurrentRemoteActor();
				return a;
			} catch(NotRemoteActorCallException e) {
				// Not a remote call
			}
		}
		
		if (t instanceof ActorThread) {
			ActorThread rt = (ActorThread) t;
			return rt.getOwner();
		}

		return null;
	}
	
	protected final LocalActor owner;

	public ActorThread(LocalActor owner, String threadId) {
		super(owner.getThreadGroup(), owner.getFullPath() + "/threads/"+threadId);
		this.owner = owner;
		
		setDaemon(true);
		setUncaughtExceptionHandler(this);
	}
	
	public final LocalActor getOwner() {
		return this.owner;
	}

	public final boolean isOwner() {
		return Thread.currentThread() == this;
	}
	
	public final void checkIsOwner() {
		if (Thread.currentThread() != this)
			throw new SecurityException();
	}

	public void uncaughtException(Thread t, Throwable e) {
		// TODO Handle unexpected end of actor thread
		Console.error("unexpected end of actor thread:\n\t%s", e.getMessage());
		terminate();
	}
	
	protected void terminate() {
		checkIsOwner();
	}
}
