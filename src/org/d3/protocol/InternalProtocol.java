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
package org.d3.protocol;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.d3.actor.Agency;
import org.d3.actor.Protocol;
import org.d3.annotation.ActorPath;
import org.d3.request.RequestListener;

@ActorPath("/d3/protocols")
public class InternalProtocol extends Protocol {
	private static final InternalProtocol THIS = new InternalProtocol();

	public static final Protocol getInternalProtocol() {
		return THIS;
	}

	private ConcurrentLinkedQueue<RequestListener> listeners;

	private InternalProtocol() {
		super("internal");
		listeners = new ConcurrentLinkedQueue<RequestListener>();
	}

	public void addRequestListener(RequestListener listener) {
		listeners.add(listener);
	}

	public void init() {
		listeners.clear();
	}

	public void removeRequestListener(RequestListener listener) {
		listeners.remove(listener);
	}

	public void sendRequest(Request r) {
		if (r.isLocalTarget()) {
			Agency.getLocalAgency().requestReceived(r);
		} else {
			Protocols.sendRequest(r);
		}
	}
}
