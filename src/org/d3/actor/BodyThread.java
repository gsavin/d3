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
import org.d3.tools.AtomicState;

/**
 * Body is a thread associated with each local actor which will handle received
 * requests.
 * 
 * @author Guilhelm Savin
 * @see org.d3.actor.ActorThread
 */
public class BodyThread extends ActorThread {

	/**
	 * Special actions which can be handle by the body.
	 */
	protected static enum SpecialAction {
		/**
		 * Tell the body that it should migrate. This action is only available
		 * for entity actors.
		 */
		MIGRATE,
		/**
		 * Tell the body to step. This action is only available for StepActor
		 * actors.
		 * 
		 * @see org.d3.actor.StepActor
		 */
		STEP
	}

	/**
	 * Defines the container for special action.
	 */
	protected static class SpecialActionTask extends ScheduledTask {

		SpecialAction action;

		SpecialActionTask(long delay, TimeUnit unit, SpecialAction action) {
			super(delay, unit);
			this.action = action;
		}
	}

	/**
	 * Defines state of the body.
	 * 
	 */
	public static enum State {
		/**
		 * The body has been created but it has not been started yet.
		 */
		INIT,
		/**
		 * The body has been started, it has not begun to handle request.
		 */
		RUNNING,
		/**
		 * The body is running and it is waiting request.
		 */
		IDLE,
		/**
		 * The body is handling a request. Before becoming WORKING, body should
		 * acquire a permit on the agency actor-thread semaphore. When handling
		 * of the request is finished, permit is released. This is to avoid the
		 * concurrency of too many threads.
		 * 
		 * @see org.d3.actor.Agency#getActorThreadSemaphore()
		 * @see java.concurrent.Semaphore
		 */
		WORKING,
		/**
		 * The body is entering in its termination state. It will not accept
		 * request anymore. According to its stop policy, remaining request will
		 * be handled or redirect.
		 */
		TERMINATING,
		/**
		 * The body is terminated.
		 */
		TERMINATED
	}

	/**
	 * Defines the termination policy about the handling of remaining requests.
	 */
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

	/**
	 * Received requests are stored in a queue, waiting to be handled. When body
	 * is running, it polls request of its queue and handle it.
	 */
	protected final BodyQueue queue;
	/**
	 * Flag indicating is the body should continue to run or not. When the body
	 * is started, this flag is set to true, then when body has to be stopped,
	 * flag turns to false and the body leaves its loop.
	 */
	private volatile boolean running;
	/**
	 * Defines the policy of the termination of the body.
	 */
	protected StopPolicy stopPolicy;
	/**
	 * Indicates the actual state of the body.
	 */
	protected final AtomicState<State> state;
	/**
	 * When the body is stopped, it is possible to define a cause of its
	 * termination. According to its stop policy, this cause will be sent to
	 * remaining requests.
	 */
	protected Throwable stopCause;

	/**
	 * Special constructor for extended class, allowing to define its own queue.
	 * 
	 * @param owner
	 *            the owner of the body.
	 * @param queue
	 *            the queue in which remaining requests are stored.
	 */
	protected BodyThread(LocalActor owner, BodyQueue queue) {
		super(owner, "request");
		this.queue = queue;
		this.running = false;
		this.state = new AtomicState<State>(State.class, State.INIT);
	}

	/**
	 * Create a new body for the local actor passed as parameter.
	 * 
	 * @param owner
	 *            the local actor to which this body will be dedicated.
	 */
	public BodyThread(LocalActor owner) {
		this(owner, new BodyQueue());
	}

	/**
	 * The method which will be run when the body will be started.
	 * 
	 * First this method will check if it is the current thread, then it
	 * registers the actor.
	 * 
	 * When the actor is registered, state of body becomes RUNNING and it
	 * triggers the <code>onRun()</code> hook and enters in its running loop.
	 * 
	 * When the loop returned, the termination method is invoked and state of
	 * the body become TERMINATED.
	 */
	public void run() {
		checkIsOwner();

		running = true;
		owner.register();

		state.set(State.RUNNING);

		onRun();

		loop();
		terminate();

		state.set(State.TERMINATED);
	}

	/**
	 * Hook called when the body start to run. It can be overriden by extended
	 * classes to perform special action on run.
	 */
	protected void onRun() {
	}

	/**
	 * The body loop in which requests will be handled.
	 */
	private final void loop() {
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
			state.set(State.IDLE);

			try {
				current = queue.take();
			} catch (InterruptedException e) {
				continue;
			}

			if (current == null)
				continue;

			try {
				actorThreadSemaphore.acquireUninterruptibly();

				state.set(State.WORKING);

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

		state.set(State.TERMINATING);

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

	/**
	 * Handling of the special action MIGRATE. In a default body, this throws a
	 * ActorInternalException.
	 * 
	 * @param sat
	 *            the task associated with the special action.
	 */
	protected void specialActionMigrate(SpecialActionTask sat) {
		throw new ActorInternalException();
	}

	/**
	 * Handling of the special action STEP. If the owner of the thread is not a
	 * StepActor, the method just returns.
	 * 
	 * @param sat
	 *            the task associated with the special action.
	 */
	protected void specialActionStep(SpecialActionTask sat) {
		if (owner instanceof StepActor) {
			StepActor sa = (StepActor) owner;
			sa.step();
			sat.delay = sa.getStepDelay(sat.unit);
			sat.reset();

			queue.add(sat);
		}
	}

	/**
	 * Creates and enqueues a new call.
	 * 
	 * @param name
	 *            name of the requested callable.
	 * @param args
	 *            arguments passed to the callable method.
	 * @return a future
	 */
	public final Future enqueue(String name, Object[] args) {
		Call c = new Call(owner, name, args);
		enqueue(c);

		return c.getFuture();
	}

	/**
	 * Enqueue a call in the body queue.
	 * 
	 * @param c
	 *            the call to enqueue.
	 */
	public final void enqueue(Call c) {
		switch (state.get()) {
		case TERMINATING:
		case TERMINATED:
			c.getFuture().init(stopCause);
			break;
		default:
			queue.add(c);
			break;
		}
	}

	/**
	 * Get the actual body state. The access is thread safe, but the returned
	 * state object just describe the state of the body at the invocation of the
	 * method and this state could be modified by another thread.
	 * 
	 * @return the state of the body at the invocation of the method.
	 */
	public State getBodyState() {
		return state.get();
	}

	/**
	 * Allows to wait until the body becomes ready.
	 * 
	 * @throws InterruptedException
	 *             if the thread is interrupted while is waiting to the RUNNING
	 *             state.
	 */
	public void waitUntilBodyReady() throws InterruptedException {
		switch (state.get()) {
		case INIT:
			state.waitForState(State.RUNNING);
			break;
		}
	}

	/*
	 * PRIVATE. Enqueue a migration request.
	 */
	void migrate() {
		checkIsOwner();

		SpecialActionTask sat = new SpecialActionTask(0, TimeUnit.SECONDS,
				SpecialAction.MIGRATE);
		queue.add(sat);
	}

	/**
	 * Terminate the body. The actor owning the body is unregistered.
	 */
	protected void terminate() {
		super.terminate();
		owner.unregister();
	}

	/**
	 * Request a termination of the body.
	 * 
	 * @param stop
	 *            the policy which should be used.
	 * @param cause
	 *            the cause of the termination
	 */
	protected void terminateBody(StopPolicy stop, Throwable cause) {
		checkIsOwner();
		stopPolicy = stop;
		running = false;
		stopCause = cause;
		interrupt();
	}
}
