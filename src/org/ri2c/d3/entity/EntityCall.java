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
package org.ri2c.d3.entity;

import java.io.Serializable;

import org.ri2c.d3.IdentifiableObject;

public class EntityCall
	implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4487473056639857209L;
	
	String 				callId;
	Object [] 			args;
	String				futureId;
	IdentifiableObject	source;
	
	public EntityCall( IdentifiableObject rio,
			String callId, Object ... args )
	{
		this.callId 		= callId;
		this.args			= args;
		this.futureId 		= null;
		this.source			= rio;
	}
	
	public void setFutureId( String futureId )
	{
		this.futureId = futureId;
	}

	public String getFutureId()
	{
		return futureId;
	}
	
	public String getCallId()
	{
		return callId;
	}
	
	public IdentifiableObject getSourceObject()
	{
		return source;
	}
	
	public int getArgsCount()
	{
		return args == null ? 0 : args.length;
	}
	
	public Object getArg( int index )
	{
		return args == null ? null : args [index];
	}
	
	public Object [] getArgs()
	{
		return args;
	}
	
	public Object call( Entity e )
	{
		EntityCallable ec = e.getCallable(callId);
		Object r;
		
		r = ec != null ? ec.call(args) : new NoSuchMethodException( callId );
	
		return r;
	}
}
