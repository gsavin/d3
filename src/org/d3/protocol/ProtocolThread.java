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

import org.d3.RegistrationException;
import org.d3.actor.ActorThread;
import org.d3.actor.Agency;
import org.d3.actor.Protocol;
import org.d3.actor.RemoteActor;

public class ProtocolThread extends ActorThread {

	public ProtocolThread(Protocol owner) {
		super(owner, "server");
	}

	public RemoteActor getCurrentRemoteActor() throws NotRemoteActorCallException {
		throw new NotRemoteActorCallException();
	}
	
	public void run() {
		try {
			Agency.getLocalAgency().getProtocols().register((Protocol) owner);
		} catch(ProtocolException e) {
			throw new RegistrationException();
		}

		((Protocol) owner).listen();
	}
}
