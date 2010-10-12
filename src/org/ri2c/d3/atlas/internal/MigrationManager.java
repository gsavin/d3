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

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Atlas;
import org.ri2c.d3.Description;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.RemoteIdentifiableObject;
import org.ri2c.d3.Request;
import org.ri2c.d3.agency.RemoteAgencyDescription;
import org.ri2c.d3.atlas.AtlasConstants;
import org.ri2c.d3.entity.Entity;
import org.ri2c.d3.entity.EntityADN;
import org.ri2c.d3.entity.EntityCall;
import org.ri2c.d3.entity.EntityMigrationStatus;
import org.ri2c.d3.entity.EntityMigrationStatus.State;
import org.ri2c.d3.protocol.Protocols;
import org.ri2c.d3.request.ObjectCoder;

public class MigrationManager
	implements AtlasConstants, IdentifiableObject
{
	protected static final Description migrationManagerDescription = new Description();
	
	public static interface MigrationManagerBridge
	{
		boolean prepareBodyForEntityReception( String entityId );
		boolean receiveEntityContent( Entity entity, Collection<Request> calls );
		void migrationRejectedByRemoteAgency( String entityId );
		void entityIsBeingMigrated( String entityId );
		void migrationDone( String entityId );
		Iterable<Request> getEntityCalls( String entityId );
	}
	
	private ConcurrentHashMap<String,EntityMigrationStatus>	pending;
	private Atlas											atlas;
	private String											id;
	private MigrationManagerBridge							bridge;
	
	MigrationManager( Atlas atlas, MigrationManagerBridge bridge )
	{
		this.atlas	= atlas;
		this.id		= String.format("%s.migrationManager",atlas.getId());
		this.bridge = bridge;
		this.pending= new ConcurrentHashMap<String,EntityMigrationStatus>();
	}
	
	public EntityMigrationStatus migrateEntity(String entityId,
			RemoteAgencyDescription rad)
	{
		
		Request request = Protocols.createRequestTo(this,rad,ATLAS_REQUEST_ENTITY_MIGRATION);
		
		request.addAttribute( "phase",		MigrationPhase.request.name() );
		request.addAttribute( "entityId",	entityId );
		
		EntityMigrationStatus status = new EntityMigrationStatus();
		status.setState(EntityMigrationStatus.State.pending);
		
		Protocols.sendRequest(rad,request);
		
		pending.put(entityId, status);
		
		return status;
	}

	@SuppressWarnings("unchecked")
	public Description getDescription()
	{
		return migrationManagerDescription;
	}

	public String getId()
	{
		return id;
	}

	public IdentifiableType getType()
	{
		return IdentifiableType.atlas;
	}

	public void handleRequest(IdentifiableObject source,
			IdentifiableObject target, Request r)
	{
		if( r.getName().equals(ATLAS_REQUEST_ENTITY_MIGRATION) )
		{
			if( r.getAttribute("phase") == null )
				return;
			
			MigrationPhase 	phase 	 = MigrationPhase.valueOf(r.getAttribute("phase"));
			String			entityId = r.getAttribute("entityId");
			Request			response;
			
			switch( phase )
			{
			case request:
				response = Protocols.createRequestTo(this,source,ATLAS_REQUEST_ENTITY_MIGRATION);
				
				response.addAttribute( "phase", 	MigrationPhase.response.name() );
				response.addAttribute( "entityId", 	entityId );
				
				if( bridge.prepareBodyForEntityReception(entityId) )
					response.addAttribute( "status", 	MigrationResponse.accepted.name() );
				else
					response.addAttribute( "status", 	MigrationResponse.rejected.name() );
				
				Protocols.sendRequest(source,response);
				
				break;
			case response:
				EntityMigrationStatus 	status 				= pending.get(entityId);
				MigrationResponse 		migrationResponse 	= MigrationResponse.valueOf(r.getAttribute("status"));
				
				if( status == null )
				{
					System.err.printf("[migration] receive response for an unknown migration%n");
				}
				else
				{
					switch(migrationResponse)
					{
					case accepted:
						switch( status.getState() )
						{
						case pending:
							bridge.entityIsBeingMigrated(entityId);

							response = Protocols.createRequestTo(this,source,ATLAS_REQUEST_ENTITY_MIGRATION);
							
							response.addAttribute( "entityId", 	entityId );
							
							Entity e = atlas.getEntity(entityId);
							
							if( e == null )
							{
								response.addAttribute( "phase", 	MigrationPhase.canceled.name() );
							}
							else
							{
								response.addAttribute( "phase", 	MigrationPhase.receive.name() );
								response.addAttribute( "adn", 		ObjectCoder.encode(e.getEntityADN()));
								
								for( EntityCall call: bridge.getEntityCalls(entityId) )
								{
									Request sub = Protocols.createRequestTo(this,source,"call");
									
									sub.addAttribute("callId", 				call.getCallId() );
									sub.addAttribute("args", 				ObjectCoder.encode(call.getArgs()));
									if( call.getFutureId() != null )
										sub.addAttribute("futureId",		call.getFutureId() );
									sub.addAttribute("source-id", 			call.getSourceObject().getId() );
									sub.addAttribute("source-type",			call.getSourceObject().getType().name() );
									if( call.getSourceObject() instanceof RemoteIdentifiableObject )
										sub.addAttribute("source-agency",	((RemoteIdentifiableObject)call.getSourceObject()).getRemoteAgencyId());
									else
										sub.addAttribute("source-agency",	Agency.getLocalAgency().getId() );
									
									response.addSubRequest(sub);
								}
							}
							
							status.setState(State.transfered);
							Protocols.sendRequest(source,response);
							
							break;
						case transfered:
							status.setState(State.done);
							bridge.migrationDone(entityId);
							break;
						case rejected:
						case done:
							System.err.printf("[migration] receive response for a past migration%n");
							break;
						}
						break;
					case error:
					case rejected:
						status.setState(State.rejected);
						bridge.migrationRejectedByRemoteAgency(entityId);
						break;
					}
				}
				
				break;
			case receive:
				EntityADN 				adn = null;
				Collection<EntityCall> 	calls = new LinkedList<EntityCall>();
				
				adn = (EntityADN) ObjectCoder.decode(r.getAttribute("adn"));
				
				for( int i = 0; i < r.getSubRequestCount(); i++ )
				{
					Request 			sub 	= r.getSubRequest(i);
					String 				callId 	= sub.getAttribute("callId");
					Object [] 			args;
					IdentifiableObject	callSource;
					
					if( callId == null )
					{
						System.err.printf("[migration] error, callId is null%n");
						continue;
					}
					
					try
					{
						args = (Object[]) ObjectCoder.decode(sub.getAttribute("args"));
					}
					catch( Exception e )
					{
						System.err.printf("[migration] error decoding args: %s%n", e.getMessage());
						args = null;
					}
					
					String				callSourceId 	= sub.getAttribute("source-id");
					IdentifiableType	callSourceType	= IdentifiableType.valueOf(sub.getAttribute("source-type"));
					String				callSourceAgency= sub.getAttribute("source-agency");
					
					if( Agency.getLocalAgency().getId().equals(callSourceAgency) )
						callSource = Agency.getLocalAgency().getIdentifiableObject(callSourceType,callSourceId);
					else
						callSource = new RemoteIdentifiableObject(callSourceAgency,callSourceId,callSourceType);
					
					if( callSource == null )
						System.err.printf("[migration] error, callSource is null%n");
					else
						calls.add( new EntityCall(callSource,callId,args) );
				}
				
				response = Protocols.createRequestTo(this,source,ATLAS_REQUEST_ENTITY_MIGRATION);
				
				response.addAttribute( "phase", 	MigrationPhase.response.name() );
				response.addAttribute( "entityId", 	entityId );
				
				if( adn == null )
					response.addAttribute( "status", 	MigrationResponse.error.name() );
				else if( bridge.receiveEntityContent(entityId, adn, calls))
					response.addAttribute( "status", 	MigrationResponse.accepted.name() );
				else
					response.addAttribute( "status", 	MigrationResponse.rejected.name() );
				
				Protocols.sendRequest(source,response);
				
				break;
			}
		}
	}
}
