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
package org.d3.test;

import org.d3.Args;
import org.d3.actor.Agency;

public class TestDiscovery {

	/**
	 * @param args
	 */
	public static void main(String[] strings) {
		String ifname = strings == null || strings.length < 1 ? "eth0"
				: strings[0];
		
		String[] toparse = { "system.net.interface = " + ifname,
				"protocols = @Discovery()",
				"protocols.discovery.interface = " + ifname
		};

		Args args = Args.parseArgs(toparse);
		Agency.enableAgency(args);
		d3Loop();
	}

	public static void d3Loop() {
		try {
			Thread.sleep(100000);
			Agency.getLocalAgency().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
