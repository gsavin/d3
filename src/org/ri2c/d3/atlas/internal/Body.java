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

//import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//import org.ri2c.l2d.Request;
//import org.ri2c.l2d.agency.RemoteAgencyDescription;
import org.ri2c.d3.Console;
import org.ri2c.d3.entity.Entity;
import org.ri2c.d3.entity.EntityCall;

public class Body
	implements Runnable
{
	public static final int STATE_NONE		= -1;
	public static final int STATE_INIT		= 0;
	public static final int STATE_RUNNING 	= 1;
	public static final int STATE_MIGRATING	= 2;
	public static final int STATE_FINISH	= 3;
	
	public static final int MIGRATION_NONE		= 0;
	public static final int MIGRATION_PENDING	= 1;
	public static final int MIGRATION_DONE		= 2;
	
	protected LinkedBlockingQueue<EntityCall>	queue;
	protected AtomicBoolean						running;
	protected AtomicInteger						migration;
	protected Entity							entity;
	protected transient FutureManager			futureManager;
	protected AtomicInteger						state;
	protected Thread							thread;
	//protected transient RemoteAgencyDescription	migrateTo;
	
	Body( FutureManager futureManager )
	{
		this.futureManager	= futureManager;
		this.queue			= new LinkedBlockingQueue<EntityCall>();
		this.running		= new AtomicBoolean( false );
		this.state			= new AtomicInteger( STATE_NONE );
		this.migration		= new AtomicInteger( MIGRATION_NONE );
	}
	
	public Body( Entity entity, FutureManager futureManager )
	{
		this.entity 		= entity;
		this.futureManager	= futureManager;
		this.queue			= new LinkedBlockingQueue<EntityCall>();
		this.running		= new AtomicBoolean( false );
		this.state			= new AtomicInteger( STATE_NONE );
		this.migration		= new AtomicInteger( MIGRATION_NONE );
		
		setState( STATE_INIT );
	}
	
	public void newCall( EntityCall call )
	{
		queue.add(call);
	}
	
	public boolean isRunning()
	{
		return running.get();
	}
	
	public Iterable<EntityCall> getCalls()
	{
		return queue;
	}
	
	public void receiveContent( Entity entity, Collection<EntityCall> calls )
	{
		this.entity = entity;
		
		Collection<EntityCall> existingCalls = queue;
		
		this.queue = new LinkedBlockingQueue<EntityCall>();
		this.queue.addAll(calls);
		this.queue.addAll(existingCalls);
		
		setState(STATE_INIT);
	}
	
	private void setState( int state )
	{
		synchronized(this.state)
		{
			this.state.set(state);
			this.state.notifyAll();
		}
	}
	
	public void enterMigration()
	{
		migration.set(MIGRATION_PENDING);
		
		if( Thread.currentThread() != thread )
		{
			thread.interrupt();
			waitForState( STATE_MIGRATING );
		}
	}
	
	public void migrationDone()
	{
		synchronized(migration)
		{
			migration.set(MIGRATION_DONE);
			migration.notifyAll();
		}
	}
	
	public void migrationCanceled()
	{
		synchronized(migration)
		{
			migration.set(MIGRATION_NONE);
			migration.notifyAll();
		}
	}
	
	public boolean waitForState( int state )
	{
		synchronized(this.state)
		{
			while( this.state.get() != state && this.state.get() >= 0  )
			{
				try { this.state.wait(200); } catch( Exception e ) {}
			}
		}
		
		return this.state.get() == state;
	}
	
	public void run()
	{
		if( ! running.compareAndSet(false,true) )
			return;
		
		if( state.get() < 0 )
		{
			Console.error("can't start a none-initialized body");
			return;
		}
		
		System.out.printf("[entity] %s body start%n", entity.getId());
		
		thread = Thread.currentThread();
		setState( STATE_RUNNING );
		
		while( running.get() )
		{
			try
			{
				EntityCall 	ec 	= queue.take();
				
				Object 		obj = ec.call(entity);
				
				if( ec.getFutureId() != null )
					futureManager.handleFuture(ec.getSourceObject(),entity,ec.getFutureId(),obj);
			}
			catch (InterruptedException e)
			{
				Console.warning("interruption");
			}
			
			if( migration.get() > 0 )
			{
				setState( STATE_MIGRATING );
				while( migration.get() == MIGRATION_PENDING ) 
				{
					try { migration.wait(200); } catch( Exception e ) {}
				}
				
				switch( migration.get() )
				{
				case MIGRATION_DONE:
					running.set(false);
					break;
				case MIGRATION_NONE:
					setState(STATE_RUNNING);
					break;
				}	
			}
		}
		
		setState( STATE_FINISH );
		
		System.out.printf("[entity] %s body stop%n", entity.getId());
	}
	/*
	@SuppressWarnings("unused")
	private void migrate()
	{
		Request migrationRequest =
			Protocols.createRequestTo(entity,migrateTo,"entity");
		
		migrationRequest.addAttribute("entity-id",entity.getId());
		
		for( EntityCall ec: queue )
		{
			Request call =
				Protocols.createRequestTo(ec.getSourceObject(),migrateTo,"call");
			
			call.addAttribute("call-id",ec.getCallId());
			
			if( ec.getFutureId() != null )
				call.addAttribute("future-id", ec.getFutureId());
			
			call.addAttribute("args",Integer.toString(ec.getArgsCount()));
			
			for( int i = 0; i < ec.getArgsCount(); i++ )
				call.addAttribute(String.format("arg%d",i),ObjectCoder.encode((Serializable) ec.getArg(i)));
			
			//call.addAttribute("data",ObjectCoder.encode(ec));
			migrationRequest.addSubRequest(call);
		}
		
		Protocols.sendRequest(migrateTo, migrationRequest);
	}
	*/
}
