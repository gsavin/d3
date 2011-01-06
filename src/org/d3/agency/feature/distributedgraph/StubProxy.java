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
package org.ri2c.d3.agency.feature.distributedgraph;

import org.ri2c.d3.agency.feature.DistributedGraph;

public class StubProxy
	implements DGConstants
{
	final DistributedGraph distributedGraph;
	
	public StubProxy( DistributedGraph distributedGraph )
	{
		this.distributedGraph = distributedGraph;
	}
	
	public Object getNodeAttribute( String nodeId, String attrId )
	{
		return getNodeAttribute( nodeId, attrId, null );
	}
	
	public Object getNodeAttribute( String nodeId, String attrId, AttributeCondition cond )
	{
		GraphComponent gc = distributedGraph.findNode(nodeId);
		return gc.getNodeAttribute(nodeId, attrId, cond);
	}
	
	public Object getEdgeAttribute( String edgeId, String attrId )
	{
		return getEdgeAttribute( edgeId, attrId, null );
	}
	
	public Object getEdgeAttribute( String edgeId, String attrId, AttributeCondition cond )
	{
		GraphComponent gc = distributedGraph.findEdge(edgeId);
		return gc.getEdgeAttribute(edgeId, attrId, cond);
	}
	
	public void setNodeAttribute( String nodeId, String attrId, Object obj )
	{
		GraphComponent gc = distributedGraph.findNode(nodeId);
		gc.setNodeAttribute(nodeId, attrId, obj);
	}
	
	public void setEdgeAttribute( String edgeId, String attrId, Object obj )
	{
		GraphComponent gc = distributedGraph.findEdge(edgeId);
		gc.setEdgeAttribute(edgeId, attrId, obj);
	}
}
