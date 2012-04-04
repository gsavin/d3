/*
 * This file is part of d3 <http://d3-project.org>.
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
 * Copyright 2010 - 2011 Guilhelm Savin
 */
package org.d3.fault;

import org.d3.Console;
import org.d3.FaultManager;
import org.d3.actor.Agency;

public class DefaultFaultManager implements FaultManager {

	protected FaultPolicy policy;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.d3.FaultManager#setFaultPolicy(org.d3.FaultManager.FaultPolicy)
	 */
	public void setFaultPolicy(FaultPolicy policy) {
		this.policy = policy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.d3.FaultManager#handle(java.lang.Throwable, java.lang.Object)
	 */
	public void handle(Throwable fault, Object context) {
		switch (policy) {
		case EXIT:
			Console.exception(fault);
			Agency.shutdown();
			System.exit(1);
			break;
		case IGNORE:
			break;
		case PRINT_AND_SKIP:
			Console.exception(fault);
			break;
		case TRY_REPAIR:
			//
			// I not made to do that
			//
			break;
		}
	}
}
