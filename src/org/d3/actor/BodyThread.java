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
package org.d3.actor;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.d3.actor.body.BodyQueue;

public class BodyThread extends ActorThread {

	public static enum SpecialAction {
		MIGRATE, STEP
	}

	protected static class SpecialActionTask extends ScheduledTask {

		SpecialAction action;

		SpecialActionTask(long delay, TimeUnit unit, SpecialAction action) {
			super(delay, unit);
			this.action = action;
		}
	}

	public static enum StopPolicy {
		/**
		 * Execute all call which delay has expired, then send a redirection
		 * exception.
		 */
		FINISH_EXECUTE_REQUEST_AND_STOP,
		/**
		 * Send a redirection exception to all remaining calls, expired and
		 * unexpired.
		 */
		SEND_REDIRECTION_AND_STOP
	}

	protected final BodyQueue queue;
	protected volatile boolean running;
	protected StopPolicy stopPolicy;

	protected Throwable stopCause;

	protected BodyThread(LocalActor owner, BodyQueue queue) {
		super(owner, "request");
		this.queue = queue;
		this.running = false;
	}

	public BodyThread(LocalActor owner) {
		this(owner, new BodyQueue());
	}

	public void run() {
		checkIsOwner();

		running = true;
		owner.register();
		
		onRun();
		
		runBody();
		terminate();
	}

	protected void onRun() {
		
	}
	
	protected final void runBody() {
		checkIsOwner();

		Object current;

		if (owner instanceof StepActor) {
			StepActor sa = (StepActor) owner;
			SpecialActionTask sat = new SpecialActionTask(
					sa.getStepDelay(TimeUnit.NANOSECONDS),
					TimeUnit.NANOSECONDS, SpecialAction.STEP);

			queue.add(sat);
		}

		Semaphore actorThreadSemaphore = Agency.getLocalAgency()
				.getActorThreadSemaphore();

		while (running) {
			try {
				current = queue.take();
			} catch (InterruptedException e) {
				continue;
			}

			if (current == null)
				continue;

			try {
				actorThreadSemaphore.acquireUninterruptibly();

				if (current instanceof Call) {
					Call c = (Call) current;
					executeCall(c);
				} else if (current instanceof SpecialActionTask) {
					SpecialActionTask sat = (SpecialActionTask) current;

					switch (sat.action) {
					case MIGRATE:
						specialActionMigrate(sat);
						break;
					case STEP:
						specialActionStep(sat);
						break;
					}
				}
			} finally {
				actorThreadSemaphore.release();
			}
		}

		switch (stopPolicy) {
		case FINISH_EXECUTE_REQUEST_AND_STOP: {
			ScheduledTask sat;

			while ((sat = queue.poll()) != null) {
				if (sat instanceof Call) {
					executeCall((Call) sat);
				}
			}
		}
		case SEND_REDIRECTION_AND_STOP: {
			for (ScheduledTask sat : queue) {
				if (sat instanceof Call) {
					Call c = (Call) sat;
					c.getFuture().init(stopCause);
				}
			}

			break;
		}
		}
	}

	private void executeCall(Call c) {
		try {
			Object r = owner.call(c.getName(), c.getArgs());
			c.getFuture().init(r);
		} catch (Exception e) {
			c.getFuture().init(new CallException(e));
		}
	}

	protected void specialActionMigrate(SpecialActionTask sat) {
		throw new ActorInternalException();
	}

	protected void specialActionStep(SpecialActionTask sat) {
		if (owner instanceof StepActor) {
			StepActor sa = (StepActor) owner;
			sa.step();
			sat.delay = sa.getStepDelay(sat.unit);
			sat.reset();

			queue.add(sat);
		}
	}

	public final Future enqueue(String name, Object[] args) {
		Call c = new Call(owner, name, args);
		enqueue(c);

		return c.getFuture();
	}

	public final void enqueue(Call c) {
		if (running) {
			queue.add(c);
		} else {
			c.getFuture().init(stopCause);
		}
	}

	void migrate() {
		checkIsOwner();
		
		SpecialActionTask sat = new SpecialActionTask(0, TimeUnit.SECONDS,
				SpecialAction.MIGRATE);
		queue.add(sat);
	}

	protected void terminate() {
		super.terminate();
		owner.unregister();
	}

	protected void terminateBody(StopPolicy stop) {
		checkIsOwner();
		stopPolicy = stop;
		running = false;
		interrupt();
	}
}
