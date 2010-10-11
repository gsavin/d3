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

import org.ri2c.d3.Description;

public class ProtocolDescription
	extends Description
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7609708092328505572L;
	
	private String id;
	private String name;
	private String desc;
	
	private boolean connected;
	
	private int dedicatedPort = -1;
	
	public ProtocolDescription( String id, String name, String desc,
			boolean connected, int port )
	{
		this.id				= id;
		this.name 			= name;
		this.desc 			= desc;
		this.connected 		= connected;
		this.dedicatedPort 	= port;
	}
	
	public String getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getDescription()
	{
		return desc;
	}
	
	public boolean isConnected()
	{
		return connected;
	}
	
	public int getDedicatedPort()
	{
		return dedicatedPort;
	}
}
