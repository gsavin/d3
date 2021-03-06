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

import com.sun.javadoc.FieldDoc;

public class FieldProcessor extends ElementProcessor<FieldDoc> {

	public final boolean brief;

	public FieldProcessor(FieldDoc elementDoc, Parameters params, boolean brief) {
		super(elementDoc, "field.template", params);
		this.brief = brief;
	}

	public boolean isBrief() {
		return brief;
	}

	public String type() {
		return getTypeNameDisplay(elementDoc.type().qualifiedTypeName(), true,
				params);
	}

	public String name() {
		return String.format("<span class=\"type-name\"><a href=\"#%s\">%s</a></span>",
				elementDoc.name(), elementDoc.name());
	}
}
