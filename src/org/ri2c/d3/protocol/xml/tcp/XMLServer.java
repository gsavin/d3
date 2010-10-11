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
package org.ri2c.d3.protocol.xml.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import org.ri2c.d3.Agency;
import org.ri2c.d3.protocol.XMLProtocol;

public class XMLServer
	implements Runnable
{
	private static XMLServer instance;
	
	public static XMLServer getDefault()
	{
		if( instance == null )
			instance = new XMLServer();
		
		return instance;
	}
	
	private ServerSocketChannel serverSocketChannel;
	private ReentrantLock		communicationCreationPending;
	
	private XMLServer()
	{
		communicationCreationPending = new ReentrantLock();
		
		try
		{
			InetSocketAddress isa = new InetSocketAddress( XMLProtocol.XML_PROTOCOL_PORT );
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().bind(isa);
			
			Thread t = new Thread(this,"xml-server");
			t.setDaemon(true);
			t.start();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
	
	public ReentrantLock getCommunicationCreationPendingLock()
	{
		return communicationCreationPending;
	}
	
	public void run()
	{
		System.out.printf("[xml-server] running%n");
		
		while( true )
		{
			try
			{
				SocketChannel channel = serverSocketChannel.accept();
				communicationCreationPending.lock();
				
				System.out.printf("[xml-server] new channel opened%n");
				
				if( ! channel.finishConnect() )
				{
					channel.close();
				}
				else
				{
					SocketAddress remote = channel.socket().getRemoteSocketAddress();
					
					if( ! ( remote instanceof InetSocketAddress ) )
						throw new IOException("unknown socket type");
					
					InetAddress remoteAddress = ((InetSocketAddress) remote).getAddress();

					String remoteId = Agency.getLocalAgency().getIpTables().getId(remoteAddress);
					
					if( remoteId != null )
					{
						System.out.printf("[xml-server] from %s%n",((InetSocketAddress) remote).getAddress().getHostAddress());

						XMLCommunication xmlc = new XMLCommunication(remoteId,channel,channel);
						XMLProtocol.getDefault().registerNewXMLCommunication(remoteId,xmlc);
					}
					else System.err.printf("[xml-server] unknown address: %s%n",remoteAddress);
				}
				
				communicationCreationPending.unlock();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
	}
}
