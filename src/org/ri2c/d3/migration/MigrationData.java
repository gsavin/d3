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
package org.ri2c.d3.migration;

import java.util.Collection;

import org.ri2c.d3.Request;
import org.ri2c.d3.entity.Entity;

public class MigrationData {

	protected Entity entity;
	protected Collection<Request> requests;
	
	public MigrationData(Entity entity, Collection<Request> requests) {
		this.entity = entity;
		this.requests = requests;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public Collection<Request> getRequests() {
		return requests;
	}
}
