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
package org.ri2c.d3.agency.feature;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.ConcurrentGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.GraphRenderer;
import org.graphstream.ui.swingViewer.Viewer;
import org.ri2c.d3.Agency;
import org.ri2c.d3.Args;
import org.ri2c.d3.IdentifiableObject;
import org.ri2c.d3.RemoteIdentifiableObject;
import org.ri2c.d3.agency.AgencyListener;
import org.ri2c.d3.agency.Feature;
import org.ri2c.d3.agency.RemoteAgencyDescription;
import org.ri2c.d3.annotation.IdentifiableObjectPath;

@IdentifiableObjectPath("/d3/features/model")
public class Model
	implements Feature, AgencyListener
{
	protected static long MODEL_ID_GENERATOR = 0;
	
	protected static class ResizableView
		extends DefaultView
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -4534471579509816964L;
		
		int width, height;
		
		public ResizableView( Viewer viewer, String identifier, GraphRenderer renderer )
		{
			super(viewer,identifier,renderer);
			
			GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			resize( dev.getDisplayMode().getWidth() / 2, dev.getDisplayMode().getHeight() / 2 );
		}
		
		@Override
		public void openInAFrame( boolean on )
		{
			if( on )
			{
				if( frame == null )
				{
					frame = new JFrame( "L2D Execution Model" );
					frame.setLayout( new BorderLayout() );
					frame.add( this, BorderLayout.CENTER );
					frame.setSize( width, height );
					frame.setVisible( true );
					frame.addWindowListener( this );
					frame.addKeyListener( shortcuts );
				}
				else
				{
					frame.setVisible( true );
				}
			}
			else
			{
				if( frame != null )
				{
					frame.removeWindowListener( this );
					frame.removeKeyListener( shortcuts );
					frame.remove( this );
					frame.setVisible( false );
					frame.dispose();
				}
			}
		}
		
		public void resize( int width, int height )
		{
			this.width = width;
			this.height = height;
			
			if( frame != null )
				frame.setSize(width,height);
		}
	}
	
	private class WeightDecreaser
		implements Runnable
	{
		public void run()
		{
			while( true )
			{
				for( Edge e: model.getEachEdge() )
					decreaseWeight(e);
				
				try
				{
					Thread.sleep(TimeUnit.MILLISECONDS.convert(
							weightDecreaserPeriod,weightDecreaserUnit));
				}
				catch( InterruptedException e )
				{
					
				}
			}
		}
	}
	
	protected static String defaultStyleSheet =
		"graph {" +
		" padding: 50px;" +
		"}" +
		"node .remote {" +
		" fill-color: red;" +
		"}";
	
	protected String 			modelId;
	protected Graph				model;
	protected Viewer 			viewer;
	protected long				weightDecreaserPeriod 	= 500;
	protected TimeUnit			weightDecreaserUnit 	= TimeUnit.MILLISECONDS;
	protected WeightDecreaser	weightDecreaser			= new WeightDecreaser();
	protected boolean			justEntities			= true;
	
	public Model()
	{
		modelId = String.format("model%x", MODEL_ID_GENERATOR++);
	}

	public String getId()
	{
		return modelId;
	}

	public IdentifiableType getType()
	{
		return IdentifiableType.feature;
	}

	public boolean initFeature(Agency agency, Args args)
	{
		model = new ConcurrentGraph(getId(),false,true);
		model.addAttribute("ui.stylesheet",defaultStyleSheet);
		agency.addAgencyListener(this);
		
		Thread t = new Thread(weightDecreaser,"l2d.features.model.decreaser");
		t.setDaemon(true);
		t.start();
		
		if( args.has("display") && Boolean.parseBoolean(args.get("display")) && isDisplayable() )
			display(true);
		
		return true;
	}
	
	protected void display( boolean autolayout )
	{
		if( isDisplayable() )
		{
			if( viewer != null )
			{
				
			}
			else
			{
				viewer = new Viewer(model,Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
				
				DefaultView view =
					new ResizableView(viewer,Viewer.DEFAULT_VIEW_ID,Viewer.newGraphRenderer());
				
				viewer.addView(view);
				view.openInAFrame(true);
				
				if( autolayout )
				{
					Layout layout = Layouts.newLayoutAlgorithm();
					viewer.enableAutoLayout( layout );
				}
			}
		}
	}

	public void agencyExit(Agency agency) {
		
	}

	public void newAgencyRegistered(RemoteAgencyDescription rad) {
		
	}

	public void remoteAgencyDescriptionUpdated(RemoteAgencyDescription rad) {
		
	}

	protected Node createNode( String id, String type, boolean isRemote )
	{
		Node n = model.getNode(id);
		
		if( n == null )
		{
			n = model.addNode(id);
			n.addAttribute("type",type);
			n.addAttribute("ui.label",id);
		}
		
		if( isRemote )
			setNodeRemote(id);
		
		return n;
	}
	
	protected void setNodeRemote( String id )
	{
		Node n = model.getNode(id);
		
		if( n != null )
			n.addAttribute("ui.class","remote");
	}
	
	public void requestReceived( IdentifiableObject source,
			IdentifiableObject target, String name)
	{
		if( justEntities && source.getType() != IdentifiableType.entity ||
				target.getType() != IdentifiableType.entity )
			return;
		
		Node targetNode = model.getNode(target.getId());
		
		if( targetNode == null )
		{
			targetNode = createNode(target.getId(),target.getType().name(),
					( target instanceof RemoteIdentifiableObject ));
		}
		
		Node sourceNode = model.getNode(source.getId());
		
		if( sourceNode == null )
		{
			sourceNode = createNode(source.getId(),source.getType().name(),
					( source instanceof RemoteIdentifiableObject ));
		}
		
		Edge e = targetNode.getEdgeFrom(sourceNode.getId());
		
		if( e == null )
		{
			e = model.addEdge(String.format("%s--%s",source.getId(),target.getId()),
					source.getId(), targetNode.getId(), true);
			e.addAttribute("weight",1);
		}
		else
		{
			double w = e.getNumber("weight");
			w *= 1.25;
			e.changeAttribute("weight",w);
		}
	}

	public void identifiableObjectRegistered(IdentifiableObject idObject) {
		if( ! justEntities || idObject.getType() == IdentifiableType.entity )
			createNode(idObject.getId(),idObject.getType().name(),
				idObject instanceof RemoteIdentifiableObject);
	}

	public void identifiableObjectUnregistered(IdentifiableObject idObject) {
		model.removeNode(idObject.getId());
	}
	
	protected boolean isDisplayable()
	{
		return ! GraphicsEnvironment.isHeadless();
	}
	
	protected void increaseWeight( Edge e )
	{
		double w = e.getNumber("weight");
		w *= 1.2;
		e.changeAttribute("weight",w);
	}
	
	protected void decreaseWeight( Edge e )
	{
		double w = e.getNumber("weight");
		w *= 0.5;
		
		if( w < 0.2 )
		{
			model.removeEdge(e.getId());
		}
		else
		{
			e.changeAttribute("weight",w);
		}
	}
}
