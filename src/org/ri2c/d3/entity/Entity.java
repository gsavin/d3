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
package org.ri2c.d3.entity;

import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.annotation.IdentifiableObjectPath;

@IdentifiableObjectPath("/d3/entity/default")
public class Entity implements IdentifiableObject {
	protected final String id;
	
	protected Entity(String id) {
		this.id = id;
	}

	public final String getId() {
		return id;
	}

	public final IdentifiableType getType() {
		return IdentifiableType.entity;
	}

}
