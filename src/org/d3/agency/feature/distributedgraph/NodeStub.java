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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.ri2c.d3.Console;
import org.ri2c.d3.agency.feature.DistributedGraph;

public class NodeStub
	implements Node, DGConstants
{
	final String id;
	DistributedGraph distributedGraph;
	GraphComponent component;
	
	public NodeStub( String id, DistributedGraph distributedGraph )
	{
		this.id 		= id;
		this.component 	= distributedGraph.findNode(id);
	}
	
	public Iterator<? extends Node> getBreadthFirstIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator<? extends Node> getBreadthFirstIterator(boolean directed) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getDegree()
	{
		Object r = component.getNodeDegree(id, NodeDegreeMode.BOTH);
		
		if( r instanceof ElementNotFoundException )
		{
			component = distributedGraph.findNode(id);
			
			if( component != null )
			{
				r = component.getNodeDegree(id, NodeDegreeMode.IN);
			}
			else
			{
				throw new Error( "node component is null" );
			}
		}
		
		if( r instanceof Exception )
		{
			
		}
		else if( r instanceof Number )
		{
			return ( (Number) r ).intValue();
		}
		
		return -1;
	}

	public Iterator<? extends Node> getDepthFirstIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator<? extends Node> getDepthFirstIterator(boolean directed) {
		// TODO Auto-generated method stub
		return null;
	}

	public Edge getEdge(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public Edge getEdgeFrom(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public Iterator<? extends Edge> getEdgeIterator()
	{
		Iterable<? extends Edge> ite = getEdgeSet();
		
		if( ite != null )
		{
			return ite.iterator();
		}
		else
		{
			return Collections.EMPTY_LIST.iterator();
		}
	}

	public Iterable<? extends Edge> getEdgeSet() {
		// TODO Auto-generated method stub
		return null;
	}

	public Edge getEdgeToward(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public Iterator<? extends Edge> getEnteringEdgeIterator()
	{
		Iterable<? extends Edge> ite = getEnteringEdgeSet();
		
		if( ite != null )
		{
			return ite.iterator();
		}
		else
		{
			return Collections.EMPTY_LIST.iterator();
		}
	}

	public Iterable<? extends Edge> getEnteringEdgeSet() {
		// TODO Auto-generated method stub
		return null;
	}

	public Graph getGraph()
	{
		return distributedGraph;
	}

	public int getInDegree()
	{
		Object r = component.getNodeDegree(id, NodeDegreeMode.IN);
		
		if( r instanceof ElementNotFoundException )
		{
			component = distributedGraph.findNode(id);
			
			if( component != null )
			{
				r = component.getNodeDegree(id, NodeDegreeMode.IN);
			}
			else
			{
				throw new Error( "node component is null" );
			}
		}
		
		if( r instanceof Exception )
		{
			
		}
		else if( r instanceof Number )
		{
			return ( (Number) r ).intValue();
		}
		
		return -1;
	}

	@SuppressWarnings("unchecked")
	public Iterator<? extends Edge> getLeavingEdgeIterator()
	{
		Iterable<? extends Edge> ite = getLeavingEdgeSet();
		
		if( ite != null )
		{
			return ite.iterator();
		}
		else
		{
			return Collections.EMPTY_LIST.iterator();
		}
	}

	public Iterable<? extends Edge> getLeavingEdgeSet() {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator<? extends Node> getNeighborNodeIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getOutDegree()
	{
		Object r = component.getNodeDegree(id, NodeDegreeMode.OUT);
		
		if( r instanceof ElementNotFoundException )
		{
			component = distributedGraph.findNode(id);
			
			if( component != null )
			{
				r = component.getNodeDegree(id, NodeDegreeMode.IN);
			}
			else
			{
				throw new Error( "node component is null" );
			}
		}
		
		if( r instanceof Exception )
		{
			
		}
		else if( r instanceof Number )
		{
			return ( (Number) r ).intValue();
		}
		
		return -1;
	}

	public boolean hasEdgeFrom(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasEdgeToward(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	public void addAttribute(String attribute, Object... values)
	{
		Object r = component.setNodeAttribute( id, attribute, values );
		
		if( r instanceof ElementNotFoundException )
		{
			component = distributedGraph.findNode(id);
			
			if( component != null )
			{
				addAttribute( attribute, values );
			}
			else
			{
				throw new Error( "node component is null" );
			}
		}
	}

	public void addAttributes(Map<String, Object> attributes)
	{
		for( String key: attributes.keySet() )
			addAttribute( key, attributes.get(key) );
	}

	public void changeAttribute(String attribute, Object... values)
	{
		Object r = component.setNodeAttribute( id, attribute, values );
		
		if( r instanceof ElementNotFoundException )
		{
			component = distributedGraph.findNode(id);
			
			if( component != null )
			{
				r = component.setNodeAttribute( id, attribute, values );
			}
			else
			{
				throw new Error( "node component is null" );
			}
		}
	}

	public void clearAttributes()
	{
		Object r = component.clearNodeAttributes(id);
		
		if( r instanceof ElementNotFoundException )
		{
			component = distributedGraph.findNode(id);
			
			if( component != null )
			{
				r = component.clearNodeAttributes(id);
			}
			else
			{
				throw new Error( "node component is null" );
			}
		}
	}

	public Object[] getArray(String key)
	{
		Object r = component.getNodeAttribute( id, key, AttributeCondition.IS_ARRAY );
		
		if( r instanceof ElementNotFoundException )
		{
			component = distributedGraph.findNode(id);
			
			if( component != null )
			{
				r = component.getNodeAttribute( id, key, AttributeCondition.IS_ARRAY );
			}
			else
			{
				throw new Error( "node component is null" );
			}
		}
		else if( r instanceof UnsatisfiedConditionException )
		{
			return null;
		}
		
		if( r.getClass().isArray() )
			return (Object[]) r;
		else return null;
	}

	public Object getAttribute(String key)
	{
		Object r = component.getNodeAttribute( id, key, AttributeCondition.NONE );
		
		if( r instanceof ElementNotFoundException )
		{
			component = distributedGraph.findNode(id);
			
			if( component != null )
			{
				r = component.getNodeAttribute( id, key, AttributeCondition.NONE );
			
				if( r instanceof ElementNotFoundException )
				{
					Console.warning( "node \"%s\" can not be found", id );
					return null;
				}
			}
			else
			{
				throw new Error( "node component is null" );
			}
		}
		
		return r;
	}

	public Object getAttribute(String key, Class<?> clazz)
	{
		// TODO
		Exception e = new Exception();
		throw new Error( String.format("not yet implemented: %s:%s", e.getStackTrace()[0].getClassName(), e.getStackTrace()[0].getMethodName()) );
	}

	public int getAttributeCount()
	{
		Object r = component.getNodeAttributeCount(id);
		
		if( r instanceof ElementNotFoundException )
		{
			component = distributedGraph.findNode(id);
			
			if( component != null )
			{
				r = component.getNodeAttributeCount(id);
			
				if( r instanceof ElementNotFoundException )
				{
					Console.warning( "node \"%s\" can not be found", id );
					return -1;
				}
			}
			else
			{
				throw new Error( "node component is null" );
			}
		}
		
		if( r instanceof Number )
			return ( (Number) r ).intValue();
		
		return -1;
	}

	public Iterator<String> getAttributeKeyIterator()
	{
		return getAttributeKeySet().iterator(); 
	}

	@SuppressWarnings("unchecked")
	public Iterable<String> getAttributeKeySet()
	{
		Object r = component.getElementAttributeKeys(id,ElementType.NODE);
		
		if( r instanceof ElementNotFoundException )
		{
			component = distributedGraph.findNode(id);
			
			if( component != null )
			{
				r = component.getElementAttributeKeys(id,ElementType.NODE);
			}
			else
			{
				throw new Error( "node component is null" );
			}
		}
		
		if( r instanceof Iterable<?> )
		{
			try
			{
				return ( Iterable<String> ) r;
			}
			catch( ClassCastException e )
			{
				Console.warning( "can not get an iterable<string>" );
			}
		}
		else if( r instanceof Exception )
		{
			Console.warning( "can not get an iterable<string>" );
		}
		
		return Collections.EMPTY_LIST;
	}

	public Object getFirstAttributeOf(String... keys)
	{
		// TODO
		Exception e = new Exception();
		throw new Error( String.format("not yet implemented: %s:%s", e.getStackTrace()[0].getClassName(), e.getStackTrace()[0].getMethodName()) );
	}

	public Object getFirstAttributeOf(Class<?> clazz, String... keys)
	{
		// TODO
		Exception e = new Exception();
		throw new Error( String.format("not yet implemented: %s:%s", e.getStackTrace()[0].getClassName(), e.getStackTrace()[0].getMethodName()) );
	}

	public HashMap<?, ?> getHash(String key)
	{
		// TODO
		Exception e = new Exception();
		throw new Error( String.format("not yet implemented: %s:%s", e.getStackTrace()[0].getClassName(), e.getStackTrace()[0].getMethodName()) );
	}

	public String getId()
	{
		return id;
	}

	public CharSequence getLabel(String key)
	{
		Object r = component.getNodeAttribute( id, key, AttributeCondition.IS_LABEL );
		
		if( r instanceof ElementNotFoundException )
		{
			component = distributedGraph.findNode(id);
			
			if( component != null )
			{
				r = component.getNodeAttribute( id, key, AttributeCondition.IS_LABEL );
				
				if( r instanceof ElementNotFoundException )
				{
					Console.warning( "node \"%s\" can not be found", id );
					return null;
				}
			}
			else
			{
				throw new Error( "node component is null" );
			}
		}
		else if( r instanceof UnsatisfiedConditionException )
		{
			return null;
		}
		
		return (CharSequence) r;
	}

	public double getNumber(String key)
	{
		Object r = component.getNodeAttribute( id, key, AttributeCondition.IS_NUMBER );
		
		if( r instanceof ElementNotFoundException )
		{
			component = distributedGraph.findNode(id);
			
			if( component != null )
			{
				r = component.getNodeAttribute( id, key, AttributeCondition.IS_NUMBER );
				
				if( r instanceof ElementNotFoundException )
				{
					Console.warning( "node \"%s\" can not be found", id );
					return Double.NaN;
				}
			}
			else
			{
				throw new Error( "node component is null" );
			}
		}
		else if( r instanceof UnsatisfiedConditionException )
		{
			return Double.NaN;
		}
		
		return ( (Number) r ).doubleValue();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<? extends Number> getVector(String key)
	{
		Object r = component.getNodeAttribute( id, key, AttributeCondition.IS_VECTOR );
		
		if( r instanceof ElementNotFoundException )
		{
			component = distributedGraph.findNode(id);
			
			if( component != null )
			{
				r = component.getNodeAttribute( id, key, AttributeCondition.IS_VECTOR );
				
				if( r instanceof ElementNotFoundException )
				{
					Console.warning( "node \"%s\" can not be found", id );
					return null;
				}
			}
			else
			{
				throw new Error( "node component is null" );
			}
		}
		else if( r instanceof UnsatisfiedConditionException )
		{
			return null;
		}
		
		return (ArrayList<? extends Number>) r;
	}

	protected boolean hasAttributeWithCondition( String key, AttributeCondition cond )
	{
		Object r = component.hasNodeAttribute( id, key, cond );

		if( r instanceof ElementNotFoundException )
		{
			component = distributedGraph.findNode(id);
			
			if( component != null )
			{
				r = component.hasNodeAttribute( id, key, cond );
				
				if( r instanceof ElementNotFoundException )
				{
					Console.warning( "node \"%s\" can not be found", id );
					return false;
				}
			}
			else
			{
				throw new Error( "node component is null" );
			}
		}
		else if( r instanceof UnsatisfiedConditionException )
		{
			return false;
		}
		
		if( r != null )
		{
			if( r instanceof Boolean )
				return (Boolean) r;
		}
		
		return false;
	}
	
	public boolean hasArray(String key)
	{
		return hasAttributeWithCondition( key, AttributeCondition.IS_ARRAY );
	}

	public boolean hasAttribute(String key)
	{
		return hasAttributeWithCondition( key, AttributeCondition.NONE );
	}

	public boolean hasAttribute(String key, Class<?> clazz)
	{
		// TODO
		Exception e = new Exception();
		throw new Error( String.format("not yet implemented: %s:%s", e.getStackTrace()[0].getClassName(), e.getStackTrace()[0].getMethodName()) );
	}

	public boolean hasHash(String key)
	{
		// TODO
		Exception e = new Exception();
		throw new Error( String.format("not yet implemented: %s:%s", e.getStackTrace()[0].getClassName(), e.getStackTrace()[0].getMethodName()) );
	}

	public boolean hasLabel(String key)
	{
		return hasAttributeWithCondition( key, AttributeCondition.IS_LABEL );
	}

	public boolean hasNumber(String key)
	{
		return hasAttributeWithCondition( key, AttributeCondition.IS_NUMBER );
	}

	public boolean hasVector(String key)
	{
		return hasAttributeWithCondition( key, AttributeCondition.IS_VECTOR );
	}

	public void removeAttribute(String attribute)
	{
		Object r = component.setNodeAttribute( id, attribute, null );
		
		if( r instanceof ElementNotFoundException )
		{
			component = distributedGraph.findNode(id);
			
			if( component != null )
			{
				r = component.setNodeAttribute( id, attribute, null );
				
				if( r instanceof ElementNotFoundException )
				{
					Console.warning( "node \"%s\" can not be found", id );
				}
			}
			else
			{
				throw new Error( "node component is null" );
			}
		}
	}

	public void setAttribute(String attribute, Object... values)
	{
		Object r = component.setNodeAttribute( id, attribute, values );
		
		if( r instanceof ElementNotFoundException )
		{
			component = distributedGraph.findNode(id);
			
			if( component != null )
			{
				r = component.setNodeAttribute( id, attribute, null );
				
				if( r instanceof ElementNotFoundException )
				{
					Console.warning( "node \"%s\" can not be found", id );
				}
			}
			else
			{
				throw new Error( "node component is null" );
			}
		}
	}

	public Iterator<Edge> iterator()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
