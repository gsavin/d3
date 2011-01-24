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
package org.d3.protocol;

import java.net.URI;
import java.net.URISyntaxException;

import org.d3.Console;
import org.d3.actor.Future;
import org.d3.remote.NoRemotePortAvailableException;
import org.d3.remote.RemoteAgency;
import org.d3.remote.RemotePort;

public class RemoteFuture extends Future {

	private RemoteAgency remote;

	public RemoteFuture(RemoteAgency ra, String id) {
		super(id);
		this.remote = ra;
	}

	public void init(Object value) {
		super.init(value);

		try {
			RemotePort rp = remote.getRandomRemotePortTransmittable();
			Transmitter t = (Transmitter) rp.getCompatibleProtocol();

			t.transmitFuture(rp, getId(), value);
		} catch (NoRemotePortAvailableException e) {
			Console.exception(e);
		}
	}

	public URI getURI() {
		try {
			return new URI(String.format("//%s/%s/%s", remote.getRemoteHost()
					.getAddress().getHost(), remote.getId(), getId()));
		} catch (URISyntaxException e) {
			return null;
		}
	}
}
