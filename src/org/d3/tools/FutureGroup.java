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
package org.d3.tools;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

import org.d3.actor.Future;

public class FutureGroup implements Iterable<Future> {
	public static enum Policy {
		WAIT_FOR_ONE, WAIT_FOR_ALL
	}

	CountDownLatch latch;
	LinkedList<Future> futures;

	public FutureGroup(int n) {
		futures = new LinkedList<Future>();
		latch = n > 0 ? new CountDownLatch(n) : null;
	}

	public FutureGroup(Policy p) {
		futures = new LinkedList<Future>();

		switch (p) {
		case WAIT_FOR_ONE:
			latch = new CountDownLatch(1);
			break;
		case WAIT_FOR_ALL:
			break;
		}
	}

	public void put(Future f) {
		synchronized (futures) {
			futures.add(f);
		}

		if (latch != null)
			f.putLatch(latch);
	}

	public void await() throws InterruptedException {
		if (latch == null) {
			while (futures.size() > 0) {
				synchronized (futures) {
					Future f = futures.poll();
					f.waitForValue();
				}
			}
		} else {
			latch.await();
		}
	}
	
	public Iterator<Future> iterator() {
		return futures.iterator();
	}
}
