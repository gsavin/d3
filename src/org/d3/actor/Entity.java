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
import java.util.LinkedList;

import org.d3.Console;
import org.d3.annotation.ActorPath;
import org.d3.entity.EReference;
import org.d3.entity.migration.CallData;
import org.d3.entity.migration.IOMap;
import org.d3.entity.migration.ImportationException;
import org.d3.entity.migration.MigrationData;
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

	public void importEntity(MigrationData data) throws ImportationException {
		if (isAlive())
			throw new ImportationException(
					"can not import entity which is alive");

		IOMap.get(getClass()).importData(this, data.getFieldsData());
		LinkedList<CallData> calls = data.getCalls();

		Console.info("import %d calls", calls.size());
		
		for (int i = 0; i < calls.size(); i++) {
			try {
				Call c = new Call(calls.get(i));
				call(c);
			} catch(CallException ce) {
				Agency.getFaultManager().handle(ce, null);
			}
		}
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

	public void beforeMigration() {

	}
	
	public EReference getReference() {
		return EReference.get(this);
	}
}
