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
package org.ri2c.d3.entity;

public class EntityMigrationStatus
{
	public static enum State
	{
		pending,
		transfered,
		rejected,
		done
	}
	
	protected State state;
	
	public State getState()
	{
		return state;
	}
	
	public void setState( State state )
	{
		synchronized(this)
		{
			this.state = state;
			notifyAll();
		}
	}
	
	public void waitMigrationEndsOrFailed()
	{
		synchronized(this)
		{
			while( state == State.pending || state == State.transfered )
			{
				try { wait(300); } catch( Exception e ) {}
			}
		}
	}
}
