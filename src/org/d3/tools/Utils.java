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
package org.d3.tools;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.d3.HostAddress;
import org.d3.actor.Agency;
import org.d3.remote.HostNotFoundException;
import org.d3.remote.NoRemotePortAvailableException;
import org.d3.remote.RemoteAgency;
import org.d3.remote.RemoteHost;
import org.d3.remote.RemotePort;
import org.d3.remote.UnknownAgencyException;

public class Utils {
	public static RemotePort getRandomRemotePortFromRemoteAgency(
			InetAddress address, String agencyId) throws HostNotFoundException,
			UnknownAgencyException, NoRemotePortAvailableException {
		RemoteHost rh = Agency.getLocalAgency().getRemoteHosts().get(
				HostAddress.getByInetAddress(address));
		RemoteAgency ra = rh.getRemoteAgency(agencyId);
		RemotePort rp = ra.getRandomRemotePort();

		return rp;
	}

	public static InetAddress getAddressForInterface(String ifname)
			throws SocketException {
		boolean inet6 = false;

		if (Agency.getArgs().has("system.net.inet6")
				&& Agency.getArgs().getBoolean("system.net.inet6"))
			inet6 = true;

		return getAddressForInterface(ifname, inet6);
	}

	public static InetAddress getAddressForInterface(String ifname,
			boolean inet6) throws SocketException {
		NetworkInterface networkInterface = NetworkInterface.getByName(ifname);
		return getAddressForInterface(networkInterface, inet6);
	}

	public static InetAddress getAddressForInterface(
			NetworkInterface networkInterface, boolean inet6)
			throws SocketException {
		if (networkInterface == null) {
			Enumeration<NetworkInterface> nie = NetworkInterface
					.getNetworkInterfaces();

			while (nie.hasMoreElements()) {
				NetworkInterface tmp = nie.nextElement();

				if (networkInterface == null)
					networkInterface = tmp;
				else if (networkInterface.isLoopback())
					networkInterface = tmp;
			}
		}

		InetAddress local = null;
		Enumeration<InetAddress> enu = networkInterface.getInetAddresses();

		while (enu.hasMoreElements()) {
			InetAddress tmp = enu.nextElement();
			int l = tmp.getAddress().length;

			if (local == null && ((inet6 && l == 16) || (!inet6 && l == 4)))
				local = tmp;
		}

		return local;
	}

	public static InetSocketAddress createSocketAddress(String ifname,
			int port, boolean inet6) throws SocketException {
		InetAddress inet = getAddressForInterface(ifname, inet6);
		return new InetSocketAddress(inet, port);
	}

	public static InetSocketAddress createSocketAddress(
			NetworkInterface networkInterface, int port, boolean inet6)
			throws SocketException {
		InetAddress inet = getAddressForInterface(networkInterface, inet6);
		return new InetSocketAddress(inet, port);
	}
}
