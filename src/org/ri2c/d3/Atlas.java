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
package org.ri2c.d3;

import org.ri2c.d3.agency.RemoteAgencyDescription;
import org.ri2c.d3.atlas.AtlasDescription;
import org.ri2c.d3.atlas.AtlasListener;
import org.ri2c.d3.entity.Entity;
import org.ri2c.d3.entity.EntityCall;
import org.ri2c.d3.entity.EntityDescription;
import org.ri2c.d3.entity.EntityMigrationStatus;

public interface Atlas
	extends IdentifiableObject
{
	void init( Agency agency );
	
	@SuppressWarnings("unchecked")
	AtlasDescription getDescription();
	
	void addAtlasListener( AtlasListener listener );
	
	void removeAtlasListener( AtlasListener listener );
	
	Entity createEntity( EntityDescription desc );
	
	Entity getEntity( String entityId );
	
	EntityMigrationStatus migrateEntity( String entityId, RemoteAgencyDescription rad );
	
	Future addFutureRequest( Request r );
	
	void reply( IdentifiableObject replyTo, IdentifiableObject replyFrom,
			Request r, Object futureValue );
	
	void entityCall( IdentifiableObject source, String entityId, EntityCall call );
	
	String [] listEntities();
	
	Future remoteEntityCall( IdentifiableObject source, IdentifiableObject target, String callId, boolean createFuture, Object ... args );
}
