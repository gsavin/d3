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

import java.lang.reflect.Method;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class TestCallMethod extends ClassLoader {

	public static interface C {
		void call();
	}
	
	public static class D implements C {
		public void call() {
			
		}
	}
	
	public void test() {
		//Math.pow(Math.tanh(10.3444), 10.333);
	}

	public C createCall(String name) throws Exception {
		ClassPool cp = ClassPool.getDefault();
		CtClass cc = cp.makeClass("call_"+name);
		cc.addInterface(cp.get(C.class.getName()));
		CtMethod cm = CtMethod.make("public void call() {}", cc);
		cc.addMethod(cm);
		
		byte[] data = cc.toBytecode();
		Class<?> cls = defineClass(cc.getName(), data, 0, data.length);
		C c = (C) cls.newInstance();
		return c;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		TestCallMethod t = new TestCallMethod();
		
		C d1 = new D();
		
		long m1, m2;
		int size = 100000;
		
		m1 = System.nanoTime();
		for (int i = 0; i < size; i++) {
			d1.call();
		}
		m2 = System.nanoTime();
		
		System.out.printf("> average [direct] : %d ns%n", (m2-m1)/size);

		m1 = System.nanoTime();
		Method m = D.class.getMethod("call");
		for (int i = 0; i < size; i++) {
			m.invoke(d1);
		}
		m2 = System.nanoTime();
		
		System.out.printf("> average [reflect] : %d ns%n", (m2-m1)/size);

		C c1 = t.createCall("test");
		m1 = System.nanoTime();
		for (int i = 0; i < size; i++) {
			c1.call();
		}
		m2 = System.nanoTime();
		
		System.out.printf("> average [compil] : %d ns%n", (m2-m1)/size);
	}
}
