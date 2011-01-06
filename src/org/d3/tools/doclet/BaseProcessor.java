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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import com.sun.javadoc.Doc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ProgramElementDoc;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class BaseProcessor<T extends Doc> {

	protected T elementDoc;

	private Configuration config;
	private Template template;
	protected Parameters params;

	public BaseProcessor(T elementDoc, String templateName, Parameters params) {
		this.elementDoc = elementDoc;
		this.config = new Configuration();
		this.params = params;

		try {
			config.setDirectoryForTemplateLoading(new File(params.templateDir));
			template = config.getTemplate(templateName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void process(Writer out) {
		HashMap<String, Object> hash = new HashMap<String, Object>();

		hash.put("doc", this);
		hash.put("header", params.header);
		hash.put("footer", params.footer);
		hash.put("generator", D3Doclet.class.getName());
		
		try {
			template.process(hash, out);
		} catch (TemplateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String name() {
		return String.format("<span class=\"type-name\">%s</span>",
				elementDoc.name());
	}

	public boolean hasComment() {
		return elementDoc.commentText() != null
				&& elementDoc.commentText().length() > 0;
	}

	public String comment(boolean brief) {
		String comment = elementDoc.commentText();

		if (brief && comment.indexOf('.') > 0)
			comment = comment.substring(0, comment.indexOf('.') + 1);

		return comment;
	}

	public String type() {
		if (elementDoc.isClass())
			return "Class";
		else if (elementDoc.isAnnotationType())
			return "Annotation";
		else if (elementDoc.isInterface())
			return "Interface";
		else if (elementDoc.isEnum())
			return "Enum";
		else
			return "";
	}

	public static String getTypeNameDisplay(String name,
			boolean brief, Parameters params) {
		
		ProgramElementDoc elementDoc = params.getClassDoc(name);
		
		if( elementDoc != null ) 
			return String.format("<span class=\"type-name\">"
				+ "<a href=\"%s%s.html\">%s</a>" + "</span>", params.baseURL,
				elementDoc.qualifiedName().replace(".", "/"),
				brief ? elementDoc.name() : elementDoc.qualifiedName());
		else
			return String.format("<span class=\"type-name\">%s</span>",name);
	}

	public static String getPackageNameDisplay(PackageDoc packageDoc,
			Parameters params) {
		String display = "";

		String name = packageDoc.name();
		String sep = "";

		while (name.length() > 0) {
			PackageDoc doc = params.getPackageDoc(name);
			String briefName = name.indexOf('.') > 0 ? name.substring(name
					.lastIndexOf('.') + 1) : name;

			if (doc == null) {
				display = briefName + sep + display;
				sep = ".";
			} else {
				display = String.format(
						"<a href=\"%s%s/index.html\">%s</a>%s%s",
						params.baseURL, name.replace(".", "/"), briefName, sep,
						display);
				sep = ".";
			}

			name = name.indexOf('.') > 0 ? name.substring(0,
					name.lastIndexOf('.')) : "";
		}

		return "<span class=\"type-name\">" + display + "</span>";
	}
}
