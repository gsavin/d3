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
package org.d3.actor;

import java.net.InetAddress;

import org.d3.Actor;
import org.d3.Future;
import org.d3.Request;
import org.d3.annotation.ActorPath;
import org.d3.protocol.Protocols;

@ActorPath("/remotes")
public class RemoteActor extends Actor {
	
	public RemoteActor(InetAddress host, String objectId) {
		super(host, objectId.startsWith("/") ? objectId.substring(1) : objectId);
	}

	public final IdentifiableType getType() {
		return IdentifiableType.remote;
	}

	public void init() {
		// XXX
	}
	
	public void handle(Request r) {
		throw new UnsupportedOperationException(
				"remote object not support request handling");
	}

	public Object call(Actor source, String name, Object... args) {
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
