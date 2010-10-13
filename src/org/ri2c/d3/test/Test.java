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

import java.net.URI;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Application;
import org.ri2c.d3.Future;
import org.ri2c.d3.agency.RemoteAgency;
import org.ri2c.d3.atlas.future.FutureAction;
import org.ri2c.d3.entity.Entity;
import org.ri2c.d3.tools.StartD3;

import static org.ri2c.d3.IdentifiableObject.Tools.call;

public class Test extends Application {
	public static class TestFutureAction extends FutureAction {
		public TestFutureAction(LinkedList<Future> futures) {
			super(futures);
		}

		public void action(Future future) {
			Object obj = future.getValue();
			System.out.printf("[test] future result: %s%n", obj);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Test test = new Test();

		StartD3.init(args);

		Agency.getLocalAgency().addAgencyListener(test);
		Agency.getLocalAgency().registerIdentifiableObject(test);

		for (int i = 0; i < 10; i++)
			test.entities.add(Agency.getLocalAgency().getAtlas()
					.createEntity(TestEntity.class));

		Thread[] threads = new Thread[Thread.activeCount()];
		Thread.enumerate(threads);

		System.out.printf("Active threads:%n");
		for (Thread t : threads)
			System.out.printf(" - %s%n", t.getName());

		// StartL2D.l2dLoop();

		while (true) {
			for (Entity e : test.entities)
				call(test, e, "step", null, true);

			try {
				Thread.sleep(400);
			} catch (Exception e) {

			}
		}
	}

	ConcurrentLinkedQueue<TestEntity> entities = new ConcurrentLinkedQueue<TestEntity>();

	Random random = new Random();

	public Test() {
		super(Test.class.getName());
	}

	public void agencyExit(Agency agency) {
		// TODO Auto-generated method stub

	}

	public void newAgencyRegistered(RemoteAgency rad) {
		remoteAgencyDescriptionUpdated(rad);
	}

	public void remoteAgencyDescriptionUpdated(RemoteAgency rad) {
		Object r = call(this, rad, "getIdentifiableObjectList",
				new Object[] { IdentifiableType.entity });
		
		if( r!= null ) {
			try {
				URI[] entities = (URI[]) r;
				System.out.printf("entities on %s%n",rad.getId());
				for( URI entity : entities ) {
					System.out.printf("- %s%n",entity);
					
					for( TestEntity testEntity: this.entities ) 
						if( random.nextFloat() < 0.2 ) call(this,testEntity,"beMyFriend",new Object[]{entity},true);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void execute() {
		// TODO Auto-generated method stub

	}

	public void init() {
		// TODO Auto-generated method stub

	}
}
