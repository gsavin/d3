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

import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class RunnableFeatureExecutor
	extends ScheduledThreadPoolExecutor
{
	protected static final int 			coreSize 					= 4;
	protected static final int 			coreMaxSize 				= 16;
	protected static final long 		keepAlive					= 500;
	protected static final ThreadGroup	runnableFeatureThreadGroup	= new ThreadGroup("runnable-feature");
	
	class RunnableFeatureTask<V>
		extends FutureTask<V> implements RunnableScheduledFuture<V>
	{
		RunnableFeatureCommand 		rfc;
		RunnableScheduledFuture<V>	org;
		
		public RunnableFeatureTask( RunnableFeatureCommand rfc,
				RunnableScheduledFuture<V> org )
		{
			super(rfc,null);
			
			this.rfc = rfc;
			this.org = org;
		}
		
		public RunnableFeatureCommand getRunnableFeatureCommand()
		{
			return rfc;
		}
		
		public boolean isPeriodic()
		{
			return rfc.isPeriodic();
		}

		public void run()
		{
			rfc.run();
			
			if( rfc.isActive() && rfc.isPeriodic() )
				RunnableFeatureExecutor.super.getQueue().add(this);
		}

		public long getDelay(TimeUnit unit)
		{
			return rfc.getDelay(unit);
		}

		public int compareTo(Delayed o)
		{
			long d = rfc.getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
			return d == 0 ? 0 : ( d < 0 ? -1 : 1 );
		}
	}
	
	private static class RunnableFeatureThreadFactory
		implements ThreadFactory
	{
		long idGenerator = 0;
		
		public Thread newThread(Runnable r)
		{
			return new Thread(runnableFeatureThreadGroup,r,
					String.format("runnable-feature-thread%X",idGenerator++));
		}
		
	}
	
	protected TimeUnit unit = TimeUnit.NANOSECONDS;
	
	public RunnableFeatureExecutor()
	{
		super(coreSize,new RunnableFeatureThreadFactory());
	}
	
	protected <V> RunnableScheduledFuture<V> decorateTask(
            Runnable r, RunnableScheduledFuture<V> task) {
		if( r instanceof RunnableFeatureCommand )
			return new RunnableFeatureTask<V>( (RunnableFeatureCommand) r, task);
		else
			return task;
	}

	protected <V> RunnableScheduledFuture<V> decorateTask(
            Callable<V> c, RunnableScheduledFuture<V> task) {
		return task;
	}

	
	protected void beforeExecute( Thread t, Runnable r )
	{
		super.beforeExecute(t,r);
	}
	
	protected void afterExecute( Runnable r, Throwable t )
	{
		super.afterExecute(r,t);
		
		if( t != null )
			System.err.printf("[runnable-feature-executor] error: %s%n",t);
	}
	
	public void submitRunnableFeatureCommand( RunnableFeatureCommand rfc )
	{
		if( rfc.isActive() )
			schedule(rfc,rfc.getDelay(unit),unit);
	}
}
