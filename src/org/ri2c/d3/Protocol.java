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
package org.ri2c.d3;

import org.ri2c.d3.request.RequestListener;

public abstract class Protocol extends IdentifiableObject {

	protected Protocol(String id) {
		super(id);
	}

	public final IdentifiableType getType() {
		return IdentifiableType.protocol;
	}

	/**
	 * Initialize this protocol.
	 */
	public abstract void init();

	/**
	 * Send a request to an identifiable object.
	 * 
	 * @param target
	 * @param r
	 */
	public abstract void sendRequest(Request r);

	/**
	 * Add a listener to received requests.
	 * 
	 * @param listener
	 */
	public abstract void addRequestListener(RequestListener listener);

	/**
	 * Remove a listener.
	 * 
	 * @param listener
	 */
	public abstract void removeRequestListener(RequestListener listener);
}
