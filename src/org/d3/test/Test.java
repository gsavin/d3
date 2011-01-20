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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.Enumeration;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
		
		while(ifs.hasMoreElements()) {
			NetworkInterface ni = ifs.nextElement();
			System.out.printf("%s%n", ni.getDisplayName());
			Enumeration<InetAddress> addresses = ni.getInetAddresses();
			while(addresses.hasMoreElements()) {
				InetAddress inet = addresses.nextElement();
				System.out.printf("- %s%n", inet.getHostAddress());
			}
		}
		
		String t = "xml://host:10000";
		URI uri = new URI(t);
		System.out.printf("-----%n%s%n------%n",uri);
	}
}
