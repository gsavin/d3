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

@IdentifiableObjectPath("/")
public class RemoteAgency extends RemoteIdentifiableObject {
	protected final String agencyId;
	protected String address;
	protected String[] protocols;
	protected long lastPresenceDate;
	protected RemoteIdentifiableObject remoteAtlas;
	protected String digest;

	public RemoteAgency(String agencyId, String address, String protocols,
			String digest) {
		super(agencyId, agencyId, IdentifiableType.agency);

		this.agencyId = agencyId;
		this.address = address;
		this.protocols = protocols.trim().split("\\s*,\\s*");
		this.lastPresenceDate = System.currentTimeMillis();
		this.digest = digest;
		this.remoteAtlas = new RemoteIdentifiableObject(agencyId, "/d3/atlas",
				IdentifiableType.atlas);
	}

	public void udpatePresence(long date) {
		this.lastPresenceDate = date;
	}

	public void updateDigest(String digest) {
		this.digest = digest;
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
	
	public String getDigest() {
		return digest;
	}
}
