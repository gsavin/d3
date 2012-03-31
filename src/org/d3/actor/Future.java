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

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Future {
	public static enum SpecialReturn {
		NULL, VOID
	}

	private static final SecureRandom random = new SecureRandom();
	private static final AtomicLong futureIdGenerator = new AtomicLong(0);

	private static String newFutureId() {
		return String.format("%016x%016x%016x", System.nanoTime(),
				random.nextLong(), futureIdGenerator.getAndIncrement());
	}

	private final String id;
	private Object value;
	private final AtomicBoolean available;
	private LinkedList<CountDownLatch> latchs;

	public Future() {
		this(newFutureId());
		Agency.getLocalAgency().getProtocols().getFutures().register(this);
	}

	protected Future(String id) {
		this.id = id;
		this.value = null;
		this.available = new AtomicBoolean(false);
		this.latchs = null;
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue() throws CallException, InterruptedException {
		synchronized (available) {
			if (!available.get())
				available.wait();
		}

		if (value instanceof CallException)
			throw (CallException) value;

		return (T) value;
	}

	public String getId() {
		return id;
	}

	public void init(Object value) {
		if (value instanceof SpecialReturn) {
			SpecialReturn sr = (SpecialReturn) value;

			switch (sr) {
			case VOID:
			case NULL:
			default:
				this.value = null;
			}
		} else {
			this.value = value;
		}

		Agency.getLocalAgency().getProtocols().getFutures().unregister(this);

		synchronized (available) {
			available.set(true);
			available.notifyAll();

			if (latchs != null) {
				while (latchs.size() > 0)
					latchs.pop().countDown();
				
				latchs = null;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T get() throws CallException {
		if (available.get()) {
			Object obj = value;

			if (obj instanceof CallException)
				throw (CallException) obj;

			return (T) obj;
		}

		throw new ValueNotAvailableException();
	}

	public boolean isAvailable() {
		return available.get();
	}

	public void waitForValue() throws InterruptedException {
		while (!available.get()) {
			synchronized (available) {
				available.wait(1000);
			}
		}
	}

	public void waitForValue(long timeout) throws InterruptedException {
		if (available.get())
			return;

		synchronized (available) {
			available.wait(timeout);
		}
	}

	public URI getURI() {
		try {
			return new URI(String.format("//%s/%s/%s", Agency.getLocalHost()
					.getHost(), Agency.getLocalAgencyId(), getId()));
		} catch (URISyntaxException e) {
			return null;
		}
	}

	public void putLatch(CountDownLatch latch) {
		synchronized (available) {
			if (available.get()) {
				latch.countDown();
			} else {
				if (latchs == null)
					latchs = new LinkedList<CountDownLatch>();

				latchs.add(latch);
			}
		}
	}
}
