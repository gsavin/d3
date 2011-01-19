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
package org.d3.test;

public class Test implements Thread.UncaughtExceptionHandler {
	
	public static class TThread extends Thread {
		public TThread(Thread.UncaughtExceptionHandler ueh, String name) {
			super(name);
			setUncaughtExceptionHandler(ueh);
		}
		
		public void run() {
			throw new RuntimeException();
		}
	}

	public void uncaughtException(Thread t, Throwable e) {
		System.out.printf("%s terminate due to %s, from %s%n", t.getName(), e.getMessage(), Thread.currentThread().getName());
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Test t = new Test();
		TThread tthread = new TThread(t, "test");
		tthread.start();
	}
}
