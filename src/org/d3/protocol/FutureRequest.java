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

import java.io.Serializable;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.d3.Console;
import org.d3.protocol.request.ObjectCoder;
import org.d3.protocol.request.ObjectCoder.CodingMethod;
import org.d3.remote.RemotePort;

public class FutureRequest {
	private String id;
	private CodingMethod coding;
	private String value;
	private URI target;

	public FutureRequest(String id, Object value,
			RemotePort remotePort) {
		this.id = id;
		this.coding = CodingMethod.HEXABYTES;
		this.value = ObjectCoder.encode(coding, (Serializable) value);

		try {
			InetAddress address = remotePort.getRemoteAgency().getRemoteHost()
					.getAddress();
			String host = address.getHostAddress();

			if (address instanceof Inet6Address)
				host = String.format("[%s]", host);

			this.target = new URI(String.format("%s://%s:%d",
					remotePort.getScheme(), host, remotePort.getPort()));
		} catch (URISyntaxException e) {
			Console.exception(e);
		}
	}

	public FutureRequest(String id, CodingMethod coding, String value,
			URI target) {
		this.id = id;
		this.value = value;
		this.coding = coding;
		this.target = target;
	}

	public URI getTarget() {
		return target;
	}
	
	public String getFutureId() {
		return id;
	}

	public CodingMethod getCodingMethod() {
		return coding;
	}

	public String getValue() {
		return value;
	}

	public Object getDecodedValue() {
		return ObjectCoder.decode(coding, value);
	}
}
