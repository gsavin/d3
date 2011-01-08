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

import java.io.Serializable;

import org.ri2c.d3.Console;
import org.ri2c.d3.Future;
import org.ri2c.d3.RemoteIdentifiableObject;
import org.ri2c.d3.Request;
import org.ri2c.d3.agency.feature.DistributedGraph;
import org.ri2c.d3.protocol.Protocols;
import org.ri2c.d3.request.ObjectCoder;

public class RemoteGraphComponent
	extends RemoteActor implements GraphComponent, DGConstants
{
	DistributedGraph distributedGraph;
	
	public RemoteGraphComponent(String agencyId, String graphId )
	{
		super(agencyId, graphId, IdentifiableType.feature);
	}

	public String getAgencyId()
	{
		return getHost();
	}
	
	public Object clearNodeAttributes( String nodeId )
	{
		Request r = Protocols.createRequestTo( distributedGraph, this,
				REQUEST_PREFIX + KnownRequest.CLEAR_NODE_ATTRIBUTES );
		
		r.addAttribute( NODE_ID, nodeId );
		
		Future f = Protocols.sendRequestWithFuture( this, r );
		Object x = f.getValue();
		
		if( x == null )
			x = new NullPointerException();
		
		if( x instanceof Exception )
		{
			Console.warning( "%s occured while clearing node attributes: %s", x.getClass().getSimpleName(), ( (Exception) x ).getMessage() );
		}
		
		return x;
	}
	
	public Object clearEdgeAttributes( String edgeId )
	{
		Request r = Protocols.createRequestTo( distributedGraph, this,
				REQUEST_PREFIX + KnownRequest.CLEAR_EDGE_ATTRIBUTES );
		
		r.addAttribute( EDGE_ID, edgeId );
		
		Future f = Protocols.sendRequestWithFuture( this, r );
		Object x = f.getValue();
		
		if( x == null )
			x = new NullPointerException();
		
		if( x instanceof Exception )
		{
			Console.warning( "%s occured while clearing edge attributes: %s", x.getClass().getSimpleName(), ( (Exception) x ).getMessage() );
		}
		
		return x;
	}
	
	public Object getNodeAttributeCount( String nodeId )
	{
		Request r = Protocols.createRequestTo( distributedGraph, this,
				REQUEST_PREFIX + KnownRequest.GET_NODE_ATTRIBUTE_COUNT );
		
		r.addAttribute( NODE_ID, nodeId );
		
		Future f = Protocols.sendRequestWithFuture( this, r );
		Object x = f.getValue();
		
		if( x == null )
			x = new NullPointerException();
		
		if( x instanceof Exception )
		{
			Console.warning( "%s occured while getting node attribute count: %s", x.getClass().getSimpleName(), ( (Exception) x ).getMessage() );
		}
		
		return x;
	}
	
	public Object getEdgeAttributeCount( String edgeId )
	{
		Request r = Protocols.createRequestTo( distributedGraph, this,
				REQUEST_PREFIX + KnownRequest.GET_EDGE_ATTRIBUTE_COUNT );
		
		r.addAttribute( EDGE_ID, edgeId );
		
		Future f = Protocols.sendRequestWithFuture( this, r );
		Object x = f.getValue();
		
		if( x == null )
			x = new NullPointerException();
		
		if( x instanceof Exception )
		{
			Console.warning( "%s occured while getting edge attribute count: %s", x.getClass().getSimpleName(), ( (Exception) x ).getMessage() );
		}
		
		return x;
	}
	
	public Object getNodeAttribute( String nodeId, String attrId, AttributeCondition cond )
	{
		Request r = Protocols.createRequestTo( distributedGraph, this,
				REQUEST_PREFIX + KnownRequest.GET_NODE_ATTRIBUTE );
		
		r.addAttribute( NODE_ID, nodeId );
		r.addAttribute( ATTR_ID, attrId );
		
		if( cond != null && cond != AttributeCondition.NONE )
			r.addAttribute( ATTR_COND, cond.name() );
		
		Future f = Protocols.sendRequestWithFuture( this, r );
		Object x = f.getValue();
		
		if( x == null )
			x = new NullPointerException();
		
		if( x instanceof Exception )
		{
			Console.warning( "%s occured while getting node attribute: %s", x.getClass().getSimpleName(), ( (Exception) x ).getMessage() );
		}
		
		return x;
	}
	/*
	public Object getEdgeAttribute( String edgeId, String attrId )
	{
		return getEdgeAttribute( edgeId, attrId, AttributeCondition.none );
	}
	*/
	public Object getEdgeAttribute( String edgeId, String attrId, AttributeCondition cond )
	{
		Request r = Protocols.createRequestTo( distributedGraph, this,
				REQUEST_PREFIX + KnownRequest.GET_EDGE_ATTRIBUTE );
		
		r.addAttribute( EDGE_ID, edgeId );
		r.addAttribute( ATTR_ID, attrId );
		
		if( cond != null && cond != AttributeCondition.NONE )
			r.addAttribute( ATTR_COND, cond.name() );
		
		Future f = Protocols.sendRequestWithFuture( this, r );
		Object x = f.getValue();
		
		if( x == null )
			x = new NullPointerException();
		
		if( x instanceof Exception )
		{
			Console.warning( "%s occured while getting edge attribute: %s", x.getClass().getSimpleName(), ( (Exception) x ).getMessage() );
		}
		
		return x;
	}
	
	public Object setNodeAttribute( String nodeId, String attrId, Object obj )
	{
		Request r = Protocols.createRequestTo( distributedGraph, this,
				REQUEST_PREFIX + KnownRequest.SET_NODE_ATTRIBUTE );
		
		r.addAttribute( NODE_ID, nodeId );
		r.addAttribute( ATTR_ID, attrId );
		r.addAttribute( ATTR_DATA, ObjectCoder.encode( (Serializable) obj ) );
		
		Future f = Protocols.sendRequestWithFuture( this, r );
		Object x = f.getValue();
		
		if( x == null )
		{
			Console.warning( "get a null reply while setting node attribute" );
			return Boolean.FALSE;
		}
		else
		{
			if( x instanceof Exception )
			{
				Console.warning( "get a %s while setting node attribute: %s", x.getClass().getSimpleName(), ((Exception) x).getMessage() );
				return x;
			}
			else
			{
				return x;
			}
		}
	}
	
	public Object setEdgeAttribute( String edgeId, String attrId, Object obj )
	{
		Request r = Protocols.createRequestTo( distributedGraph, this,
				REQUEST_PREFIX + KnownRequest.SET_EDGE_ATTRIBUTE );
		
		r.addAttribute( EDGE_ID, edgeId );
		r.addAttribute( ATTR_ID, attrId );
		r.addAttribute( ATTR_DATA, ObjectCoder.encode( (Serializable) obj ) );
		
		Future f = Protocols.sendRequestWithFuture( this, r );
		Object x = f.getValue();
		
		if( x == null )
		{
			Console.warning( "get a null reply while setting edge attribute" );
			return Boolean.FALSE;
		}
		else
		{
			if( x instanceof Exception )
			{
				Console.warning( "get a %s while setting edge attribute: %s", x.getClass().getSimpleName(), ((Exception) x).getMessage() );
				return x;
			}
			else
			{
				return x;
			}
		}
	}

	public Object hasEdgeAttribute(String edgeId, String attrId,
			AttributeCondition cond)
	{
		Request r = Protocols.createRequestTo( distributedGraph, this,
				REQUEST_PREFIX + KnownRequest.HAS_EDGE_ATTRIBUTE );
		
		r.addAttribute( EDGE_ID, edgeId );
		r.addAttribute( ATTR_ID, attrId );
		
		if( cond ==  null )
			cond = AttributeCondition.NONE;
		
		r.addAttribute( ATTR_COND, cond.name() );
		
		Future f = Protocols.sendRequestWithFuture( this, r );
		Object x = f.getValue();
		
		if( x == null )
		{
			Console.warning( "get a null reply while testing edge attribute" );
			return Boolean.FALSE;
		}
		else
		{
			if( x instanceof Exception )
			{
				Console.warning( "get a %s while testing edge attribute: %s", x.getClass().getSimpleName(), ((Exception) x).getMessage() );
				return x;
			}
			else
			{
				return x;
			}
		}
	}

	public Object hasNodeAttribute(String nodeId, String attrId,
			AttributeCondition cond)
	{
		Request r = Protocols.createRequestTo( distributedGraph, this,
				REQUEST_PREFIX + KnownRequest.HAS_NODE_ATTRIBUTE );
		
		r.addAttribute( NODE_ID, nodeId );
		r.addAttribute( ATTR_ID, attrId );
		
		if( cond ==  null )
			cond = AttributeCondition.NONE;
		
		r.addAttribute( ATTR_COND, cond.name() );
		
		Future f = Protocols.sendRequestWithFuture( this, r );
		Object x = f.getValue();
		
		if( x == null )
		{
			Console.warning( "get a null reply while testing node attribute" );
			return Boolean.FALSE;
		}
		else
		{
			if( x instanceof Exception )
			{
				Console.warning( "get a %s while testing node attribute: %s", x.getClass().getSimpleName(), ((Exception) x).getMessage() );
				return x;
			}
			else
			{
				return x;
			}
		}
	}
	
	public Object getNodeDegree( String nodeId, NodeDegreeMode mode )
	{
		Request r = Protocols.createRequestTo( distributedGraph, this,
				REQUEST_PREFIX + KnownRequest.GET_NODE_DEGREE );
		
		if( mode == null )
			mode = NodeDegreeMode.BOTH;
		
		r.addAttribute( NODE_ID, nodeId );
		r.addAttribute( NODE_DEGREE_MODE, mode.name() );
		
		Future f = Protocols.sendRequestWithFuture( this, r );
		Object x = f.getValue();
		
		if( x == null )
			x = new NullPointerException();
		
		if( x instanceof Exception )
		{
			Console.warning( "%s occured while getting node attribute count: %s", x.getClass().getSimpleName(), ( (Exception) x ).getMessage() );
		}
		
		return x;
	}
	
	public Object getNodeAttributeKeys( String nodeId )
	{
		Request r = Protocols.createRequestTo( distributedGraph, this,
				REQUEST_PREFIX + KnownRequest.GET_NODE_ATTRIBUTE_KEYS );
		
		r.addAttribute( NODE_ID, nodeId );
		
		Future f = Protocols.sendRequestWithFuture( this, r );
		Object x = f.getValue();
		
		if( x == null )
			x = new NullPointerException();
		
		if( x instanceof Exception )
		{
			Console.warning( "%s occured while getting node attribute keys: %s", x.getClass().getSimpleName(), ( (Exception) x ).getMessage() );
		}
		
		return x;
	}
	
	public Object getEdgeAttributeKeys( String edgeId )
	{
		Request r = Protocols.createRequestTo( distributedGraph, this,
				REQUEST_PREFIX + KnownRequest.GET_EDGE_ATTRIBUTE_KEYS );
		
		r.addAttribute( EDGE_ID, edgeId );
		
		Future f = Protocols.sendRequestWithFuture( this, r );
		Object x = f.getValue();
		
		if( x == null )
			x = new NullPointerException();
		
		if( x instanceof Exception )
		{
			Console.warning( "%s occured while getting edge attribute keys: %s", x.getClass().getSimpleName(), ( (Exception) x ).getMessage() );
		}
		
		return x;
	}
}
