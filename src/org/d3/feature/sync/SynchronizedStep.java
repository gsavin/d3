/*
 * This file is part of d3 <http://d3-project.org>.
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
 * Copyright 2010 - 2011 Guilhelm Savin
 */
package org.d3.feature.sync;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.d3.Actor;
import org.d3.ActorNotFoundException;
import org.d3.actor.ActorThread;
import org.d3.actor.Agency;
import org.d3.actor.Feature;
import org.d3.actor.Future;
import org.d3.actor.LocalActor;
import org.d3.actor.NotActorThreadException;
import org.d3.actor.StepActor;
import org.d3.actor.UnregisteredActorException;
import org.d3.annotation.ActorPath;
import org.d3.tools.FutureGroup;

@ActorPath("/d3/feature/sync")
public class SynchronizedStep extends Feature implements StepActor {

	public static void synchronizeMeAt(String id, String name, Object... args)
			throws SynchronizationException {
		Actor a = ActorThread.getCurrentActor();

		if (a != null) {
			SynchronizedStep sync = null;
			 
			try {
				LocalActor la = null;
				la = Agency.getLocalAgency().getActors()
						.get(getTypePath(SynchronizedStep.class, id));

				if (la instanceof SynchronizedStep) {
					sync = (SynchronizedStep) la;
				} else {
					throw new SynchronizationException(
							"not a SynchronizedStep id");
				}
			} catch (ActorNotFoundException e) {
				sync = new SynchronizedStep(id);
				sync.init();
			} catch (UnregisteredActorException e) {
				throw new SynchronizationException(e);
			}
			
			sync.sync(a, name, args);
		} else {
			throw new SynchronizationException(new NotActorThreadException());
		}
	}

	protected static class Entry {
		String name;
		Object[] args;
		Actor actor;
		
		public Entry(Actor actor, String name, Object... args) {
			this.actor = actor;
			this.name = name;
			this.args = args;
		}
	}

	protected long delay;
	protected TimeUnit delayUnit;
	protected long lastStepDuration;
	protected ConcurrentLinkedQueue<Entry> entries;

	protected SynchronizedStep(String id) {
		super(id);
		lastStepDuration = -1;
	}

	public long getStepDelay(TimeUnit unit) {
		if (lastStepDuration < 0)
			return 0;

		return Math.max(
				0,
				unit.convert(delay, delayUnit)
						- unit.convert(lastStepDuration, delayUnit));
	}

	public void sync(Actor actor, String name, Object... args) {
		Entry e = new Entry(actor, name, args);
		entries.add(e);
	}

	public void step() {
		long start = System.currentTimeMillis();

		FutureGroup latch = new FutureGroup(FutureGroup.Policy.WAIT_FOR_ALL);

		for (Entry e : entries) {
			Object r = e.actor.call(e.name, e.args);

			if (r instanceof Future)
				latch.put((Future) r);
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
		} finally {
			lastStepDuration = delayUnit.convert(System.currentTimeMillis()
					- start, TimeUnit.MILLISECONDS);
		}
	}

	public void initFeature() {
		// TODO Auto-generated method stub

	}

}
