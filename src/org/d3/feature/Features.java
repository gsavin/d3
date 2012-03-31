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
package org.d3.feature;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.d3.Console;
import org.d3.actor.Agency;
import org.d3.actor.Feature;
import org.d3.actor.Agency.Argument;

public class Features {
	public static void init() {
		Agency.getLocalAgency().checkBodyThreadAccess();

		String featuresToLoad = Agency.getArg(Argument.FEATURES.key);

		if (featuresToLoad != null) {
			Pattern feature = Pattern
					.compile("(@?[a-zA-Z0-9_]+(?:[.][a-zA-Z0-9_]+)*)\\(([^\\)]+)?\\)");
			Matcher features = feature.matcher(featuresToLoad);

			while (features.find()) {
				String cls = features.group(1);
				String fid = features.group(2);

				cls = cls.replace("@", Features.class.getPackage().getName()
						+ ".");

				try {
					enableFeature(cls, fid);
				} catch (BadFeatureException e) {
					Console.exception(e);
				}
			}
		}
	}

	public static void enableFeature(String classname)
			throws BadFeatureException {
		enableFeature(classname, null);
	}

	@SuppressWarnings("unchecked")
	public static void enableFeature(String classname, String featureId)
			throws BadFeatureException {
		Class<? extends Feature> cls;

		try {
			cls = (Class<? extends Feature>) Class.forName(classname);

			Feature f;

			if (featureId == null)
				f = cls.newInstance();
			else
				f = cls.getConstructor(String.class).newInstance(featureId);

			f.init();
		} catch (Exception e) {
			throw new BadFeatureException(e);
		}
	}

	public Features() {

	}

	public void register(Feature f) {
		f.checkBodyThreadAccess();
	}
}
