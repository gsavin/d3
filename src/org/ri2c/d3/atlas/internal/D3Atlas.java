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

import java.io.Serializable;
//import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.LinkedList;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Atlas;
import org.ri2c.d3.Console;
import org.ri2c.d3.Future;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.Request;
import org.ri2c.d3.agency.RemoteAgencyDescription;
import org.ri2c.d3.atlas.AtlasConstants;
import org.ri2c.d3.atlas.AtlasDescription;
import org.ri2c.d3.atlas.AtlasListener;
import org.ri2c.d3.entity.Entity;
import org.ri2c.d3.entity.EntityADN;
import org.ri2c.d3.entity.EntityCall;
import org.ri2c.d3.entity.EntityDescription;
import org.ri2c.d3.entity.EntityMigrationStatus;
import org.ri2c.d3.entity.RemoteEntityDescription;
import org.ri2c.d3.protocol.Protocols;
import org.ri2c.d3.request.ObjectCoder;
//import org.ri2c.l2d.RemoteIdentifiableObject;

public class D3Atlas
	implements Atlas, AtlasConstants
{
	public static final AtlasDescription l2dAtlasDescription =
		new AtlasDescription("l2d:atlas", "L2D Atlas", "Internal L2D Atlas implementation.");
	
	protected static long L2D_ATLAS_ID_GENERATOR = 0;
	
	private class __MigrationManagerBridge
		implements MigrationManager.MigrationManagerBridge
	{
		public boolean prepareBodyForEntityReception( String entityId )
		{
			if( entities.hasEntity(entityId) )
				return false;
			
			Body body = new Body(futureManager);
			entities.host(entityId, body);
			
			return true;
		}
		
		public boolean receiveEntityContent( String entityId, EntityADN adn,
				Collection<EntityCall> calls )
		{
			Body body = entities.getBody(entityId);
			
			if( body != null )
			{
				D3Entity entity = new D3Entity(entityId,adn);
				body.receiveContent(entity,calls);
				
				if( ! agency.registerIdentifiableObject(entity) )
					return false;

				if( entity instanceof D3Entity )
					((D3Entity) entity).setAtlas(D3Atlas.this);
				
				entities.update(entityId, entity);
				startBody(entityId);
				
				return body.waitForState(Body.STATE_RUNNING);
			}
			else Console.warning("receive entity content but I don't know this body.");
			
			return false;
		}
		
		public void migrationRejectedByRemoteAgency( String entityId )
		{
			Body body = entities.getBody(entityId);
			
			if( body != null )
			{
				body.migrationCanceled();
			}
			else Console.warning("migrationRejected but I don't know this body.");
		}
		
		public void entityIsBeingMigrated( String entityId )
		{
			Body body = entities.getBody(entityId);
			
			if( body != null )
			{
				body.enterMigration();
			}
			else Console.warning("entityIsBeingMigrated but I don't know this body.");
		}
		
		public void migrationDone( String entityId )
		{
			Body body = entities.getBody(entityId);
			
			if( body != null )
			{
				body.migrationDone();
				body.waitForState(Body.STATE_FINISH);
			
				agency.unregisterIdentifiableObject(entities.getEntity(entityId));
				entities.unhost(entityId);
			}
			else Console.warning("migrationDone but I don't know this body.");
		}
		
		public Iterable<EntityCall> getEntityCalls( String entityId )
		{
			if( entities.hasEntity(entityId) )
				return entities.getBody(entityId).getCalls();
			
			return null;
		}
	}
	
	private EntitiesPool 		entities;
	private ThreadGroup			threadGroup;
	private long				entityIdGenerator;
	private long				futureIdGenerator;
	private FutureManager		futureManager;
	private Agency				agency;
	private String				atlasId;
	private MigrationManager	migrationManager;
	
	public D3Atlas()
	{
		entities 			= new EntitiesPool();
		threadGroup 		= new ThreadGroup("l2d-atlas-bodies");
		entityIdGenerator 	= 0;
		futureIdGenerator 	= 0;
		atlasId				= String.format("l2d.atlas.%x", L2D_ATLAS_ID_GENERATOR);
		futureManager		= new FutureManager( atlasId + ".futureManager" );
		migrationManager	= new MigrationManager(this,new __MigrationManagerBridge());
	}
	
	public String getId()
	{
		return atlasId;
	}
	
	public IdentifiableType getType()
	{
		return IdentifiableType.atlas;
	}
	
	@SuppressWarnings("unchecked")
	public AtlasDescription getDescription() 
	{
		return l2dAtlasDescription;
	}
	
	public void init( Agency agency )
	{
		this.agency = agency;
		
		agency.registerIdentifiableObject(futureManager);
		agency.registerIdentifiableObject(migrationManager);
		
		agency.interceptRequest( futureManager,		ATLAS_REQUEST_FUTURE );
		agency.interceptRequest( this,				ATLAS_REQUEST_ENTITY_CALL );
		agency.interceptRequest( migrationManager, 	ATLAS_REQUEST_ENTITY_MIGRATION );
	}
	
	public void addAtlasListener(AtlasListener listener)
	{
		// TODO Auto-generated method stub
	}

	public void removeAtlasListener(AtlasListener listener)
	{
		// TODO Auto-generated method stub
	}

	public Entity createEntity(EntityDescription desc)
	{
		String entityId = newEntityId();
		
		try
		{
			Entity 	entity 	= new D3Entity(entityId,desc);
			Body	body	= new Body(entity,futureManager);
			
			if( ! agency.registerIdentifiableObject(entity) )
			{
				return null;
			}

			if( entity instanceof D3Entity )
				((D3Entity) entity).setAtlas(this);
			
			entities.host(entity,body,desc);
			startBody(entityId);
			
			return entity;
		}
		catch( Exception e )
		{
			System.err.printf("[l2d-atlas] error while creating entity: %s %n", e.getMessage() );
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Entity getEntity( String entityId )
	{
		return entities.getEntity(entityId);
	}

	public void entityCall( IdentifiableObject source, String entityId, EntityCall call )
	{
		if( entityId != null && entities.hasEntity(entityId) )
			entities.getBody(entityId).newCall(call);
		else System.err.printf("[warning] call unknown entity: %s%n", entityId );
	}
	
	public Future addFutureRequest( Request r )
	{
		String futureId = newFutureId();
		Future future = new D3Future();
		
		futureManager.registerNewFuture(futureId,future);
		
		r.addAttribute("futureId", futureId);
		
		return future;
	}
	
	public void reply( IdentifiableObject replyTo, IdentifiableObject replyFrom,
			Request r, Object futureValue )
	{
		if( r.getAttribute( "futureId" ) != null )
		{
			futureManager.handleFuture(replyTo, replyFrom, r.getAttribute("futureId"), futureValue);
		}
	}
	
	public Future remoteEntityCall( IdentifiableObject source, IdentifiableObject target,
			String callId, boolean createFuture, Object ... args )
	{
		if( target.getType() != IdentifiableType.entity )
			return null;
		
		String futureId = null;
		Future future 	= null;
		/*
		RemoteIdentifiableObject rid =
			new RemoteIdentifiableObject(remoteId,targetEntityId,IdentifiableType.entity);
		*/
		Request r = Protocols.createRequestTo(source,target, ATLAS_REQUEST_ENTITY_CALL);
		
		//r.addAttribute("targetEntityId",	targetEntityId);
		r.addAttribute("callId",			callId);
		
		if( createFuture )
		{
			futureId = newFutureId();
			future = new D3Future();
			futureManager.registerNewFuture(futureId,future);
			
			r.addAttribute("futureId", futureId);
		}
		
		if( args != null && args.length > 0 )
		{
			for( Object obj: args )
			{
				Request arg = Protocols.createRequestTo(source,target,"object");
				
				if( obj instanceof Serializable )
					arg.addAttribute("data",ObjectCoder.encode((Serializable) obj));
				else
				{
					arg.addAttribute("data","");
					System.err.printf("[l2d-atlas] warning, non-serializable object%n");
				}
				
				r.addSubRequest(arg);
			}
		}
		
		Protocols.sendRequest(target,r);
		
		return future;
	}

	public EntityMigrationStatus migrateEntity(String entityId,
			RemoteAgencyDescription rad)
	{
		return migrationManager.migrateEntity(entityId, rad);
	}
	
	public String [] listEntities()
	{
		return entities.list();
	}

	private void startBody( String entityId )
	{
		Body body = entities.getBody(entityId);
		
		if( body != null && ! body.isRunning() )
		{
			Thread t = new Thread( threadGroup, body, entityId + "-body" );
			t.start();
		}
		else System.err.printf("[l2d-atlas] start a null body !%n");
	}
	
	private String newEntityId()
	{
		return String.format( "%s:%016X:%016X", agency.getId(),
				System.nanoTime(), entityIdGenerator++ );
	}
	
	private String newFutureId()
	{
		return String.format( "%s:%016X:%016X", agency.getId(),
				System.nanoTime(), futureIdGenerator++ );
	}

	public void handleRequest( IdentifiableObject source,
			IdentifiableObject target, Request r)
	{
		if( target == null || source == null )
		{
			System.err.printf("[l2d-atlas] error: null target or source%n");
			System.err.printf("[l2d-atlas] request = %s%n", r.getName() );
			Thread.dumpStack();
			return;
		}
		
		if( r.getName().equals( ATLAS_REQUEST_ENTITY_CALL ) )
		{
			String 	callId		= r.getAttribute("callId");
			String 	futureId	= r.getAttribute("futureId");
			
			Object [] args = null;
			
			if( r.getSubRequestCount() > 0 )
			{
				args = new Object [r.getSubRequestCount()];
				
				for( int i = 0; i < r.getSubRequestCount(); i++ )
					args [i] = ObjectCoder.decode(r.getSubRequest(i));
			}

			EntityCall call = new EntityCall( source, callId, args );
			
			if( futureId != null )
				call.setFutureId(futureId);
			
			if( entities.hasEntity(target.getId()) )
			{
				entities.getBody(target.getId()).newCall(call);
			}
			else
			{
				//futureManager.handleFuture(source,this,futureId,new NotFoundException(target.getId()));
			}
			//Agency.getLocalAgency().getAtlas().entityCall(target.getId(),call);
		}
		else if( r.getName().equals( ATLAS_REQUEST_GET_ENTITY_LIST ) )
		{
			String [] entities = listEntities();
			
			Request listBack = Protocols.createRequestTo(target,source,ATLAS_REQUEST_ENTITY_LIST);
			
			if( entities != null && entities.length > 0 )
			{
				for( String e: entities )
				{
					Request er = Protocols.createRequestTo(target,source,"entity");
					er.addAttribute("id",e);
					listBack.addSubRequest(er);
				}
			}
			
			Protocols.sendRequest(source,listBack);
		}
		else if( r.getName().equals( ATLAS_REQUEST_ENTITY_LIST ) )
		{
			boolean changed = false;
			
			RemoteAgencyDescription rad =
				Agency.getLocalAgency().getRemoteAgencyDescription(r.getSource());
			
			LinkedList<String> unavailableEntities = null;//new LinkedList<String>();
			
			for( RemoteEntityDescription red: rad.eachRemoteEntityDescription() )
			{
				boolean present = false;
				for( int i = 0; i < r.getSubRequestCount(); i++ )
				{
					if( r.getSubRequest(i).getAttribute("id").equals(red.getEntityId()))
					{
						present = true;
						break;
					}
				}
				
				if( ! present )
				{
					if( unavailableEntities == null )
						unavailableEntities = new LinkedList<String>();
					
					unavailableEntities.add(red.getEntityId());
					changed = true;
				}
			}
			
			if( unavailableEntities != null )
			{
				for( String s: unavailableEntities )
					rad.removeRemoteEntityDescription(s);
			}
			
			for( int i = 0; i < r.getSubRequestCount(); i++ )
			{
				RemoteEntityDescription red =
					rad.getRemoteEntityDescription(r.getSubRequest(i).getAttribute("id"));
				
				if( red == null )
				{
					red = new RemoteEntityDescription(r.getSubRequest(i).getAttribute("id"),rad.getRemoteAgencyId());
					rad.addRemoteEntityDescription(red);
					changed = true;
				}
			}
			
			if( changed )
				Agency.getLocalAgency().remoteAgencyDescriptionChanged(rad.getId());
		}
		else if( r.getName().equals( ATLAS_REQUEST_FUTURE ) )
		{
			futureManager.handleRequest(source, target, r);
		}
	}
}
