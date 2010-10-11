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

import java.util.LinkedList;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Application;
import org.ri2c.d3.Console;
import org.ri2c.d3.Future;
import org.ri2c.d3.RemoteIdentifiableObject;
import org.ri2c.d3.agency.RemoteAgencyDescription;
import org.ri2c.d3.entity.EntityADN;
import org.ri2c.d3.entity.EntityDescription;
import org.ri2c.d3.entity.EntityMigrationStatus;

public class TestMigration
	extends Application
{
	static MigrationEntityDescription entityDescription = new MigrationEntityDescription();
	
	static class MigrationEntityDescription
		extends EntityDescription
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 5030697857917597208L;

		public MigrationEntityDescription()
		{
			super( "Migration Entity", MigrationEntity.class.getName(), "", "whereIam" );
		}
	}
	
	public static class MigrationEntity
		implements EntityADN
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 4519822789886385904L;

		public MigrationEntity( String id )
		{
			
		}
		
		public EntityDescription getEntityDescription()
		{
			return entityDescription;
		}
		
		public String whereIam()
		{
			return Agency.getLocalAgency().getId();
		}
	}
	
	LinkedList<String> entitiesId;
	boolean				migrate = false;
	EntityMigrationStatus status;
	String				where;
	
	public TestMigration()
	{
		super( "l2d.test.migration" );
		
		entitiesId = new LinkedList<String>();
	}
	
	public void init()
	{
		entitiesId.add(
				Agency.getLocalAgency().getAtlas().createEntity(entityDescription).getId() );
		entitiesId.add(
				Agency.getLocalAgency().getAtlas().createEntity(entityDescription).getId() );
		
		checkMigration();
	}
	
	public void execute()
	{
		while( ! migrate )
		{
			try { Thread.sleep(200); } catch( Exception e ) {}
		}
		
		status.waitMigrationEndsOrFailed();
		
		RemoteIdentifiableObject rid = new RemoteIdentifiableObject(where,entitiesId.get(0),IdentifiableType.entity);
		
		Console.info("calling");
		Future f = Agency.getLocalAgency().getAtlas().remoteEntityCall(this,rid,"whereIam",true);
		Console.info("where %s is ? %s",entitiesId.get(0),f.getValue());
	}
	
	public void newAgencyRegistered(RemoteAgencyDescription rad)
	{
		checkMigration();
	}
	
	protected synchronized void checkMigration()
	{
		if( migrate )
			return;
		
		for( RemoteAgencyDescription rad: Agency.getLocalAgency().eachRemoteAgency() )
		{
			Console.info("migrate to %s",rad.getId());
			status = Agency.getLocalAgency().getAtlas().migrateEntity(entitiesId.get(0), rad);
			where = rad.getRemoteAgencyId();
			migrate = true;
			break;
		}
	}
}
