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
/*
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
*/
public class Test
{

	public static void main(String[] args)
		throws IOException
	{
		ConnectionManager cm = new ConnectionManager(6011);
		/*
		SocketChannel channel = SocketChannel.open();
		channel.connect( new InetSocketAddress(6010) );
			*/
		InetSocketAddress sa = new InetSocketAddress(6010);
		//ByteBuffer b1 = ByteBuffer.wrap( "Hellow world !".getBytes() );
		
		for( int i = 0; i < 5; i++ )
		{
			//channel.write(b1.duplicate());
			cm.send(sa,"Hello world !".getBytes());
			
			try
			{
				Thread.sleep(1000);
			}
			catch( Exception e ) {}
		}
		
		//channel.close();
	}
}