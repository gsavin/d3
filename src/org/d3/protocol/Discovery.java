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
package org.d3.protocol;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.d3.Args;
import org.d3.Console;
import org.d3.HostAddress;
import org.d3.actor.Agency;
import org.d3.actor.CallException;
import org.d3.actor.Future;
import org.d3.actor.Protocol;
import org.d3.actor.StepActor;
import org.d3.annotation.ActorDescription;
import org.d3.annotation.ActorPath;
import org.d3.remote.HostNotFoundException;
import org.d3.remote.RemoteAgency;
import org.d3.remote.RemoteHost;
import org.d3.remote.UnknownAgencyException;
import org.d3.template.ReverseException;
import org.d3.template.Template;
import org.d3.tools.Time;
import org.d3.tools.Utils;

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
 * <list> <item>minDelay: the minimum delay between two messages</item>
 * <item>averagePeriod: an average period added to the delay</item> </list>
 * 
 * So, the delay is: <var>minDelay</var> + random.nextInt(
 * <var>averagePeriod</var> ).
 * 
 * Following keys are usable to discovery:
 * 
 * <description> <content> <label>l2d.features.discovery.interface</label>
 * Network interface used by the discovery feature. </content> <content>
 * <label>l2d.features.discovery.min_delay</label> Minimum delay between two
 * messages/ </content> <content>
 * <label>l2d.features.discovery.avg_period</label> Average period added to
 * minimum delay. </content> <content>
 * <label>l2d.features.discovery.seed</label> Seed used in random generator.
 * </content> </description>
 * 
 * @author Guilhelm Savin
 * 
 */
@ActorPath("/protocols/discovery")
@ActorDescription("Try to discover other agencies on the network.")
@InetProtocol
public class Discovery extends Protocol implements StepActor {

	protected final static String DISCOVERY_MESSAGE_TEMPLATE = "discovery agency_at id({%id%}) "
			+ "address({%address%}) protocols({%protocols%}) digest({%digest%})\n";

	protected final static Pattern DISCOVERY_MESSAGE_HEADER = Pattern
			.compile("^discovery (agency_at|agency_exit)");

	static enum MessageType {
		AGENCY_AT
	}

	/**
	 * Port used in discovery.
	 */
	protected static final int discoveryPort = 5456;
	/**
	 * Multicast ipv6 address used by all instances of Discovery.
	 */
	protected static final String discoveryAddress = "FF02:6C32:642D:6469:7363:6F76:6572:7900";

	protected static long DISCOVERY_ID_GENERATOR = 0;

	/**
	 * Time unit used in delay.
	 */
	protected TimeUnit unit;
	/**
	 * Message spreads as a datagram packet.
	 */
	protected DatagramPacket thePacket;
	/**
	 * Network interface used by discovery.
	 */
	protected NetworkInterface networkInterface;
	/**
	 * Socket listening to discovery messages.
	 */
	protected MulticastSocket discoverySocket;
	/**
	 * InetAddress associated to the discovery multicast address.
	 */
	protected InetAddress discoveryGroup = null;
	/**
	 * Flag used to indicate if discovery is active.
	 */
	protected boolean spread;
	/**
	 * Local address according to network interface.
	 */
	protected String localAddress;
	/**
	 * Local agency.
	 */
	protected Agency localAgency;
	/**
	 * Random generator used to compute delays.
	 */
	protected Random random;
	/**
	 * Minimum delay between two messages.
	 */
	protected long minDelay;
	/**
	 * Average period added to minimum delay.
	 */
	protected long averagePeriod;

	protected String agencyDigest;

	protected EnumMap<MessageType, Template> templates;

	// protected Template messageTemplate;

	public Discovery() throws IOException {
		this(discoveryPort);
	}

	public Discovery(NetworkInterface networkInterface) throws IOException {
		this(Utils.createSocketAddress(networkInterface, discoveryPort, true));
	}

	public Discovery(int port) throws IOException {
		this(new InetSocketAddress(port));
	}

	public Discovery(InetSocketAddress isa) throws IOException {
		super("discovery", Integer.toString(isa.getPort()), isa);

		templates = new EnumMap<MessageType, Template>(MessageType.class);
		templates.put(MessageType.AGENCY_AT, new Template(
				DISCOVERY_MESSAGE_TEMPLATE));

		unit = TimeUnit.MILLISECONDS;
		discoveryGroup = InetAddress.getByName(discoveryAddress);
		discoverySocket = new MulticastSocket(socketAddress);
		discoverySocket.joinGroup(discoveryGroup);
		spread = true;

		Args args = Agency.getActorArgs(this);
		InetAddress address = Utils.getAddressForInterface(args
				.get("interface"), args.getBoolean("inet6"));

		// if (address.isLoopbackAddress())
		// throw new ActorInternalException();

		localAddress = address.getHostAddress();
		createDiscoveryPacket();

		if (args.has("seed"))
			random = new Random(Long.parseLong(args.get("seed")));
		else
			random = new Random();

		if (args.has("min_delay")) {
			Time t = args.getTime("min_delay");
			minDelay = t.time;
			unit = t.unit;
		} else {
			minDelay = 5000;
			unit = TimeUnit.MILLISECONDS;
		}

		if (args.has("avg_period")) {
			Time t = args.getTime("avg_period");
			averagePeriod = unit.convert(t.time, t.unit);
		} else {
			averagePeriod = unit.convert(5000, TimeUnit.MILLISECONDS);
		}
	}

