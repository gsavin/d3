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
package org.d3.feature;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.d3.Args;
import org.d3.Console;
import org.d3.actor.Agency;
import org.d3.actor.CallException;
import org.d3.actor.Feature;
import org.d3.actor.Future;
import org.d3.actor.StepActor;
import org.d3.annotation.ActorPath;
import org.d3.remote.HostNotFoundException;
import org.d3.remote.RemoteAgency;
import org.d3.remote.RemoteHost;
import org.d3.remote.UnknownAgencyException;
import org.d3.tools.Time;

@ActorPath("/features/sibler")
public class Sibler extends Feature implements StepActor {

	File container;
	File description;
	Time delay;
	String digest;
	HashSet<String> knownAgencies;

	public Sibler() {
		this("default");
	}

	public Sibler(String id) {
		super(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.d3.actor.Feature#initFeature()
	 */
	public void initFeature() {
		Args args = Agency.getActorArgs(this);
		String path;

		knownAgencies = new HashSet<String>();
		path = args.get("path", "agencies");
		delay = args.getTime("delay");
		digest = "";
		container = new File(path);

		if (delay == null)
			delay = new Time(1, TimeUnit.SECONDS);

		description = new File(path + File.separator
				+ Agency.getLocalAgencyId());

		File parent = new File(description.getParent());

		if (!parent.exists())
			parent.mkdirs();

		description.deleteOnExit();

		step();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.d3.actor.StepActor#getStepDelay(java.util.concurrent.TimeUnit)
	 */
	public long getStepDelay(TimeUnit unit) {
		return unit.convert(delay.time, delay.unit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.d3.actor.StepActor#step()
	 */
	public void step() {
		final Agency agency = Agency.getLocalAgency();

		if (!agency.getDigest().equals(digest)) {
			StringBuilder buffer = new StringBuilder();
			String protocols = agency.getProtocols().exportDescription();
			buffer.append("protocols = ").append(protocols).append("\n");
			buffer.append("digest = ").append(agency.getDigest()).append("\n");

			FileWriter out;

			try {
				out = new FileWriter(description);
				out.write(buffer.toString());
				out.flush();
				out.close();
			} catch (IOException e) {
				Agency.getFaultManager().handle(e, null);
			}

			digest = agency.getDigest();
		}

		File[] agencies = container.listFiles();
		HashSet<String> remaining = new HashSet<String>(knownAgencies);

		for (int i = 0; i < agencies.length; i++) {
			if (!agencies[i].getName().equals(agency.getId())) {
				String id, digest, protocols;

				id = agencies[i].getName();
				remaining.remove(id);

				try {
					FileReader in = new FileReader(agencies[i]);
					int r;
					char[] buffer = new char[256];
					StringBuilder content = new StringBuilder();

					while ((r = in.read(buffer)) > 0)
						content.append(buffer, 0, r);

					Args args = Args.parseArgs(content.toString().split("\n"));
					digest = args.get("digest");
					protocols = args.get("protocols");
				} catch (FileNotFoundException e2) {
					Console.warning("sibler not found for '%s'", id);
					continue;
				} catch (IOException e) {
					Console.warning("unable to read sibler of '%s'", id);
					continue;
				}

				RemoteHost host = null;

				try {
					host = Agency.getLocalAgency().getRemoteHosts().get(
							agency.getHost());
				} catch (HostNotFoundException e) {
					Future f = (Future) Agency.getLocalAgency()
							.call(Agency.CALLABLE_REGISTER_NEW_HOST,
									agency.getHost());

					try {
						f.waitForValue();
					} catch (InterruptedException ie) {
						if (!f.isAvailable())
							continue;
					}

					try {
						host = f.get();
					} catch (CallException e1) {
						Agency.getFaultManager().handle(e1, null);
						continue;
					}
				}

				RemoteAgency remote = null;

				try {
					remote = host.getRemoteAgency(id);
				} catch (UnknownAgencyException e) {
					Console.info("new agency found : %s\n", agencies[i]
							.getPath());

					Future f = (Future) Agency.getLocalAgency().call(
							Agency.CALLABLE_REGISTER_NEW_AGENCY, host, id);

					try {
						f.waitForValue();
					} catch (InterruptedException ie) {
						if (!f.isAvailable())
							continue;
					}

					if (!Thread.interrupted()) {
						try {
							remote = f.get();
						} catch (CallException e1) {
							Agency.getFaultManager().handle(e1, null);
							continue;
						}
					}
				}

				knownAgencies.add(id);

				if (!remote.getDigest().equals(digest)) {
					Console.info("agency digest updated '%s'", id);
					remote.update(digest, protocols);
				}
			}
		}

		for (String aid : remaining) {
			try {
				RemoteAgency remote = Agency.getLocalAgency().getRemoteHosts()
						.getRemoteAgency(aid);
				agency.call(Agency.CALLABLE_UNREGISTER_AGENCY, remote);
				Console.info("agency unregistered '%s'", aid);
			} catch (UnknownAgencyException e) {
				// Nothing to do
			}
		}
	}

}
