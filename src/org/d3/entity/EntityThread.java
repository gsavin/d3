/*
 * This file is part of d3 <http://d3-project.org>.
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
 * Copyright 2010 - 2011 Guilhelm Savin
 */
package org.d3.entity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.d3.Console;
import org.d3.actor.Agency;
import org.d3.actor.BodyThread;
import org.d3.actor.Entity;
import org.d3.entity.migration.CallData;
import org.d3.entity.migration.ExportationException;
import org.d3.entity.migration.IOMap;
import org.d3.entity.migration.MigratableField;
import org.d3.entity.migration.MigrationData;
import org.d3.entity.migration.MigrationException;
import org.d3.entity.migration.MigrationProtocol;
import org.d3.remote.RemoteActors;
import org.d3.remote.RemoteAgency;

public class EntityThread extends BodyThread {

	public EntityThread(Entity owner) {
		super(owner, new EntityBodyQueue());
	}

	protected void onRun() {
		checkIsOwner();
		((Entity) owner).initEntity();
	}
	
	protected void specialActionMigrate(SpecialActionTask sat) {
		checkIsOwner();

		EntityBodyQueue queue = (EntityBodyQueue) this.queue;

		Entity e = (Entity) owner;
		RemoteAgency remote = e.getMigrationDestination();

		if (remote != null) {

			MigrationProtocol emp = (MigrationProtocol) Agency.getLocalAgency()
					.getProtocols().getSchemes()
					.getCompatibleProtocol(MigrationProtocol.SCHEME);

			boolean open = false;
			boolean success = false;

			MigrationException failed = null;
			
			try {
				open = emp.open(remote);
			} catch (MigrationException me) {
				failed = me;
			}

			if (open) {
				queue.swap();

				try {
					e.beforeMigration();
				} catch(Exception ex) {
					Console.error("error while calling beforeMigration()");
				}
				
				String fullpath = e.getFullPath();
				
				try {
					LinkedList<CallData> data = queue.exportSwapForMigration();
					MigratableField[] fieldsData;
					
					fieldsData = IOMap.get(e.getClass()).exportData(e);
					
					MigrationData migration = new MigrationData(e, data, fieldsData);
					success = emp.migrate(migration);
				} catch (MigrationException e1) {
					success = false;
					failed = e1;
				} catch (ExportationException ee) {
					success = false;
					failed = new MigrationException(ee);
				}

				if (success) {
					Redirection cause = null;
					
					try {
						URI uri = RemoteActors.getRemoteActorURI(remote, fullpath);
						cause = new Redirection(uri);
					} catch(URISyntaxException ex) {
						cause = new Redirection(null);
					}
					
					queue.clearSwapped();
					terminateBody(StopPolicy.SEND_REDIRECTION_AND_STOP, cause);
					
					Console.info("has migrated");
				} else {
					queue.restore();
				}

			} else {
				Console.error("failed to open migration negociation");
			}

			if (!success)
				e.migrationFailed(remote, failed);
		} else {
			Console.error("destination is null");
		}
	}

	public Entity getOwnerAsEntity() {
		return (Entity) getOwner();
	}
}
