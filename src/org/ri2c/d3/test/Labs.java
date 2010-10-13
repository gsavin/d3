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
package org.ri2c.d3.test;

//import org.ri2c.d3.Atlas;
import java.security.MessageDigest;

import org.ri2c.d3.annotation.IdentifiableObjectPath;
import org.ri2c.d3.atlas.internal.D3Atlas;
import org.ri2c.d3.request.ObjectCoder;

public class Labs {

	@IdentifiableObjectPath("/base/interface")
	public static interface BaseInterface {
		
	}
	
	//@IdentifiableObjectPath("/base")
	public static class BaseClass implements BaseInterface {
		
	}
	
	//@IdentifiableObjectPath("/base/daughter/interface/A")
	public static interface DaughterInterfaceA {
		
	}
	
	//@IdentifiableObjectPath("/base/daughter/interface/B")
	public static interface DaughterInterfaceB {
		
	}
	
	//@IdentifiableObjectPath("/base/daughter")
	public static class Daughter extends BaseClass implements DaughterInterfaceB, DaughterInterfaceA {
		
	}
	/**
	 * @param args
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws Exception {
		IdentifiableObjectPath path = null;
		;
		Class<?> cls = Daughter.class;

		while (cls != Object.class && path == null) {
			path = cls.getAnnotation(IdentifiableObjectPath.class);

			if (path == null) {
				for (Class<?> i : cls.getInterfaces()) {
					path = i.getAnnotation(IdentifiableObjectPath.class);
					if (path != null)
						break;
				}
			}
			
			cls = cls.getSuperclass();
		}

		if (path != null) {
			System.out.printf("path is \"%s\"%n", path.value());
		} else {
			System.out.printf("no path found%n");
		}
		
		MessageDigest md = MessageDigest.getInstance("SHA");
		md.update("Ceci est un test".getBytes());
		String sha1 = ObjectCoder.byte2hexa(md.digest());
		System.out.printf("sha: %s%n",sha1);
		md.update("Ceci est un tes".getBytes());
		sha1 = ObjectCoder.byte2hexa(md.digest());
		System.out.printf("sha: %s%n",sha1);
		md.update("Ceci est un tes".getBytes());
		md.update("Ceci est un test".getBytes());
		sha1 = ObjectCoder.byte2hexa(md.digest());
		System.out.printf("sha: %s%n",sha1);
	}

}
