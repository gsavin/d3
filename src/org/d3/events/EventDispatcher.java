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
package org.d3.events;

import java.util.EnumMap;

import org.d3.Actor;
import org.d3.actor.ActorThread;

public class EventDispatcher<K extends Enum<K>> {

	private final EnumMap<K, BindableGroup> bound;
	private final BindableGroup all;

	public EventDispatcher(Class<K> cls) {
		this.bound = new EnumMap<K, BindableGroup>(cls);
		this.all = new BindableGroup();
	}

	public void bind(Bindable bindable, K event) {
		if (!bound.containsKey(event))
			bound.put(event, new BindableGroup());
		
		bound.get(event).add(bindable);
	}
	
	public void bind(K event) throws NonBindableActorException {
		Actor actor = ActorThread.getCurrentActor();

		if (actor instanceof Bindable)
			bind((Bindable) actor, event);
		else
			throw new NonBindableActorException();
	}

	public void bind(Bindable bindable) {
		all.add(bindable);
	}
	
	public void bind() throws NonBindableActorException {
		Actor actor = ActorThread.getCurrentActor();

		if (actor instanceof Bindable)
			bind((Bindable) actor);
		else
			throw new NonBindableActorException();
	}

	public void unbind(Bindable bindable, K event) {
		if (bound.containsKey(event))
			bound.get(event).remove(bindable);
	}
	
	public void unbind(K event) throws NonBindableActorException {
		Actor actor = ActorThread.getCurrentActor();

		if (actor instanceof Bindable)
			unbind((Bindable) actor, event);
		else
			throw new NonBindableActorException();
	}

	public void unbind(Bindable bindable) {
		all.remove(bindable);
	}
	
	public void unbind() throws NonBindableActorException {
		Actor actor = ActorThread.getCurrentActor();

		if (actor instanceof Bindable)
			unbind((Bindable) actor);
		else
			throw new NonBindableActorException();
	}

	public void trigger(K event, Object ... data) {
		BindableGroup group = bound.get(event);

		if (group != null)
			group.trigger(event, data);
		
		all.trigger(event, data);
	}
}
