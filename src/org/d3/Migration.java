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

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import org.d3.actor.LocalActor;
import org.d3.agency.RemoteAgency;
import org.d3.annotation.ActorPath;
import org.d3.annotation.RequestCallable;
//import org.d3.atlas.internal.Body;
import org.d3.migration.BadMigrationSideException;
import org.d3.migration.MigrationData;
import org.d3.protocol.Protocols;

@ActorPath("/migrations")
public class Migration extends LocalActor {
	public static enum MigrationStatus {
		ERROR, PENDING, TRANSFERING, TRANSFERED, SUCCESS, CANCELED, REJECTED
	}

	public static enum MigrationSide {
		SENDER, RECEIVER
	}

	private static long migrationIdGenerator = 0;

	private static String newMigrationId() {
		return String.format("migration_%016x%016x", System.nanoTime(),
				migrationIdGenerator++);
	}

	// protected final String id;
	protected final MigrationSide side;
	protected final MigrationData data;
	protected final URI sender;
	protected final RemoteAgency receiver;
	protected final AtomicReference<MigrationStatus> status;
	protected final Body body;

	public Migration(URI sender, Body body) {
		super(newMigrationId());

		this.side = MigrationSide.RECEIVER;
		this.data = null;
		this.sender = sender;
		this.receiver = null;
		this.body = body;
		this.status = new AtomicReference<MigrationStatus>(
				MigrationStatus.PENDING);

		init();
	}

	public Migration(RemoteAgency remote, MigrationData data) {
		super(newMigrationId());

		this.side = MigrationSide.SENDER;
		this.data = data;
		this.sender = null;
		this.receiver = remote;
		this.body = null;
		this.status = new AtomicReference<MigrationStatus>(
				MigrationStatus.PENDING);

		init();
	}

	public final IdentifiableType getType() {
		return IdentifiableType.migration;
	}

	public void init() {
		register();

		switch (side) {
		case SENDER: {
			Request r = new Request(this, receiver.getRemoteAtlas(), "host",
					new Object[] { getURI(), data.getEntity().getFullPath(),
							data.getEntity().getClass().getName() });

			Protocols.sendRequest(r);

			break;
		}
		case RECEIVER: {
			Actor target;

			try {
				target = Agency.getLocalAgency().getIdentifiableObject(sender);

				Request r = new Request(this, target, "transfer",
						new Object[] { getURI() });
				Protocols.sendRequest(r);

				setStatus(MigrationStatus.TRANSFERING);
			} catch (ActorNotFoundException e) {
				// TODO
				e.printStackTrace();
			}

			break;
		}
		}
	}

	@RequestCallable("transfer")
	public void transfer(URI destination) {
		if (side == MigrationSide.RECEIVER)
			throw new BadMigrationSideException();

		setStatus(MigrationStatus.TRANSFERING);

		try {
			Actor target = Agency.getLocalAgency()
					.getIdentifiableObject(destination);

			Request r = new Request(this, target, "receive",
					new Object[] { data });
			Protocols.sendRequest(r);

			setStatus(MigrationStatus.TRANSFERED);
		} catch (ActorNotFoundException e) {
			// TODO
			e.printStackTrace();
		}
	}

	@RequestCallable("receive")
	public void receive(MigrationData data) {
		if (side == MigrationSide.SENDER)
			throw new BadMigrationSideException();

		data.getEntity().register();
		body.receiveContent(data.getEntity(), data.getRequests());

		try {
			Actor target = Agency.getLocalAgency()
					.getIdentifiableObject(sender);

			Request r = new Request(this, target, "confirm",
					new Object[] { MigrationStatus.SUCCESS });
			Protocols.sendRequest(r);

			unregister();
			setStatus(MigrationStatus.SUCCESS);
		} catch (ActorNotFoundException e) {
			// TODO
			e.printStackTrace();
		}
	}

	@RequestCallable("confirm")
	public void confirm(MigrationStatus status) {
		if (side == MigrationSide.RECEIVER)
			throw new BadMigrationSideException();

		unregister();
		setStatus(status == null ? MigrationStatus.ERROR : status);
	}

	@RequestCallable("cancel")
	public void cancel() {
		// TODO
		setStatus(MigrationStatus.CANCELED);
	}

	@RequestCallable("reject")
	public void reject() {
		// TODO
		setStatus(MigrationStatus.REJECTED);
	}

	public MigrationStatus waitEndOfMigration() {
		boolean end = false;

		do {
			switch (status.get()) {
			case CANCELED:
			case ERROR:
			case REJECTED:
			case SUCCESS:
				end = true;
				break;
			default:
				end = false;
			}

			if (!end) {
				synchronized (status) {
					try {
						status.wait(200);
					} catch (InterruptedException e) {
					}
				}
			}
		} while (!end);

		return status.get();
	}

	protected void setStatus(MigrationStatus s) {
		synchronized (status) {
			status.set(s);
			status.notifyAll();
		}
	}
}
