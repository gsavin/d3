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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class ScheduledTask implements Delayed {

	protected long date;
	protected long delay;
	protected TimeUnit unit;
	
	public ScheduledTask(long delay, TimeUnit unit) {
		this.delay = delay;
		this.unit = unit;
		
		reset();
	}
	
	public int compareTo(Delayed o) {
		TimeUnit unit = TimeUnit.NANOSECONDS;
		return (int) Math.signum(getDelay(unit) - o.getDelay(unit));
	}

	public long getDelay(TimeUnit unit) {
		return date - System.nanoTime();
	}

	public void reset() {
		date = System.nanoTime() + TimeUnit.NANOSECONDS.convert(delay, unit);
	}
}
