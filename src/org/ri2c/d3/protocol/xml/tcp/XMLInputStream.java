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
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.modelmbean.XMLParseException;

import org.ri2c.d3.protocol.xml.XMLStanza;
import org.ri2c.d3.protocol.xml.XMLStanzaBuilder;
import org.ri2c.d3.protocol.xml.XMLStanzaFactory;

public class XMLInputStream
{
	protected static final String HEADERS_PATTERN = "<\\?xml(\\s+version=('[^']*'|\"[^\"]*\"))?(\\s+encoding=('[^']*'|\"[^\"]*\"))?\\s*\\?>";
	
	protected ReadableByteChannel 	channel;
	protected Charset				charset;
	protected Reader				in;
	protected XMLStanzaFactory		factory;
	//CharsetDecoder dec;
	
	public void init( XMLStanzaFactory factory, ReadableByteChannel channel )
	{
		this.channel = channel;
		this.charset = Charset.defaultCharset();
		this.factory = factory;
		//this.dec     = charset.newDecoder();
		readHeaders();
		
		in = Channels.newReader( channel, charset.newDecoder(), -1 );
	}
	
	protected void readHeaders()
	{
		ByteBuffer bbuffer = ByteBuffer.allocate(1);
		StringBuffer buffer = new StringBuffer();
		
		try
		{
			do
			{
				bbuffer.clear();
				channel.read(bbuffer);
				buffer.append( (char) bbuffer.get(0) );
			}
			while( bbuffer.get(0) != '>' );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		String headers = buffer.toString();
		
		if( ! headers.matches( HEADERS_PATTERN ) )
		{
			System.err.printf( "invalid xml headers: %s%n", headers );
			this.channel = null;
		}
		else
		{
			Pattern versionPattern = Pattern.compile( "version=(?:'([^']*)'|\"([^\"]*)\")" );
			Matcher version = versionPattern.matcher(headers);
			
			if( version.find() )
				System.out.printf("xml version: %s%n", version.group(1) );
			
			Pattern encodingPattern = Pattern.compile( "encoding=(?:'([^']*)'|\"([^\"]*)\")" );
			Matcher encoding = encodingPattern.matcher(headers);
			
			if( encoding.find() )
			{
				charset = Charset.forName(encoding.group(1));
				System.out.printf("xml encoding: %s (%s)%n", encoding.group(1), charset );
				
				if( charset == null )
				{
					System.err.printf("unknown encoding: %s%n", encoding.group(1));
					channel = null;
				}
				else
				{
					//this.dec     = charset.newDecoder();
				}
			}
		}
	}
	
	public XMLStanza nextStanza()
		throws IOException, XMLParseException
	{
		if( channel == null )
			return null;
		
		int 			count = 0;
		int 			c = -1;
		int				cLess1 = -1;
		StringBuffer 	buffer = new StringBuffer();
		
		cLess1 = c;
		
		c = in.read();
		
		if( c <= 0 )
		{
			return null;
		}
		
		buffer.append( (char) c );
		
		while( Character.isWhitespace( c ) )
		{
			cLess1 = c;
			
			c = in.read();
			
			buffer.append( (char) c );
		}
		
		if( c == '<' )
		{
			count++;
			
			while( ( c = in.read() ) != '>' )
			{
				cLess1 = c;
				buffer.append( (char) c );
			}

			buffer.append( (char) c );
			
			if( cLess1 == '/' )
				return XMLStanzaBuilder.string2stanza( factory, buffer.toString() );
			
			while( count > 0 )
			{
				cLess1 = c;
				c = in.read();
				buffer.append( (char) c );
				
				if( c == '<' )
				{
					cLess1 = c;
					c = in.read();
					buffer.append( (char) c );
					
					if( c == '/' )
						count--;
					else count++;
					
					while( c != '>' )
					{
						cLess1 = c;
						c = in.read();
						buffer.append( (char) c );
					}
					
					if( cLess1 == '/' ) count--;
				}
				else if( c == '>' && cLess1 == '/' )
				{
					count--;
				}
			}
			
			return XMLStanzaBuilder.string2stanza(factory,buffer.toString());
		}
		
		return null;
	}
}
