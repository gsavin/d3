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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.DelayQueue;

import org.d3.actor.Call;
import org.d3.actor.ScheduledTask;
import org.d3.actor.body.BodyQueue;
import org.d3.entity.migration.CallData;

class EntityBodyQueue extends BodyQueue {

	protected DelayQueue<ScheduledTask> swap;
	
	EntityBodyQueue() {
		super();
		swap = new DelayQueue<ScheduledTask>();
	}
	
	void swap() {
		DelayQueue<ScheduledTask> t = theQueue;
		theQueue = swap;
		swap = t;
	}
	
	LinkedList<CallData> exportSwapForMigration() {
		LinkedList<CallData> data = new LinkedList<CallData>();

		Iterator<ScheduledTask> ite = swap.iterator();

		while (ite.hasNext()) {
			ScheduledTask d = ite.next();

			if (d instanceof Call)
				data.add(new CallData((Call) d));
		}

		return data;
	}
	
	void clearSwapped() {
		swap.clear();
	}
	
	void restore() {
		swap();
		theQueue.addAll(swap);
		swap.clear();
	}
}
