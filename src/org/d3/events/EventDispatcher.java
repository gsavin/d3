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
import org.d3.actor.LocalActor;

public class EventDispatcher<K extends Enum<K>> {

	private final EnumMap<K, BindableGroup> bound;
	private final LocalActor owner;

	public EventDispatcher(Class<K> cls, LocalActor owner) {
		this.bound = new EnumMap<K, BindableGroup>(cls);
		this.owner = owner;
	}

	public void bind(K event) throws NonBindableActorException {
		if (!bound.containsKey(event))
			bound.put(event, new BindableGroup());

		Actor actor = ActorThread.getCurrentActor();

		if (actor instanceof Bindable)
			bound.get(event).add((Bindable) actor);
		else
			throw new NonBindableActorException();
	}

	public void unbind(K event) {
		Actor actor = ActorThread.getCurrentActor();

		if (actor instanceof Bindable && bound.containsKey(event))
			bound.get(event).remove((Bindable) actor);
	}

	public void trigger(K event, Object data) {
		owner.checkBodyThreadAccess();
		
		BindableGroup group = bound.get(event);

		if (group != null)
			group.trigger(event, data);
	}
}
