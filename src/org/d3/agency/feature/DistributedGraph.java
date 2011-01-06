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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeFactory;
import org.graphstream.graph.Element;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.NodeFactory;
import org.graphstream.stream.AttributeSink;
import org.graphstream.stream.ElementSink;
import org.graphstream.stream.GraphParseException;
import org.graphstream.stream.Sink;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSource;
import org.graphstream.ui.swingViewer.Viewer;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Args;
import org.ri2c.d3.Description;
import org.ri2c.d3.Future;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.RemoteIdentifiableObject;
import org.ri2c.d3.Request;
import org.ri2c.d3.agency.Feature;
import org.ri2c.d3.agency.FeatureDescription;
import org.ri2c.d3.agency.feature.distributedgraph.DGConstants;
import org.ri2c.d3.agency.feature.distributedgraph.DGRequestManager;
import org.ri2c.d3.agency.feature.distributedgraph.GraphComponent;
import org.ri2c.d3.agency.feature.distributedgraph.UnsatisfiedConditionException;
import org.ri2c.d3.agency.feature.distributedgraph.DGConstants.ElementType;
import org.ri2c.d3.protocol.Protocols;

public class DistributedGraph
	implements Feature, Graph, GraphComponent, DGConstants
{
	protected static FeatureDescription modelDescription =
		new FeatureDescription( "l2d.features.distributedgraph", "Distributed Graph", "" );
	
	String 							graphId;
	LinkedList<GraphComponent> 		components;
	HashMap<String,GraphComponent>	agencyToComponent;
	
	Graph							localGraph;
	
	DGRequestManager				requestManager;
	
	@SuppressWarnings("unchecked")
	public FeatureDescription getDescription()
	{
		return modelDescription;
	}

	public String getId()
	{
		return graphId;
	}

	public IdentifiableType getType()
	{
		return IdentifiableType.feature;
	}

	public String getAgencyId()
	{
		return Agency.getLocalAgency().getId();
	}
	
	public Graph getLocalGraph()
	{
		return localGraph;
	}
	
	public boolean initFeature(Agency agency, Args args)
	{
		// TODO Auto-generated method stub
		
		requestManager = new DGRequestManager(this);
		
		return false;
	}

	public void handleRequest(IdentifiableObject source,
			IdentifiableObject target, Request r)
	{
		requestManager.handleRequest(source, target, r);
	}
	
	public void moveNode( String id, GraphComponent component )
	{
		
	}
	
	public void checkKnownComponent( RemoteIdentifiableObject remote )
	{
		if( graphId.equals(remote.getId()) &&
				! agencyToComponent.containsKey(remote.getHost()) )
		{
			
		}
	}

	protected void checkEdgeExistence( String id )
		throws IdAlreadyInUseException
	{
		GraphComponent gc = findEdge(id);
		
		if( gc != null )
			throw new IdAlreadyInUseException( String.format( "edge found on \"%s\"", gc.getAgencyId() ) );
	}
	
	protected void checkNodeExistence( String id )
		throws IdAlreadyInUseException
	{
		GraphComponent gc = findNode(id);
		
		if( gc != null )
			throw new IdAlreadyInUseException( String.format( "node found on \"%s\"", gc.getAgencyId() ) );
	}
	
	protected GraphComponent findRemoteElement( String id, String attr, KnownRequest req )
	{
		HashMap<Future,GraphComponent> futures = new HashMap<Future,GraphComponent>();
		
		for( GraphComponent component: components )
		{
			Request r = Protocols.createRequestTo( this, component, REQUEST_PREFIX + req.name() );
			r.addAttribute(attr,id);
			
			Future f = Protocols.sendRequestWithFuture(component, r);
			f.interruptMeWhenDone();
			futures.put( f, component );
		}
		
		boolean b = false;
		
		Future current = null;
		
		while( futures.size() > 0 )
		{
			b = false;
			
			for( Future f : futures.keySet() )
			{
				if( f.isAvailable() )
				{
					current = f;
					break;
				}
			}

			if( current != null )
			{
				Object obj = current.getValue();

				boolean has = ( obj instanceof Boolean && (Boolean) obj );

				if( has )
				{
					return futures.get(current);
				}
				
				futures.remove(current);
				current = null;
				
				b = true;
			}
			
			try
			{
				if( ! b )
					Thread.sleep(200);
			}
			catch( InterruptedException e )
			{

			}
		}
		
		return null;
	}
	
	public GraphComponent findNode( String id )
	{
		if( localGraph.getNode(id) != null )
			return this;
		
		return findRemoteElement( id, "nodeId", KnownRequest.HAS_NODE );
	}
	
	public GraphComponent findEdge( String id )
	{
		if( localGraph.getEdge(id) != null )
			return this;
		
		return findRemoteElement( id, "edgeId", KnownRequest.HAS_EDGE );
	}
	
	// GraphComponent

	public Object clearNodeAttributes( String nodeId )
	{
		Node n = localGraph.getNode(nodeId);
		
		if( n != null )
		{
			n.clearAttributes();
			return Boolean.TRUE;
		}
		
		return new ElementNotFoundException( String.format( "node \"%s\"", nodeId ) );
	}
	
	public Object clearEdgeAttributes( String edgeId )
	{
		Edge e = localGraph.getEdge(edgeId);
		
		if( e != null )
		{
			e.clearAttributes();
			return Boolean.TRUE;
		}

		return new ElementNotFoundException( String.format( "edge \"%s\"", edgeId ) );
	}
	
	public Object getNodeAttributeCount( String nodeId )
	{
		Node n = localGraph.getNode(nodeId);
		
		if( n != null )
			return n.getAttributeCount();
		
		return new ElementNotFoundException( String.format( "node \"%s\"", nodeId ) );
	}
	
	public Object getEdgeAttributeCount( String edgeId )
	{
		Edge e = localGraph.getEdge(edgeId);
		
		if( e != null )
			return e.getAttributeCount();

		return new ElementNotFoundException( String.format( "edge \"%s\"", edgeId ) );
	}
	
	public Object getEdgeAttribute( String edgeId, String attrId,
			AttributeCondition cond )
	{
		Edge e = localGraph.getEdge(edgeId);
		
		if( e != null )
		{
			Object obj = e.getAttribute(attrId);
			
			if( cond == null || obj == null )
			{
				return obj;
			}
			else
			{
				switch( cond )
				{
				case IS_ARRAY:
					return obj.getClass().isArray() ? obj : new UnsatisfiedConditionException();
				case IS_LABEL:
					return obj instanceof CharSequence ? obj : new UnsatisfiedConditionException();
				case IS_NUMBER:
					return obj instanceof Number ? obj : new UnsatisfiedConditionException();
				case IS_VECTOR:
					return obj instanceof java.util.Vector<?> ? obj : new UnsatisfiedConditionException();
				}
			}
		}
		
		return new ElementNotFoundException( String.format( "edge \"%s\"", edgeId ) );
	}
	
	public Object getNodeAttribute(String nodeId, String attrId,
			AttributeCondition cond)
	{
		Node n = localGraph.getNode(nodeId);
		
		if( n != null )
		{
			Object obj = n.getAttribute(attrId);
			
			if( cond == null || obj == null )
			{
				return obj;
			}
			else
			{
				switch( cond )
				{
				case IS_ARRAY:
					return obj.getClass().isArray() ? obj : new UnsatisfiedConditionException();
				case IS_LABEL:
					return obj instanceof CharSequence ? obj : new UnsatisfiedConditionException();
				case IS_NUMBER:
					return obj instanceof Number ? obj : new UnsatisfiedConditionException();
				case IS_VECTOR:
					return n.getVector(attrId) == null ? new UnsatisfiedConditionException() : obj;
				case NONE:
					return obj;
				default:
					return new UnsatisfiedConditionException();	
				}
			}
		}
		
		return new ElementNotFoundException( String.format( "node \"%s\"", nodeId ) );
	}

	public Object setEdgeAttribute( String edgeId, String attrId, Object obj )
	{
		Edge e = localGraph.getEdge(edgeId);
		
		if( e != null )
		{
			e.changeAttribute( attrId, obj );
		}
		
		return Boolean.TRUE;
	}

	public Object setNodeAttribute( String nodeId, String attrId, Object obj )
	{
		Node n = localGraph.getNode(nodeId);
		
		if( n != null )
		{
			n.changeAttribute( attrId, obj );
		}
		
		return Boolean.TRUE;
	}
	
	public Object hasNodeAttribute( String nodeId, String attrId, AttributeCondition cond )
	{
		Node n = localGraph.getNode(nodeId);
		
		Object r;
		
		if( n != null )
		{
			if( cond == null )
				cond = AttributeCondition.NONE;
			
			switch( cond )
			{
			case NONE:
				r = n.hasAttribute(attrId);
				break;
			case IS_ARRAY:
				r = n.hasArray(attrId);
				break;
			case IS_LABEL:
				r = n.hasLabel(attrId);
				break;
			case IS_VECTOR:
				r = n.hasVector(attrId);
				break;
			case IS_NUMBER:
				r = n.hasNumber(attrId);
				break;
			default:
				r = false;
			}
		}
		else
		{
			r = new ElementNotFoundException( String.format( "node \"%s\"", nodeId ) );
		}
		
		return r;
	}
	
	public Object hasEdgeAttribute( String edgeId, String attrId, AttributeCondition cond )
	{
		Edge e = localGraph.getEdge(edgeId);
		
		Object r;
		
		if( e != null )
		{
			if( cond == null )
				cond = AttributeCondition.NONE;
			
			switch( cond )
			{
			case NONE:
				r = e.hasAttribute(attrId);
				break;
			case IS_ARRAY:
				r = e.hasArray(attrId);
				break;
			case IS_LABEL:
				r = e.hasLabel(attrId);
				break;
			case IS_VECTOR:
				r = e.hasVector(attrId);
				break;
			case IS_NUMBER:
				r = e.hasNumber(attrId);
				break;
			default:
				r = false;
			}
		}
		else
		{
			r = new ElementNotFoundException( String.format( "edge \"%s\"", edgeId ) );
		}
		
		return r;
	}
	
	public Object getNodeDegree( String nodeId, NodeDegreeMode mode )
	{
		Node n = localGraph.getNode(nodeId);
		
		if( n == null )
		{
			return new ElementNotFoundException( String.format( "node \"%s\"", nodeId ) );
		}
		else
		{
			switch(mode)
			{
			case IN:
				return n.getInDegree();
			case OUT:
				return n.getOutDegree();
			case BOTH:
				return n.getDegree();
			default:
				return -1;
			}
		}
	}
	
	public Object getElementAttributeKeys( String elementId, ElementType type )
	{
		try
		{
			return getElement(elementId,type).getAttributeKeySet();
		}
		catch( ElementNotFoundException e )
		{
			return e;
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
			e = localGraph.getNode(elementId);
		case EDGE:
			e = localGraph.getEdge(elementId);
		case GRAPH:
			if( localGraph.getId().equals(elementId) )
				e = localGraph;
		}
		
		if( e == null )
			throw new ElementNotFoundException( String.format( "%s \"%s\"", type.name(), elementId ) );
		
		return e;
	}
	
///////////////////////////////////////////////////////////
	
	public <T extends Edge> T addEdge(String id, String node1, String node2)
			throws IdAlreadyInUseException, ElementNotFoundException
	{
		checkEdgeExistence(id);
		
		return null;
	}

	public <T extends Edge> T addEdge(String id, String from, String to, boolean directed)
			throws IdAlreadyInUseException, ElementNotFoundException
	{
		checkEdgeExistence(id);
		
		return null;
	}

	public <T extends Node> T addNode(String id)
		throws IdAlreadyInUseException
	{
		checkNodeExistence(id);
		
		return null;
	}

	public <T> T getAttribute(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T getFirstAttributeOf(String... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T getAttribute(String key, Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T getFirstAttributeOf(Class<T> clazz, String... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	public CharSequence getLabel(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public double getNumber(String key) {
		// TODO Auto-generated method stub
		return 0;
	}

	public ArrayList<? extends Number> getVector(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] getArray(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public HashMap<?, ?> getHash(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasAttribute(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasAttribute(String key, Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasLabel(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasNumber(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasVector(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasArray(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasHash(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	public Iterator<String> getAttributeKeyIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterable<String> getAttributeKeySet() {
		// TODO Auto-generated method stub
		return null;
	}

	public void clearAttributes() {
		// TODO Auto-generated method stub
		
	}

	public void addAttribute(String attribute, Object... values) {
		// TODO Auto-generated method stub
		
	}

	public void changeAttribute(String attribute, Object... values) {
		// TODO Auto-generated method stub
		
	}

	public void setAttribute(String attribute, Object... values) {
		// TODO Auto-generated method stub
		
	}

	public void addAttributes(Map<String, Object> attributes) {
		// TODO Auto-generated method stub
		
	}

	public void removeAttribute(String attribute) {
		// TODO Auto-generated method stub
		
	}

	public int getAttributeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void addSink(Sink sink) {
		// TODO Auto-generated method stub
		
	}

	public void removeSink(Sink sink) {
		// TODO Auto-generated method stub
		
	}

	public void addAttributeSink(AttributeSink sink) {
		// TODO Auto-generated method stub
		
	}

	public void removeAttributeSink(AttributeSink sink) {
		// TODO Auto-generated method stub
		
	}

	public void addElementSink(ElementSink sink) {
		// TODO Auto-generated method stub
		
	}

	public void removeElementSink(ElementSink sink) {
		// TODO Auto-generated method stub
		
	}

	public void clearElementSinks() {
		// TODO Auto-generated method stub
		
	}

	public void clearAttributeSinks() {
		// TODO Auto-generated method stub
		
	}

	public void clearSinks() {
		// TODO Auto-generated method stub
		
	}

	public void graphAttributeAdded(String sourceId, long timeId,
			String attribute, Object value) {
		// TODO Auto-generated method stub
		
	}

	public void graphAttributeChanged(String sourceId, long timeId,
			String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	public void graphAttributeRemoved(String sourceId, long timeId,
			String attribute) {
		// TODO Auto-generated method stub
		
	}

	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
			String attribute, Object value) {
		// TODO Auto-generated method stub
		
	}

	public void nodeAttributeChanged(String sourceId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	public void nodeAttributeRemoved(String sourceId, long timeId,
			String nodeId, String attribute) {
		// TODO Auto-generated method stub
		
	}

	public void edgeAttributeAdded(String sourceId, long timeId, String edgeId,
			String attribute, Object value) {
		// TODO Auto-generated method stub
		
	}

	public void edgeAttributeChanged(String sourceId, long timeId,
			String edgeId, String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	public void edgeAttributeRemoved(String sourceId, long timeId,
			String edgeId, String attribute) {
		// TODO Auto-generated method stub
		
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		// TODO Auto-generated method stub
		
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		// TODO Auto-generated method stub
		
	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		// TODO Auto-generated method stub
		
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		// TODO Auto-generated method stub
		
	}

	public void graphCleared(String sourceId, long timeId) {
		// TODO Auto-generated method stub
		
	}

	public void stepBegins(String sourceId, long timeId, double step) {
		// TODO Auto-generated method stub
		
	}

	public Iterator<Node> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends Node> T getNode(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends Edge> T getEdge(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNodeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getEdgeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public <T extends Node> Iterator<T> getNodeIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends Edge> Iterator<T> getEdgeIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends Node> Iterable<? extends T> getEachNode() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends Edge> Iterable<? extends T> getEachEdge() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends Node> Collection<T> getNodeSet() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends Edge> Collection<T> getEdgeSet() {
		// TODO Auto-generated method stub
		return null;
	}

	public NodeFactory<? extends Node> nodeFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	public EdgeFactory<? extends Edge> edgeFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isStrict() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isAutoCreationEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public double getStep() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setNodeFactory(NodeFactory<? extends Node> nf) {
		// TODO Auto-generated method stub
		
	}

	public void setEdgeFactory(EdgeFactory<? extends Edge> ef) {
		// TODO Auto-generated method stub
		
	}

	public void setStrict(boolean on) {
		// TODO Auto-generated method stub
		
	}

	public void setAutoCreate(boolean on) {
		// TODO Auto-generated method stub
		
	}

	public void clear() {
		// TODO Auto-generated method stub
		
	}

	public <T extends Node> T removeNode(String id)
			throws ElementNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends Edge> T removeEdge(String from, String to)
			throws ElementNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends Edge> T removeEdge(String id)
			throws ElementNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	public void stepBegins(double time) {
		// TODO Auto-generated method stub
		
	}

	public Iterable<AttributeSink> attributeSinks() {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterable<ElementSink> elementSinks() {
		// TODO Auto-generated method stub
		return null;
	}

	public void read(String filename) throws IOException, GraphParseException,
			ElementNotFoundException {
		// TODO Auto-generated method stub
		
	}

	public void read(FileSource input, String filename) throws IOException,
			GraphParseException {
		// TODO Auto-generated method stub
		
	}

	public void write(String filename) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void write(FileSink output, String filename) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public Viewer display() {
		// TODO Auto-generated method stub
		return null;
	}

	public Viewer display(boolean autoLayout) {
		// TODO Auto-generated method stub
		return null;
	}
}
