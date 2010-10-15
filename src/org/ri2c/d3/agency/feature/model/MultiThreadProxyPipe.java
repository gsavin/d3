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
package org.ri2c.d3.agency.feature.model;

import java.util.concurrent.locks.ReentrantLock;

import org.graphstream.graph.Graph;
import org.graphstream.stream.Source;
import org.graphstream.stream.thread.ThreadProxyPipe;
import org.ri2c.d3.Console;

public class MultiThreadProxyPipe extends ThreadProxyPipe {

	protected final ReentrantLock lock = new ReentrantLock();

	public MultiThreadProxyPipe(Graph source) {
		super(source,false);
		
		replayGraph(source);
	}

	protected void replayGraph(Graph graph) {
		lock.lock();
		super.replayGraph(graph);
		lock.unlock();
	}
	
	public void pump() {
		lock.lock();
		super.pump();
		lock.unlock();
	}
	
	public void edgeAttributeAdded(String graphId, long timeId, String edgeId,
			String attribute, Object value) {
		lock.lock();
		super.edgeAttributeAdded(graphId, timeId, edgeId, attribute, value);
		lock.unlock();
	}

	public void edgeAttributeChanged(String graphId, long timeId,
			String edgeId, String attribute, Object oldValue, Object newValue) {
		lock.lock();
		super.edgeAttributeChanged(graphId, timeId, edgeId, attribute,
				oldValue, newValue);
		lock.unlock();
	}

	public void edgeAttributeRemoved(String graphId, long timeId,
			String edgeId, String attribute) {
		lock.lock();
		super.edgeAttributeRemoved(graphId, timeId, edgeId, attribute);
		lock.unlock();
	}

	public void graphAttributeAdded(String graphId, long timeId,
			String attribute, Object value) {
		lock.lock();
		super.graphAttributeAdded(graphId, timeId, attribute, value);
		lock.unlock();
	}

	public void graphAttributeChanged(String graphId, long timeId,
			String attribute, Object oldValue, Object newValue) {
		lock.lock();
		super.graphAttributeChanged(graphId, timeId, attribute, oldValue,
				newValue);
		lock.unlock();
	}

	public void graphAttributeRemoved(String graphId, long timeId,
			String attribute) {
		lock.lock();
		super.graphAttributeRemoved(graphId, timeId, attribute);
		lock.unlock();
	}

	public void nodeAttributeAdded(String graphId, long timeId, String nodeId,
			String attribute, Object value) {
		lock.lock();
		super.nodeAttributeAdded(graphId, timeId, nodeId, attribute, value);
		lock.unlock();
	}

	public void nodeAttributeChanged(String graphId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		lock.lock();
		super.nodeAttributeChanged(graphId, timeId, nodeId, attribute,
				oldValue, newValue);
		lock.unlock();
	}

	public void nodeAttributeRemoved(String graphId, long timeId,
			String nodeId, String attribute) {
		lock.lock();
		super.nodeAttributeRemoved(graphId, timeId, nodeId, attribute);
		lock.unlock();
	}

	public void edgeAdded(String graphId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		lock.lock();
		super.edgeAdded(graphId, timeId, edgeId, fromNodeId, toNodeId, directed);
		lock.unlock();
	}

	public void edgeRemoved(String graphId, long timeId, String edgeId) {
		lock.lock();
		super.edgeRemoved(graphId, timeId, edgeId);
		lock.unlock();
	}

	public void graphCleared(String graphId, long timeId) {
		lock.lock();
		super.graphCleared(graphId, timeId);
		lock.unlock();
	}

	public void nodeAdded(String graphId, long timeId, String nodeId) {
		lock.lock();
		super.nodeAdded(graphId, timeId, nodeId);
		lock.unlock();
	}

	public void nodeRemoved(String graphId, long timeId, String nodeId) {
		lock.lock();
		super.nodeRemoved(graphId, timeId, nodeId);
		lock.unlock();
	}

	public void stepBegins(String graphId, long timeId, double step) {
		lock.lock();
		super.stepBegins(graphId, timeId, step);
		lock.unlock();
	}

}
