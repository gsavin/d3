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
package org.ri2c.d3.agency;

import java.util.concurrent.TimeUnit;

public abstract class RunnableFeatureCommand
	implements Runnable
{
	long 		nextDate 			= System.currentTimeMillis();
	TimeUnit	unit 				= TimeUnit.MILLISECONDS;
	boolean		active				= true;
	boolean		periodic			= true;
	
	public RunnableFeatureCommand()
	{
		periodic = false;
		active   = true;
		nextDate = System.currentTimeMillis();
	}
	
	public RunnableFeatureCommand( long initialDelay, TimeUnit unit )
	{
		active =  true;
		resetDelay(initialDelay,unit);
	}
	
	public void resetDelay( long delay, TimeUnit unit )
	{
		nextDate = System.currentTimeMillis() + this.unit.convert(delay,unit);
		periodic = delay!=0;
	}
	
	public long getDelay( TimeUnit unit )
	{
		return unit.convert( nextDate - System.currentTimeMillis(), this.unit );
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	public boolean isPeriodic()
	{
		return periodic;
	}
	
	public abstract void run();
}
