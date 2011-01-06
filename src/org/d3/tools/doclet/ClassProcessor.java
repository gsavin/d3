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
import java.util.LinkedList;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;

public class ClassProcessor extends ElementProcessor<ClassDoc> {

	public ClassProcessor(ClassDoc classDoc, Parameters params) {
		super(classDoc, "class.template", params);
	}

	public String classPackage() {
		return getPackageNameDisplay(elementDoc.containingPackage(), params);
	}

	public String modifiers() {
		String modifiers = super.modifiers();

		if (elementDoc.isAbstract())
			modifiers = String.format("%s%s%s", "abstract",
					modifiers.length() > 0 ? " " : "", modifiers);

		return modifiers;
	}

	public String fields(boolean brief) {
		StringWriter out = new StringWriter();

		if (hasFields()) {
			for (FieldDoc fieldDoc : elementDoc.fields()) {
				FieldProcessor fieldProcessor = new FieldProcessor(
						fieldDoc, params, brief);
				fieldProcessor.process(out);
			}
		}

		out.flush();

		return out.toString();
	}

	public String constructors(boolean brief) {
		StringWriter out = new StringWriter();

		if (hasConstructors()) {
			for (ConstructorDoc constructorDoc : elementDoc.constructors()) {
				ConstructorProcessor constructorProcessor = new ConstructorProcessor(
						constructorDoc, params, brief);
				constructorProcessor.process(out);
			}
		}

		out.flush();

		return out.toString();
	}

	public String methods(boolean brief) {
		StringWriter out = new StringWriter();

		if (hasMethods()) {
			for (MethodDoc methodDoc : elementDoc.methods()) {
				MethodProcessor methodProcessor = new MethodProcessor(
						methodDoc, params, brief);
				methodProcessor.process(out);
			}
		}

		out.flush();

		return out.toString();
	}

	public String superclasses() {
		StringWriter out = new StringWriter();

		LinkedList<String> superclasses = new LinkedList<String>();
		ClassDoc superclass = elementDoc;

		while (superclass != null) {
			superclasses.addFirst(superclass.qualifiedName());
			superclass = superclass.superclass();
		}

		if (superclasses.size() > 0) {
			String indent = "&nbsp;&nbsp;&nbsp;";
			String corner = "<div style=\"display: inline-block; "
					+ "border-left: solid 1px black; border-bottom: solid 1px black;"
					+ "width: 5px; height: 10px; vertical-align: top; margin-bottom: 2px;\"></div>";

			out.write("<div class=\"class-superclasses\">\n");
			for (int i = 0; i < superclasses.size(); i++) {
				out.write("<div class=\"class-superclass\">");
				for (int j = 0; j < i; j++)
					out.write(indent);
				if (i > 0) {
					out.write(corner);
					out.write(indent);
				}

				ClassDoc superDoc = params.getClassDoc(superclasses.get(i));

				out.write(String.format(
						"<span class=\"type-name\">%s</span>",
						superDoc == null ? superclasses.get(i) : superDoc
								.containingPackage().name()
								+ "."
								+ getTypeNameDisplay(superDoc.qualifiedName(),
										true, params)));
				out.write("</div>\n");
			}
			out.write("</div>\n");
		}

		return out.toString();
	}

	public String interfaces() {
		StringWriter out = new StringWriter();

		ClassDoc[] interfaces = elementDoc.interfaces();

		if (interfaces != null && interfaces.length > 0) {
			out.write("<h3>Implements:</h3>\n<ul class=\"class-interface-entries\">\n");
			for (ClassDoc i : interfaces)
				out.write(String.format(
						"<li class=\"class-interface-entry\">%s</li>%n",
						getTypeNameDisplay(i.qualifiedName(), true, params)));
			out.write("</ul>\n");
		}

		return out.toString();
	}

	public boolean hasFields() {
		return elementDoc.fields() != null && elementDoc.fields().length > 0;
	}

	public boolean hasConstructors() {
		return elementDoc.constructors() != null
				&& elementDoc.constructors().length > 0;
	}

	public boolean hasMethods() {
		return elementDoc.methods() != null && elementDoc.methods().length > 0;
	}
}
