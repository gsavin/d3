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
package org.ri2c.d3.test;

import java.net.URI;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.ri2c.d3.Agency;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.RemoteIdentifiableObject;
import org.ri2c.d3.IdentifiableObject.IdentifiableType;
import org.ri2c.d3.annotation.IdentifiableObjectPath;
import org.ri2c.d3.annotation.RequestCallable;
import org.ri2c.d3.entity.Entity;

@IdentifiableObjectPath("/d3/test/entities")
public class TestEntity extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4661324109912888393L;

	static Random random = new Random();

	ConcurrentLinkedQueue<IdentifiableObject> idObjects;

	public TestEntity(String entityId) {
		super(entityId);
		idObjects = new ConcurrentLinkedQueue<IdentifiableObject>();
	}

	@RequestCallable("beMyFriend")
	public void addFriend(URI uri) {
		IdentifiableObject idObject = Agency.getLocalAgency().getIdentifiableObject(uri);

		if (idObject != null)
			idObjects.add(idObject);
	}
	
	@RequestCallable("ping")
	public void ping() {
		
	}

	@RequestCallable("pong")
	public void pong() {
		
	}

	public void step() {
		int i = random.nextInt(idObjects.size());

		for (IdentifiableObject idObject : idObjects) {
			if (i-- == 0) {
				
			}
		}
	}
}
