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
package org.ri2c.d3.protocol.xmpp.auth;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.ri2c.d3.protocol.xml.XMLStanza;
import org.ri2c.d3.protocol.xmpp.XMPPAuthentification;
import org.ri2c.d3.protocol.xmpp.XMPPSession;
import org.ri2c.d3.protocol.xmpp.protocol.XMPPTLSProtocol;

public class XMPPTLSAuthentification
	implements XMPPAuthentification
{
	protected static final XMLStanza FAILURE =
		new XMLStanza("failure").addAttribute("xmlns","urn:ietf:params:xml:ns:xmpp-tls");
	
	protected static final XMLStanza PROCEED =
		new XMLStanza("proceed").addAttribute("xmlns","urn:ietf:params:xml:ns:xmpp-tls");
	
	protected static final XMLStanza START_TLS =
		new XMLStanza("starttls").addAttribute("xmlns","urn:ietf:params:xml:ns:xmpp-tls");
	
	public boolean authentify( XMPPSession session )
	{
		if( session.isServer() ) return server(session);
		else return client(session);
	}
	
	protected boolean server( XMPPSession session )
	{
		XMLStanza response;
		
		session.getXMLOut().sendStanza(getFeaturesStanza());
		response = session.getXMLIn().nextStanza();
		
		if( ! response.getName().equals("starttls") ||
				response.checkAttribute("xmlns","urn:ietf:params:xml:ns:xmpp-tls") )
		{
			session.getXMLOut().sendStanza(FAILURE);
			session.close();
			
			return false;
		}
		
		session.getXMLOut().sendStanza(PROCEED);
		
		if( ! tlsNegociation(session) )
		{
			session.close();
			return false;
		}
		
		return false;
	}
	
	protected XMLStanza getFeaturesStanza()
	{
		XMLStanza features = new XMLStanza("stream:features");
		
		XMLStanza startTls = new XMLStanza("starttls");
		startTls.addAttribute("xmlns","urn:ietf:params:xml:ns:xmpp-tls");
		startTls.addChild( new XMLStanza("required") );
		
		XMLStanza mechanisms = new XMLStanza("mechanisms");
		mechanisms.addAttribute("xmlns","urn:ietf:params:xml:ns:xmpp-sasl");
		mechanisms.addChild( new XMLStanza("mechanism","DIGEST-MD5") );
		mechanisms.addChild( new XMLStanza("mechanism","PLAIN") );
		
		features.addChild(startTls);
		features.addChild(mechanisms);
		
		return features;
	}
	
	protected boolean client( XMPPSession session )
	{
		XMLStanza res = session.getXMLIn().nextStanza();
		
		if( ! res.is("stream:features") || res.getChildrenCount() == 0 )
		{
			System.err.printf("authentification error\n");
			return false;
		}
		
		if( res.getChild(0).is("starttls") &&
				res.getChild(0).getChild(0).is("required") )
		{
			session.getXMLOut().sendStanza(START_TLS);
			res = session.getXMLIn().nextStanza();
			
			if( ! res.is("proceed") )
			{
				System.err.printf("authentification error\n");
				return false;
			}
			
			return tlsNegociation(session);
		}
		
		return false;
	}
	
	protected boolean tlsNegociation( XMPPSession session )
	{
		try
		{
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init( null, null, null);

			SSLEngine engine = sslContext.createSSLEngine();
			engine.setUseClientMode( ! session.isServer() );

			
			XMPPTLSProtocol tlsProtocol = new XMPPTLSProtocol(session,engine);
			
			if( tlsProtocol.init() )
			{
				session.setProtocol(tlsProtocol);
				return true;
			}
			else return false;
		}
		catch( NoSuchAlgorithmException e )
		{
			e.printStackTrace();
		}
		catch( KeyManagementException e )
		{
			e.printStackTrace();
		}

		return false;
	}
}
