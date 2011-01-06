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

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;

public class ElementProcessor<T extends ProgramElementDoc> extends BaseProcessor<T> {

	public ElementProcessor(T elementDoc, String templateName, Parameters params) {
		super(elementDoc,templateName,params);
	}

	public String name(boolean brief) {
		return getTypeNameDisplay(elementDoc.qualifiedName(), brief, params);
	}
	
	public String cleanName(boolean brief) {
		return brief ? elementDoc.name() : elementDoc.qualifiedName();
	}

	public String access() {
		if (elementDoc.isPublic())
			return "public";
		else if (elementDoc.isProtected())
			return "protected";
		else if (elementDoc.isPrivate())
			return "private";
		else
			return "";
	}

	public String modifiers() {
		String modifiers = "";

		if (elementDoc.isStatic())
			modifiers = String.format("%s%s%s", modifiers,
					modifiers.length() > 0 ? " " : "", "static");

		if (elementDoc.isFinal())
			modifiers = String.format("%s%s%s", modifiers,
					modifiers.length() > 0 ? " " : "", "final");

		return modifiers;
	}

	public String annotations() {
		StringWriter out = new StringWriter();
		
		AnnotationDesc[] annotations = elementDoc.annotations();

		if (annotations != null && annotations.length > 0) {
			for (AnnotationDesc annotation : annotations) {
				if (annotation.annotationType().name()
						.equals("IdentifiableObjectPath")) {
					out.write("<div class=\"class-identifiableobject-path\">");
					out.write("<h3>Identifiable Object Path:</h3>");

					String sep = "";

					for (ElementValuePair evp : annotation.elementValues()) {
						out.write(sep + evp.value().value());
						sep = ", ";
					}
					out.write("</div>\n");
				} else if (annotation.annotationType().name()
						.equals("IdentifiableObjectDescription")) {
					out.write("<div class=\"class-identifiableobject-description\">");
					out.write("<h3>Identifiable Object Description:</h3>");

					for (ElementValuePair evp : annotation.elementValues()) {
						out.write(evp.value().value().toString());
					}
					out.write("</div>\n");
				}
			}
		}
		
		return out.toString();
	}
}
