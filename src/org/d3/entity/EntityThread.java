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
package org.d3.entity;

import java.util.LinkedList;

import org.d3.actor.BodyThread;
import org.d3.actor.Entity;
import org.d3.entity.migration.CallData;
import org.d3.remote.RemoteAgency;

public class EntityThread extends BodyThread {

	public EntityThread(Entity owner) {
		super(owner, new EntityBodyQueue());
	}

	public LinkedList<CallData> exportCalls() {
		LinkedList<CallData> calls = new LinkedList<CallData>();
		
		return calls;
	}
	
	protected void specialActionMigrate(SpecialActionTask sat) {
		EntityBodyQueue queue = (EntityBodyQueue) this.queue;
		queue.swap();
		
		Entity e = (Entity) owner;
		RemoteAgency remote = e.getMigrationDestination();

		if (remote != null) {
			LinkedList<CallData> data = queue.exportSwapForMigration();
		}
	}
}
