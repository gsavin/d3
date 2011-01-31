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

import org.d3.actor.Future;
import org.d3.RegistrationException;

public class Futures {

	private final ConcurrentHashMap<String, Future> futures;

	public Futures() {
		futures = new ConcurrentHashMap<String, Future>();
	}

	public void initFuture(String id, Object value) {
		Future future = futures.get(id);

		if (future != null) {
			future.init(value);
			futures.remove(id);
		}
	}

	public void register(Future future) throws RegistrationException {
		if (futures.putIfAbsent(future.getId(), future) != null)
			throw new RegistrationException();
	}
	
	public Future get(String id) {
		return futures.get(id);
	}
}
