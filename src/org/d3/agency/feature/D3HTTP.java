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
package org.d3.agency.feature;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;

import org.d3.Agency;
import org.d3.Args;
import org.d3.agency.Feature;
import org.d3.agency.RemoteAgency;
import org.d3.annotation.ActorDescription;
import org.d3.annotation.ActorPath;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@ActorPath("/features")
@ActorDescription("HTTP Web server")
public class D3HTTP extends Feature implements HttpHandler {
	protected static final int HTTP_PORT = 6080;

	HttpServer server;

	public D3HTTP() {
		super("http");
	}

	public void handle(HttpExchange arg0) throws IOException {
		String response = getL2DH("org/d3/agency/feature/http/content.l2dh");
		byte[] responseData = response.getBytes();

		arg0.sendResponseHeaders(200, responseData.length);
		arg0.getResponseBody().write(responseData);
		arg0.getResponseBody().close();
	}

	protected String getL2DH(String l2dh) throws IOException {
		URL content = ClassLoader.getSystemResource(l2dh);
		InputStream in = content.openStream();
		StringBuilder out = new StringBuilder();

		int c;
		while (in.available() > 0) {
			c = in.read();

			if (c == '$') {
				StringBuilder buffer = new StringBuilder();

				do {
					c = in.read();

					if (c != '$')
						buffer.append((char) c);
				} while (c != '$');

				String csname = buffer.toString();

				if (csname.equals("agency_id"))
					out.append(Agency.getLocalAgency().getId());
				else if (csname.equals("entities_list"))
					out.append(entitiesList());
				else if (csname.equals("agencies_list"))
					out.append(agenciesList());
				else
					out.append("unknown csname");
			} else
				out.append((char) c);
		}

		return out.toString();
	}

	protected String entitiesList() {
		StringBuilder buffer = new StringBuilder("<ul>");
		
		URI[] entities = Agency.getLocalAgency().getIdentifiableObjectList(IdentifiableType.entity);
		
		if (entities != null)
			for (URI e : entities)
				buffer.append("<li>").append(e).append("</li>");
		
		return buffer.append("</ul>").toString();
	}

	protected String agenciesList() {
		StringBuilder buffer = new StringBuilder("<ul>");
		for (RemoteAgency rad : Agency.getLocalAgency()
				.eachRemoteAgency())
			buffer.append("<li>").append(rad.getId()).append("&nbsp;@&nbsp;")
					.append(rad.getAddress()).append("&nbsp;:&nbsp;")
					.append(rad.getFirstProtocol()).append("</li>");

		return buffer.append("</ul>").toString();
	}

	public boolean initFeature(Agency agency, Args args) {
		try {

			server = HttpServer.create(new InetSocketAddress(HTTP_PORT), 4);
			server.createContext("/d3/", this);

			server.start();

			return true;
		} catch (IOException e) {
			System.err.printf("[l2d-http] error : %s%n", e.getMessage());
			return false;
		}
	}
	
	public void terminateFeature() {
		server.stop(0);
	}
}
