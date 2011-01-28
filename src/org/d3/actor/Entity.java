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

import java.io.Serializable;

import org.d3.annotation.ActorPath;
import org.d3.entity.migration.MigrationException;
import org.d3.remote.RemoteAgency;

@ActorPath("/entities")
public class Entity extends LocalActor implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2008980313521087227L;
	
	private transient RemoteAgency migrationDestination;
	
	protected Entity(String id) {
		super(id);
	}

	public void initEntity() {
		
	}
	
	public final IdentifiableType getType() {
		return IdentifiableType.ENTITY;
	}

	public void migrateTo(RemoteAgency remote) {
		migrationDestination = remote;
		migrate();
	}
	
	public RemoteAgency getMigrationDestination() {
		return migrationDestination;
	}
	
	public void migrationFailed(RemoteAgency dest, MigrationException e) {
		// Override by child to handle migration failed
	}
}
