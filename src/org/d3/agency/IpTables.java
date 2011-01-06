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
package org.d3.agency;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IpTables
{
	ConcurrentHashMap<InetAddress,String> 	address2id;
	ConcurrentHashMap<String,InetAddress> 	id2address;
	ConcurrentLinkedQueue<InetAddress>		blacklist;
	ConcurrentHashMap<InetAddress,Integer>	errors;
	
	public IpTables()
	{
		address2id = new ConcurrentHashMap<InetAddress,String>();
		id2address = new ConcurrentHashMap<String,InetAddress>();
		blacklist  = new ConcurrentLinkedQueue<InetAddress>();
		errors	   = new ConcurrentHashMap<InetAddress,Integer>();
	}
	
	public void registerId( String id, String address )
	{
		try
		{
			InetAddress inet = InetAddress.getByName(address);
			address2id.put(inet,id);
			id2address.put(id,inet);
		}
		catch (UnknownHostException e) {
			System.err.printf("[iptables] unknown host: %s%n",address);
		}
	}
	
	public String getId( InetAddress inet )
	{
		return address2id.get(inet);
	}
	
	public InetAddress getAddress( String id )
	{
		return id2address.get(id);
	}
	
	public boolean isBlacklisted( String address )
	{
		try
		{
			InetAddress inet = InetAddress.getByName(address);
			return blacklist.contains(inet);
		}
		catch(Exception e )
		{
		
		}
		
		return false;
	}
	
	public boolean isBlacklisted( InetAddress inet )
	{
		return blacklist.contains(inet);
	}
	
	public void declareErrorOn( String address )
	{
		try
		{
			InetAddress inet = InetAddress.getByName(address);
			
			int current = 1;
			
			if( errors.containsKey(inet) )
				current += errors.get(inet);
			
			if( current > 5 )
				blacklist(inet);
			
			errors.put(inet,current);
		}
		catch( Exception e )
		{
			
		}
	}
	
	protected void blacklist( InetAddress inet )
	{
		System.out.printf("[iptables] blacklist %s%n",inet);
		blacklist.add(inet);
	}
}
