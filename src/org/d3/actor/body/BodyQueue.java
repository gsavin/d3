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
package org.d3.actor.body;

import java.util.Iterator;
import java.util.concurrent.DelayQueue;

import org.d3.actor.ScheduledTask;

public class BodyQueue implements Iterable<ScheduledTask> {
	protected DelayQueue<ScheduledTask> theQueue;

	public BodyQueue() {
		theQueue = new DelayQueue<ScheduledTask>();
	}

	public void add(ScheduledTask task) {
		theQueue.add(task);
	}

	public ScheduledTask take() throws InterruptedException {
		return theQueue.take();
	}

	public Iterator<ScheduledTask> iterator() {
		return theQueue.iterator();
	}

	public ScheduledTask poll() {
		return theQueue.poll();
	}
}
