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
package org.d3.request;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.d3.Agency;
import org.d3.Args;
import org.d3.Console;
import org.d3.IdentifiableObject;
import org.d3.Request;
import org.d3.agency.AgencyListener;

//import static org.d3.IdentifiableObject.Tools.handleRequest;

public class RequestService {
	private class RequestCommand implements Runnable {
		Request request;

		public RequestCommand(Request r) {
			request = r;
		}

		public void run() {
			handleRequest(request);
		}
	}

	private class RequestServiceThreadFactory implements ThreadFactory {
		long idGenerator = 0;

		public Thread newThread(Runnable r) {
			return new Thread(requestServiceThreadGroup, r, String.format(
					"request-service-thread%X", idGenerator++));
		}
	}

	ThreadGroup requestServiceThreadGroup;
	ThreadPoolExecutor pool;
	ThreadFactory threadFactory;
	Collection<AgencyListener> listeners;

	public RequestService() {
		requestServiceThreadGroup = new ThreadGroup("request-service");
		threadFactory = new RequestServiceThreadFactory();
	}

	public void init(Collection<AgencyListener> listeners, Args args) {
		int coreSize, maxSize;
		long keepTime;
		TimeUnit unit = TimeUnit.MILLISECONDS;

		if (args.has("core_size"))
			coreSize = Integer.parseInt(args.get("core_size"));
		else
			coreSize = 5;

		if (args.has("max_size"))
			maxSize = Integer.parseInt(args.get("max_size"));
		else
			maxSize = 15;

		if (args.has("keep_time")) {
			String s = args.get("keep_time").trim();

			if (s.matches("^\\d+$"))
				keepTime = Long.parseLong(args.get("keep_time"));
			else if (s
					.matches("\\d+ (DAYS|HOURS|MINUTES|SECONDS|MILLISECONDS|MICROSECONDS|NANOSECONDS)")) {
				TimeUnit localUnit = TimeUnit.valueOf(s.substring(
						s.indexOf(' ') + 1).trim());
				keepTime = unit.convert(
						Long.parseLong(s.substring(0, s.indexOf(' '))),
						localUnit);
			} else
				keepTime = 200;
		} else
			keepTime = 200;

		this.listeners = listeners;
		this.pool = new ThreadPoolExecutor(coreSize, maxSize, keepTime, unit,
				new LinkedBlockingQueue<Runnable>(), threadFactory);
	}

	public void executeRequest(Request r) {
		IdentifiableObject source = Agency.getLocalAgency()
				.getIdentifiableObject(r.getSourceURI());

		IdentifiableObject target = Agency.getLocalAgency()
				.getIdentifiableObject(r.getTargetURI());

		if (target == null) {
			Console.warning("target is null, skipping request");
		} else {
			switch (r.getTargetType()) {
			case entity:
				Agency.getLocalAgency().getAtlas().entityCall(r);
				break;
			default:
				pool.execute(new RequestCommand(r));
			}

			switch (r.getTargetType()) {
			case entity:
			case agency:
			case protocol:
			case application:
				for (AgencyListener l : listeners)
					l.requestReceived(source, target, r.getCallable());
				break;
			}
		}
	}
}
