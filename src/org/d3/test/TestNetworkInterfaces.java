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

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

public class TestNetworkInterfaces {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Enumeration<NetworkInterface> netifs = NetworkInterface
				.getNetworkInterfaces();

		while (netifs.hasMoreElements()) {
			NetworkInterface netif = netifs.nextElement();

			System.out.printf("Interface \"%s\"%n-------------------%n",
					netif.getDisplayName());

			Enumeration<InetAddress> addresses = netif.getInetAddresses();
			
			System.out.printf(" InetAddress :%n");
			while(addresses.hasMoreElements()) {
				InetAddress address = addresses.nextElement();
				System.out.printf(" - %s%n", address);
			}
			
			List<InterfaceAddress> ifaddress = netif.getInterfaceAddresses();
			
			System.out.printf(" InterfaceAddress :%n");
			for(int i=0; i<ifaddress.size(); i++)
				System.out.printf(" - %s%n", ifaddress.get(i));
			
			System.out.printf("%n");
		}
	}

}
