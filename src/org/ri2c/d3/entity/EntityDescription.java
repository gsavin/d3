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

import java.util.LinkedList;

import org.ri2c.d3.Description;

public class EntityDescription
	extends Description
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5126945000562654319L;
	
	private String 				classname;
	private LinkedList<String>	callableMethods;
	
	protected String			displayName;
	protected String			description;
	
	public EntityDescription( String displayName, String adnClassname,
			String description, String ... callableMethods )
	{
		this.classname 		 = adnClassname;
		this.displayName	 = displayName;
		this.description	 = description;
		this.callableMethods = new LinkedList<String>();
		
		if( callableMethods != null )
		{
			for( String cm: callableMethods )
				this.callableMethods.add(cm);
		}
	}
	
	public String getADNClassname()
	{
		return classname;
	}
	
	public Iterable<String> getCallableMethod()
	{
		return callableMethods;
	}
}
