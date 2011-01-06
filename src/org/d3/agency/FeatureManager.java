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
package org.d3.agency;

import java.util.concurrent.ConcurrentHashMap;

import org.d3.Agency;
import org.d3.Console;

public class FeatureManager
{
	Agency								agency;
	ConcurrentHashMap<String,Feature>	features;
	RunnableFeatureExecutor				runnableFeatureExecutor;

	public FeatureManager( Agency agency )
	{
		this.agency = agency;
		this.features = new ConcurrentHashMap<String,Feature>();
		this.runnableFeatureExecutor = new RunnableFeatureExecutor();
	}
	
	public void addFeature( Feature f )
	{
		if( ! features.containsKey(f.getId()) )
		{
			if( ! agency.registerIdentifiableObject(f) )
				return;
			
			features.put(f.getId(),f);
			
			if( ! f.initFeature(agency,agency.getArgs().getArgs(f)) )
			{
				features.remove(f.getId());
				agency.unregisterIdentifiableObject(f);
				
				Console.error("failed to init feature \"%s\"",f.getId());
			}
			else
			{
				Console.info("succeed to init feature \"%s\"",f.getId());
				
				if( f instanceof RunnableFeature )
					runnableFeatureExecutor.submitRunnableFeatureCommand( ((RunnableFeature) f).getRunnableFeatureCommand() );
			}
		}
		else
		{
			Console.warning("feature already active: \"%s\"", f.getId() );
		}
	}
	
	public void removeFeature( String id )
	{
		if( features.contains(id) )
		{
			Feature f = features.get(id);
			agency.unregisterIdentifiableObject(f);
		}
	}
}
