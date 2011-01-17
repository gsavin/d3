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
package org.d3.feature;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.d3.Args;
import org.d3.Console;
import org.d3.actor.Agency;
import org.d3.agency.RunnableFeature;
import org.d3.agency.RunnableFeatureCommand;
import org.d3.annotation.ActorPath;
import org.d3.remote.RemoteAgency;

@ActorPath("/d3/features/remoteInformationsUpdater")
public class RemoteInformationsUpdater extends RunnableFeature {
	protected class RIUCommand extends RunnableFeatureCommand {
		public RIUCommand() {
			super(delay, unit);
		}

		public void run() {
			for (RemoteAgency rad : agency.eachRemoteAgency()) {
				if (random.nextFloat() < updateProbability)
					update(rad);
			}

			resetDelay(delay, unit);
		}

		protected void update(RemoteAgency rad) {
			Console.info("update %s", rad.getHost());

			try {
				if (!agency.getIpTables().getAddress(rad.getHost())
						.isReachable(1000))
					throw new Exception();

				agency.lazyCheckEntitiesOn(rad);
			} catch (Exception e) {
				agency.unregisterAgency(rad);
			}
		}
	}

	protected Random random;
	protected Agency agency;
	protected long delay;
	protected TimeUnit unit;
	protected float updateProbability;
	protected RIUCommand riuCommand;

	public RemoteInformationsUpdater() {
		super("remoteInformationUpdater");
	}

	public RunnableFeatureCommand getRunnableFeatureCommand() {
		return riuCommand;
	}

	public boolean initFeature(Agency agency, Args args) {
		this.random = new Random();
		this.agency = agency;
		this.delay = 2000;
		this.unit = TimeUnit.MILLISECONDS;
		this.updateProbability = 0.3f;
		this.riuCommand = new RIUCommand();

		return true;
	}

	public void terminateFeature() {

	}
}
