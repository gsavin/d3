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
package org.d3;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.d3.tools.Cache;
import org.d3.tools.CacheCreationException;

public class HostAddress {

	public static HostAddress getByName(String host)
			throws UnknownHostException {
		try {
			InetAddress address = ADDRESS_CACHE.get(host);
			return getByInetAddress(address);
		} catch (CacheCreationException e) {
			throw new UnknownHostException(host);
		}
	}

	public static HostAddress getByInetAddress(InetAddress address) {
		try {
			return HOST_CACHE.get(address);
		} catch (CacheCreationException e) {
			return null;
		}
	}

	private final InetAddress address;

	public HostAddress(InetAddress address) {
		if (address == null)
			throw new NullPointerException();

		this.address = address;
	}

	public String getHost() {
		String format = isInet6() ? "[%s]" : "%s";
		String host = address.getHostAddress();

		return String.format(format, host);
	}

	public boolean isInet6() {
		return address instanceof Inet6Address;
	}

	public InetAddress asInetAddress() {
		return address;
	}

	public String toString() {
		return getHost();
	}

	public int hashCode() {
		return address.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj instanceof HostAddress)
			return address.equals(((HostAddress) obj).address);
		return false;
	}

	public boolean isLocal() {
		return address.isAnyLocalAddress() || address.isLoopbackAddress()
				|| address.isSiteLocalAddress() || address.isLinkLocalAddress();
	}

	private static class AddressCache extends Cache<String, InetAddress> {
		AddressCache(int capacity) {
			super(capacity);
		}

		protected InetAddress createObject(String key)
				throws CacheCreationException {
			try {
				return InetAddress.getByName(key);
			} catch (Exception e) {
				throw new CacheCreationException(e);
			}
		}
	}

	private static AddressCache ADDRESS_CACHE = new AddressCache(1000);

	private static class HostCache extends Cache<InetAddress, HostAddress> {
		HostCache(int capacity) {
			super(capacity);
		}

		protected HostAddress createObject(InetAddress key)
				throws CacheCreationException {
			return new HostAddress(key);
		}
	}

	private static HostCache HOST_CACHE = new HostCache(1000);
}
