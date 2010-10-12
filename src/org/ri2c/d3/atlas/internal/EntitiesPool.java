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

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.ri2c.d3.Console;
import org.ri2c.d3.entity.Entity;

import static org.ri2c.d3.IdentifiableObject.Tools.getFullPath;

public class EntitiesPool {
	public static class PoolEntry {
		Entity entity;
		Body body;

		public PoolEntry(Entity entity, Body body) {
			this.body = body;
			this.entity = entity;
		}
	}

	private ConcurrentHashMap<String, PoolEntry> entities;

	public EntitiesPool() {
		this.entities = new ConcurrentHashMap<String, PoolEntry>();
	}

	void host(String entityPath, Body body) {
		if (entities.containsKey(entityPath))
			return;

		entities.put(entityPath, new PoolEntry(null, body));
	}
/*
	void update(String entityId, Entity entity) {
		if (entities.containsKey(entityId)) {
			entities.get(entityId).entity = entity;
		}
	}
*/
	public void host(Entity e, Body body) {
		String entityPath = getFullPath(e);
		if (entities.containsKey(entityPath)) {
			Console.error("want to host entity already hosted");
			return;
		}

		entities.put(entityPath, new PoolEntry(e, body));
	}

	public Entity getEntity(String entityPath) {
		if (entities.containsKey(entityPath))
			return entities.get(entityPath).entity;

		return null;
	}

	public Body getBody(String entityPath) {
		if (entities.containsKey(entityPath))
			return entities.get(entityPath).body;

		return null;
	}

	public boolean hasEntity(String entityPath) {
		return entities.containsKey(entityPath);
	}

	public String[] list() {
		ArrayList<String> list = new ArrayList<String>(entities.size());
		list.addAll(entities.keySet());
		list.trimToSize();

		return list.toArray(new String[list.size()]);
	}

	public void unhost(String entityPath) {
		PoolEntry pe = entities.get(entityPath);

		if (pe != null) {
			if (pe.body.isRunning())
				Console.warning("remove running body");

			entities.remove(entityPath);
		}
	}
}
