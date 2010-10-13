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
package org.ri2c.d3.agency.feature;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Args;
import org.ri2c.d3.Console;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.Request;
import org.ri2c.d3.agency.FeatureDescription;
import org.ri2c.d3.agency.RemoteAgency;
import org.ri2c.d3.agency.RunnableFeature;
import org.ri2c.d3.agency.RunnableFeatureCommand;

public class RemoteInformationsUpdater
	implements RunnableFeature
{
	protected static final FeatureDescription riuDescription =
		new FeatureDescription("l2d.features.RemoteInformationsUpdater","Remote Informations Updater", "");
	
	protected class RIUCommand
		extends RunnableFeatureCommand
	{
		public RIUCommand()
		{
			super(delay,unit);
		}
		
		public void run()
		{
			for( RemoteAgency rad: agency.eachRemoteAgency() )
			{
				if( random.nextFloat() < updateProbability )
					update(rad);
			}
			
			resetDelay(delay,unit);
		}
		
		protected void update( RemoteAgency rad )
		{
			Console.info("update %s",rad.getRemoteAgencyId());
			
			try
			{
				if( ! agency.getIpTables().getAddress(rad.getRemoteAgencyId()).isReachable(1000) )
					throw new Exception();
				
				agency.lazyCheckEntitiesOn(rad);
			}
			catch( Exception e )
			{
				agency.unregisterAgency(rad);
			}
		}
	}
	
	protected Random		random;
	protected Agency		agency;
	protected long			delay;
	protected TimeUnit		unit;
	protected float			updateProbability;
	protected RIUCommand	riuCommand;
	
	public RunnableFeatureCommand getRunnableFeatureCommand()
	{
		return riuCommand;
	}

	@SuppressWarnings("unchecked")
	public FeatureDescription getDescription()
	{
		return riuDescription;
	}

	public String getId()
	{
		return riuDescription.getId();
	}

	public IdentifiableType getType()
	{
		return IdentifiableType.feature;
	}

	public boolean initFeature(Agency agency, Args args)
	{
		this.random 			= new Random();
		this.agency 			= agency;
		this.delay				= 2000;
		this.unit				= TimeUnit.MILLISECONDS;
		this.updateProbability	= 0.3f;
		this.riuCommand			= new RIUCommand();
		
		return true;
	}

	public void handleRequest(IdentifiableObject source,
			IdentifiableObject target, Request r) {
	}

}
