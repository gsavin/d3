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
package org.ri2c.d3.test;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.ri2c.d3.Agency;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.RemoteIdentifiableObject;
import org.ri2c.d3.IdentifiableObject.IdentifiableType;
import org.ri2c.d3.entity.EntityADN;
import org.ri2c.d3.entity.EntityDescription;

public class TestEntity
	implements EntityADN
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4661324109912888393L;

	static Random random = new Random();
	
	ConcurrentLinkedQueue<IdentifiableObject> idObjects;
	String entityId;
	
	public TestEntity( String entityId )
	{
		this.entityId = entityId;
		idObjects = new ConcurrentLinkedQueue<IdentifiableObject>();
	}
	
	public String getMyId()
	{
		//System.out.printf("get my id ! (%s)\n",entityId);
		return entityId;
	}
	
	public void addFriend( String friendId, String agencyId )
	{
		IdentifiableObject idObject;
		
		if( agencyId.equals(Agency.getLocalAgency().getId()) )
		{
			idObject = Agency.getLocalAgency().getIdentifiableObject(IdentifiableType.entity,friendId);
		}
		else
		{
			idObject = new RemoteIdentifiableObject(agencyId,friendId,IdentifiableType.entity);
		}
		
		if( idObject != null )
			idObjects.add(idObject);
	}
	
	public void step()
	{
		int i = random.nextInt(idObjects.size());
		
		for( IdentifiableObject idObject: idObjects )
		{
			if( i-- == 0 )
			{
				Agency.getLocalAgency().getAtlas().remoteEntityCall(
						Agency.getLocalAgency().getIdentifiableObject(IdentifiableType.entity,entityId),
						idObject,"getMyId",false);
			}
		}
	}

	public EntityDescription getEntityDescription() {
		return TestEntityDescription.def;
	}
}
