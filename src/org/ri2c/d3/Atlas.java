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
package org.ri2c.d3;

import org.ri2c.d3.Migration.MigrationStatus;
import org.ri2c.d3.agency.RemoteAgency;
import org.ri2c.d3.annotation.IdentifiableObjectPath;
import org.ri2c.d3.atlas.AtlasListener;
import org.ri2c.d3.entity.Entity;

@IdentifiableObjectPath("/d3")
public abstract class Atlas implements IdentifiableObject {
	public final String getId() {
		return "atlas";
	}

	public final IdentifiableType getType() {
		return IdentifiableType.atlas;
	}

	public abstract void init(Agency agency);

	public abstract void addAtlasListener(AtlasListener listener);

	public abstract void removeAtlasListener(AtlasListener listener);

	public abstract <T extends Entity> T createEntity(Class<T> desc);

	public abstract void entityCall(Request r);

	public abstract MigrationStatus migrateEntity(Entity entity,
			RemoteAgency rad);
}
