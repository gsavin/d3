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
package org.d3.remote;

import org.d3.actor.Agency;
import org.d3.actor.Protocol;
import org.d3.protocol.Transmitter;

public class RemotePort {

	private final String scheme;
	private final int port;
	private final RemoteAgency remoteAgency;
	private Protocol protocol;

	public RemotePort(RemoteAgency remoteAgency, String scheme, int port) {
		this.remoteAgency = remoteAgency;
		this.scheme = scheme;
		this.port = port;
		this.protocol = Agency.getLocalAgency().getProtocols().getSchemes()
				.getCompatibleProtocol(scheme);
	}

	public RemoteAgency getRemoteAgency() {
		return remoteAgency;
	}

	public String getScheme() {
		return scheme;
	}

	public int getPort() {
		return port;
	}

	public Protocol getCompatibleProtocol() {
		return protocol;
	}

	public boolean isTransmitter() {
		return protocol == null ? false : (protocol instanceof Transmitter);
	}

	public boolean equals(Object obj) {
		if (obj instanceof RemotePort) {
			RemotePort rp = (RemotePort) obj;

			return rp.scheme.equals(scheme) && rp.port == port
					&& rp.remoteAgency.getId().equals(remoteAgency.getId());
		}

		return false;
	}
}
