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

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.d3.actor.Agency;
import org.d3.actor.Call;
import org.d3.actor.Future;
import org.d3.protocol.RemoteFuture;
import org.d3.remote.RemoteAgency;
import org.d3.remote.UnknownAgencyException;

public class CallData {

	URI source;
	URI target;
	String name;
	Object[] args;
	URI future;
	long delay;
	TimeUnit unit;

	public CallData(Call c) {
		source = c.getSource().getURI();
		target = c.getTarget().getURI();
		name = c.getName();
		args = c.getArgs();
		delay = c.getDelay(TimeUnit.NANOSECONDS);
		unit = TimeUnit.NANOSECONDS;
		future = c.getFuture().getURI();
	}

	public String getName() {
		return name;
	}

	public long getDelay() {
		return delay;
	}

	public TimeUnit getTimeUnit() {
		return unit;
	}

	public Object[] getArgs() {
		return args;
	}

	public Future getFuture() throws UnknownAgencyException {
		String agencyId, futureId;
		String path = future.getPath();

		agencyId = path.substring(1, path.indexOf('/', 1));
		futureId = path.substring(path.indexOf('/', 1)+1);

		if (Agency.getLocalAgencyId().equals(agencyId)) {
			Future f = Agency.getLocalAgency().getProtocols().getFutures()
					.get(futureId);

			if (f != null)
				return f;
		}

		RemoteAgency ra = Agency.getLocalAgency().getRemoteHosts()
				.getRemoteAgency(agencyId);

		return new RemoteFuture(ra, futureId);
	}
	
	public URI getSourceURI() {
		return source;
	}
	
	public URI getTargetURI() {
		return target;
	}
}
