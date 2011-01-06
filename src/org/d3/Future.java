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
package org.d3;

import java.util.concurrent.atomic.AtomicBoolean;

import org.d3.annotation.IdentifiableObjectPath;
import org.d3.annotation.RequestCallable;

@IdentifiableObjectPath("/d3/futures")
public class Future extends LocalIdentifiableObject {
	public static enum SpecialReturn {
		NULL, VOID
	}

	private static long futureIdGenerator = 0;

	private static String newFutureId() {
		return String.format("%016X%016X", System.nanoTime(),
				futureIdGenerator++);
	}

	Object value;
	AtomicBoolean available;
	Thread thread2interrupt;

	public Future() {
		super(newFutureId());
		
		this.value = null;
		this.available = new AtomicBoolean(false);

		register();
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

		unregister();
	}

	public boolean isAvailable() {
		return available.get();
	}

	public void interruptMeWhenDone() {
		thread2interrupt = Thread.currentThread();
	}

	public void waitForValue() {
		while (!available.get()) {
			try {
				synchronized(available) {
					available.wait(200);
				}
			} catch (InterruptedException e) {

			}
		}
	}
	
	public void waitForValue( long timeout ) {
		if(available.get())
			return;
		
		try {
			synchronized(available) {
				available.wait(timeout);
			}
		} catch (InterruptedException e) {

		}
	}

	public final IdentifiableType getType() {
		return IdentifiableType.future;
	}
}
