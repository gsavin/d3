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

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ri2c.d3.Agency;
import org.ri2c.d3.Args;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.Request;
import org.ri2c.d3.agency.FeatureDescription;
import org.ri2c.d3.agency.RunnableFeature;
import org.ri2c.d3.agency.RunnableFeatureCommand;

/**
 * <title>Discovery L2D feature.</title> This feature allows to discover other
 * agencies. It uses multicast broadcasting over ipv6 to send very small
 * messages containing address of its agency.
 * 
 * Address group used in multicast is ff02:6c32:642d:6469:7363:6F76:6572:7900,
 * (hex code for 'l2d-discovery' prefix by the multicast flag). All agencies
 * have to used the same multicast address.
 * 
 * Discovery also listens to messages from other discoveries and update remote
 * agencies list according to received messages.
 * 
 * Delay between send messages depends on two parameters:
 * 
 * <list>
 * 	<item>minDelay: the minimum delay between two messages</item>
 * 	<item>averagePeriod: an average period added to the delay</item>
 * </list>
 * 
 * So, the delay is: <var>minDelay</var> + random.nextInt( <var>averagePeriod</var> ).
 * 
 * Following keys are usable to discovery:
 * 
 * <description>
 * 	<content>
 * 		<label>l2d.features.discovery.interface</label>
 * 	Network interface used by the discovery feature.
 * 	</content>
 * 	<content>
 * 		<label>l2d.features.discovery.min_delay</label>
 * 	Minimum delay between two messages/
 * 	</content>
 * 	<content>
 * 		<label>l2d.features.discovery.avg_period</label>
 * 	Average period added to minimum delay.
 * 	</content>
 * 	<content>
 * 		<label>l2d.features.discovery.seed</label>
 * 	Seed used in random generator.
 * 	</content>
 * </description>
 * 
 * @author Guilhelm Savin
 * 
 */
