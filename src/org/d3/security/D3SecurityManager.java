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
package org.d3.security;

import java.security.Permission;

public class D3SecurityManager extends SecurityManager {
	public void checkPermission( Permission p ) {
		//System.out.println(p);
	}
	
	public void checkPermission( Permission p, Object ctx ) {
		//System.out.println(p);
	}
	
	public void checkAccess(Thread t) {
		System.out.printf("new thread \"%s\" by \"%s\" (%s)%n", t.getName(), Thread.currentThread().getName(),t.isAlive());
	}
	
	public static void main( String ... args ) {
		Thread[] threads = new Thread [Thread.activeCount()];
		Thread.enumerate(threads);
		
		for(Thread t: threads)
			System.out.printf("thread \"%s\"%n",t.getName());
		
		System.setSecurityManager(new D3SecurityManager());
		
		System.out.printf("main thread: %s%n",Thread.currentThread().getName());
		
		Thread t = new Thread( new Runnable() {
			public void run() {
				System.out.printf("thread running%n");
			}
		}, "test");
		
		t.start();
	}
}
