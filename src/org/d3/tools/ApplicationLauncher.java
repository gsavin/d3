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
package org.d3.tools;

import java.util.Arrays;

import org.d3.Application;
import org.d3.actor.Agency;

public class ApplicationLauncher
{
	@SuppressWarnings("unchecked")
	public static void main( String [] args )
	{
		String appname = args [0];
		
		args = args == null ? null : Arrays.copyOfRange(args,1,args.length);
		
		StartD3.init( args );
		
		try
		{
			Class<? extends Application> cls =
				(Class<? extends Application>)Class.forName(appname);
			
			Application app = cls.newInstance();
			Agency.getLocalAgency().launch(app);
		}
		catch( Exception e )
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		StartD3.d3Loop();
	}
}
