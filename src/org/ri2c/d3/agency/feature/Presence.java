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
package org.ri2c.d3.agency.feature;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Args;
import org.ri2c.d3.Request;
import org.ri2c.d3.agency.RemoteAgency;
import org.ri2c.d3.agency.RunnableFeature;
import org.ri2c.d3.agency.RunnableFeatureCommand;
import org.ri2c.d3.protocol.Protocols;

/**
 * <title>Presence L2D Feature</title>
 * 
 * @author Guilhelm Savin
 * 
 */
public class Presence extends RunnableFeature {
	protected static long PRESENCE_ID_GENERATOR = 0;

	private class PresenceCommand extends RunnableFeatureCommand {
		public PresenceCommand() {
			super(minDelay, unit);
		}

		public void run() {
			for (RemoteAgency rad : localAgency.eachRemoteAgency()) {
				Request r = new Request(Presence.this, rad, "presence", null);
				Protocols.sendRequest(r);
			}

			resetDelay(getNewDelay(), unit);
		}

		/**
		 * Get a new delay.
		 * 
		 * @return the next delay
		 */
		protected long getNewDelay() {
			return random.nextInt(averagePeriod) + minDelay;
		}
	}

	protected Agency localAgency;
	protected PresenceCommand presenceCommand;
	/**
	 * Random generator used to compute delays.
	 */
	protected Random random;
	/**
	 * Minimum delay between two messages.
	 */
	protected long minDelay;
	/**
	 * Average period added to minimum delay.
	 */
	protected int averagePeriod;
	protected TimeUnit unit;

	public Presence() {
		super(String.format("presence-%X", PRESENCE_ID_GENERATOR));
		unit = TimeUnit.MILLISECONDS;
	}

	public boolean initFeature(Agency agency, Args args) {
		this.localAgency = agency;
		this.presenceCommand = new PresenceCommand();

		if (args.has("seed"))
			random = new Random(Long.parseLong(args.get("seed")));
		else
			random = new Random();

		if (args.has("min_delay")) {
			String s = args.get("min_delay").trim();

			if (s.matches("\\d+"))
				minDelay = Long.parseLong(args.get("min_delay"));
			else if (s
					.matches("\\d+ (DAYS|HOURS|MINUTES|SECONDS|MILLISECONDS|MICROSECONDS|NANOSECONDS)")) {
				TimeUnit localUnit = TimeUnit.valueOf(s.substring(
						s.indexOf(' ') + 1).trim());
				minDelay = unit.convert(
						Long.parseLong(s.substring(0, s.indexOf(' '))),
						localUnit);
			}
		} else
			minDelay = 3000;

		if (args.has("avg_period")) {
			String s = args.get("avg_period").trim();

			if (s.matches("\\d+"))
				averagePeriod = Integer.parseInt(args.get("avg_period"));
			else if (s
					.matches("\\d+ (DAYS|HOURS|MINUTES|SECONDS|MILLISECONDS|MICROSECONDS|NANOSECONDS)")) {
				TimeUnit localUnit = TimeUnit.valueOf(s.substring(
						s.indexOf(' ') + 1).trim());
				averagePeriod = (int) unit.convert(
						Long.parseLong(s.substring(0, s.indexOf(' '))),
						localUnit);
			}
		} else
			averagePeriod = 2000;

		return true;
	}

	public void terminateFeature() {

	}

	public Runnable getRunnableFeature() {
		return null;
	}

	public RunnableFeatureCommand getRunnableFeatureCommand() {
		return presenceCommand;
	}
}
