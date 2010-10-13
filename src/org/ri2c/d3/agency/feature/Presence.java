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

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Args;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.Request;
import org.ri2c.d3.agency.FeatureDescription;
import org.ri2c.d3.agency.RemoteAgency;
import org.ri2c.d3.agency.RunnableFeature;
import org.ri2c.d3.agency.RunnableFeatureCommand;
import org.ri2c.d3.protocol.Protocols;

/**
 * <title>Presence L2D Feature</title>
 * 
 * @author Guilhelm Savin
 *
 */
public class Presence
	implements RunnableFeature
{
	public static final FeatureDescription presenceFeatureDescription =
		new FeatureDescription( "l2d.features.presence", "Presence Diffusion", "Diffuse status of this agency to known agencies." );

	protected static long PRESENCE_ID_GENERATOR = 0;
	
	private class PresenceCommand
		extends RunnableFeatureCommand
	{
		HashMap<String,Request> requestByProtocol;
		
		public PresenceCommand()
		{
			super(minDelay,unit);
			requestByProtocol = new HashMap<String,Request>();
		}
		
		public void run()
		{
			for( RemoteAgency rad: localAgency.eachRemoteAgency() )
			{
				if( rad.getFirstProtocol() != null )
				{
					String protoId = rad.getFirstProtocol();
					
					if( ! requestByProtocol.containsKey(protoId) )
					{
						//Protocol proto = Protocols.getProtocol(protoId);
						
						Request r = Protocols.createRequestTo(Presence.this,rad,"presence");//proto.newRequest(Presence.this,"presence");
						requestByProtocol.put(protoId,r);
					}
					
					if( requestByProtocol.containsKey(protoId) )
					{
						Protocols.sendRequest(rad,requestByProtocol.get(protoId));
					}
					else System.err.printf("[presence] cant find request%n");
				}
			}
			
			resetDelay(getNewDelay(),unit);
		}
		
		/**
		 * Get a new delay.
		 * @return the next delay
		 */
		protected long getNewDelay()
		{
			return random.nextInt(averagePeriod) + minDelay;
		}
	}
	
	protected Agency 			localAgency;
	protected PresenceCommand	presenceCommand;
	/**
	 * Random generator used to compute delays.
	 */
	protected Random 			random;
	/**
	 * Minimum delay between two messages.
	 */
	protected long				minDelay;
	/**
	 * Average period added to minimum delay.
	 */
	protected int				averagePeriod;
	protected TimeUnit			unit;
	protected String			presenceId;
	
	public Presence()
	{
		unit 		= TimeUnit.MILLISECONDS;
		presenceId	= String.format("presence-%X",PRESENCE_ID_GENERATOR);
	}
	
	public String getId()
	{
		return presenceId;
	}
	
	public IdentifiableType getType()
	{
		return IdentifiableType.feature;
	}
	
	@SuppressWarnings("unchecked")
	public FeatureDescription getDescription()
	{
		return presenceFeatureDescription;
	}

	public boolean initFeature(Agency agency, Args args)
	{
		this.localAgency 		= agency;
		this.presenceCommand 	= new PresenceCommand();
		
		if( args.has("seed") )
			random = new Random( Long.parseLong(args.get("seed")) );
		else
			random = new Random();
		
		if( args.has("min_delay") )
		{
			String s = args.get("min_delay").trim();
			
			if( s.matches("\\d+") )
				minDelay = Long.parseLong(args.get("min_delay"));
			else if( s.matches("\\d+ (DAYS|HOURS|MINUTES|SECONDS|MILLISECONDS|MICROSECONDS|NANOSECONDS)") )
			{
				TimeUnit localUnit = TimeUnit.valueOf(s.substring(s.indexOf(' ')+1).trim());
				minDelay = unit.convert(Long.parseLong(s.substring(0,s.indexOf(' '))),localUnit);
			}
		}
		else minDelay = 3000;
		
		if( args.has("avg_period") )
		{
			String s = args.get("avg_period").trim();
			
			if( s.matches("\\d+") )
				averagePeriod = Integer.parseInt(args.get("avg_period"));
			else if( s.matches("\\d+ (DAYS|HOURS|MINUTES|SECONDS|MILLISECONDS|MICROSECONDS|NANOSECONDS)") )
			{
				TimeUnit localUnit = TimeUnit.valueOf(s.substring(s.indexOf(' ')+1).trim());
				averagePeriod = (int) unit.convert(Long.parseLong(s.substring(0,s.indexOf(' '))),localUnit);
			}
		}
		else averagePeriod = 2000;
		
		return true;
	}

	public Runnable getRunnableFeature()
	{
		return null;
	}

	public RunnableFeatureCommand getRunnableFeatureCommand()
	{
		return presenceCommand;
	}
	
	public void handleRequest( IdentifiableObject source,
			IdentifiableObject target, Request r )
	{
		
	}
}
