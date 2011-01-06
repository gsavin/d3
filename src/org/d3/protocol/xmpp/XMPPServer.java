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
package org.ri2c.d3.protocol.xmpp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.ri2c.l2d.protocol.xmpp.auth.XMPPTLSAuthentification;

public class XMPPServer
	implements Runnable
{
	class SessionHandler
		implements Runnable
	{
		XMPPSession session;
		
		public SessionHandler( XMPPSession session )
		{
			this.session = session;
		}
		
		public void run()
		{
			auth.authentify(session);
		}
	}
	
	ServerSocket serverSocket;
	XMPPAuthentification auth = new XMPPTLSAuthentification();
	  
	ThreadPoolExecutor executor =
		new ThreadPoolExecutor(1,10,1,TimeUnit.SECONDS,new PriorityBlockingQueue<Runnable>());
	
	public XMPPServer()
	{
		try {
			serverSocket = new ServerSocket(5222);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		System.out.printf("xmpp-server running\n");
		while( true )
		{
			try {
				Socket socket = serverSocket.accept();
				
				XMPPSession session = new XMPPSession(true,socket.getChannel());
				executor.execute( new SessionHandler(session) );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main( String [] args )
	{
		XMPPServer server = new XMPPServer();
		
		Thread t = new Thread( server, "xmpp-server" );
		t.start();
	}
}
