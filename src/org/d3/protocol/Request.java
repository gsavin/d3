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

import java.io.Serializable;
import java.net.InetAddress;
import java.net.Inet6Address;
import java.net.URI;
import java.net.URISyntaxException;

import org.d3.actor.Call;
import org.d3.protocol.request.ObjectCoder;
import org.d3.protocol.request.ObjectCoder.CodingMethod;
import org.d3.remote.RemotePort;

public class Request implements Serializable {
	private static final long serialVersionUID = -836303930336000404L;

	private static URI format(URI uri, Transmitter transmitter) {
		InetAddress address = transmitter.getAddress();
		String host = address.getHostAddress();

		if (address instanceof Inet6Address) {
			if (host.indexOf('%') > 0)
				host = host.substring(0, host.indexOf('%'));
			host = String.format("[%s]", host);
		}

		try {
			return new URI(String.format("%s://%s:%d%s", transmitter
					.getScheme(), host, transmitter.getPort(), uri.getPath()));
		} catch (URISyntaxException e) {
			return uri;
		}
	}

	private static URI format(URI uri, RemotePort remotePort) {
		try {
			return new URI(String.format("%s://%s:%d%s",
					remotePort.getScheme(), uri.getHost(),
					remotePort.getPort(), uri.getPath()));
		} catch (URISyntaxException e) {
			return uri;
		}
	}

	/**
	 * URI of the actor who emits the request.
	 */
	protected final URI source;
	/**
	 * URI of the actor who should receive the request.
	 */
	protected final URI target;
	/**
	 * Name of the callable.
	 */
	protected final String call;
	/**
	 * Method used to encode arguments of the call.
	 */
	protected final CodingMethod codingMethod;
	/**
	 * Arguments of the call.
	 */
	protected final byte[] args;
	/**
	 * If a value should be returned, this is the id of the remote future.
	 */
	protected final String futureId;

	/**
	 * Constructor used by transmitter to create a new request. The request is
	 * built using data of the {@link org.d3.actor.Call} object.
	 * 
	 * @param call
	 *            {@link org.d3.actor.Call} object used to build the request
	 * @param transmitter
	 *            transmitter used to transmit the request
	 * @param remotePort
	 *            port of the remote transmitter that will receive the request
	 */
	public Request(Call call, Transmitter transmitter, RemotePort remotePort) {
		URI s = call.getSource().getURI();
		URI t = call.getTarget().getURI();

		this.source = format(s, transmitter);
		this.target = format(t, remotePort);
		this.call = call.getName();
		this.codingMethod = transmitter.getPreferredCodingMethod();
		this.args = ObjectCoder.encode(codingMethod, call.getArgs());
		this.futureId = call.getFuture().getId();
	}

	/**
	 * Constructor used by transmitter to build remote request it receives.
	 * 
	 * @param source
	 *            uri of the actor emitting the request
	 * @param target
	 *            uri of the actor that should receive the request
	 * @param call
	 *            name of the requested callable
	 * @param cm
	 *            method used to encode arguments of the call
	 * @param args
	 *            arguments of call
	 * @param futureId
	 *            id of the future used to return a value or null if there is no
	 *            future
	 */
	public Request(URI source, URI target, String call, CodingMethod cm,
			byte[] args, String futureId) {
		this.source = source;
		this.target = target;
		this.call = call;
		this.codingMethod = cm;
		this.args = args;
		this.futureId = futureId;
	}

	public URI getSourceURI() {
		return source;
	}

	public URI getTargetURI() {
		return target;
	}

	public String getCall() {
		return call;
	}

	public CodingMethod getCodingMethod() {
		return codingMethod;
	}

	public byte[] getArgs() {
		return args;
	}

	public String getFutureId() {
		return futureId;
	}

	public Object[] getDecodedArgs() {
		Object data = ObjectCoder.decode(codingMethod, args);

		if (data == null)
			return null;

		if (data.getClass().isArray())
			return (Object[]) data;
		else
			return new Object[] { data };
	}
}
