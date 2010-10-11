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
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Application;
import org.ri2c.d3.Description;
import org.ri2c.d3.Future;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.Request;
import org.ri2c.d3.agency.AgencyListener;
import org.ri2c.d3.agency.RemoteAgencyDescription;
import org.ri2c.d3.atlas.future.FutureAction;
import org.ri2c.d3.entity.Entity;
import org.ri2c.d3.entity.RemoteEntityDescription;
import org.ri2c.d3.protocol.Protocols;
import org.ri2c.d3.tools.StartL2D;

public class Test
	extends Application
{
	public static class TestFutureAction
		extends FutureAction
	{
		public TestFutureAction( LinkedList<Future> futures )
		{
			super(futures);
		}
		
		public void action( Future future )
		{
			Object obj = future.getValue();
			System.out.printf("[test] future result: %s%n",obj);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Test test = new Test();
		TestEntityDescription testEntityDescription = new TestEntityDescription();
		
		StartL2D.init(args);
		
		Agency.getLocalAgency().addAgencyListener(test);
		Agency.getLocalAgency().registerIdentifiableObject(test);
		
		for( int i = 0; i < 100; i++ )
			test.entities.add(Agency.getLocalAgency().getAtlas().createEntity(testEntityDescription));
		
		Thread [] threads = new Thread [Thread.activeCount()];
		Thread.enumerate(threads);
		
		System.out.printf("Active threads:%n");
		for( Thread t: threads )
		{
			System.out.printf(" - %s%n", t.getName() );
		}
		
		Request r = Protocols.createRequestTo(
				test,
				Agency.getLocalAgency().getIdentifiableObject(IdentifiableType.atlas,"default"),
				"entity:getlist");
		Protocols.sendRequest(Agency.getLocalAgency().getIdentifiableObject(IdentifiableType.atlas,"default"), r);
		
		//StartL2D.l2dLoop();
		
		while( true )
		{
			for( Entity e: test.entities )
			{
				Agency.getLocalAgency().getAtlas().remoteEntityCall(test,e,"step",false);
			}
			
			test.step();
			
			try
			{
				Thread.sleep(400);
			}
			catch( Exception e )
			{
				
			}
		}
	}

	ConcurrentLinkedQueue<Entity> entities =
		new ConcurrentLinkedQueue<Entity>();
	
	Random random = new Random();
	
	public Test()
	{
		super( Test.class.getName() );
	}
	
	public void agencyExit(Agency agency) {
		// TODO Auto-generated method stub
		
	}

	public void newAgencyRegistered(RemoteAgencyDescription rad) {
		Agency.getLocalAgency().lazyCheckEntitiesOn(rad);
	}

	public void remoteAgencyDescriptionUpdated(RemoteAgencyDescription rad) {
		System.out.printf("[test] remote description for %s changed:%n Entities:%n", rad.getId());
		
		//LinkedList<Future> futures = new LinkedList<Future>();
		
		for( RemoteEntityDescription red: rad.eachRemoteEntityDescription() )
		{
			System.out.printf("  - %s%n", red.getEntityId() );
			/*
			Future f = Agency.getLocalAgency().getAtlas().remoteEntityCall(this, red, "getMyId", true );
			futures.add(f);
			*/
			for( Entity e: entities )
				((TestEntity) e.getEntityADN()).addFriend(red.getId(),red.getRemoteAgencyId());
		}
		
		//new TestFutureAction(futures);
	}

	public void step()
	{
		if( random.nextFloat() < 0.2 )
		{
			for( RemoteAgencyDescription rad : Agency.getLocalAgency().eachRemoteAgency() )
				Agency.getLocalAgency().lazyCheckEntitiesOn(rad);
		}
	}

	public void execute() {
		// TODO Auto-generated method stub
		
	}

	public void init() {
		// TODO Auto-generated method stub
		
	}
}
