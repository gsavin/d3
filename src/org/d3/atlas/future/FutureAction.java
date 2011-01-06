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
package org.d3.atlas.future;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import org.d3.Future;

public abstract class FutureAction
	implements Runnable
{
	protected static final ThreadGroup 		futureActionGroup 	= new ThreadGroup("future-actions");
	protected static final AtomicInteger	FA_ID_GENERATOR		= new AtomicInteger(0);
	
	public static enum FutureActionPolicy
	{
		WaitForAll,
		WaitForOne
	}
	
	private LinkedList<Future> 	futures;
	private FutureActionPolicy	policy	= FutureActionPolicy.WaitForAll;
	
	public FutureAction( Collection<Future> futures )
	{
		this.futures = new LinkedList<Future>();
		this.futures.addAll(futures);
		init();
	}
	
	public FutureAction( Future ... futures )
	{
		this.futures = new LinkedList<Future>();
		
		if( futures != null )
			for( Future f: futures ) this.futures.add(f);
		
		init();
	}
	
	private void init()
	{
		Thread t = new Thread(futureActionGroup,this,
				String.format("future-action-%x",FA_ID_GENERATOR.getAndIncrement()));
		
		t.setDaemon(true);
		t.start();
	}
	
	public final void run()
	{
		if( futures != null )
		{
			for( Future f: futures )
				f.interruptMeWhenDone();
			
			while( futures.size() > 0 )
			{
				for( int i = 0; i < futures.size(); i++ )
				{
					if( futures.get(i).isAvailable() )
					{
						action(futures.get(i));
						futures.remove(i);
						i--;
						
						if( policy == FutureActionPolicy.WaitForOne )
							futures.clear();
					}
				}
				
				try
				{
					if( futures.size() > 0 )
						Thread.sleep(500);
				}
				catch( InterruptedException e )
				{
					// Nothing to do, just continue to check
				}
			}
		}
	}
	
	/**
	 * Defines what to do with each future.
	 * @param f future available
	 */
	public abstract void action( Future f );
}
