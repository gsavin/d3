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
package org.d3.entity.migration;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.d3.Console;
import org.d3.actor.Entity;
import org.d3.entity.Migratable;

public class IOMap {

	private static final ConcurrentHashMap<Class<? extends Entity>, IOMap> maps = new ConcurrentHashMap<Class<? extends Entity>, IOMap>();

	public static synchronized IOMap get(Class<? extends Entity> cls) {
		IOMap map = null;

		if (maps.containsKey(cls)) {
			map = maps.get(cls);
		} else {
			long m1, m2;
			
			m1 = System.currentTimeMillis();
			map = new IOMap(cls);
			maps.put(cls, map);
			m2 = System.currentTimeMillis();
			
			Console.info("iomap for \"%s\" created in %d ms", cls.getSimpleName(), m2-m1);
		}

		return map;
	}

	private final Class<? extends Entity> associatedClass;
	private final Map<String, Field> fields;

	@SuppressWarnings("unchecked")
	private IOMap(Class<? extends Entity> associatedClass) {
		this.associatedClass = associatedClass;

		HashMap<String, Field> m = new HashMap<String, Field>();

		while (associatedClass != null) {
			Field[] fields = associatedClass.getDeclaredFields();

			if (fields != null) {
				for (int i = 0; i < fields.length; i++) {
					if (fields[i].getAnnotation(Migratable.class) != null)
						m.put(fields[i].getName(), fields[i]);
				}
			}

			Class<?> superclass = associatedClass.getSuperclass();

			if (superclass != Entity.class
					&& Entity.class.isAssignableFrom(superclass)) {
				associatedClass = (Class<? extends Entity>) superclass;
			} else {
				associatedClass = null;
			}
		}

		this.fields = Collections.unmodifiableMap(m);
	}

	public Class<? extends Entity> getAsssociatedClass() {
		return associatedClass;
	}

	public void importData(Entity target, MigratableField[] fieldsData)
			throws ImportationException {
		if (fieldsData != null) {
			for (int i = 0; i < fieldsData.length; i++) {
				if (!fields.containsKey(fieldsData[i].getName()))
					throw new ImportationException(new NoSuchFieldException(
							fieldsData[i].getName()));
			}

			for (int i = 0; i < fieldsData.length; i++) {
				Field f = fields.get(fieldsData[i].getName());

				try {
					boolean accessible = f.isAccessible();
					
					f.setAccessible(true);
					f.set(target, fieldsData[i].getValue());
					f.setAccessible(accessible);
				} catch (IllegalArgumentException e) {
					throw new ImportationException(e);
				} catch (IllegalAccessException e) {
					throw new ImportationException(e);
				}
			}
		}
	}

	public MigratableField[] exportData(Entity target)
			throws ExportationException {
		MigratableField[] fieldsData = new MigratableField[fields.size()];

		int i = 0;

		for (Field f : fields.values()) {
			MigratableField mf;
			
			try {
				boolean accessible = f.isAccessible();
				f.setAccessible(true);
				
				mf = new MigratableField(f.getName(), f.get(target));
				fieldsData[i++] = mf;
				
				f.setAccessible(accessible);
			} catch (IllegalArgumentException e) {
				throw new ExportationException(e);
			} catch (IllegalAccessException e) {
				throw new ExportationException(e);
			}
		}

		return fieldsData;
	}
}
