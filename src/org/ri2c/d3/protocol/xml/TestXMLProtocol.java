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

import java.net.URI;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Args;
import org.ri2c.d3.Request;
import org.ri2c.d3.protocol.XMLProtocol;
import org.ri2c.d3.request.RequestListener;

public class TestXMLProtocol
{
	public static class FakeRequestListener
		implements RequestListener
	{
		public void requestReceived(Request r)
		{
			System.out.printf( "[fake-rl] receive request %s%n", r );
		}
	}
	
	public static void main( String [] args ) throws Exception
	{
		Agency.enableAgency(Args.processFile("org/ri2c/d3/resources/default.cfg"));
		
		if( "server".equals(args[0]) )
		{
			FakeRequestListener frl = new FakeRequestListener();
			XMLProtocol protocol = XMLProtocol.getDefault();
			protocol.init();
			protocol.addRequestListener(frl);
			
			while(true) { try { Thread.sleep(1000); } catch( Exception e ) {} }
		}
		else
		{
			FakeRequestListener frl = new FakeRequestListener();
			XMLProtocol protocol = XMLProtocol.getDefault();
			protocol.addRequestListener(frl);
			URI uri = new URI("agency://l2d-machineA/l2d-machineA?callable=ping");
			Request hello = new Request(uri);

			Thread.sleep(3000);
			
			protocol.sendRequest(hello);
		}
	}
}
