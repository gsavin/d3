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

import java.nio.channels.SocketChannel;

import org.ri2c.d3.protocol.xml.tcp.XMLInputStream;
import org.ri2c.d3.protocol.xml.tcp.XMLOutputStream;

public class XMPPSession
{
	protected SocketChannel 	channel;
	protected XMLInputStream 	in;
	protected XMLOutputStream 	out;
	protected XMPPProtocol		protocol;
	protected boolean			server;
	
	public XMPPSession( boolean server, SocketChannel channel )
	{
		this.channel = channel;
		this.server  = server;
		in			 = new XMLInputStream();
		out			 = new XMLOutputStream();
	}
	
	public XMLOutputStream getXMLOut()
	{
		return out;
	}
	
	public XMLInputStream getXMLIn()
	{
		return in;
	}
	
	public SocketChannel getChannel()
	{
		return channel;
	}
	
	public boolean isServer()
	{
		return server;
	}
	
	public void setProtocol( XMPPProtocol p )
	{
		protocol = p;
	}
	
	public void close()
	{
		out.close();
	}
}
