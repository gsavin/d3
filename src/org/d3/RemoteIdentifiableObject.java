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
package org.d3;

import java.net.InetAddress;

import org.d3.annotation.IdentifiableObjectPath;
import org.d3.protocol.Protocols;

@IdentifiableObjectPath("/remotes")
public class RemoteIdentifiableObject extends IdentifiableObject {
	protected final IdentifiableType objectType;

	public RemoteIdentifiableObject(InetAddress host, String objectId,
			IdentifiableType objectType) {
		super(host, objectId.startsWith("/") ? objectId.substring(1) : objectId);

		this.objectType = objectType;
	}

	public final IdentifiableType getType() {
		return objectType;
	}

	public void handle(Request r) {
		throw new UnsupportedOperationException(
				"remote object not support request handling");
	}

	public Object call(IdentifiableObject source, String name, Object... args) {
		Future f = new Future();
		Request r = new Request(source, this, name, args, f);

		Protocols.sendRequest(r);

		f.waitForValue();

		if (f.getValue() == Future.SpecialReturn.NULL)
			return null;

		return f.getValue();
	}

	public final boolean isRemote() {
		return true;
	}
}
