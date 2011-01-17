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
package org.d3.events;

public class NonBindableActorException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9042402562768761750L;

	public NonBindableActorException() {
		super();
	}
	
	public NonBindableActorException(String msg) {
		super(msg);
	}
	
	public NonBindableActorException(Throwable cause) {
		super(cause);
	}
}
