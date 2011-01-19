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
import java.net.URI;

import org.d3.actor.Call;
import org.d3.protocol.request.ObjectCoder;
import org.d3.protocol.request.ObjectCoder.CodingMethod;

public class Request implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -836303930336000404L;

	protected final URI source;
	protected final URI target;
	protected final String futureId;
	protected final CodingMethod codingMethod;
	protected final String args;
	protected final String call;

	public Request(Call call) {
		this.source = call.getSource().getURI();
		this.target = call.getTarget().getURI();
		this.call = call.getName();
		this.codingMethod = CodingMethod.HEXABYTES;
		this.args = ObjectCoder.encode(codingMethod, call.getArgs());
		this.futureId = call.getFuture().getId();
	}

	public Request(URI source, URI target, String call, CodingMethod cm,
			String args, String futureId) {
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

	public String getArgs() {
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
