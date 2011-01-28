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

import java.util.concurrent.TimeUnit;

import org.d3.Console;
import org.d3.actor.Agency;
import org.d3.actor.Entity;
import org.d3.actor.StepActor;
import org.d3.annotation.ActorPath;
import org.d3.entity.migration.MigrationException;
import org.d3.events.Bindable;
import org.d3.remote.RemoteAgency;
import org.d3.remote.RemoteEvent;

@ActorPath("/entity/traveller")
public class Traveller extends Entity implements Bindable, StepActor {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2322402743561649216L;
	
	private RemoteAgency dest;
	
	public Traveller() {
		super(Long.toHexString(System.nanoTime()));
		dest = null;
	}
	
	public long getStepDelay(TimeUnit unit) {
		return unit.convert(4, TimeUnit.SECONDS);
	}

	public void step() {
		if( dest != null ) {
			Console.warning("begin migration");
			migrateTo(dest);
		}
	}

	public void initEntity() {
		Agency.getLocalAgency().getRemoteHosts().getEventDispatcher().bind(this);
	}

	public <K extends Enum<K>> void trigger(K event, Object ... data) {
		if(event instanceof RemoteEvent) {
			RemoteEvent revent = (RemoteEvent) event;
			switch(revent) {
			case REMOTE_AGENCY_REGISTERED:
				dest = (RemoteAgency) data[0];
				break;
			}
		}
	}
	
	public void migrationFailed(RemoteAgency dest, MigrationException ex) {
		Console.error("failed to migrate");
		Console.exception(ex);
	}
}
