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
package org.ri2c.d3.atlas.internal;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import org.ri2c.d3.Description;
import org.ri2c.d3.Future;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.Request;
import org.ri2c.d3.protocol.Protocols;
import org.ri2c.d3.request.ObjectCoder;
//import org.ri2c.l2d.RemoteIdentifiableObject;

public class FutureManager
	implements IdentifiableObject
{
	protected static final Description futureManagerDescription = new Description();
	
	ConcurrentHashMap<String,D3Future> registeredFutures;
	String id;
	
	public FutureManager( String id )
	{
		this.registeredFutures = new ConcurrentHashMap<String,D3Future>();
		this.id = id;
	}
	
	public void handleFuture( IdentifiableObject ridObject,
			IdentifiableObject idObject, String futureId, Object value )
	{
		if( ! ( value instanceof Serializable ) )
		{
			System.err.printf("non-serializable: %s%n",value);
			value = new Exception( "non-serializable object" );
		}
		
		Request futureRequest = Protocols.createRequestTo(idObject, ridObject, "future");
		futureRequest.addAttribute("id",futureId);
		
		if( value != null )
			futureRequest.addAttribute("data",ObjectCoder.encode((Serializable)value));
		
		System.out.printf("[future-manager] send future to %s/%s%n",ridObject.getId(),ridObject.getType());
		
		Protocols.sendRequest(ridObject,futureRequest);
	}
	
	public void registerNewFuture( String futureId, Future future )
	{
		if( futureId == null )
		{
			System.err.printf("[future-manager] error, futureId is null%n" );
			return;
		}
		
		if( future instanceof D3Future )
			registeredFutures.put(futureId, (D3Future) future );
		else System.err.printf("[future-manager] unknown future type%n");
	}
	
	public void handleRequest( IdentifiableObject source,
			IdentifiableObject target, Request r )
	{
		if( r.getName().equals("future") )
		{
			String futureId = r.getAttribute("id");
			Object obj		= ObjectCoder.decode(r.getAttribute("data"));
			
			if( registeredFutures.containsKey(futureId) )
			{
				D3Future future = registeredFutures.remove(futureId);
				
				if( future != null )
					future.init(obj);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Description getDescription() {
		return futureManagerDescription;
	}

	public String getId()
	{
		return id;
	}

	public IdentifiableType getType()
	{
		return IdentifiableType.atlas;
	}
}
