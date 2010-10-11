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
package org.ri2c.d3.atlas.internal;

import java.util.concurrent.atomic.AtomicBoolean;

import org.ri2c.d3.Future;

public class D3Future
	implements Future
{
	Object 			value;
	AtomicBoolean 	available;
	Thread			thread2interrupt;
	
	public D3Future()
	{
		this.value 		= null;
		this.available 	= new AtomicBoolean(false);
	}
	
	public Object getValue()
	{
		synchronized(available)
		{
			try
			{
				if( ! available.get() )
					available.wait();
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		
		return value;
	}
	
	public void init( Object value )
	{
		this.value = value;
		
		synchronized(available)
		{
			available.set(true);
			available.notifyAll();
		}
		
		if( thread2interrupt != null )
		{
			try
			{
				thread2interrupt.interrupt();
			}
			catch( Exception e )
			{
				
			}
		}
	}

	public boolean isAvailable()
	{
		return available.get();
	}

	public void interruptMeWhenDone()
	{
		thread2interrupt = Thread.currentThread();
	}
}
