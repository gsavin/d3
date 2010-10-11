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

import org.ri2c.d3.protocol.ProtocolDescription;
import org.ri2c.d3.request.RequestListener;

public interface Protocol
	extends IdentifiableObject
{
	/**
	 * Init this protocol.
	 */
	void init();
	
	/**
	 * Get a description of this protocol.
	 * @return description of the protocol
	 */
	@SuppressWarnings("unchecked")
	ProtocolDescription getDescription();
	
	/**
	 * Send a request to an identifiable object.
	 * 
	 * @param target
	 * @param r
	 */
	void sendRequest( IdentifiableObject target, Request r );
	
	/**
	 * Add a listener to received requests.
	 * @param listener
	 */
	void addRequestListener( RequestListener listener );
	
	/**
	 * Remove a listener.
	 * @param listener
	 */
	void removeRequestListener( RequestListener listener );
	
	/**
	 * Create a new request for this protocol.
	 * @param source
	 * @param target
	 * @param name
	 * @return
	 */
	Request newRequest( IdentifiableObject source, IdentifiableObject target, String name );
}
