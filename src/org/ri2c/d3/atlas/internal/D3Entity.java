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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.ri2c.d3.Atlas;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.Request;
import org.ri2c.d3.entity.Entity;
import org.ri2c.d3.entity.EntityADN;
import org.ri2c.d3.entity.EntityCallable;
import org.ri2c.d3.entity.EntityDescription;

public class D3Entity
	implements Entity
{
	private class MethodCallable
		implements EntityCallable
	{
		Method method;
		
		public MethodCallable( String name )
			throws NoSuchMethodException
		{
			Method [] methods = adn.getClass().getMethods();
			
			for( Method m : methods )
			{
				if( m.getName().equals(name) && 
						Modifier.isPublic(m.getModifiers()) )
				{
					method = m;
					break;
				}
			}
			
			if( method == null )
				throw new NoSuchMethodException( name );
		}
		
		public Object call(Object... args)
		{
			Class<?> [] argsTypes = method.getParameterTypes();
			
			if( args == null && argsTypes != null && argsTypes.length > 0 )
				return new Exception( "need some arguments" );
			
			if( args != null && argsTypes != null && args.length != argsTypes.length )
				return new Exception( "invalid arguments count" );
			
			if( args != null )
			{
				for( int i = 0; i < args.length; i++ )
				{
					if( ! argsTypes [i].isAssignableFrom(args [i].getClass()) )
						return new Exception( "invalid argument" );
				}
			}
			
			try
			{
				return method.invoke(adn,args);
			}
			catch( Exception e )
			{
				return e;
			}
		}
	}
	
	protected String 						entityId;
	protected Map<String,EntityCallable> 	calls;
	protected EntityDescription				entityDescription;
	protected Atlas							atlas;
	protected EntityADN						adn;
	
	@SuppressWarnings("unused")
	private D3Entity()
	{
		throw new Error("forbidden creation" );
	}
	
	D3Entity( String entityId, EntityADN adn )
	{
		this.entityId 			= entityId;
		this.calls	  			= new HashMap<String,EntityCallable>();
		this.entityDescription	= adn.getEntityDescription();
		this.adn				= adn;
		
		for( String m: entityDescription.getCallableMethod() )
			turnMethodCallable(m);
	}
	
	@SuppressWarnings("unchecked")
	D3Entity( String entityId, EntityDescription desc )
		throws InstantiationException
	{
		this.entityId 			= entityId;
		this.calls	  			= new HashMap<String,EntityCallable>();
		this.entityDescription	= desc;
		
		try
		{
			Class<? extends EntityADN> cls =
				(Class<? extends EntityADN>) Class.forName(desc.getADNClassname());

			Constructor<? extends EntityADN> cons =
				cls.getConstructor(String.class);
			
			this.adn = cons.newInstance(entityId);
		}
		catch( Exception e )
		{
			throw new InstantiationException(e.getMessage());
		}
		
		for( String m: desc.getCallableMethod() )
			turnMethodCallable(m);
	}

	protected void turnMethodCallable( String methodName )
	{
		turnMethodCallable(methodName,methodName);
	}
	
	protected void turnMethodCallable( String callId, String methodName )
	{
		try
		{
			EntityCallable ec = new MethodCallable(methodName);
			calls.put(callId,ec);
		}
		catch( Exception e )
		{
			System.err.printf("[entity] no such method: %s%n", methodName);
		}
	}
	
	public void setAtlas( Atlas atlas )
	{
		if( atlas != null )
			this.atlas = atlas;
	}
	
	public String getId()
	{
		return entityId;
	}
	
	public IdentifiableType getType()
	{
		return IdentifiableType.entity;
	}
	
	public EntityADN getEntityADN()
	{
		return adn;
	}
	
	@SuppressWarnings("unchecked")
	public EntityDescription getDescription()
	{
		return entityDescription;
	}

	public EntityCallable getCallable(String callId)
	{
		return calls.get(callId);
	}

	public void handleRequest( IdentifiableObject source,
			IdentifiableObject target, Request r )
	{
		if( atlas != null )
			atlas.handleRequest(source, target, r);
	}
}
