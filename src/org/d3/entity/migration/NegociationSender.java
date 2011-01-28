/*
 * This file is part of d3 <http://d3-project.org>.
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
 * Copyright 2010 - 2011 Guilhelm Savin
 */
package org.d3.entity.migration;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.d3.actor.Agency;
import org.d3.actor.CallException;
import org.d3.actor.Entity;
import org.d3.actor.Future;
import org.d3.entity.EntityThread;
import org.d3.protocol.request.ObjectCoder;
import org.d3.protocol.request.ObjectCoder.CodingMethod;

class NegociationSender extends Negociation {

	public static enum State {
		INIT,
		AUTHORIZATION_REQUEST_SENT,
		GOT_AUTHORIZATION,
		REQUEST_REJECTED,
		DATA_SENT,
		MIGRATION_DONE,
		MIGRATION_FAILED,
		ERROR
	}
	
	State state;
	Future migrationAccepted;
	Future migrationSucceed;
	Entity entity;
	EntityThread thread;
	
	NegociationSender(SocketChannel channel, EntityThread thread,
			InetSocketAddress address) {
		super(channel, address);
		this.entity = thread.getOwnerAsEntity();
		this.thread = thread;
		this.migrationAccepted = new Future();
		this.state = State.INIT;
	}

	public boolean isAuthorized() throws BadStateException {
		switch(state) {
		case INIT:
			throw new BadStateException();
		}
		
		try {
			return (Boolean) migrationAccepted.getValue();
		} catch (CallException e) {
			// TODO Handle why request failed
			return false;
		}
	}

	public boolean isMigrationDoneSuccessfully() throws BadStateException {
		switch(state) {
		case INIT:
		case AUTHORIZATION_REQUEST_SENT:
		case REQUEST_REJECTED:
			throw new BadStateException();
		}
		
		try {
			return (Boolean) migrationAccepted.getValue();
		} catch (CallException e) {
			// TODO Handle why request failed
			return false;
		}
	}
	
	protected void requestAuthorization() throws BadStateException {
		switch(state) {
		case INIT:
			break;
		default:
			throw new BadStateException();
		}
		
		String message = String.format("%s;%s;%s;%s",
				Agency.getLocalAgencyId(), entity.getClass().getName(),
				entity.getPath(), entity.getId());

		write(HEADER_REQUEST, message);
		
		state = State.AUTHORIZATION_REQUEST_SENT;
	}

	protected void sendData(MigrationData data) throws BadStateException {
		switch(state) {
		case GOT_AUTHORIZATION:
			break;
		default:
			throw new BadStateException();
		}
		
		CodingMethod coding = CodingMethod.HEXABYTES;
		String encodedData = ObjectCoder.encode(coding, data);
		String message = String.format("%s;%s", coding, encodedData);

		write(HEADER_SEND, message);

		migrationSucceed = new Future();
		
		state = State.DATA_SENT;
	}
	
	protected void handle(int req, String[] data) {
		Response r = Response.valueOf(data[0]);

		switch (req) {
		case HEADER_REQUEST_RESPONSE:
			switch (r) {
			case MIGRATION_ACCEPTED:
				migrationAccepted.init(Boolean.TRUE);
				state = State.GOT_AUTHORIZATION;
				break;
			case MIGRATION_REJECTED:
				migrationAccepted.init(Boolean.FALSE);
				state = State.REQUEST_REJECTED;
				break;
			default:
				migrationAccepted.init(Boolean.FALSE);
				state = State.ERROR;
			}
			
			break;
		case HEADER_SEND_RESPONSE:
			switch (r) {
			case MIGRATION_SUCCEED:
				migrationSucceed.init(Boolean.TRUE);
				state = State.MIGRATION_DONE;
				break;
			case MIGRATION_FAILED:
				migrationSucceed.init(Boolean.FALSE);
				state = State.MIGRATION_FAILED;
				break;
			default:
				state = State.ERROR;
			}

			close();

			break;
		default:
		}
	}
}
