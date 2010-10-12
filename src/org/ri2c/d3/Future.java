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
package org.ri2c.d3;

import java.util.concurrent.atomic.AtomicBoolean;

import org.ri2c.d3.annotation.IdentifiableObjectPath;
import org.ri2c.d3.annotation.RequestCallable;

@IdentifiableObjectPath("/d3/futures")
public class Future implements IdentifiableObject {
	private static long futureIdGenerator = 0;
	private static String newFutureId() {
		return String.format("%016X%016X", System.nanoTime(),
				futureIdGenerator++);
	}

	Object value;
	AtomicBoolean available;
	Thread thread2interrupt;
	protected final String id;
	
	public Future() {
		this.value = null;
		this.available = new AtomicBoolean(false);
		this.id = newFutureId();
	}

	public Object getValue() {
		synchronized (available) {
			try {
				if (!available.get())
					available.wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return value;
	}

	@RequestCallable("init")
	public void init(Object value) {
		this.value = value;

		synchronized (available) {
			available.set(true);
			available.notifyAll();
		}

		if (thread2interrupt != null) {
			try {
				thread2interrupt.interrupt();
			} catch (Exception e) {

			}
		}
	}

	public boolean isAvailable() {
		return available.get();
	}

	public void interruptMeWhenDone() {
		thread2interrupt = Thread.currentThread();
	}

	public String getId() {
		return id;
	}

	public IdentifiableType getType() {
		return IdentifiableType.future;
	}
}
