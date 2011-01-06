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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;

public class D3Doclet {
	public static boolean start(RootDoc root) {
		String outputDirectory = ".";
		String headerFile = null;
		String footerFile = null;
		String baseURL = "/";
		String templateDir = "org/ri2c/d3/tools/doclet/";

		for (String[] option : root.options()) {
			if (option[0].equals("-d"))
				outputDirectory = option[1];
			else if (option[0].equals("-header"))
				headerFile = option[1];
			else if (option[0].equals("-footer"))
				footerFile = option[1];
			else if (option[0].equals("-base-url"))
				baseURL = option[1];
			else if (option[0].equals("-template-dir"))
				templateDir = option[1];
		}

		ClassDoc[] classes = root.classes();

		String header = getFileContent(headerFile);
		String footer = getFileContent(footerFile);

		Parameters params = new Parameters(baseURL, templateDir, header, footer);

		for (ClassDoc classDoc : classes) {
			PackageDoc packageDoc = classDoc.containingPackage();

			String classPath = outputDirectory + File.separator
					+ classDoc.qualifiedName().replace(".", File.separator)
					+ ".html";
			String packagePath = outputDirectory + File.separator
					+ packageDoc.name().replace(".", File.separator)
					+ File.separator + "index.html";

			params.add(packageDoc, packagePath);
			params.add(classDoc, classPath);
		}

		try {
			String indexPath = outputDirectory + File.separator + "index.html";
			File file = new File(indexPath);
			file.getParentFile().mkdirs();
			FileWriter out = new FileWriter(file);
			IndexProcessor processor = new IndexProcessor(root,params);
			processor.process(out);
			out.flush();
			out.close();
			
			System.out.printf("- write %s%n",indexPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (ClassDoc classDoc : params.eachClassDoc()) {
			try {
				String path = params.getPath(classDoc.qualifiedName());
				File file = new File(path);
				file.getParentFile().mkdirs();
				FileWriter out = new FileWriter(file);
				ClassProcessor processor = new ClassProcessor(classDoc, params);
				processor.process(out);
				out.flush();
				out.close();
				
				System.out.printf("- write %s%n",path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (PackageDoc packageDoc : params.eachPackageDoc()) {
			try {
				String path = params.getPath(packageDoc.name());
				File file = new File(path);
				file.getParentFile().mkdirs();
				FileWriter out = new FileWriter(file);
				PackageProcessor processor = new PackageProcessor(packageDoc,
						params);
				processor.process(out);
				out.flush();
				out.close();
				
				System.out.printf("- write %s%n",path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return true;
	}

	public static String getFileContent(String path) {
		try {
			FileReader reader = new FileReader(path);
			StringWriter outString = new StringWriter();
			PrintWriter out = new PrintWriter(outString);

			char[] buffer = new char[1024];

			int r;

			while (reader.ready()) {
				r = reader.read(buffer, 0, 1024);
				out.write(buffer, 0, r);
			}

			return outString.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}

	public static int optionLength(String option) {
		if (option.matches("^-(d|header|footer|base-url|template-dir)$")) {
			return 2;
		}

		return 0;
	}
}
