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

public class Test {

	static class RunA implements Runnable {
		Object obj;

		public RunA(Object obj) {
			this.obj = obj;
		}

		public void run() {
			System.out.printf("A started%n");

			synchronized (obj) {
				try {
					System.out.printf("A starts waiting%n");
					obj.wait();
					System.out.printf("A has not been interrupted%n");
				} catch (InterruptedException e) {
					System.out.printf("A has been interrupted%n");
				}
			}
		}
	}

	static class RunB implements Runnable {
		Object obj;

		public RunB(Object obj) {
			this.obj = obj;
		}

		public void run() {
			System.out.printf("B started%n");

			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			synchronized (obj) {
				System.out.printf("B notify all%n");
				obj.notifyAll();
			}

			System.out.printf("B exited%n");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// Object obj = new Object();

		// RunA ra = new RunA(obj);
		// RunB rb = new RunB(obj);

		// Thread a = new Thread(ra);
		// Thread b = new Thread(rb);

		// b.start();
		// a.start();
	}
}