public class Discovery
	implements RunnableFeature, Runnable
{
	/**
	 * Description of this feature.
	 */
	protected static final FeatureDescription discoveryDescription =
		new FeatureDescription( "l2d.features.discovery", "Discovery", "Try to discover other agencies on the network." );
	
	/**
	 * Prefix of discovery messages.
	 */
	protected static final String 	DISCOVERY_MESSAGE_PREFIX 	= "l2d:discovery";
	/**
	 * Message to diffuse agency address.
	 */
	protected static final String 	DISCOVERY_AGENCY_AT 		= "agency-at";
	/**
	 * Message to announce that agency will disconnect.
	 */
	protected static final String 	DISCOVERY_AGENCY_EXIT 		= "agency-exit";
	
	protected static final String 	DISCOVERY_PROTOCOLS			= "protocols";
	
	/**
	 * Port used in discovery.
	 */
	protected static final int 		discoveryPort = 5456;
	/**
	 * Multicast ipv6 address used by all instances of Discovery.
	 */
	protected static final String 	discoveryAddress = 
		"FF02:6C32:642D:6469:7363:6F76:6572:7900";
	
	/**
	 * InetAddress associated to the discovery multicast address.
	 */
	protected static InetAddress discoveryGroup = null;
	
	static
	{
		try
		{
			discoveryGroup = InetAddress.getByName(discoveryAddress);
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	protected static long DISCOVERY_ID_GENERATOR = 0;
	
	/**
	 * Runnable feature command which send discovery message.
	 * 
	 * @author Guilhelm Savin
	 *
	 */
	private class DiscoverySpreader
		extends RunnableFeatureCommand
	{
		/**
		 * Create a new discovery message spreader.
		 */
		public DiscoverySpreader()
		{
			super(minDelay,unit);
		}
		
		/**
		 * Get a new delay.
		 * @return the next delay
		 */
		protected long getNewDelay()
		{
			return random.nextInt(averagePeriod) + minDelay;
		}
		
		/**
		 * Command action.
		 * A message is sent.
		 */
		public void run()
		{
			try
			{
				sendDiscoveryPacket();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			resetDelay( getNewDelay(), unit );
		}
	}
	
	protected static final Pattern idPattern 		= Pattern.compile("id\\(([^\\(]+)\\)");
	protected static final Pattern addressPattern 	= Pattern.compile("address\\(([^\\(]+)\\)");
	protected static final Pattern protocolPattern 	= Pattern.compile("protocol\\(([^\\(]+)\\)");
	
	/**
	 * Time unit used in delay.
	 */
	protected TimeUnit			unit;
	/**
	 * Message spreads as a datagram packet.
	 */
	protected DatagramPacket 	thePacket;
	/**
	 * Network interface used by discovery.
	 */
	protected NetworkInterface 	networkInterface;
	/**
	 * Socket listening to discovery messages.
	 */
	protected MulticastSocket	discoverySocket;
	/**
	 * Spreader of discovery messages.
	 */
	protected DiscoverySpreader discoverySpreader;
	/**
	 * Flag used to indicate if discovery is active.
	 */
	protected boolean			spread;
	/**
	 * Local address according to network interface.
	 */
	protected String			localAddress;
	/**
	 * Local agency.
	 */
	protected Agency			localAgency;
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
	
	protected String			discoveryId;
	
	public Discovery()
	{
		unit 		= TimeUnit.MILLISECONDS;
		discoveryId	= String.format("l2d.features.discovery.%X", DISCOVERY_ID_GENERATOR++);
	}
	
	/**
	 * Init network objets.
	 * 
	 * @param args
	 * @throws SocketException
	 */
	protected void initNetworkInterface( Args args )
		throws SocketException
	{
		boolean inet6 = Boolean.parseBoolean( args.get("inet6") );
		
		this.networkInterface = NetworkInterface.getByName( args.get("interface") );
		
		if(networkInterface == null)
		{
			Enumeration<NetworkInterface> nie = NetworkInterface.getNetworkInterfaces();
			
			while( nie.hasMoreElements() )
			{
				NetworkInterface tmp = nie.nextElement();
				
				if( this.networkInterface == null )
					this.networkInterface = tmp;
				else if( this.networkInterface.isLoopback() )
					this.networkInterface = tmp;
			}
		}
		
		try
		{
			discoverySocket = new MulticastSocket( discoveryPort );
			discoverySocket.joinGroup(discoveryGroup);
			//discoverySocket.setSoTimeout(1000);
			
			InetAddress local = null;
			
			Enumeration<InetAddress> enu = networkInterface.getInetAddresses();

			System.out.printf("[discovery] available address for interface %s:%n",networkInterface.getDisplayName());
			while( enu.hasMoreElements() )
			{
				InetAddress tmp = enu.nextElement();
				int l = tmp.getAddress().length;
				
				if( local == null && ! tmp.isLoopbackAddress() && ( ( inet6 && l == 16 ) || ( ! inet6 && l == 4 ) ) )
				{
					System.out.printf(" (x) %s%n", tmp.getHostAddress() );
					local = tmp;
				}
				else
				{
					System.out.printf(" ( ) %s%n", tmp.getHostAddress() );
				}
			}
			
			if( local.isLoopbackAddress() )
				local = null;
			
			localAddress = local.getHostAddress();
			
			String message = String.format( "%s %s id(%s) address(%s) protocol(%s)",
					DISCOVERY_MESSAGE_PREFIX, DISCOVERY_AGENCY_AT,localAgency.getId(), localAddress, Agency.getArg("l2d.protocols") );
			
			byte [] messageData = message.getBytes();
			
			thePacket = new DatagramPacket(messageData,0,messageData.length,
					discoveryGroup,discoveryPort);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public String getId()
	{
		return discoveryId;
	}
	
	public IdentifiableType getType()
	{
		return IdentifiableType.feature;
	}
	
	/**
	 * Get feature description.
	 * @see org.ri2c.d3.agency.Feature
	 * @see org.ri2c.d3.agency.FeatureDescription
	 */
	@SuppressWarnings("unchecked")
	public FeatureDescription getDescription()
	{
		return discoveryDescription;
	}

	/**
	 * Init this feature.
	 * @return true if init succeed
	 * @see org.ri2c.d3.agency.Feature
	 * @see org.ri2c.d3.agency.FeatureDescription
	 */
	public boolean initFeature(Agency agency, Args args)
	{
		if( args.has("seed") )
			random = new Random( Long.parseLong(args.get("seed")) );
		else
			random = new Random();
		
		if( args.has("min_delay") )
		{
			String s = args.get("min_delay").trim();
			
			if( s.matches("^\\d+$") )
				minDelay = Long.parseLong(args.get("min_delay"));
			else if( s.matches("\\d+ (DAYS|HOURS|MINUTES|SECONDS|MILLISECONDS|MICROSECONDS|NANOSECONDS)") )
			{
				TimeUnit localUnit = TimeUnit.valueOf(s.substring(s.indexOf(' ')+1).trim());
				minDelay = unit.convert(Long.parseLong(s.substring(0,s.indexOf(' '))),localUnit);
			}
		}
		else minDelay = 5000;
		
		if( args.has("avg_period") )
		{
			String s = args.get("avg_period").trim();
			
			if( s.matches("^\\d+$") )
				averagePeriod = Integer.parseInt(args.get("avg_period"));
			else if( s.matches("\\d+ (DAYS|HOURS|MINUTES|SECONDS|MILLISECONDS|MICROSECONDS|NANOSECONDS)") )
			{
				TimeUnit localUnit = TimeUnit.valueOf(s.substring(s.indexOf(' ')+1).trim());
				averagePeriod = (int) unit.convert(Long.parseLong(s.substring(0,s.indexOf(' '))),localUnit);
			}
		}
		else averagePeriod = 5000;

		this.localAgency = agency;
		
		try
		{
			initNetworkInterface(args);
		}
		catch( SocketException e )
		{
			System.err.printf("[%s] can not init network interface%n",discoveryDescription.getId());
			return false;
		}
		
		this.discoverySpreader = new DiscoverySpreader();
		this.spread = true;
		
		Thread t;
		
		t = new Thread( this, "l2d-discovery-listener" );
		t.setDaemon(true);
		t.start();
		
		return true;
	}

	/**
	 * Send a discovery packet.
	 * @throws IOException
	 */
	protected synchronized void sendDiscoveryPacket()
		throws IOException
	{
		discoverySocket.send(thePacket);
	}
	
	/**
	 * Discovery server method.
	 * Listen to messages.
	 */
	public void run()
	{
		byte [] bufferData 		= new byte [1024];
		DatagramPacket buffer 	= new DatagramPacket(bufferData,1024);
		
		while( spread )
		{
			try
			{
				//do
				{
					discoverySocket.receive(buffer);
				}
				//while( Agency.getLocalAgency().getIpTables().isBlacklisted(buffer.getAddress()) && spread );
				
				String message = new String( buffer.getData() );
				handleMessage(message);
			}
			catch( SocketTimeoutException e )
			{
				
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Action if agency exit.
	 * @param agency
	 */
	public void agencyExit( Agency agency )
	{
		spread = false;
	}
	
	protected String consume( String str, String prefix )
	{
		return str.substring(prefix.length()+1).trim();
	}
	
	/**
	 * Handle a received message.
	 * @param message
	 */
	protected void handleMessage( String message )
	{
		if( message.startsWith( DISCOVERY_MESSAGE_PREFIX ) )
		{
			message = consume(message,DISCOVERY_MESSAGE_PREFIX);
			
			if( message.startsWith(DISCOVERY_AGENCY_AT) )
			{
				message = consume(message,DISCOVERY_AGENCY_AT);
				
				if( message.indexOf("id(" + localAgency.getId() + ")" ) < 0 )
				{
					Matcher id, protocol, address;
					
					id       = idPattern.matcher(message);
					address  = addressPattern.matcher(message);
					protocol = protocolPattern.matcher(message);
					
					if( id.find() && address.find() && protocol.find() &&
							localAgency.getRemoteAgencyDescription(id.group(1)) == null )
					{
						/*
						 * Send a discovery packet, to allow remote agency to recognize this.
						 */
						try   { sendDiscoveryPacket(); }
						catch (IOException e) {}
						
						localAgency.registerAgency( id.group(1), address.group(1), protocol.group(1) );
					}
				}
			}
			else System.err.printf( "[%s] not understood message: \"%s\"%n", discoveryDescription.getId(), message );
		}
		else System.err.printf( "[%s] unknown message: \"%s\"%n", discoveryDescription.getId(), message );
	}
	
	/**
	 * Get the runnable feature command.
	 * @see org.ri2c.d3.agency.RunnableFeature
	 */
	public RunnableFeatureCommand getRunnableFeatureCommand()
	{
		return discoverySpreader;
	}

	public void handleRequest(IdentifiableObject source,
			IdentifiableObject target, Request r)
	{
		
	}
}
