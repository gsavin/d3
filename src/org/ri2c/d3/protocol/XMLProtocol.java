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
package org.ri2c.d3.protocol;

import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Protocol;
import org.ri2c.d3.Request;
import org.ri2c.d3.annotation.IdentifiableObjectPath;
import org.ri2c.d3.protocol.xml.XMLInterface;
import org.ri2c.d3.protocol.xml.udp.XMLUDPInterface;
import org.ri2c.d3.request.RequestListener;

@IdentifiableObjectPath("/d3/protocols")
public class XMLProtocol extends Protocol {
	public static final int XML_PROTOCOL_PORT = 10001;

	private static XMLProtocol xmlProtocol = null;

	public static XMLProtocol getDefault() {
		if (xmlProtocol == null)
			xmlProtocol = new XMLProtocol();

		return xmlProtocol;
	}

	class XMLProtocolRequestBridge implements RequestListener {
		public void requestReceived(Request r) {
			for (RequestListener rl : listeners)
				rl.requestReceived(r);
		}
	}

	private ConcurrentLinkedQueue<RequestListener> listeners;
	private XMLProtocolRequestBridge xprb;
	private boolean initDone;
	private XMLInterface xmlInterface;

	private XMLProtocol() {
		super("xml");
		initDone = false;
	}

	public void init() {
		if (initDone)
			return;

		initDone = true;
		listeners = new ConcurrentLinkedQueue<RequestListener>();
		xprb = new XMLProtocolRequestBridge();

		if( Agency.getLocalAgency() != null )
			addRequestListener(Agency.getLocalAgency());

		try {
			xmlInterface = new XMLUDPInterface();
			xmlInterface.init(xprb, Agency.getLocalAgency().getIpTables());
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void addRequestListener(RequestListener listener) {
		listeners.add(listener);
	}

	public void removeRequestListener(RequestListener listener) {
		listeners.remove(listener);
	}

	public void sendRequest(Request r) {
		if (!r.isLocalTarget()) {
			xmlInterface.sendXMLRequest(r.getTargetAgency(), r);
		}
	}
}
