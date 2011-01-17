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

public interface DGConstants
{
	public static final String REQUEST_PREFIX = "distributedgraph:";
	
	public static final String NODE_ID = "nodeId";
	
	public static final String EDGE_ID = "edgeId";
	
	public static final String ATTR_ID = "attrId";
	
	public static final String ATTR_COND = "attrCond";
	
	public static final String ATTR_DATA = "attrData";
	
	public static final String NODE_DEGREE_MODE = "nodeDegreeMode"; 
	
	public static final String ELEMENT_ID = "elementId";
	
	public static final String ELEMENT_TYPE = "elementType";
	
	public static enum KnownRequest
	{
		HAS_NODE,
		HAS_EDGE,
		HOST_NODE,
		CLEAR_NODE_ATTRIBUTES,
		CLEAR_EDGE_ATTRIBUTES,
		GET_NODE_ATTRIBUTE_COUNT,
		GET_EDGE_ATTRIBUTE_COUNT,
		GET_NODE_ATTRIBUTE,
		SET_NODE_ATTRIBUTE,
		HAS_NODE_ATTRIBUTE,
		GET_EDGE_ATTRIBUTE,
		SET_EDGE_ATTRIBUTE,
		HAS_EDGE_ATTRIBUTE,
		GET_NODE_ATTRIBUTE_KEYS,
		GET_EDGE_ATTRIBUTE_KEYS,
		GET_NODE_DEGREE,
		DISPATCH_EVENT
	}
	
	public static enum ElementType
	{
		NODE,
		EDGE,
		GRAPH
	}
	
	public static enum NodeDegreeMode
	{
		IN,
		OUT,
		BOTH
	}
	
	public static enum AttributeCondition
	{
		NONE,
		IS_ARRAY,
		IS_LABEL,
		IS_NUMBER,
		IS_VECTOR
	}
}
