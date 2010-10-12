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
package org.ri2c.d3.atlas.internal;

import java.net.URI;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Atlas;
import org.ri2c.d3.Console;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.Migration;
import org.ri2c.d3.Migration.MigrationStatus;
import org.ri2c.d3.Request;
import org.ri2c.d3.agency.RemoteAgencyDescription;
import org.ri2c.d3.annotation.IdentifiableObjectDescription;
import org.ri2c.d3.annotation.RequestCallable;
import org.ri2c.d3.atlas.AtlasListener;
import org.ri2c.d3.entity.Entity;
import org.ri2c.d3.migration.MigrationData;
import org.ri2c.d3.protocol.Protocols;

import static org.ri2c.d3.IdentifiableObject.Tools.getFullPath;

@IdentifiableObjectDescription("Internal L2D Atlas implementation.")
public class D3Atlas extends Atlas {
	// protected static long L2D_ATLAS_ID_GENERATOR = 0;
	/*
	 * private class __MigrationManagerBridge implements
	 * MigrationManager.MigrationManagerBridge { public boolean
	 * prepareBodyForEntityReception(String entityId) { if
	 * (entities.hasEntity(entityId)) return false;
	 * 
	 * Body body = new Body(futureManager); entities.host(entityId, body);
	 * 
	 * return true; }
	 * 
	 * public boolean receiveEntityContent(String entityId, EntityADN adn,
	 * Collection<EntityCall> calls) { Body body = entities.getBody(entityId);
	 * 
	 * if (body != null) { D3Entity entity = new D3Entity(entityId, adn);
	 * body.receiveContent(entity, calls);
	 * 
	 * if (!agency.registerIdentifiableObject(entity)) return false;
	 * 
	 * if (entity instanceof D3Entity) ((D3Entity)
	 * entity).setAtlas(D3Atlas.this);
	 * 
	 * entities.update(entityId, entity); startBody(entityId);
	 * 
	 * return body.waitForState(Body.STATE_RUNNING); } else
	 * Console.warning("receive entity content but I don't know this body.");
	 * 
	 * return false; }
	 * 
	 * public void migrationRejectedByRemoteAgency(String entityId) { Body body
	 * = entities.getBody(entityId);
	 * 
	 * if (body != null) { body.migrationCanceled(); } else
	 * Console.warning("migrationRejected but I don't know this body."); }
	 * 
	 * public void entityIsBeingMigrated(String entityId) { Body body =
	 * entities.getBody(entityId);
	 * 
	 * if (body != null) { body.enterMigration(); } else
	 * Console.warning("entityIsBeingMigrated but I don't know this body."); }
	 * 
	 * public void migrationDone(String entityId) { Body body =
	 * entities.getBody(entityId);
	 * 
	 * if (body != null) { body.migrationDone();
	 * body.waitForState(Body.STATE_FINISH);
	 * 
	 * agency.unregisterIdentifiableObject(entities .getEntity(entityId));
	 * entities.unhost(entityId); } else
	 * Console.warning("migrationDone but I don't know this body."); }
	 * 
	 * public Iterable<EntityCall> getEntityCalls(String entityId) { if
	 * (entities.hasEntity(entityId)) return
	 * entities.getBody(entityId).getCalls();
	 * 
	 * return null; } }
	 */
	private EntitiesPool entities;
	private ThreadGroup threadGroup;
	private long entityIdGenerator;
	private Agency agency;

	// private MigrationManager migrationManager;

	public D3Atlas() {
		entities = new EntitiesPool();
		threadGroup = new ThreadGroup("d3-atlas-bodies");
		entityIdGenerator = 0;
	}

	public void init(Agency agency) {
		this.agency = agency;
	}

	public void addAtlasListener(AtlasListener listener) {
		// TODO Auto-generated method stub
	}

	public void removeAtlasListener(AtlasListener listener) {
		// TODO Auto-generated method stub
	}

	public Entity createEntity(Class<? extends Entity> desc) {
		String entityId = newEntityId();

		try {
			Entity entity = desc.getConstructor(String.class).newInstance(
					entityId);
			Body body = new Body(entity);

			if (!agency.registerIdentifiableObject(entity)) {
				return null;
			}

			entities.host(entity, body);
			startBody(getFullPath(entity));

			return entity;
		} catch (Exception e) {
			System.err.printf("[l2d-atlas] error while creating entity: %s %n",
					e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	public void entityCall(Request r) {
		IdentifiableObject idObject = Agency.getLocalAgency()
				.getIdentifiableObject(r.getTargetURI());

		if( idObject == null ) {
			Console.warning("[atlas] call entity which does not exists: %s", r.getTargetURI());
		}
		
		if (idObject.getType() == IdentifiableType.entity) {
			Body body = entities.getBody(getFullPath(idObject));

			if (body == null) {
				throw new NullPointerException("body is null");
			}

			body.newRequest(r);
		}
	}

	public MigrationStatus migrateEntity(Entity entity,
			RemoteAgencyDescription rad) {
		Body body = entities.getBody(getFullPath(entity));
		MigrationData data = new MigrationData(entity, body.queue);

		body.enterMigration();

		Migration m = new Migration(rad, data);

		MigrationStatus s = m.waitEndOfMigration();

		if (s != MigrationStatus.SUCCESS) {
			body.migrationCanceled();
		} else {
			body.migrationDone();
			body.waitForState(Body.STATE_FINISH);
			agency.unregisterIdentifiableObject(entity);
			entities.unhost(getFullPath(entity));
		}

		return s;
	}

	@RequestCallable("host")
	public void host(URI source, String entityPath, String entityClassname) {
		boolean accepted = true;

		if (accepted) {
			Body body = new Body();
			entities.host(entityPath, body);

			Migration m = new Migration(source, body);
			MigrationStatus s = m.waitEndOfMigration();

			if (s == MigrationStatus.SUCCESS)
				startBody(entityPath);
			else
				entities.unhost(entityPath);
		} else {
			IdentifiableObject target = Agency.getLocalAgency()
					.getIdentifiableObject(source);

			Request r = new Request(this, target, "reject", null);
			Protocols.sendRequest(r);
		}
	}

	private void startBody(String entityId) {
		Body body = entities.getBody(entityId);

		if (body != null && !body.isRunning()) {
			Thread t = new Thread(threadGroup, body, entityId + "-body");
			t.start();
		} else
			System.err.printf("[l2d-atlas] start a null body !%n");
	}

	private String newEntityId() {
		return String.format("%s:%016X:%016X", agency.getId(),
				System.nanoTime(), entityIdGenerator++);
	}
}
