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
package org.d3.actor;

import java.util.concurrent.TimeUnit;

/**
 * This define actors executing step. When body of such actors is started, a
 * special 'step' action is put on the queue and is called after delay given by
 * {@link #getStepDelay(TimeUnit)} has been reached. When the step is executed,
 * a new special 'step' action is enqueued.
 * 
 * @author Guilhelm Savin
 * 
 */
public interface StepActor {
	/**
	 * Delay until next step. Delay not has to be the same between all the step
	 * since executor get the delay at each step.
	 * 
	 * @param unit
	 * @return
	 */
	long getStepDelay(TimeUnit unit);

	/**
	 * Code to execute at each step.
	 */
	void step();
}
