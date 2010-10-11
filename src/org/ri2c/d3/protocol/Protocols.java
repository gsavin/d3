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

import java.lang.reflect.Method;
import java.util.HashMap;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Future;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.Protocol;
import org.ri2c.d3.RemoteIdentifiableObject;
import org.ri2c.d3.Request;
import org.ri2c.d3.agency.RemoteAgencyDescription;

@SuppressWarnings("unchecked")
public class Protocols
{
	private static final HashMap<String,Protocol> knownProtocols =
		new HashMap<String,Protocol>();
	private static final HashMap<String,Protocol> protocols =
		new HashMap<String,Protocol>();
	
	static
	{
		String [] map = {
				"org.ri2c.l2d.protocol.XMLProtocol"	
		};
		
		for( String entry: map )
			enableProtocol(entry);
	}
	
	public static void enableProtocol( String classname )
	{
		try
		{
			Class<? extends Protocol> cls = (Class<? extends Protocol>) Class.forName(classname);
			Method m = cls.getMethod("getDefault");
			Protocol p = (Protocol) m.invoke(null); 
			
			if( p != null )
			{
				System.out.printf("[protocols] enable %s --> %s%n", p.getDescription().getId(), classname );
				knownProtocols.put(p.getDescription().getId(),p);
			}
			else System.err.printf("[protocols] error getting protocol %s%n", classname );
		} 
		catch(Exception e)
		{
			System.err.printf("[protocols] error while loading \"%s\"%n",classname);
		}
	}
	
	public static void initProtocol( String id )
	{
		if( knownProtocols.containsKey(id) )
		{
			Protocol p = knownProtocols.get(id);
			
			if( Agency.getLocalAgency().registerIdentifiableObject(p) )
			{
				p.init();
				knownProtocols.remove(id);
				protocols.put(id,p);
				
				System.out.printf("[protocols] %s ready%n", id );
			}
		}
	}
	
	private static final Protocol getProtocol( String id )
	{
		if( knownProtocols.containsKey(id) )
			initProtocol(id);
		
		return protocols.get(id);
	}
	
	private static final Protocol getProtocolTo( RemoteAgencyDescription rad )
	{
		return getProtocol(rad.getFirstProtocol());
	}
	
	public static final Request createRequestTo( IdentifiableObject source,
			IdentifiableObject target, String name )
	{
		if( target == null || source == null )
		{
			System.err.printf("[protocols] error: null target or source%n");
			return null;
		}
		
		Request r;
		
		if( target instanceof RemoteIdentifiableObject )
		{
			RemoteIdentifiableObject rid = (RemoteIdentifiableObject) target;
			
			RemoteAgencyDescription rad =
				Agency.getLocalAgency().getRemoteAgencyDescription(rid.getRemoteAgencyId());
		
			r = getProtocolTo(rad).newRequest(source, target, name);
			
			r.addAttribute( "source-agency-id", Agency.getLocalAgency().getId() );
		}
		else
		{
			r = InternalProtocol.getInternalProtocol().newRequest(source, target, name);
		}
		
		r.addAttribute("source-id", 	source.getId() );
		r.addAttribute("source-type", 	source.getType().name() );
		
		r.addAttribute("target-id", 	target.getId() );
		r.addAttribute("target-type", 	target.getType().name() );
		
		return r;
	}
	
	public static final Future sendRequestWithFuture( IdentifiableObject target, Request r )
	{
		final Future f = Agency.getLocalAgency().getAtlas().addFutureRequest(r);
		
		sendRequest(target,r);
		
		return f;
	}
	
	public static final void sendRequest( IdentifiableObject target, Request r )
	{
		if( target instanceof RemoteIdentifiableObject )
		{
			RemoteIdentifiableObject rid = (RemoteIdentifiableObject) target;
			
			RemoteAgencyDescription rad =
				Agency.getLocalAgency().getRemoteAgencyDescription(rid.getRemoteAgencyId());
		
			getProtocolTo(rad).sendRequest(target,r);
		}
		else
		{
			InternalProtocol.getInternalProtocol().sendRequest(target,r);
		}
	}
	
	public static void reply( IdentifiableObject replyTo, IdentifiableObject replyFrom,
			Request r, Object futureValue )
	{
		Agency.getLocalAgency().getAtlas().reply(replyTo, replyFrom, r, futureValue);
	}
}
