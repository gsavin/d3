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
package org.d3.tools;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Time {
	public static final Pattern PATTERN = Pattern
			.compile("^\\s*(\\d+)\\s*(DAYS|HOURS|MINUTES|SECONDS|MILLISECONDS|MICROSECONDS|NANOSECONDS)\\s*$");

	public long time;
	public TimeUnit unit;

	public Time(long time, TimeUnit unit) {
		this.time = time;
		this.unit = unit;
	}

	public static Time valueOf(String str) {
		Matcher m = PATTERN.matcher(str);

		if (m.matches()) {
			long time = Integer.parseInt(m.group(1));
			TimeUnit unit = TimeUnit.valueOf(m.group(2));

			return new Time(time, unit);
		}
		
		return null;
	}
}
