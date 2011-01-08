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

import org.d3.actor.LocalActor;
import org.d3.agency.AgencyListener;
import org.d3.agency.RemoteAgency;

public abstract class Application extends LocalActor implements
		AgencyListener {

	protected Application(String name) {
		super(name);
	}

	public final IdentifiableType getType() {
		return IdentifiableType.application;
	}

	public void handleRequest(Actor source,
			Actor target, Request r) {
		// TODO Auto-generated method stub

	}

	public void agencyExit(Agency agency) {
		// TODO Auto-generated method stub

	}

	public void identifiableObjectRegistered(Actor idObject) {
		// TODO Auto-generated method stub

	}

	public void identifiableObjectUnregistered(Actor idObject) {
		// TODO Auto-generated method stub

	}

	public void newAgencyRegistered(RemoteAgency rad) {
		// TODO Auto-generated method stub

	}

	public void remoteAgencyDescriptionUpdated(RemoteAgency rad) {
		// TODO Auto-generated method stub

	}

	public void requestReceived(Actor source,
			Actor target, String name) {
		// TODO Auto-generated method stub

	}

	public abstract void init();

	public abstract void execute();
}
