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
package org.d3.entity;

import java.util.concurrent.TimeUnit;

import org.d3.Console;
import org.d3.actor.Entity;
import org.d3.actor.Future;
import org.d3.actor.StepActor;

public class TravellerChecker extends Entity implements StepActor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 805570828006781412L;

	EReference reference;
	
	public TravellerChecker(String id) {
		super(id);
	}

	public void init(EReference eref) {
		this.reference = eref;
		
		if(eref==null) throw new NullPointerException();
		
		super.init();
	}
	
	public long getStepDelay(TimeUnit unit) {
		return unit.convert(3, TimeUnit.SECONDS);
	}

	public void step() {
		try {
			Future f = reference.call("get_hopes");
			f.waitForValue();
			Integer h = f.get();
			
			Console.info("reference has done %d hopes", h);
		} catch (Exception e) {
			Console.exception(e);
		}
	}

}
