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
package org.ri2c.d3.protocol.xml;

import org.ri2c.d3.Request;
import org.ri2c.d3.protocol.XMLProtocol;
import org.ri2c.d3.request.RequestListener;

public class TestXMLProtocol
{
	public static class FakeRequestListener
		implements RequestListener
	{
		public void requestReceived(String source, Request r)
		{
			System.out.printf( "[fake-rl] receive request from %s: %s%n", source, r.getName() );
		}
	}
	
	public static void main( String [] args )
	{
		if( "server".equals(args[0]) )
		{
			FakeRequestListener frl = new FakeRequestListener();
			XMLProtocol protocol = XMLProtocol.getDefault();
			protocol.addRequestListener(frl);
		}
		else
		{
			FakeRequestListener frl = new FakeRequestListener();
			XMLProtocol protocol = XMLProtocol.getDefault();
			protocol.addRequestListener(frl);
			Request hello = protocol.newRequest("hello");

			protocol.sendRequest(args[0],hello);
		}
		
		while(true) { try { Thread.sleep(1000); } catch( Exception e ) {} }
	}
}
