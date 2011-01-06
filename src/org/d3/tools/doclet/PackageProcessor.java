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

import java.util.Collections;
import java.util.LinkedList;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;

public class PackageProcessor extends BaseProcessor<PackageDoc> {

	public PackageProcessor(PackageDoc elementDoc, Parameters params) {
		super(elementDoc, "package.template", params);
	}

	public String name() {
		return getPackageNameDisplay(elementDoc, params);
	}

	public boolean containsClasses() {
		return elementDoc.allClasses() != null
				&& elementDoc.allClasses().length > 0;
	}

	public String classes() {
		LinkedList<String> classes = new LinkedList<String>();

		for (ClassDoc classDoc : elementDoc.allClasses()) {
			String p = classDoc.qualifiedName();
			classes.add(p);
		}

		String ret = "";

		if (classes.size() > 0) {
			Collections.sort(classes);

			for (int i = 0; i < classes.size(); i++)
				ret += String.format(
						"<li class=\"package-class-entry\">%s</li>\n",
						getTypeNameDisplay(classes.get(i), true, params));
		}

		return ret;
	}

	public boolean containsSubpackages() {
		for (PackageDoc packageDoc : params.eachPackageDoc()) {
			String p = packageDoc.name();

			if (p.startsWith(elementDoc.name()) && !p.equals(elementDoc.name())) {
				return true;
			}
		}

		return false;
	}

	public String subpackages() {
		LinkedList<String> subpackages = new LinkedList<String>();

		for (PackageDoc packageDoc : params.eachPackageDoc()) {
			String p = packageDoc.name();

			if (p.startsWith(elementDoc.name()) && !p.equals(elementDoc.name())) {
				subpackages.add(p);
			}
		}

		String ret = "";

		if (subpackages.size() > 0) {
			Collections.sort(subpackages);

			for (int i = 0; i < subpackages.size(); i++)
				ret += String.format(
						"<li class=\"package-subpackage-entry\">%s</li>\n",
						getPackageNameDisplay(
								params.getPackageDoc(subpackages.get(i)),
								params));
		}

		return ret;
	}
}
