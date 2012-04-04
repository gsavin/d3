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

import org.d3.Actor;
import org.d3.Console;
import org.d3.annotation.ActorPath;
import org.d3.protocol.TransmissionException;
import org.d3.protocol.Transmitter;
import org.d3.remote.NoRemotePortAvailableException;
import org.d3.remote.RemoteAgency;
import org.d3.remote.RemotePort;

@ActorPath("/remotes")
public class RemoteActor extends Actor {

	private RemoteAgency remoteAgency;

	public RemoteActor(RemoteAgency remoteAgency, String objectPath,
			String objectId) {
		super(remoteAgency.getRemoteHost().getAddress(), remoteAgency.getId(),
				objectPath, objectId);

		this.remoteAgency = remoteAgency;
	}

	public final IdentifiableType getType() {
		return IdentifiableType.REMOTE;
	}

	public void init() {
		// XXX
	}

	private void doCall(Call call) {
		try {
			RemotePort rp = remoteAgency.getRandomRemotePortTransmittable();
			Transmitter t = (Transmitter) rp.getCompatibleProtocol();

			t.transmit(rp, call);
		} catch (NoRemotePortAvailableException e) {
			call.getFuture().init(new CallException(e));
		} catch (TransmissionException e) {
			Console.error("unable to transmit call");
			Agency.getFaultManager().handle(e, null);

			if (call.getFuture() != null)
				call.getFuture().init(new CallException(e));
		}
	}

	public void call(String name, Future future, Object... args) {
		Call call = new Call(this, name, future, args);
		doCall(call);
	}

	public Object call(String name, Object... args) {
		Call call = new Call(this, name, args);
		doCall(call);

		return call.getFuture();
	}

	public final boolean isRemote() {
		return true;
	}

	public RemoteAgency getRemoteAgency() {
		return remoteAgency;
	}
}
