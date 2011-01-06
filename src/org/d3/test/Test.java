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
package org.d3.test;

import java.net.URI;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.d3.Agency;
import org.d3.Application;
import org.d3.Console;
import org.d3.Future;
import org.d3.agency.RemoteAgency;
import org.d3.atlas.future.FutureAction;
import org.d3.entity.Entity;
import org.d3.tools.StartD3;

import static org.d3.IdentifiableObject.Tools.call;

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

		Agency.getLocalAgency().launch(test);

		Thread[] threads = new Thread[Thread.activeCount()];
		Thread.enumerate(threads);

		System.out.printf("Active threads:%n");
		for (Thread t : threads)
			if (t != null)
				System.out.printf(" - %s%n", t.getName());

		StartD3.d3Loop();
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
		Console.warning("remote agency updated: %s", rad.getId());
		Object r = call(this, rad, "getIdentifiableObjectList",
				new Object[] { IdentifiableType.entity });

		if (r != null) {
			try {
				URI[] entities = (URI[]) r;
				System.out.printf("entities on %s%n", rad.getId());
				for (URI entity : entities) {
					System.out.printf("- %s%n", entity);

					for (TestEntity testEntity : this.entities)
						if (random.nextFloat() < 0.2)
							call(this, testEntity, "beMyFriend",
									new Object[] { entity }, true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void execute() {
		while (true) {
			// Console.warning("test running");
			for (Entity e : entities)
				call(this, e, "step", null, true);

			try {
				Thread.sleep(400);
			} catch (Exception e) {

			}
		}
	}

	public void init() {

		for (int i = 0; i < 50; i++)
			entities.add(Agency.getLocalAgency().getAtlas()
					.createEntity(TestEntity.class));

		for (RemoteAgency remote : Agency.getLocalAgency().eachRemoteAgency())
			remoteAgencyDescriptionUpdated(remote);
	}
}
