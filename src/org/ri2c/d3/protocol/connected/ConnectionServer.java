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
package org.ri2c.d3.protocol.connected;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ConnectionServer
	implements Runnable
{
	ServerSocketChannel socketChannel;
	ConnectionManager	connectionManager;
	ConnectionFactory	connectionFactory;
	
	public ConnectionServer( ConnectionManager connectionManager, 
			ConnectionFactory connectionFactory, int port )
	{
		try
		{
			socketChannel 		= ServerSocketChannel.open();
			socketChannel.socket().bind( new InetSocketAddress(port) );
			
			this.connectionManager 	= connectionManager;
			this.connectionFactory	= connectionFactory;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		while( true )
		{
			try
			{
				SocketChannel channel = socketChannel.accept();
				connectionManager.register( connectionFactory.createConnection(channel) );
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
