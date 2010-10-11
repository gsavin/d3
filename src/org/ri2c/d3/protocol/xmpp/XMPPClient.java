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
import java.net.Socket;
import java.net.UnknownHostException;

import org.ri2c.d3.protocol.xmpp.auth.XMPPTLSAuthentification;

public class XMPPClient
{
	public XMPPClient( String host, int port )
	{
		System.out.printf("xmpp-client running\n  connection to %s:%d\n", host, port );
		
		try {
			Socket socket = new Socket(host,port);
			XMPPSession session = new XMPPSession(false,socket.getChannel());
			XMPPAuthentification auth = new XMPPTLSAuthentification();
			boolean b = auth.authentify(session);
			
			System.out.printf("session is authentified ? %s\n",b);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
