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
package org.d3.protocol.xml;

import org.d3.Agency;
import org.d3.Args;
import org.d3.IdentifiableObject;
import org.d3.Request;
import org.d3.agency.RemoteAgency;
import org.d3.protocol.XMLProtocol;
import org.d3.request.RequestListener;

public class TestXMLProtocol {
	public static class FakeRequestListener implements RequestListener {
		public void requestReceived(Request r) {
			System.out.printf("[fake-rl] receive request %s%n", r);
		}
	}

	public static void main(String[] args) throws Exception {
		Agency.enableAgency(Args
				.processFile("org/ri2c/d3/resources/default.cfg"));

		if ("server".equals(args[0])) {
			FakeRequestListener frl = new FakeRequestListener();
			XMLProtocol protocol = XMLProtocol.getDefault();
			protocol.init();
			protocol.addRequestListener(frl);

			while (true) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
			}
		} else {
			FakeRequestListener frl = new FakeRequestListener();
			XMLProtocol protocol = XMLProtocol.getDefault();
			protocol.addRequestListener(frl);

			Thread.sleep(3000);

			RemoteAgency remote = Agency.getLocalAgency()
					.getRemoteAgencyDescription("l2d-machineA");
			
			Object f = IdentifiableObject.Tools.call(Agency.getLocalAgency(), remote, "ping", null);
			
			System.out.printf("reponse: %s%n",f);
		}
	}
}
