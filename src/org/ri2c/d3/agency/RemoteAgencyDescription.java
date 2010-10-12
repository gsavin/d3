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
package org.ri2c.d3.agency;

import org.ri2c.d3.RemoteIdentifiableObject;
import org.ri2c.d3.annotation.IdentifiableObjectPath;

@IdentifiableObjectPath("/d3/remotes")
public class RemoteAgencyDescription extends RemoteIdentifiableObject {
	protected final String agencyId;
	protected String address;
	protected String[] protocols;
	protected long lastPresenceDate;
	protected RemoteIdentifiableObject remoteAtlas;

	public RemoteAgencyDescription(String agencyId, String address,
			String protocols) {
		super(agencyId, agencyId, IdentifiableType.agency);

		this.agencyId = agencyId;
		this.address = address;
		this.protocols = protocols.trim().split("\\s*,\\s*");
		this.lastPresenceDate = System.currentTimeMillis();
		this.remoteAtlas = new RemoteIdentifiableObject(agencyId, "/d3/atlas",
				IdentifiableType.atlas);
	}

	public void udpatePresence(long date) {
		this.lastPresenceDate = date;
	}

	public final String getId() {
		return agencyId;
	}

	public String getAddress() {
		return address;
	}
	
	public RemoteIdentifiableObject getRemoteAtlas() {
		return remoteAtlas;
	}

	public String getFirstProtocol() {
		if (protocols == null || protocols.length == 0)
			return null;

		return protocols[0];
	}
}
