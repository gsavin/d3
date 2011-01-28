/*
 * This file is part of d3 <http://d3-project.org>.
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
 * Copyright 2010 - 2011 Guilhelm Savin
 */
package org.d3.entity.migration;

import java.nio.channels.SocketChannel;

import org.d3.Console;
import org.d3.protocol.request.ObjectCoder;
import org.d3.protocol.request.ObjectCoder.CodingMethod;

public class NegociationReceiver extends Negociation {

	NegociationReceiver(SocketChannel channel) {
		super(channel);
	}
	
	protected void handle(int req, String[] data) {
		switch (req) {
		case HEADER_REQUEST:
			// String agencyId = data[0];
			String className = data[1];
			String entityPath = data[2];
			// String entityId = data[3];

			checkClassName(className);
			checkEntityPath(entityPath);

			Console.warning("receive migration request for \"%s\"", className);
			
			write(HEADER_REQUEST_RESPONSE,
					Response.MIGRATION_ACCEPTED.name());
			break;
		case HEADER_SEND:
			CodingMethod coding = CodingMethod.valueOf(data[0]);
			MigrationData entity = (MigrationData) ObjectCoder.decode(coding, data[1]);

			// TODO Reification
			Console.warning("receive entity migration data");
			
			write(HEADER_SEND_RESPONSE, Response.MIGRATION_SUCCEED.name());

			close();

			break;
		default:
		}
	}

	protected void checkEntityPath(String entityPath) {

	}

	protected void checkClassName(String className) {
	}
}