	protected void createDiscoveryPacket() {
		HashMap<String, String> env = new HashMap<String, String>();
		env.put("id", Agency.getLocalAgencyId());
		env.put("address", localAddress);
		env.put("protocols", Agency.getLocalAgency().getProtocols()
				.exportDescription());
		env.put("digest", Agency.getLocalAgency().getDigest());

		String message = templates.get(MessageType.AGENCY_AT).toString(env);
		byte[] messageData = message.getBytes();

		thePacket = new DatagramPacket(messageData, 0, messageData.length,
				discoveryGroup, discoveryPort);
	}

	/**
	 * Send a discovery packet.
	 * 
	 * @throws IOException
	 */
	protected synchronized void sendDiscoveryPacket() throws IOException {
		if (!Agency.getLocalAgency().getDigest().equals(agencyDigest))
			createDiscoveryPacket();

		discoverySocket.send(thePacket);
	}

	/**
	 * Discovery server method. Listen to messages.
	 */
	public void listen() {
		byte[] bufferData = new byte[1024];
		DatagramPacket buffer = new DatagramPacket(bufferData, 1024);

		while (spread) {
			try {
				discoverySocket.receive(buffer);

				String message = new String(buffer.getData());
				handleMessage(buffer.getAddress(), message);
			} catch (SocketTimeoutException e) {

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected String consume(String str, String prefix) {
		return str.substring(prefix.length() + 1).trim();
	}

	/**
	 * Handle a received message.
	 * 
	 * @param message
	 */
	protected void handleMessage(InetAddress from, String message) {
		Matcher h = DISCOVERY_MESSAGE_HEADER.matcher(message);

		if (h.find()) {
			MessageType mt = MessageType.valueOf(h.group(1).toUpperCase());

			switch (mt) {
			case AGENCY_AT:
				Map<String, String> env;

				try {
					env = templates.get(mt).reverse(message);
				} catch (ReverseException e) {
					return;
				}

				if (!Agency.getLocalAgencyId().equals(env.get("id"))) {

					String id = env.get("id");
					String straddress = env.get("address");
					String protocols = env.get("protocols");
					String digest = env.get("digest");

					HostAddress address;

					try {
						address = HostAddress.getByName(straddress);// .getByInetAddress(from);
					} catch (UnknownHostException e) {
						Console.error("failed to retrieve address : %s",
								straddress);
						return;
					}

					RemoteHost host = null;

					// Console.warning("from : %s", address);

					try {
						host = Agency.getLocalAgency().getRemoteHosts().get(
								address);
					} catch (HostNotFoundException e) {
						Future f = (Future) Agency.getLocalAgency().call(
								Agency.CALLABLE_REGISTER_NEW_HOST, address);

						try {
							f.waitForValue();
						} catch (InterruptedException ie) {
							if (!f.isAvailable())
								return;
						}

						try {
							host = f.get();
						} catch (CallException e1) {
							Console.exception(e1);
							return;
						}
					}

					RemoteAgency remote = null;

					try {
						remote = host.getRemoteAgency(id);
					} catch (UnknownAgencyException e) {
						/*
						 * Send a discovery packet to be detected by the remote
						 * agency.
						 */
						try {
							sendDiscoveryPacket();
						} catch (IOException ioe) {
							Console.exception(ioe);
						}

						Future f = (Future) Agency.getLocalAgency().call(
								Agency.CALLABLE_REGISTER_NEW_AGENCY, host, id);

						try {
							f.waitForValue();
						} catch (InterruptedException ie) {
							if (!f.isAvailable())
								return;
						}

						if (!Thread.interrupted()) {
							try {
								remote = f.get();
							} catch (CallException e1) {
								Console.exception(e1);
								return;
							}
						}
					}

					if (!remote.getDigest().equals(digest)) {
						remote.updateDigest(digest);
						remote.updateProtocols(protocols);
					}
				}
			}
		} else
			System.err.printf("[%s] unknown message: \"%s\"%n", getId(),
					message.replace("\n", "\\n"));
	}

	public long getStepDelay(TimeUnit unit) {
		return unit.convert(random.nextInt((int) averagePeriod) + minDelay,
				this.unit);
	}

	public void step() {
		try {
			sendDiscoveryPacket();
		} catch (IOException e) {
			Console.exception(e);
		}
	}
}
