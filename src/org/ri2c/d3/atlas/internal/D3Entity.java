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
	protected String 						entityId;
	protected Atlas							atlas;
	
	@SuppressWarnings("unused")
	private D3Entity()
	{
		throw new Error("forbidden creation" );
	}
	
	D3Entity( String entityId )
	{
		this.entityId 			= entityId;
	}

	protected void turnMethodCallable( String methodName )
	{
		turnMethodCallable(methodName,methodName);
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
}
