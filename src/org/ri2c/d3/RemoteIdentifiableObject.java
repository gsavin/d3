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

import org.ri2c.d3.annotation.IdentifiableObjectPath;

@IdentifiableObjectPath("/")
public class RemoteIdentifiableObject extends IdentifiableObject {
	protected String remoteAgencyId;
	protected final IdentifiableType objectType;

	public RemoteIdentifiableObject(String agencyId, String objectId,
			IdentifiableType objectType) {
		super(objectId.startsWith("/") ? objectId.substring(1) : objectId);

		this.remoteAgencyId = agencyId;
		this.objectType = objectType;
	}

	public String getRemoteAgencyId() {
		return remoteAgencyId;
	}

	public final IdentifiableType getType() {
		return objectType;
	}

	public void handleRequest(IdentifiableObject source,
			IdentifiableObject target, Request r) {
		throw new UnsupportedOperationException(
				"remote object not support request handling");
	}
}
