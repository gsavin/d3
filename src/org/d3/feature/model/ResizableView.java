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
package org.d3.feature.model;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.GraphRenderer;
import org.graphstream.ui.swingViewer.Viewer;

public class ResizableView extends DefaultView {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4534471579509816964L;

	int width, height;

	public ResizableView(Viewer viewer, String identifier,
			GraphRenderer renderer) {
		super(viewer, identifier, renderer);

		GraphicsDevice dev = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		resize(dev.getDisplayMode().getWidth() / 2, dev.getDisplayMode()
				.getHeight() / 2);
	}

	@Override
	public void openInAFrame(boolean on) {
		if (on) {
			if (frame == null) {
				frame = new JFrame("D3 Execution Model");
				frame.setLayout(new BorderLayout());
				frame.add(this, BorderLayout.CENTER);
				frame.setSize(width, height);
				frame.setVisible(true);
				frame.addWindowListener(this);
				frame.addKeyListener(shortcuts);
			} else {
				frame.setVisible(true);
			}
		} else {
			if (frame != null) {
				frame.removeWindowListener(this);
				frame.removeKeyListener(shortcuts);
				frame.remove(this);
				frame.setVisible(false);
				frame.dispose();
			}
		}
	}

	public void resize(int width, int height) {
		this.width = width;
		this.height = height;

		if (frame != null)
			frame.setSize(width, height);
	}
}