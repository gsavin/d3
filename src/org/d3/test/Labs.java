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
package org.d3.test;

//import org.ri2c.d3.Atlas;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.d3.annotation.IdentifiableObjectPath;
import org.d3.atlas.internal.D3Atlas;
import org.d3.request.ObjectCoder;

public class Labs {

	@IdentifiableObjectPath("/base/interface")
	public static interface BaseInterface {

	}

	// @IdentifiableObjectPath("/base")
	public static class BaseClass implements BaseInterface {

	}

	// @IdentifiableObjectPath("/base/daughter/interface/A")
	public static interface DaughterInterfaceA {

	}

	// @IdentifiableObjectPath("/base/daughter/interface/B")
	public static interface DaughterInterfaceB {

	}

	// @IdentifiableObjectPath("/base/daughter")
	public static class Daughter extends BaseClass implements
			DaughterInterfaceB, DaughterInterfaceA {

	}

	/**
	 * @param args
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws Exception {
		Pattern p = Pattern.compile("/([^/]+)");

		long m1 = System.currentTimeMillis();

		for (int i = 0; i < 100000; i++) {
			String path = "/d3/features/discover/discover0";
			Matcher m = p.matcher(path);

			String current;

			if (m.find()) {
				do {
					current = m.group(1);
					if (!m.find()) {
						// System.out.printf("id is \"%s\"%n",current);
						break;
					} else {
						// System.out.printf("subpath is \"%s\"%n",current);
					}
				} while (true);
			}
		}
		
		long m2 = System.currentTimeMillis();
		
		for( int i =0; i< 100000; i++ )  {
			String path = "/d3/features/discover/discover0";
			String[] subpath = path.split("/");
		}
		
		long m3 = System.currentTimeMillis();
		
		System.out.printf("%dms%n%dms%n",m2-m1,m3-m2);
	}

}
