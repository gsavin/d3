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

import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.Parameter;

public class ExecutableMemberProcessor<T extends ExecutableMemberDoc> extends
		ElementProcessor<T> {

	public final boolean brief;

	public ExecutableMemberProcessor(T elementDoc,
			String templateName, Parameters params, boolean brief) {
		super(elementDoc, templateName, params);
		this.brief = brief;
	}
	
	public String name() {
		return String.format(
				"<span class=\"type-name\"><a href=\"#%s\">%s</a></span>",
				elementDoc.name(), elementDoc.name());
	}

	public boolean isBrief() {
		return brief;
	}

	public String signature() {
		String display = "<span style=\"font-weight: bold;\">"
				+ elementDoc.name() + "</span>(";

		String sep = "";

		for (Parameter p : elementDoc.parameters()) {
			display = String.format(
					"%s%s%s <span style=\"font-weight: normal;\">%s</span>",
					display, sep, getTypeNameDisplay(p.type().qualifiedTypeName(),true,params), p.name());
			sep = ", ";
		}

		display += ")";

		return display;
	}

}
