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

import java.util.LinkedList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Node;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.InvalidRequestFormatException;
import org.ri2c.d3.RemoteIdentifiableObject;
import org.ri2c.d3.Request;
import org.ri2c.d3.agency.feature.DistributedGraph;
import org.ri2c.d3.agency.feature.distributedgraph.DGConstants.ElementType;
import org.ri2c.d3.protocol.Protocols;
import org.ri2c.d3.request.ObjectCoder;

public class DGRequestManager
	implements DGConstants
{
	private DistributedGraph distributedGraph;
	
	public DGRequestManager( DistributedGraph dg )
	{
		this.distributedGraph = dg;
	}

	public void handleRequest(IdentifiableObject source,
			IdentifiableObject target, Request r)
	{
		if( source instanceof RemoteIdentifiableObject )
		{
			RemoteIdentifiableObject remoteSource = (RemoteIdentifiableObject) source;
			distributedGraph.checkKnownComponent(remoteSource);
			
			if( r.getName().startsWith( REQUEST_PREFIX ) )
			{
				KnownRequest req = KnownRequest.valueOf( r.getName().substring( REQUEST_PREFIX.length() ) );
				
				if( req != null )
					handleKnownRequest(req,r,source);
			}
		}
	}
	
	protected Element getElement( String elementId, ElementType type )
		throws ElementNotFoundException
	{
		assert type != null;
	
		Element e = null;
		
		switch(type)
		{
		case NODE:
			e = distributedGraph.getLocalGraph().getNode(elementId);
		case EDGE:
			e = distributedGraph.getLocalGraph().getEdge(elementId);
		case GRAPH:
			e = distributedGraph.getLocalGraph();
		}
		
		if( e == null )
			throw new ElementNotFoundException( String.format( "%s \"%s\"", type.name(), elementId ) );
		
		return e;
	}
	
	protected void handleKnownRequest( KnownRequest req, Request org, IdentifiableObject source )
	{
		Object r;
		
		switch( req )
		{
		case HAS_NODE:
			r = requestHasNode(req,org,source);
			break;
		case HAS_EDGE:
			r = requestHasEdge(req,org,source);
			break;
		case CLEAR_NODE_ATTRIBUTES:
			r = requestClearNodeAttributes(req,org,source);
			break;
		case CLEAR_EDGE_ATTRIBUTES:
			r = requestClearEdgeAttributes(req,org,source);
			break;
		case GET_NODE_ATTRIBUTE_COUNT:
			r = requestGetNodeAttributeCount(req,org,source);
			break;
		case GET_EDGE_ATTRIBUTE_COUNT:
			r = requestGetEdgeAttributeCount(req,org,source);
			break;
		case GET_NODE_ATTRIBUTE:
			r = requestGetNodeAttribute(req,org,source);
			break;
		case GET_EDGE_ATTRIBUTE:
			r = requestGetEdgeAttribute(req,org,source);
			break;
		case SET_NODE_ATTRIBUTE:
			r = requestSetNodeAttribute(req,org,source);
			break;
		case SET_EDGE_ATTRIBUTE:
			r = requestSetEdgeAttribute(req,org,source);
			break;
		case HAS_NODE_ATTRIBUTE:
			r = requestHasNodeAttribute(req,org,source);
			break;
		case HAS_EDGE_ATTRIBUTE:
			r = requestHasEdgeAttribute(req,org,source);
			break;
		default:
			r = new Exception( "unsupported request: " + req.name() );	
		}
		
		Protocols.reply(source, distributedGraph, org, r);
	}
	
	protected Object requestHasNode( KnownRequest req, Request org, IdentifiableObject source )
	{
		String nodeId = org.getAttribute(NODE_ID);
		
		boolean hasNode;
		
		if( nodeId == null ) hasNode = false;
		else hasNode = distributedGraph.getLocalGraph().getNode(nodeId) != null;
		
		return hasNode;
	}
	
	protected Object requestGetNodeAttributeCount( KnownRequest req, Request org, IdentifiableObject source )
	{
		String nodeId = org.getAttribute(NODE_ID);
		
		Object r = null;
		
		if( nodeId == null )
		{
			r = new InvalidRequestFormatException( "need \"nodeId\" attributes");
		}
		else
		{
			Node n = distributedGraph.getLocalGraph().getNode(nodeId);
			
			if( n == null )
			{
				r = new ElementNotFoundException( String.format( "node \"%s\"",nodeId) );
			}
			else
			{
				r = n.getAttributeCount();
			}
		}
		
		return r;
	}
	
	protected Object requestGetNodeAttribute( KnownRequest req, Request org, IdentifiableObject source )
	{
		String nodeId = org.getAttribute(NODE_ID);
		String attrId = org.getAttribute(ATTR_ID);
		
		Object r = null;
		
		if( nodeId == null || attrId == null )
		{
			r = new InvalidRequestFormatException( "need \"nodeId\" and \"attrId\" attributes");
		}
		else
		{
			Node n = distributedGraph.getLocalGraph().getNode(nodeId);
			
			if( n == null )
			{
				r = new ElementNotFoundException( String.format( "node \"%s\"",nodeId) );
			}
			else
			{
				r = n.getAttribute(attrId);
			}
		}
		
		return r;
	}
	
	protected Object requestSetNodeAttribute( KnownRequest req, Request org, IdentifiableObject source )
	{
		String nodeId = org.getAttribute(NODE_ID);
		String attrId = org.getAttribute(ATTR_ID);

		Object r = null;
		
		if( nodeId == null || attrId == null )
		{
			r = new InvalidRequestFormatException( "need \"nodeId\" and \"attrId\" attributes");
		}
		else
		{
			Node n = distributedGraph.getLocalGraph().getNode(nodeId);
			
			if( n == null )
			{
				r = new ElementNotFoundException( String.format( "node \"%s\"",nodeId) );
			}
			else
			{
				String objData = org.getAttribute(ATTR_DATA);
			
				if( objData != null )
				{
					Object obj = ObjectCoder.decode( objData );
					n.changeAttribute(attrId,obj);
				}
				else
				{
					n.removeAttribute(attrId);
				}
				
				r = Boolean.TRUE;
			}
		}
		
		return r;
	}
	
	protected Object requestClearNodeAttributes( KnownRequest req, Request org, IdentifiableObject source )
	{
		String nodeId = org.getAttribute(NODE_ID);
		
		Object r = null;
		
		if( nodeId == null )
		{
			r = new InvalidRequestFormatException( "need \"nodeId\" attribute");
		}
		else
		{
			Node n = distributedGraph.getLocalGraph().getNode(nodeId);
			
			if( n == null )
			{
				r = new ElementNotFoundException( String.format( "node \"%s\"",nodeId) );
			}
			else
			{
				n.clearAttributes();
				r = Boolean.TRUE;
			}
		}
		
		return r;
	}
	
	protected Object requestHasNodeAttribute( KnownRequest req, Request org, IdentifiableObject source )
	{
		String nodeId = org.getAttribute(NODE_ID);
		String attrId = org.getAttribute(ATTR_ID);
		String attrCond = org.getAttribute(ATTR_COND);
		
		AttributeCondition cond = attrCond == null ?
				AttributeCondition.NONE : AttributeCondition.valueOf( attrCond );
		
		Object r = null;
		
		if( nodeId == null || attrId == null )
		{
			r = new InvalidRequestFormatException( "need \"nodeId\" and \"attrId\" attributes");
		}
		else
		{
			r = distributedGraph.hasNodeAttribute( nodeId, attrId, cond );
		}
		
		return r;
	}
	
	protected Object requestGetElementAttributeKeys( KnownRequest req, Request org, IdentifiableObject source )
	{
		String elementId = org.getAttribute(ELEMENT_ID);
		String elementType = org.getAttribute(ELEMENT_TYPE);
		
		Object r = null;
		
		if( elementId == null || elementType == null )
		{
			r = new InvalidRequestFormatException( "need \"elementId\" and \"elementType\" attributes");
		}
		else
		{
			ElementType type = ElementType.valueOf(elementType);
			
			try
			{
				Element e = getElement(elementId,type);
				LinkedList<String> keys = new LinkedList<String>();
				
				for( String s: e.getAttributeKeySet() )
					keys.add(s);
				
				r = keys;
			}
			catch( ElementNotFoundException exc )
			{
				r = exc;
			}
		}
		
		return r;
	}
	
	protected Object requestHasEdge( KnownRequest req, Request org, IdentifiableObject source )
	{
		String edgeId = org.getAttribute(EDGE_ID);
		
		boolean hasEdge;
		
		if( edgeId == null ) hasEdge = false;
		else hasEdge = distributedGraph.getLocalGraph().getEdge(edgeId) != null;
		
		return hasEdge;
	}
	
	protected Object requestGetEdgeAttributeCount( KnownRequest req, Request org, IdentifiableObject source )
	{
		String edgeId = org.getAttribute(EDGE_ID);
		
		Object r = null;
		
		if( edgeId == null )
		{
			r = new InvalidRequestFormatException( "need \"edgeId\" attributes");
		}
		else
		{
			Edge e = distributedGraph.getLocalGraph().getEdge(edgeId);
			
			if( e == null )
			{
				r = new ElementNotFoundException( String.format( "edge \"%s\"",edgeId) );
			}
			else
			{
				r = e.getAttributeCount();
			}
		}
		
		return r;
	}
	
	protected Object requestGetEdgeAttribute( KnownRequest req, Request org, IdentifiableObject source )
	{
		String edgeId = org.getAttribute(EDGE_ID);
		String attrId = org.getAttribute(ATTR_ID);
		
		Object r = null;
		
		if( edgeId == null || attrId == null )
		{
			r = new InvalidRequestFormatException( "need \"nodeId\" and \"attrId\" attributes");
		}
		else
		{
			Edge e = distributedGraph.getLocalGraph().getEdge(edgeId);
			
			if( e == null )
			{
				r = new ElementNotFoundException( String.format( "edge \"%s\"",edgeId) );
			}
			else
			{
				r = e.getAttribute(attrId);
			}
		}
		
		return r;
	}
	
	protected Object requestSetEdgeAttribute( KnownRequest req, Request org, IdentifiableObject source )
	{
		String edgeId = org.getAttribute(EDGE_ID);
		String attrId = org.getAttribute(ATTR_ID);

		Object r = null;
		
		if( edgeId == null || attrId == null )
		{
			r = new InvalidRequestFormatException( "need \"edgeId\" and \"attrId\" attributes");
		}
		else
		{
			Edge e = distributedGraph.getLocalGraph().getEdge(edgeId);
			
			if( e == null )
			{
				r = new ElementNotFoundException( String.format( "edge \"%s\"",edgeId) );
			}
			else
			{
				String objData = org.getAttribute(ATTR_DATA);
			
				if( objData != null )
				{
					Object obj = ObjectCoder.decode( objData );
					e.changeAttribute(attrId,obj);
				}
				else
				{
					e.removeAttribute(attrId);
				}
				
				r = Boolean.TRUE;
			}
		}
		
		return r;
	}
	
	protected Object requestHasEdgeAttribute( KnownRequest req, Request org, IdentifiableObject source )
	{
		String edgeId = org.getAttribute(EDGE_ID);
		String attrId = org.getAttribute(ATTR_ID);
		String attrCond = org.getAttribute(ATTR_COND);
		
		AttributeCondition cond = attrCond == null ?
				AttributeCondition.NONE : AttributeCondition.valueOf( attrCond );
		
		Object r = null;
		
		if( edgeId == null || attrId == null )
		{
			r = new InvalidRequestFormatException( "need \"edgeId\" and \"attrId\" attributes");
		}
		else
		{
			r = distributedGraph.hasEdgeAttribute( edgeId, attrId, cond );
		}
		
		return r;
	}
	
	protected Object requestClearEdgeAttributes( KnownRequest req, Request org, IdentifiableObject source )
	{
		String edgeId = org.getAttribute(EDGE_ID);
		
		Object r = null;
		
		if( edgeId == null )
		{
			r = new InvalidRequestFormatException( "need \"edgeId\" attribute");
		}
		else
		{
			Edge e = distributedGraph.getLocalGraph().getEdge(edgeId);
			
			if( e == null )
			{
				r = new ElementNotFoundException( String.format( "node \"%s\"",edgeId) );
			}
			else
			{
				e.clearAttributes();
				r = Boolean.TRUE;
			}
		}
		
		return r;
	}
}
