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
package org.ri2c.d3.request;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Args;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.RemoteIdentifiableObject;
import org.ri2c.d3.Request;
import org.ri2c.d3.IdentifiableObject.IdentifiableType;
import org.ri2c.d3.agency.AgencyListener;
import org.ri2c.d3.request.ObjectCoder.CodingMethod;

import static org.ri2c.d3.IdentifiableObject.Tools.call;

public class RequestService {
	private class RequestCommand implements Runnable {
		Request request;

		public RequestCommand(Request r) {
			request = r;
		}

		public void run() {
			/*
			 * IdentifiableObject source = null; IdentifiableObject target =
			 * null; IdentifiableObject handler = null;
			 * 
			 * String sourceId = request.getAttribute("source-id"); String
			 * sourceType = request.getAttribute("source-type"); String targetId
			 * = request.getAttribute("target-id"); String targetType =
			 * request.getAttribute("target-type");
			 * 
			 * if( ! Agency.getLocalAgency().getId().equals(request.getSource())
			 * ) { source = new RemoteIdentifiableObject(request.getSource(),
			 * sourceId,IdentifiableType.valueOf(sourceType)); } else { source =
			 * Agency.getLocalAgency().getIdentifiableObject(
			 * IdentifiableType.valueOf(sourceType),sourceId);
			 * 
			 * if( source == null ) {
			 * System.err.printf("[request-service] error, unknown source: %s/%s%n"
			 * , sourceId, sourceType); return; } }
			 * 
			 * target = Agency.getLocalAgency().getIdentifiableObject(
			 * IdentifiableType.valueOf(targetType),targetId);
			 * 
			 * if( target == null ) {
			 * //System.out.printf("[requests] (%s) %s/%s --> %s/%s%n",
			 * request.getName(), sourceId, sourceType, targetId, targetType );
			 * System.err.printf("[request-service] unknown target: %s%n",
			 * targetId); return; }
			 * 
			 * if( handlers.containsKey(request.getName()) ) handler =
			 * handlers.get(request.getName()); else handler = target;
			 * 
			 * if( handler != null ) { for( AgencyListener l: listeners )
			 * l.requestReceived(source, target, request.getName());
			 * 
			 * handler.handleRequest(source,target,request); } else {
			 * System.err.
			 * printf("[request-service] no handler to %s%n",request.getName());
			 * }
			 */

			//IdentifiableObject source = Agency.getLocalAgency()
			//		.getIdentifiableObject(request.getSourceURI());

			IdentifiableObject target = Agency.getLocalAgency()
					.getIdentifiableObject(request.getTargetURI());

			if (request.targetQueryContains(Request.CALLABLE)) {
				String callableName = request
						.getTargetQueryArgument(Request.CALLABLE);
				Object[] args = null;

				if (request.targetQueryContains(Request.ARGUMENTS)) {
					CodingMethod method = CodingMethod.valueOf(request
							.getTargetQueryArgument(Request.DATA_ENCODING));
					Object d = ObjectCoder.decode(method,
							request.getTargetQueryArgument(Request.ARGUMENTS));
					
					if( d.getClass().isArray()) {
						args = (Object[]) d;
					} else {
						args = new Object[] {d};
					}
				}

				call(target, callableName, args);
			}
		}
	}

	private class RequestServiceThreadFactory implements ThreadFactory {
		long idGenerator = 0;

		public Thread newThread(Runnable r) {
			return new Thread(requestServiceThreadGroup, r, String.format(
					"request-service-thread%X", idGenerator++));
		}
	}

	// RequestInterpreter interpreter;
	ThreadGroup requestServiceThreadGroup;
	ThreadPoolExecutor pool;
	ThreadFactory threadFactory;
	Collection<AgencyListener> listeners;
	Map<String, IdentifiableObject> handlers;

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

		this.handlers = new ConcurrentHashMap<String, IdentifiableObject>();
		this.listeners = listeners;
		this.pool = new ThreadPoolExecutor(coreSize, maxSize, keepTime, unit,
				new LinkedBlockingQueue<Runnable>(), threadFactory);
	}

	public void executeRequest(Request r) {
		switch(r.getTargetType()) {
		case entity:
			Agency.getLocalAgency().getAtlas().entityCall(r);
			break;
		default:
			pool.execute(new RequestCommand(r));
		}
	}

	public void interceptRequest(IdentifiableObject handler, String name) {
		if (!handlers.containsKey(name))
			handlers.put(name, handler);
		else
			System.err
					.printf("[request-service] conflict between handler for %s%n",
							name);
	}
}
