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
package org.d3.feature;

import java.util.concurrent.TimeUnit;

import org.d3.Args;
import org.d3.actor.Agency;
import org.d3.actor.Feature;
import org.d3.actor.StepActor;
import org.d3.annotation.ActorPath;

@ActorPath("/features/step/")
public class StepFeature extends Feature implements StepActor {

	public StepFeature() {
		super("test");
	}

	public long getStepDelay(TimeUnit unit) {
		return unit.convert(1, TimeUnit.SECONDS);
	}

	int step = 0;
	
	public void step() {
		System.out.printf("step %03d%n", step++);
	}

	public boolean initFeature(Agency agency, Args args) {
		// TODO Auto-generated method stub
		return false;
	}

	public void terminateFeature() {
		// TODO Auto-generated method stub

	}

}
