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
package org.ri2c.d3;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ri2c.d3.IdentifiableObject.IdentifiableType;
import org.ri2c.d3.request.ObjectCoder;
import org.ri2c.d3.request.ObjectCoder.CodingMethod;

public class Request implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -836303930336000404L;

	public static final String ENCODING = "UTF-8";

	public static final String CALLABLE = "callable";
	public static final String DATA_ENCODING = "data_encoding";
	public static final String ARGUMENTS = "args";
	public static final String SOURCE = "source";
	public static final String FUTURE = "future";

	protected final URI source;
	protected final URI target;

	public Request(IdentifiableObject source, IdentifiableObject target,
			String callable, Object[] args) {
		this(source, target, callable, args, null);
	}

	public Request(IdentifiableObject source, IdentifiableObject target,
			String callable, Object[] args, IdentifiableObject future) {
		this.source = source.getURI();

		String query = null;

		try {
			query = String.format("%s=%s", SOURCE,
					URLEncoder.encode(this.source.toString(), ENCODING));
		} catch (Exception e) {
			e.printStackTrace();
		}

		CodingMethod cm = CodingMethod.HEXABYTES;

		query = String.format("%s%s%s=%s", query,
				query.length() > 0 ? "&" : "", CALLABLE, callable);
		query = String.format("%s%s%s=%s", query,
				query.length() > 0 ? "&" : "", DATA_ENCODING, cm);

		if (args != null) {
			query = String.format("%s%s%s=%s", query, query.length() > 0 ? "&"
					: "", ARGUMENTS, ObjectCoder.encode(cm, args));
		}

		if (future != null) {
			try {
				query = String.format("%s%s%s=%s", query,
						query.length() > 0 ? "&" : "", FUTURE,
						URLEncoder.encode(future.getURI().toString(), ENCODING));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		this.target = target.getQueryURI(query);
	}

	public Request(IdentifiableObject source, String target) throws URISyntaxException {
		if (target == null)
			throw new NullPointerException("target is null");

		this.target = new URI(target);

		if (targetQueryContains(SOURCE)) {
			URI decoded = null;
			try {
				decoded = new URI(URLDecoder.decode(
						getTargetQueryArgument(SOURCE), ENCODING));
			} catch (Exception e) {
				e.printStackTrace();
			}

			this.source = decoded;
		} else {
			this.source = source.getURI();
		}
	}

	public Request(IdentifiableObject source, URI target) {
		if (target == null)
			throw new NullPointerException("target is null");

		this.target = target;
		
		if (targetQueryContains(SOURCE)) {
			URI decoded = null;
			try {
				decoded = new URI(URLDecoder.decode(
						getTargetQueryArgument(SOURCE), ENCODING));
			} catch (Exception e) {
				e.printStackTrace();
			}

			this.source = decoded;
		} else {
			this.source = source.getURI();
		}
	}

	public URI getSourceURI() {
		return source;
	}

	public URI getTargetURI() {
		return target;
	}

	public IdentifiableType getTargetType() {
		return IdentifiableType.valueOf(target.getScheme());
	}
	
	public String getSourceAgency() {
		return source == null ? null : source.getHost();
	}
	
	public String getTargetAgency() {
		return target.getHost();
	}

	public boolean isLocalTarget() {
		return target.getHost().equals(Agency.getLocalAgency().getId());
	}

	public boolean targetQueryContains(String key) {
		return target.getQuery().matches(String.format("(^|.*\\&)%s=.*", key));
	}

	public String getTargetQueryArgument(String key) {
		Pattern p = Pattern.compile(String.format("(?:^|.*\\&)%s=([^\\&]*).*",
				key));
		Matcher m = p.matcher(target.getQuery());

		if (m.matches()) {
			return m.group(1);
		} else {
			return null;
		}
	}

	public boolean hasCallable() {
		return targetQueryContains(CALLABLE);
	}

	public String getCallable() {
		return getTargetQueryArgument(CALLABLE);
	}

	public boolean hasCallableArguments() {
		return targetQueryContains(ARGUMENTS);
	}

	public Object[] getCallableArguments() {
		if (!targetQueryContains(ARGUMENTS))
			return null;

		CodingMethod method = CodingMethod
				.valueOf(getTargetQueryArgument(DATA_ENCODING));
		Object data = ObjectCoder.decode(method,
				getTargetQueryArgument(ARGUMENTS));

		if (data == null) {
			return null;
		}

		if (data.getClass().isArray()) {
			return (Object[]) data;
		} else {
			return new Object[] { data };
		}
	}

	public boolean hasFuture() {
		return targetQueryContains(FUTURE);
	}

	public URI getFutureURI() {
		try {
			return new URI(URLDecoder.decode(getTargetQueryArgument(FUTURE), ENCODING));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public String toString() {
		return target.toString();
	}
}
