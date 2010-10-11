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

import java.util.LinkedList;

public class SolidTimeCollection<T>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7079331632659555853L;

	protected static class Entry<U>
	{
		U 		data;
		long 	time;
	}
	
	public static enum StructurePolicy
	{
		OUT_OF_WINDOW_NOTHING,
		UNTIL_CURRENT_TIME
	}
	
	private LinkedList< Entry<T> > data;
	private long lastTime;
	private long currentTime;
	private StructurePolicy structurePolicy;
	
	public boolean hasNext( long time )
	{
		assert structurePolicy != null;
		
		switch(structurePolicy)
		{
		case OUT_OF_WINDOW_NOTHING:
			return time >= lastTime && time <= currentTime;
		case UNTIL_CURRENT_TIME:
			return time <= currentTime;
		}
		
		return false;
	}
	
	public Entry<T> getNext( long time )
	{
		
	}
}
