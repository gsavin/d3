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
package org.d3.protocol.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
//import java.net.NetworkInterface;
import java.net.SocketException;
//import java.util.Enumeration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.d3.Agency;

public abstract class UDPInterface {
	protected DatagramSocket socket;
	protected Thread service;
	protected ThreadPoolExecutor packetHandlerExecutor;

	public UDPInterface() {

	}

	public void init(String networkIf, int port) throws SocketException {
		/*
		 * NetworkInterface networkInterface = networkIf == null ? null :
		 * NetworkInterface.getByName( networkIf );
		 * 
		 * if(networkInterface == null) { Enumeration<NetworkInterface> nie =
		 * NetworkInterface.getNetworkInterfaces();
		 * 
		 * while( nie.hasMoreElements() ) { NetworkInterface tmp =
		 * nie.nextElement();
		 * 
		 * if( networkInterface == null ) networkInterface = tmp; else if(
		 * networkInterface.isLoopback() ) networkInterface = tmp; } }
		 * 
		 * InetAddress local = null;
		 * 
		 * Enumeration<InetAddress> enu = networkInterface.getInetAddresses();
		 * System.out.printf("[udp] available address for interface %s:%n",
		 * networkInterface.getDisplayName());
		 * 
		 * while( enu.hasMoreElements() ) { InetAddress tmp = enu.nextElement();
		 * 
		 * if( local == null && ! tmp.isLoopbackAddress() ) {
		 * System.out.printf(" (x) %s%n", tmp.getHostAddress() ); local = tmp; }
		 * else { System.out.printf(" ( ) %s%n", tmp.getHostAddress() ); }
		 * 
		 * if( local.isLoopbackAddress() ) local = null; }
		 */
		socket = new DatagramSocket(port);
		packetHandlerExecutor = new ThreadPoolExecutor(1, 10, 200,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

		if (!socket.isBound())
			System.err.printf("[udp] socket not bound%n");
		else
			System.out.printf("[udp] listening on %s%n",
					socket.getLocalSocketAddress());
	}

	public void sendUDPRequest(InetAddress address, int port, byte[] data)
			throws IOException {
		// byte [] data = r.convertToBytes();

		// if( data.length > socket.getSendBufferSize() )
		// throw new IOException( "too much data" );

		DatagramPacket packet = new DatagramPacket(data, data.length, address,
				port);

		if (!socket.isBound())
			System.err.printf("[udp] socket not bound%n");

		socket.send(packet);
	}

	protected synchronized void runService() {
		if (service != null)
			return;

		PacketListener pl = new PacketListener();
		service = new Thread(pl, "udp-socket-listener");
		service.setDaemon(true);
		service.start();
	}

	class PacketListener implements Runnable {
		public void run() {
			try {
				System.out.printf("[udp] max size is: %d%n",
						socket.getReceiveBufferSize());
			} catch (Exception e) {

			}

			while (socket.isBound()) {
				try {
					byte[] data = new byte[socket.getReceiveBufferSize()];
					DatagramPacket rec = new DatagramPacket(data, data.length);

					do {
						socket.receive(rec);
						// dataReceived(rec.getAddress(),rec.getData(),rec.getLength());
					} while (Agency.getLocalAgency().getIpTables()
							.isBlacklisted(rec.getAddress())
							&& socket.isBound());
					packetHandlerExecutor.execute(new PacketHandler(rec));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			System.out.printf("[udp] socket not bound%n");
		}
	}

	class PacketHandler implements Runnable {
		DatagramPacket packet;

		public PacketHandler(DatagramPacket packet) {
			this.packet = packet;
		}

		public void run() {
			dataReceived(packet.getAddress(), packet.getData(),
					packet.getLength());
		}
	}

	public abstract void dataReceived(InetAddress from, byte[] data, int length);
}
