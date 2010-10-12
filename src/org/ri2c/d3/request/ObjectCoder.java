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
package org.ri2c.d3.request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectCoder
{
	public static enum CodingMethod {
		HEXABYTES,
		BASE_64
	}
	
	private static byte [] encodeObject( Serializable obj )
	{
		try
		{
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream out 		= new ObjectOutputStream(bytes);
			
			out.writeObject(obj);
			out.flush();
			
			return bytes.toByteArray();
		}
		catch( Exception e ) {}
		
		return new byte [0];
	}
	
	private static String byte2hexa( byte [] data )
	{
		StringBuilder buffer = new StringBuilder();
		
		if( data != null )
		{
			for( int i = 0; i < data.length; i++ )
				buffer.append( String.format("%02X",data[i]) );
		}
		
		return buffer.toString();
	}
	
	private static byte [] hexa2byte( String hexa )
	{
		hexa = hexa.trim();
		
		if( ! hexa.matches("^[0-9a-fA-F]*$") )
			return new byte [0];
		
		byte [] data = new byte [hexa.length()/2];
		
		for( int i = 0; i < hexa.length()-1; i+=2 )
			data [i/2] = (byte) Integer.parseInt( hexa.substring(i,i+2),16);
		
		return data;
	}
	
	private static Object decodeObject( byte [] data )
	{
		try
		{
			ByteArrayInputStream bytes  = new ByteArrayInputStream(data);
			ObjectInputStream 	 in		= new ObjectInputStream(bytes);
			
			return in.readObject();
		}
		catch( Exception e ) {}
		
		return null;
	}
	
	public static Object decode( String hexa )
	{
		if( hexa == null )
			return null;
		
		byte [] data = hexa2byte(hexa);
		return decodeObject(data);
	}
	
	public static String encode( Serializable obj )
	{
		if( obj == null )
			return null;
		
		byte [] data = encodeObject(obj);
		return byte2hexa(data);
	}
	
	public static String encode( CodingMethod method, Serializable data ) {
		switch(method) {
		case HEXABYTES:
			return encode(data);
		}
		
		return null;
	}
	
	public static Object decode( CodingMethod method, String data )
	{
		switch(method) {
		case HEXABYTES:
			return decode(data);
		}
		
		return data;
	}
}
