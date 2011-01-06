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

import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedList;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;

public class IndexProcessor extends BaseProcessor<RootDoc> {

	public IndexProcessor(RootDoc elementDoc, Parameters params) {
		super(elementDoc, "index.template", params);
	}

	public boolean containsPackages() {
		return elementDoc.classes() != null && elementDoc.classes().length > 0;
	}

	public String content() {
		StringWriter out = new StringWriter();

		LinkedList<String> packages = new LinkedList<String>();
		for (PackageDoc packageDoc : params.eachPackageDoc())
			packages.add(packageDoc.name());

		Collections.sort(packages);

		for (int i = 0; i < packages.size(); i++) {
			PackageDoc packageDoc = params.getPackageDoc(packages.get(i));
			LinkedList<String> classes = new LinkedList<String>();

			int max = 0;

			for (ClassDoc classDoc : packageDoc.allClasses()) {
				classes.add(classDoc.qualifiedName());
				max = Math.max(max, classDoc.name().length());
			}

			Collections.sort(classes);

			out.write(String.format("<h2>%s</h2>%n",
					getPackageNameDisplay(packageDoc, params)));
			out.write("<ul class=\"package-subpackage-entries\">\n");

			for (int j = 0; j < classes.size(); j++) {
				out.write(String
						.format("<li class=\"package-class-entry\" style=\"float: left; width: %dpx;\">%s</li>%n",
								max * 10,
								getTypeNameDisplay(classes.get(j), true, params)));
			}

			out.write("</ul>\n<br style=\"clear: both;\"/>\n");
		}

		return out.toString();
	}
}
