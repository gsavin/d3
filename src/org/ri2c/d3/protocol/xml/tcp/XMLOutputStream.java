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
package org.ri2c.d3.protocol.xml.tcp;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

import org.ri2c.d3.protocol.xml.XMLStanza;

public class XMLOutputStream
{
	protected Charset 				charset;
	protected String 				headers;
	protected WritableByteChannel 	channel;
	protected Writer				out;
	
	public XMLOutputStream()
	{
		this( Charset.defaultCharset() );
	}
	
	public XMLOutputStream( Charset charset )
	{
		this.charset = charset;
		this.headers = String.format("<?xml version='1.0' encoding='%s'?>",charset.name());
	}
	
	public void init( WritableByteChannel channel )
		throws IOException
	{
		this.channel = channel;
		this.out     = Channels.newWriter(channel,charset.newEncoder(),-1);
		
		write(headers);
	}
	
	public void close()
	{
		try
		{
			channel.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
	
	public void sendStanza( XMLStanza stanza )
		throws IOException
	{
		write( stanza.toString() );
	}
	
	protected void write( String str ) 
		throws IOException
	{
		ByteBuffer buffer = charset.encode(str);
		channel.write(buffer);
	}
}
