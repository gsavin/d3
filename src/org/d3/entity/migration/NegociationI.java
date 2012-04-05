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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.SocketChannel;

import org.d3.Console;
import org.d3.actor.Entity;
import org.d3.protocol.request.ObjectCoder;
import org.d3.protocol.request.ObjectCoder.CodingMethod;

public class NegociationI extends Negociation {

	String className;

	NegociationI(SocketChannel channel) {
		super(channel);
	}

	protected void handle(int req, String[] data) {
		switch (req) {
		case HEADER_REQUEST:
			// String agencyId = data[0];
			className = data[1];
			String entityPath = data[2];
			// String entityId = data[3];

			checkClassName(className);
			checkEntityPath(entityPath);

			Console.warning("receive migration request for \"%s\"", className);

			write(HEADER_REQUEST_RESPONSE, Response.MIGRATION_ACCEPTED.name());
			break;
		case HEADER_SEND:
			CodingMethod coding = CodingMethod.valueOf(data[0]);
			MigrationData entity = (MigrationData) ObjectCoder.decode(coding,
					data[1].getBytes());

			// TODO Reification
			Console.warning("receive entity migration data");
			Throwable error = createTheEntity(entity);

			if (error == null) {
				write(HEADER_SEND_RESPONSE, Response.MIGRATION_SUCCEED.name());
			} else {
				write(HEADER_SEND_RESPONSE, Response.MIGRATION_FAILED.name());
			}

			close();

			break;
		default:
		}
	}

	@SuppressWarnings("unchecked")
	protected Throwable createTheEntity(MigrationData data) {
		if (!className.equals(data.className)) {
			Console.error("entity received class is not the authorized class");
			return new SecurityException();
		} else {
			Class<? extends Entity> entityClass;

			try {
				entityClass = (Class<? extends Entity>) Class
						.forName(this.className);
				Constructor<? extends Entity> c = entityClass
						.getConstructor(String.class);

				Entity e = c.newInstance(data.id);
				e.importEntity(data);
				e.init();
			} catch (ClassNotFoundException e) {
				return e;
			} catch (SecurityException e) {
				return e;
			} catch (NoSuchMethodException e) {
				return e;
			} catch (IllegalArgumentException e) {
				return e;
			} catch (InstantiationException e) {
				return e;
			} catch (IllegalAccessException e) {
				return e;
			} catch (InvocationTargetException e) {
				return e;
			} catch (ImportationException e) {
				return e;
			}

			return null;
		}
	}

	protected void checkEntityPath(String entityPath) {

	}

	protected void checkClassName(String className) {
	}
}
