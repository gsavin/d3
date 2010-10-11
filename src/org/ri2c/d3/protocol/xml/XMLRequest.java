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
package org.ri2c.d3.protocol.xml;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Request;

public class XMLRequest
	extends XMLStanza implements Request
{
	protected String source;
	
	public XMLRequest( String name )
	{
		super(name);
		this.source = Agency.getLocalAgency().getId();
	}
	
	public String getSource()
	{
		return source;
	}
	
	public void setSource( String source )
	{
		this.source = source;
	}

	public XMLStanza addChild( XMLStanza child )
	{
		if( child instanceof XMLRequest )
			return super.addChild(child);
		else return this;
	}
	
	public void addSubRequest( Request r )
	{
		if( r instanceof XMLStanza )
			addChild((XMLStanza) r);
	}
	
	public Request getSubRequest(int index)
	{
		return (XMLRequest) getChild(index);
	}

	public int getSubRequestCount()
	{
		return getChildrenCount();
	}

}
