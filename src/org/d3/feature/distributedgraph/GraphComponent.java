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
package org.d3.feature.distributedgraph;

import org.ri2c.d3.IdentifiableObject;

public interface GraphComponent
	extends Actor, DGConstants
{
	String getAgencyId();
	
	Object clearNodeAttributes( String nodeId );
	Object clearEdgeAttributes( String edgeId );
	
	Object getNodeAttributeCount( String nodeId );
	Object getEdgeAttributeCount( String edgeId );
	
	Object getNodeAttribute( String nodeId, String attrId, AttributeCondition cond );
	Object getEdgeAttribute( String edgeId, String attrId, AttributeCondition cond );
	
	Object setNodeAttribute( String nodeId, String attrId, Object obj );
	Object setEdgeAttribute( String edgeId, String attrId, Object obj );
	
	Object hasNodeAttribute( String nodeId, String attrId, AttributeCondition cond );
	Object hasEdgeAttribute( String edgeId, String attrId, AttributeCondition cond );
	
	Object getNodeDegree( String nodeId, NodeDegreeMode mode );
	
	Object getElementAttributeKeys( String elementId, ElementType type );
}
