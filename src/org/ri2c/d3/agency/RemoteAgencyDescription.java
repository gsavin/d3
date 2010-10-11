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
package org.ri2c.d3.agency;

import java.util.concurrent.ConcurrentHashMap;

import org.ri2c.d3.RemoteIdentifiableObject;
import org.ri2c.d3.entity.RemoteEntityDescription;

public class RemoteAgencyDescription
	extends RemoteIdentifiableObject
{
	String		agencyId;
	String		address;
	String[]	protocols;
	long		lastPresenceDate;
	
	ConcurrentHashMap<String,RemoteEntityDescription>	knownRemoteEntities;
	
	public RemoteAgencyDescription( String agencyId, String address, String protocols )
	{
		super(agencyId,agencyId,IdentifiableType.agency);
		
		this.agencyId    = agencyId;
		this.address 	 = address;
		this.protocols   = protocols.trim().split("\\s*,\\s*");
		lastPresenceDate = System.currentTimeMillis();
		knownRemoteEntities = new ConcurrentHashMap<String,RemoteEntityDescription>();
	}
	
	public void udpatePresence( long date )
	{
		this.lastPresenceDate = date;
	}
	
	public String getId()
	{
		return agencyId;
	}
	
	public String getAddress()
	{
		return address;
	}
	
	public String getFirstProtocol()
	{
		if( protocols == null || protocols.length == 0 )
			return null;
		
		return protocols [0];
	}

	public void addRemoteEntityDescription( RemoteEntityDescription red )
	{
		knownRemoteEntities.put(red.getEntityId(),red);
	}
	
	public void removeRemoteEntityDescription( String entityId )
	{
		knownRemoteEntities.remove(entityId);
	}
	
	public RemoteEntityDescription getRemoteEntityDescription( String entityId )
	{
		return knownRemoteEntities.get(entityId);
	}
	
	public Iterable<RemoteEntityDescription> eachRemoteEntityDescription()
	{
		return knownRemoteEntities.values();
	}
}
