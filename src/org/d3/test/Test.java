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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

public class Test {

	static class A {
		final int a;
		
		A(int a) {
			this.a = a;
		}
	}
	
	static class B extends A implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8645516531113093947L;
		
		int b;
		
		B(int a) {
			super(a);
			b = 1;
		}
		
		private void writeObject(ObjectOutputStream out) throws IOException {
			out.writeInt(a);
			out.writeInt(b);
		}
		
		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			int a = in.readInt();
			
			try {
				Field f_a = A.class.getDeclaredField("a");
				f_a.setAccessible(true);
				f_a.set(this, a);
			} catch(Exception e) {
				e.printStackTrace();
			}

			b = in.readInt();
		}
	}
	
	static class C extends B {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8645516531113093947L;
		
		int c;
		
		C(int a) {
			super(a);
			c = 2;
		}
		
		public void set(int b, int c) {
			this.b = b;
			this.c = c;
		}
		
		public String toString() {
			return String.format("%d;%d;%d", a, b, c);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(byteOut);
		
		C c1 = new C(11);
		c1.set(987,9876);
		
		System.out.println(c1);
		
		out.writeObject(c1);
		out.flush();
		out.close();
		
		byte[] data = byteOut.toByteArray();
		
		ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
		ObjectInputStream in = new ObjectInputStream(byteIn);
		
		C c2 = (C) in.readObject();
		System.out.println(c2);
	}
}
