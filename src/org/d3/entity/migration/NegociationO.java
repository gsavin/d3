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

import org.d3.Console;
import org.d3.actor.Agency;
import org.d3.actor.Entity;
import org.d3.entity.EntityThread;
import org.d3.protocol.request.ObjectCoder;
import org.d3.protocol.request.ObjectCoder.CodingMethod;
import org.d3.tools.AtomicState;

class NegociationO extends Negociation {

	public static enum State {
		INIT, AUTHORIZATION_REQUEST_SENT, GOT_AUTHORIZATION, REQUEST_REJECTED, DATA_SENT, MIGRATION_DONE, MIGRATION_FAILED, ERROR
	}

	AtomicState<State> stateRef;
	Entity entity;
	EntityThread thread;

	NegociationO(SocketChannel channel, EntityThread thread,
			InetSocketAddress address) {
		super(channel, address);
		this.entity = thread.getOwnerAsEntity();
		this.thread = thread;
		stateRef = new AtomicState<State>(State.class, State.INIT);
	}

	public boolean isAuthorized() throws BadStateException {
		try {
			stateRef.waitForState(State.GOT_AUTHORIZATION);
		} catch (InterruptedException e) {
			// TODO Handle this exception
		}

		switch (stateRef.get()) {
		case DATA_SENT:
		case MIGRATION_DONE:
		case MIGRATION_FAILED:
		case GOT_AUTHORIZATION:
			return true;
		case REQUEST_REJECTED:
			return false;
		default:
			throw new BadStateException(stateRef.get().name());
		}
	}

	public boolean isMigrationDoneSuccessfully() throws BadStateException {
		try {
			stateRef.waitForState(State.MIGRATION_DONE);
		} catch (InterruptedException e) {
			// TODO Handle this exception
		}

		switch (stateRef.get()) {
		case MIGRATION_DONE:
			return true;
		case ERROR:
		case MIGRATION_FAILED:
			return false;
		default:
			throw new BadStateException();
		}
	}

	protected void requestAuthorization() throws BadStateException {
		switch (stateRef.get()) {
		case INIT:
			break;
		default:
			throw new BadStateException();
		}

		String message = String.format("%s;%s;%s;%s",
				Agency.getLocalAgencyId(), entity.getClass().getName(),
				entity.getPath(), entity.getId());

		write(HEADER_REQUEST, message);

		stateRef.set(State.AUTHORIZATION_REQUEST_SENT);
	}

	protected void sendData(MigrationData data) throws BadStateException {
		switch (stateRef.get()) {
		case GOT_AUTHORIZATION:
			break;
		default:
			throw new BadStateException();
		}

		CodingMethod coding = CodingMethod.HEXABYTES;
		String encodedData = ObjectCoder.encode(coding, data);
		String message = String.format("%s;%s", coding, encodedData);

		write(HEADER_SEND, message);
		stateRef.set(State.DATA_SENT);
	}

	protected void handle(int req, String[] data) {
		Response r = Response.valueOf(data[0]);

		switch (req) {
		case HEADER_REQUEST_RESPONSE:
			switch (r) {
			case MIGRATION_ACCEPTED:
				stateRef.set(State.GOT_AUTHORIZATION);
				break;
			case MIGRATION_REJECTED:
				stateRef.set(State.REQUEST_REJECTED);
				break;
			default:
				Console.error("error#1");
				stateRef.set(State.ERROR);
			}

			break;
		case HEADER_SEND_RESPONSE:
			switch (r) {
			case MIGRATION_SUCCEED:
				stateRef.set(State.MIGRATION_DONE);
				break;
			case MIGRATION_FAILED:
				stateRef.set(State.MIGRATION_FAILED);
				break;
			default:
				Console.error("error#2");
				stateRef.set(State.ERROR);
			}

			close();

			break;
		default:
		}
	}

	protected void close() {
		super.close();

		switch (stateRef.get()) {
		case INIT:
			break;
		case AUTHORIZATION_REQUEST_SENT:
		case DATA_SENT:
			Exception e = new Exception();
			e.printStackTrace();
			Console.error("error#3");
			stateRef.set(State.ERROR);
			break;
		}
	}
}
