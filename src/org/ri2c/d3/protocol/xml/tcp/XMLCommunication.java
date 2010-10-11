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
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

import javax.management.modelmbean.XMLParseException;

import org.ri2c.d3.Request;
import org.ri2c.d3.protocol.xml.XMLRequest;
import org.ri2c.d3.protocol.xml.XMLStanza;
import org.ri2c.d3.protocol.xml.XMLStanzaFactory;
import org.ri2c.d3.request.RequestListener;

public class XMLCommunication
	implements Runnable
{
	private class InnerXMLStanzaFactory
		implements XMLStanzaFactory
	{
		public XMLStanza newXMLStanza(String name)
		{
			return new XMLRequest(remoteId,name);
		}
	}
	
	String				remoteId;
	XMLInputStream 		in;
	XMLOutputStream 	out;
	XMLStanzaFactory	factory;
	ReadableByteChannel	inChannel;
	WritableByteChannel outChannel;
	
	RequestListener		bridge;
	
	boolean				active;
	
	public XMLCommunication( String remoteId,
			ReadableByteChannel in, WritableByteChannel out )
		throws IOException
	{
		this.remoteId = remoteId;
		this.factory  = new InnerXMLStanzaFactory();
		
		this.outChannel = out;
		this.out = new XMLOutputStream();
		this.out.init(out);
		
		this.inChannel = in;
		this.in = new XMLInputStream();
		this.in.init(this.factory,in);
		
		this.active = true;
	}
	
	public void setBridge( RequestListener rl )
	{
		this.bridge = rl;
	}
	
	public boolean isActive()
	{
		if( outChannel instanceof SocketChannel &&
				! ((SocketChannel) outChannel).isConnected() )
			return false;
		
		if( inChannel instanceof SocketChannel &&
				! ((SocketChannel) inChannel).isConnected() )
			return false;
		
		return outChannel.isOpen() &&
			inChannel.isOpen();
	}
	
	public void close()
	{
		if( ! active )
			return;
		
		System.out.printf("[xml-comm] close %s%n", remoteId);
		
		try
		{
			active = false;
			inChannel.close();
			outChannel.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		while( active )
		{
			lookForNewRequest();
		}
	}
	
	public void lookForNewRequest()
	{
		if( bridge == null )
			return;
		
		try
		{
			XMLStanza xmls = null;
			
			do
			{
				xmls = in.nextStanza();
				
				if( xmls != null )
					bridge.requestReceived( (XMLRequest) xmls );
			}
			while( xmls != null );
		}
		catch (IOException e)
		{
			if( ! active )
			{
				System.err.printf("[xml-comm] error looking for request on %s, closing...%n",remoteId);
				close();
			}
		}
		catch (XMLParseException e)
		{
			e.printStackTrace();
		}
	}
	
	public void sendRequest(Request r)
		throws IOException
	{
		if( r instanceof XMLStanza )
		{
			out.sendStanza( (XMLStanza) r );
		}
		else
		{
			System.err.printf("this request is not done for xml protocol%n" );
		}
	}
}
