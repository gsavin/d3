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
package org.d3.tools.doclet;

import java.util.HashMap;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;

public class Parameters {
	public final String baseURL;
	public final String header;
	public final String footer;
	public final String templateDir;

	public final HashMap<String, ClassDoc> classes = new HashMap<String, ClassDoc>();
	public final HashMap<String, PackageDoc> packages = new HashMap<String, PackageDoc>();

	public final HashMap<String, String> paths = new HashMap<String, String>();

	public Parameters(String baseURL, String templateDir, String header,
			String footer) {
		this.baseURL = baseURL;
		this.templateDir = templateDir;
		this.header = header;
		this.footer = footer;
	}

	public void add(ClassDoc classDoc, String path) {
		classes.put(classDoc.qualifiedName(), classDoc);
		paths.put(classDoc.qualifiedName(), path);
	}

	public void add(PackageDoc packageDoc, String path) {
		packages.put(packageDoc.name(), packageDoc);
		paths.put(packageDoc.name(), path);
	}
	
	public String getPath( String name ) {
		return paths.get(name);
	}

	public Iterable<ClassDoc> eachClassDoc() {
		return classes.values();
	}
	
	public Iterable<PackageDoc> eachPackageDoc() {
		return packages.values();
	}

	public PackageDoc getPackageDoc(String name) {
		return packages.get(name);
	}

	public ClassDoc getClassDoc(String name) {
		return classes.get(name);
	}
}
