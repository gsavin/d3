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

public class RemoteIdentifiableObject
	implements IdentifiableObject
{
	protected String 			remoteAgencyId;
	protected String 			objectId;
	protected IdentifiableType	objectType;
	
	public RemoteIdentifiableObject( String agencyId,
			String objectId, IdentifiableType objectType )
	{
		this.remoteAgencyId = agencyId;
		this.objectId		= objectId;
		this.objectType		= objectType;
	}
	
	public String getRemoteAgencyId()
	{
		return remoteAgencyId;
	}
	
	public String getId()
	{
		return objectId;
	}
	
	public IdentifiableType getType()
	{
		return objectType;
	}

	public <T extends Description> T getDescription() {
		return null;
	}

	public void handleRequest(IdentifiableObject source,
			IdentifiableObject target, Request r) {
		throw new UnsupportedOperationException("remote object not support request handling");
	}
}
