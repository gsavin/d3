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
package org.d3.entity;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.d3.Console;
import org.d3.actor.Agency;
import org.d3.actor.Entity;
import org.d3.actor.StepActor;
import org.d3.annotation.ActorPath;
import org.d3.entity.migration.MigrationException;
import org.d3.remote.RemoteAgency;
import org.d3.remote.RemoteHost;

@ActorPath("/entity/traveller")
public class Traveller extends Entity implements StepActor {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2322402743561649216L;

	transient Random r;

	@Migratable
	protected int hop = 0;

	public Traveller(String id) {
		super(id);
		r = new Random();
	}

	public Traveller() {
		this(Long.toHexString(System.nanoTime()));
	}

	public long getStepDelay(TimeUnit unit) {
		return unit.convert(3, TimeUnit.SECONDS);
	}

	public void step() {
		Console.info("I'm here ! I have make %d hope%s !!", hop, hop > 1 ? "s"
				: "");

		if (r.nextBoolean()) {
			Iterator<RemoteHost> hosts = Agency.getLocalAgency()
					.getRemoteHosts().iterator();

			if (hosts.hasNext()) {
				RemoteHost host = hosts.next();
				Iterator<RemoteAgency> agencies = host.iterator();

				if (agencies.hasNext()) {
					Console.info("but I will left soon ...");
					migrateTo(agencies.next());
				}
			}
		}
	}

	public void migrationFailed(RemoteAgency dest, MigrationException ex) {
		Console.error("failed to migrate");
	}
	
	public void beforeMigration() {
		hop++;
	}
}
