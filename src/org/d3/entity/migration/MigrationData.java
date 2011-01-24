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
package org.d3.entity.migration;

import java.io.Serializable;
import java.util.LinkedList;

import org.d3.actor.Entity;

public class MigrationData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6441852201508531522L;

	protected String className;
	protected String path;
	protected String id;
	protected LinkedList<CallData> calls;

	public MigrationData(Entity e, LinkedList<CallData> calls) {
		this.className = e.getClass().getName();
		this.path = e.getPath();
		this.id = e.getId();
		this.calls = calls;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getId() {
		return id;
	}
}
