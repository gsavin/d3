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
package org.ri2c.d3.protocol;

import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.Protocol;
import org.ri2c.d3.Request;
import org.ri2c.d3.request.RequestListener;

public class UDPProtocol
	implements Protocol
{

	public void addRequestListener(RequestListener listener) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	public ProtocolDescription getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public void init() {
		// TODO Auto-generated method stub

	}
	
	public Request newRequest( IdentifiableObject source, IdentifiableObject target, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeRequestListener(RequestListener listener) {
		// TODO Auto-generated method stub

	}

	public void sendRequest( IdentifiableObject target, Request r) {
		// TODO Auto-generated method stub

	}

	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	public IdentifiableType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public void handleRequest( IdentifiableObject source,
			IdentifiableObject target, Request r) {
		// TODO Auto-generated method stub
		
	}

}
