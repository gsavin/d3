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

import java.net.URI;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ThreadFactory;

import javax.management.modelmbean.XMLParseException;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Console;
import org.ri2c.d3.Request;
import org.ri2c.d3.agency.IpTables;
import org.ri2c.d3.agency.RemoteAgency;
import org.ri2c.d3.protocol.connected.ConnectedInterface;
import org.ri2c.d3.protocol.connected.Connection;
import org.ri2c.d3.protocol.connected.ConnectionFactory;
import org.ri2c.d3.protocol.connected.ConnectionManager;
import org.ri2c.d3.protocol.xml.XMLInterface;
import org.ri2c.d3.protocol.xml.XMLStanza;
import org.ri2c.d3.protocol.xml.XMLStanzaBuilder;
import org.ri2c.d3.protocol.xml.XMLStanzaFactory;
import org.ri2c.d3.request.RequestListener;

public class XMLTCPInterface implements XMLInterface, ConnectedInterface {
	public static final int XML_TCP_PORT = 6003;

	private class InnerXMLStanzaFactory implements XMLStanzaFactory {
		public XMLStanza newXMLStanza(String name) {
			return new XMLStanza(name);
		}
	}

	class XMLConnection extends Connection {
		String buffer;
		Charset cs;
		RemoteAgency remote;
		
		public XMLConnection(SocketChannel channel) {
			super(channel);

			remote = Agency.getLocalAgency().getRemoteAgencyDescription(
					iptables.getId(channel.socket().getInetAddress()));

			buffer = "";
			cs = Charset.defaultCharset();
		}

		public void addData(byte[] data) {
			String str = new String(data, cs);
			buffer += str;

			int index = XMLStanzaBuilder.stanzaEndPosition(buffer);

			if (index > 0) {
				str = buffer.substring(0, index);
				buffer = buffer.substring(index);

				try {
					String sourceId = iptables.getId(channel.socket()
							.getInetAddress());

					if (sourceId == null) {
						Console.error("unknown address: %s", channel.socket()
								.getInetAddress());
						return;
					}

					XMLStanza xr = XMLStanzaBuilder.string2stanza(
							stanzaFactory, str);

					try {
						URI uri = new URI(xr.getContent());
						Request r = new Request(remote,uri);

						xprb.requestReceived(r);
					} catch (Exception e) {
						Console.error(e.getMessage());
					}
				} catch (XMLParseException e) {
					Console.error(e.getMessage());
				}
			}
		}
	}

	class XMLConnectionFactory implements ConnectionFactory {
		public Connection createConnection(SocketChannel channel) {
			return new XMLConnection(channel);
		}
	}

	static ThreadGroup group = new ThreadGroup("xml-tcp-threads");

	class InnerThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r);
			return t;
		}
	}

	private RequestListener xprb;
	private IpTables iptables;
	private XMLStanzaFactory stanzaFactory;
	private XMLConnectionFactory xmlConnectionFactory;
	@SuppressWarnings("unused")
	private ConnectionManager connectionManager;

	public void init(RequestListener bridge, IpTables iptables) {
		this.stanzaFactory = new InnerXMLStanzaFactory();
		this.xprb = bridge;
		this.iptables = iptables;
		this.xmlConnectionFactory = new XMLConnectionFactory();
		this.connectionManager = new ConnectionManager(xmlConnectionFactory,
				new InnerThreadFactory(), XML_TCP_PORT, 2);
	}

	public void sendXMLRequest(String remoteId, Request request) {

	}

	public void receiveData(Connection conn, byte[] data) {
		if (conn instanceof XMLConnection)
			((XMLConnection) conn).addData(data);
	}

}
