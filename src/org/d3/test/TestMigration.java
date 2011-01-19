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
package org.d3.test;

import java.net.URI;
import java.util.LinkedList;

import org.d3.Application;
import org.d3.Console;
import org.d3.Actor;
import org.d3.actor.Agency;
import org.d3.actor.Entity;
import org.d3.annotation.Callable;
import org.d3.entity.Migration.MigrationStatus;
import org.d3.remote.RemoteAgency;
import org.d3.tools.StartD3;

import static org.d3.Actor.Tools.call;

public class TestMigration extends Application {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestMigration test = new TestMigration();

		StartD3.init(args);

		Agency.getLocalAgency().addAgencyListener(test);
		Agency.getLocalAgency().register(test);

		test.init();
		test.execute();
	}

	public static class MigrationEntity extends Entity {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4519822789886385904L;

		public MigrationEntity(String id) {
			super(id);
		}

		@Callable("whereAreYou")
		public String whereIam() {
			return Agency.getLocalAgency().getId();
		}
	}

	LinkedList<URI> entitiesPath;
	boolean migrate = false;
	MigrationStatus status;
	String where;

	public TestMigration() {
		super("l2d.test.migration");

		entitiesPath = new LinkedList<URI>();
	}

	public void init() {
		entitiesPath.add(Agency.getLocalAgency().getAtlas()
				.createEntity(MigrationEntity.class).getURI());
		entitiesPath.add(Agency.getLocalAgency().getAtlas()
				.createEntity(MigrationEntity.class).getURI());

		displayPosition();

		checkMigration();
	}

	public void execute() {
		while (!migrate) {
			try {
				Thread.sleep(200);
			} catch (Exception e) {
			}
		}

		displayPosition();
	}

	protected void displayPosition() {
		for (URI uri : entitiesPath) {
			Actor idObject = Agency.getLocalAgency()
					.getIdentifiableObject(uri);
			String str = (String) call(this, idObject, "whereAreYou", null);
			Console.info("where %s is ? %s", idObject.getId(), str);
		}
	}

	public void newAgencyRegistered(RemoteAgency rad) {
		if (!migrate) {

			Entity e = (Entity) Agency.getLocalAgency().getIdentifiableObject(
					entitiesPath.get(0));

			Agency.getLocalAgency().getAtlas().migrateEntity(e, rad);

			where = String.format("entity://%s%s", rad.getHost(),
					entitiesPath.get(0).getPath());

			try {
				URI uri = new URI(where);

				Console.warning("new uri: %s", uri);

				entitiesPath.set(0, uri);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			migrate = true;
		}
	}

	protected synchronized void checkMigration() {
		if (migrate)
			return;

		for (RemoteAgency rad : Agency.getLocalAgency().eachRemoteAgency()) {
			Entity e = (Entity) Agency.getLocalAgency().getIdentifiableObject(
					entitiesPath.get(0));

			Agency.getLocalAgency().getAtlas().migrateEntity(e, rad);

			where = String.format("entity://%s%s", rad.getHost(),
					entitiesPath.get(0).getPath());

			try {
				URI uri = new URI(where);

				Console.warning("new uri: %s", uri);

				entitiesPath.set(0, uri);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			migrate = true;
			break;
		}
	}
}
