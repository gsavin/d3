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
import org.ri2c.d3.annotation.IdentifiableObjectPath;
import org.ri2c.d3.atlas.internal.D3Atlas;

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
	public static void main(String[] args) {
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
	}

}
