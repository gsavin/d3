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
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.d3.template.Template;

import javassist.ClassPool;
import javassist.CtClass;

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

		private void readObject(ObjectInputStream in) throws IOException,
				ClassNotFoundException {
			int a = in.readInt();

			try {
				Field f_a = A.class.getDeclaredField("a");
				f_a.setAccessible(true);
				f_a.set(this, a);
			} catch (Exception e) {
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

		protected int c;
		transient long d;
		A e;

		C() {
			this(0);
		}
		
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

	public static class FieldData {
		String name;
		Object value;
		
		public FieldData(String name, Object value) {
			this.name = name;
			this.value = value;
		}
	}

	public static class Importer {
		HashMap<String, Field> fields;

		public Importer(Class<?> cls) {
			ObjectStreamField[] osfields = getExportableFields(cls);
			fields = new HashMap<String, Field>();
			
			for (int i = 0; i < osfields.length; i++) {
				String name = osfields[i].getName();
				
				try {
					Field f = cls.getDeclaredField(name);
					fields.put(name, f);
				} catch (Exception e) {
					System.err.printf("can not get field \"%s\" %n", name);
					e.printStackTrace();
				}
			}
		}

		public void importObject(Object target, FieldData[] data) {
			for (int i = 0; i < data.length; i++) {
				Field f = fields.get(data[i].name);
				try {
					f.set(target, data[i].value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static ObjectStreamField[] getExportableFields(Class<?> cls) {
		LinkedList<ObjectStreamField> fields = new LinkedList<ObjectStreamField>();

		ObjectStreamClass osc = ObjectStreamClass.lookup(cls);
		while (osc != null) {
			ObjectStreamField[] thisClassFields = osc.getFields();

			if (thisClassFields != null) {
				for (int i = 0; i < thisClassFields.length; i++)
					fields.addLast(thisClassFields[i]);
			}

			osc = ObjectStreamClass.lookup(osc.forClass().getSuperclass());
		}

		return fields.toArray(new ObjectStreamField[fields.size()]);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(byteOut);
		C c1 = new C(11);
		c1.set(987, 9876);

		FieldData[] data = {
			new FieldData("c", 111)	
		};
		
		C c2 = new C();
		Importer i = new Importer(C.class);
		System.out.println(c2);
		i.importObject(c2, data);
		System.out.println(c2);
		
		/*
		 * System.out.println(c1);
		 * 
		 * out.writeObject(c1); out.flush(); out.close();
		 * 
		 * byte[] data = byteOut.toByteArray();
		 * 
		 * ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
		 * ObjectInputStream in = new ObjectInputStream(byteIn);
		 * 
		 * C c2 = (C) in.readObject(); System.out.println(c2);
		 */
	}
}
