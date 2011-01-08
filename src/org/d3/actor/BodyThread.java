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

import java.net.URI;
import java.util.concurrent.PriorityBlockingQueue;

import org.d3.Agency;
import org.d3.Future;
import org.d3.Actor;
import org.d3.ActorNotFoundException;
import org.d3.Request;
import org.d3.protocol.Protocols;

public class BodyThread extends ActorThread {

	public static enum SpecialAction {
		STOP
	}

	public static final Actor getCurrentIdentifiableObject() {
		Thread t = Thread.currentThread();

		if (t instanceof ActorThread) {
			ActorThread rt = (ActorThread) t;
			return rt.getOwner();
		}

		return null;
	}

	private PriorityBlockingQueue<Object> queue;

	public BodyThread(LocalActor owner) {
		super(owner,"request");
		this.queue = new PriorityBlockingQueue<Object>();
	}

	public void run() {
		boolean running = true;
		Object current;

		owner.register();
		
		while (running) {
			current = queue.poll();

			if (current == null)
				continue;

			if (current instanceof Request) {
				Request r = (Request) current;
				try {
					runRequest(r);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (current instanceof SpecialAction) {
				SpecialAction sa = (SpecialAction) current;

				switch (sa) {
				case STOP:
					running = false;
					break;
				}
			}
		}
	}

	public final void enqueue(Request r) {
		queue.add(r);
	}

	protected void runRequest(Request r)
			throws ActorNotFoundException {
		Actor source = Agency.getLocalAgency()
				.getIdentifiableObject(r.getSourceURI());
		Actor target = Agency.getLocalAgency()
				.getIdentifiableObject(r.getTargetURI());

		Object ret = owner.call(source, r.getCallable(),
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