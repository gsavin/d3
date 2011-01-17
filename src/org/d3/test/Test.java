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

import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URI;
import java.nio.charset.Charset;

import org.d3.Console;
import org.d3.actor.Agency.Argument;
import org.d3.protocol.BadProtocolException;
import org.d3.protocol.Protocols;

public class Test {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		/*
		 * SecureRandom random = new SecureRandom(); String agencyId;
		 * 
		 * agencyId = String.format("%x%x", System.nanoTime(),
		 * random.nextLong());
		 * 
		 * String uriString = "//host/object/path"; URI uri = new
		 * URI(uriString);
		 * 
		 * System.out.printf("> scheme=\"%s\", host=\"%s\", path=\"%s\"%n",
		 * uri.getScheme(), uri.getHost(), uri.getPath());
		 * System.out.println(Charset.defaultCharset().name());
		 */

		long m1, m2;
		String uri = "scheme://host:port/path/id";
		int size = 100000;

		m1 = System.nanoTime();
		for (int i = 0; i < size; i++)
			//new URI(uri);
			new URI("scheme", null, "host", 1, "/path", null, null);
		
		m2 = System.nanoTime();

		System.out.printf("> average : %d ns%n", (m2 - m1) / size);
	}

}
