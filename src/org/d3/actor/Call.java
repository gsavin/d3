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

import java.util.concurrent.TimeUnit;

import org.d3.Actor;
import org.d3.ActorNotFoundException;
import org.d3.entity.migration.CallData;
import org.d3.remote.UnknownAgencyException;

public class Call extends ScheduledTask {

	private final Actor source;
	private final Actor target;
	private final String name;
	private final Object[] args;
	private final Future future;

	public Call(Actor target, String name, Object... args) {
		this(0, TimeUnit.NANOSECONDS, target, name, args);
	}

	public Call(Actor target, String name, Future future, Object... args) {
		this(0, TimeUnit.NANOSECONDS, target, name, future, args);
	}

	public Call(long delay, TimeUnit unit, Actor target, String name,
			Object... args) {
		this(delay, unit, target, name, new Future(), args);
	}

	public Call(long delay, TimeUnit unit, Actor target, String name,
			Future future, Object... args) {
		super(delay, unit);

		source = ActorThread.getCurrentActor();

		if (source == null || name == null || target == null)
			throw new NullPointerException();

		this.target = target;
		this.name = name;
		this.args = args;
		this.future = future;

		Agency.getLocalAgency().getActors().getEventDispatcher()
				.trigger(ActorsEvent.CALL, source, target);
	}

	public Call(CallData data) throws CallException {
		super(data.getDelay(), data.getTimeUnit());

		this.name = data.getName();
		this.args = data.getArgs();

		try {
			this.source = Agency.getLocalAgency().getActors()
					.get(data.getSourceURI());

			this.target = Agency.getLocalAgency().getActors()
					.get(data.getTargetURI());
		} catch (ActorNotFoundException e) {
			throw new CallException(e);
		} catch (UnregisteredActorException e) {
			if (e.getCause() != null)
				throw new CallException(e.getCause());
			else
				throw new CallException(e);
		}

		try {
			this.future = data.getFuture();
		} catch (UnknownAgencyException e) {
			throw new CallException(e);
		}
	}

	public Actor getSource() {
		return source;
	}

	public Actor getTarget() {
		return target;
	}

	public String getName() {
		return name;
	}

	public int getArgCount() {
		return args == null ? 0 : args.length;
	}

	public Object[] getArgs() {
		return args;
	}

	public Future getFuture() {
		return future;
	}
}
