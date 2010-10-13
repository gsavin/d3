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
package org.ri2c.d3.tools;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Args;

public class StartD3 {
	public static void d3Loop() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {

			}
		}
	}

	public static void init(String[] mainArgs) {
		String cfg = System.getProperty("org.ri2c.d3.config",
				"org/ri2c/d3/resources/default.cfg");
		
		Args args = Args.processFile(cfg);
		Args.parseArgs(args, mainArgs);

		Agency.enableAgency(args);
	}

	public static void main(String[] mainArgs) {
		init(mainArgs);
		d3Loop();
	}
}
