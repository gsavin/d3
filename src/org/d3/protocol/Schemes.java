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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.d3.actor.Protocol;

public class Schemes {

	private static class Scheme extends ConcurrentLinkedQueue<Protocol> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -630028359855052622L;
	}

	private final ConcurrentHashMap<String, Scheme> schemes;

	public Schemes() {
		this.schemes = new ConcurrentHashMap<String, Scheme>();
	}

	public synchronized void register(Protocol protocol)
			throws ProtocolException {
		Scheme scheme;

		if (schemes.containsKey(protocol.getScheme()))
			scheme = schemes.get(protocol.getScheme());
		else {
			scheme = new Scheme();
			schemes.put(protocol.getScheme(), scheme);
		}

		if (scheme.contains(protocol))
			throw new ProtocolException();

		scheme.add(protocol);
	}
}
