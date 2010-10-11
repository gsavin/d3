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
package org.ri2c.d3.protocol.xmpp.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;

import org.ri2c.d3.protocol.xmpp.XMPPSession;

public class XMPPTLSProtocol
	extends BufferProtocol
{
	protected XMPPSession session;
	protected SSLEngine engine;
	protected SSLSession sslSession;
	protected ByteBuffer appData;
	protected ByteBuffer netData;
	protected ByteBuffer peerAppData;
	protected ByteBuffer peerNetData;
	
	public XMPPTLSProtocol( XMPPSession session, SSLEngine engine )
	{
		this.session = session;
		this.engine  = engine;
	}
	
	public boolean init()
	{
		sslSession = engine.getSession();
		
		appData     = ByteBuffer.allocate( sslSession.getApplicationBufferSize() );
		peerAppData = ByteBuffer.allocate( sslSession.getApplicationBufferSize() );
		netData     = ByteBuffer.allocate( sslSession.getPacketBufferSize() );
		peerNetData = ByteBuffer.allocate( sslSession.getPacketBufferSize() );
		
		try
		{
			doHandshake(session.getChannel(),engine,netData,peerNetData);
			return true;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	@Override
	protected void read( ByteBuffer buffer )
	{
		try
		{
			// Read SSL/TLS encoded data from peer
			int num = session.getChannel().read(peerNetData);

			if( num == -1 )
			{
				// Handle closed channel
			}
			else if( num == 0 )
			{
				// No bytes read; try again ...
			}
			else
			{
				// Process incoming data
				peerNetData.flip();
				SSLEngineResult res = engine.unwrap(peerNetData, peerAppData);

				if (res.getStatus() == SSLEngineResult.Status.OK) {
					peerNetData.compact();

					if (peerAppData.hasRemaining())
					{
						buffer.put(peerAppData);
					}
				}
				else
				{
					// TODO Handle other status:  BUFFER_OVERFLOW, BUFFER_UNDERFLOW, CLOSED
					System.err.printf("error: unhandle response status\n");
				}
			}
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void write( ByteBuffer buffer )
	{
		appData.clear();
		appData.put(buffer);
		appData.flip();
		
		try
		{
			while( appData.hasRemaining() )
			{
				// Generate SSL/TLS encoded data (handshake or application data)
				SSLEngineResult res = engine.wrap(appData,netData);

				// Process status of call
				if (res.getStatus() == SSLEngineResult.Status.OK)
				{
					appData.compact();

					// Send SSL/TLS encoded data to peer
					while( netData.hasRemaining() )
					{
						int num = session.getChannel().write(netData);

						if( num == -1 )
						{
							// handle closed channel
						}
						else if( num == 0 )
						{
							// no bytes written; try again later
						}
					}
				}
				else
				{
					// TODO Handle other status:  BUFFER_OVERFLOW, BUFFER_UNDERFLOW, CLOSED
					System.err.printf("error: unhandle response status\n");
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	void doHandshake(SocketChannel socketChannel, SSLEngine engine,
	        ByteBuffer myNetData, ByteBuffer peerNetData) throws Exception {

	    // Create byte buffers to use for holding application data
	    int appBufferSize = engine.getSession().getApplicationBufferSize();
	    ByteBuffer myAppData = ByteBuffer.allocate(appBufferSize);
	    ByteBuffer peerAppData = ByteBuffer.allocate(appBufferSize);

	    // Begin handshake
	    engine.beginHandshake();
	    SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();

	    // Process handshaking message
	    while (hs != SSLEngineResult.HandshakeStatus.FINISHED &&
	        hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {

	        switch (hs) {

	        case NEED_UNWRAP:
	            // Receive handshaking data from peer
	            if (socketChannel.read(peerNetData) < 0) {
	                // Handle closed channel
	            }

	            // Process incoming handshaking data
	            peerNetData.flip();
	            SSLEngineResult res = engine.unwrap(peerNetData, peerAppData);
	            peerNetData.compact();
	            hs = res.getHandshakeStatus();

	            // Check status
	            switch (res.getStatus()) {
	            case OK :
	                // Handle OK status
	                break;

	            // Handle other status: BUFFER_UNDERFLOW, BUFFER_OVERFLOW, CLOSED
	            }
	            break;

	        case NEED_WRAP :
	            // Empty the local network packet buffer.
	            myNetData.clear();

	            // Generate handshaking data
	            res = engine.wrap(myAppData, myNetData);
	            hs = res.getHandshakeStatus();

	            // Check status
	            switch (res.getStatus()) {
	            case OK :
	                myNetData.flip();

	                // Send the handshaking data to peer
	                while (myNetData.hasRemaining()) {
	                    if (socketChannel.write(myNetData) < 0) {
	                        // Handle closed channel
	                    }
	                }
	                break;

	            // Handle other status:  BUFFER_OVERFLOW, BUFFER_UNDERFLOW, CLOSED
	            }
	            break;

	        case NEED_TASK :
		    // Handle blocking tasks
	            break;

	        // Handle other status:  // FINISHED or NOT_HANDSHAKING
	        }
	    }
	}
}
