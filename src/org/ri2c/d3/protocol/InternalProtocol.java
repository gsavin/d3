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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.ri2c.d3.Agency;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.Protocol;
import org.ri2c.d3.RemoteIdentifiableObject;
import org.ri2c.d3.Request;
import org.ri2c.d3.request.RequestListener;

public class InternalProtocol
	implements Protocol
{
	protected static final ProtocolDescription internalProtocolDescription = 
		new ProtocolDescription( "internal", "Internal Protocol", "This protocol is used between components of the agency.",
				false, -1 );
	
	private static final InternalProtocol THIS = new InternalProtocol();
	
	public static final Protocol getInternalProtocol()
	{
		return THIS;
	}
	
	private static class InternalRequest
		implements Request
	{
		String 					name;
		HashMap<String,String>	attribute;
		LinkedList<Request>		children;
		
		public InternalRequest( String name )
		{
			this.name 	= name;
		}
		
		public void addAttribute(String key, String val)
		{
			if( attribute == null )
				attribute = new HashMap<String,String>();
			
			attribute.put(key,val);
		}

		public void addSubRequest(Request r)
		{
			if( children == null )
				children = new LinkedList<Request>();
			
			children.add(r);
		}

		public Iterable<String> attributeKeySet()
		{
			if( attribute == null )
				attribute = new HashMap<String,String>();
			
			return attribute.keySet();
		}

		public String getAttribute(String key)
		{
			if( attribute == null )
				return null;
			
			return attribute.get(key);
		}

		public String getName()
		{
			return name;
		}

		public String getSource()
		{
			return Agency.getLocalAgency().getId();
		}

		public Request getSubRequest(int index)
		{
			if( children == null )
				return null;
			
			return children.get(index);
		}

		public int getSubRequestCount()
		{
			if( children == null )
				return 0;
			
			return children.size();
		}
		
	}
	
	private ConcurrentLinkedQueue<RequestListener> listeners;
	
	private InternalProtocol()
	{
		listeners = new ConcurrentLinkedQueue<RequestListener>();
	}
	
	public void addRequestListener(RequestListener listener) {
		listeners.add(listener);
	}

	@SuppressWarnings("unchecked")
	public ProtocolDescription getDescription()
	{
		return internalProtocolDescription;
	}

	public void init()
	{
		listeners.clear();
	}

	public Request newRequest( IdentifiableObject source,
			IdentifiableObject target, String name )
	{
		if( target instanceof RemoteIdentifiableObject ||
				source instanceof RemoteIdentifiableObject )
			throw new UnsupportedOperationException("internal protocol must be used between internal components");
		
		return new InternalRequest(name);
	}

	public void removeRequestListener(RequestListener listener)
	{
		listeners.remove(listener);
	}

	public void sendRequest( IdentifiableObject target, Request r )
	{
		if( target instanceof RemoteIdentifiableObject )
		{
			Protocols.sendRequest(target,r);
		}
		else
		{
			if( r instanceof InternalRequest )
				Agency.getLocalAgency().requestReceived(r);
			else System.err.printf("[internal-protocol] something is wrong%n");
		}
	}

	public String getId()
	{
		return "l2d.protocol.internal";
	}

	public IdentifiableType getType()
	{
		return IdentifiableType.protocol;
	}

	public void handleRequest(IdentifiableObject source,
			IdentifiableObject target, Request r) {
		// TODO Auto-generated method stub

	}

}
