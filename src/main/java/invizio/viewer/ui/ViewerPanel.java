/*
 * Copyright 2017 Benoit Lombardot
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * 1 - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 
 * 2 - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * 3 - Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package invizio.viewer.ui;

import invizio.viewer.data.DataAxis;
import invizio.viewer.ui.ChannelControlPanel;
import invizio.viewer.ui.FreeDimensionControl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import vtk.vtkCanvas;
import vtk.rendering.jogl.*;

//	- add action listener to buttons


public class ViewerPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	vtkJoglCanvasComponent renWin;
	JPanel freeDimensionsPanel;
	Map< String, FreeDimensionControl> freeDimControls;
	JPanel controlsPanel;
	Map< Integer, ChannelControlPanel> channelControls;
	Map< String, JComponent> buttons;
	
	int controlWidth = 300;
	
	public ViewerPanel(){
		
		super(new BorderLayout());
		
		////////////////////////////////////////////////////////////////////////
		// Central panel ///////////////////////////////////////////////////////
		
		// panel for the vtk rendering ///////////////////////////////////
		renWin = new vtkJoglCanvasComponent();
		
		// panel containing horizontal scroll bar for each free dimension ////
		freeDimensionsPanel = new JPanel();
		freeDimensionsPanel.setLayout(new BoxLayout(freeDimensionsPanel, BoxLayout.Y_AXIS));
		
		freeDimControls = new HashMap<String,FreeDimensionControl>();
		
		// add sub panel to center panel
		JPanel centerPanel  = new JPanel();
		centerPanel.setLayout( new BorderLayout() );
		centerPanel.setPreferredSize(new Dimension(centerPanel.getPreferredSize().width, centerPanel.getPreferredSize().height));
		centerPanel.add(renWin.getComponent(),BorderLayout.CENTER);
		centerPanel.add(freeDimensionsPanel, BorderLayout.SOUTH);
		
		
		////////////////////////////////////////////////////
		// side panel //////////////////////////////////////
		
		// mode buttons /////////////////////////////////////////////
		JPanel modePanel = new JPanel();
		//JLabel modeLabel = new JLabel("Manipulation: ");
		JPanel modeButtons = new JPanel();
		//modeButtons.setLayout( new BoxLayout(modeButtons,BoxLayout.X_AXIS));
		modeButtons.setLayout( new GridLayout(1,3));
		JToggleButton widgetModeButton = new JToggleButton("Widget"); 
		JToggleButton linkedModeButton = new JToggleButton("Link"); 
		JToggleButton cameraModeButton = new JToggleButton("Camera"); 
		modeButtons.add(widgetModeButton);
		modeButtons.add(linkedModeButton);
		modeButtons.add(cameraModeButton);
		//modePanel.add(modeLabel);
		modePanel.add(modeButtons);
		
		// widgets buttons
		JPanel widgetsPanel = new JPanel();
		//JLabel widgetsLabel = new JLabel("Widgets: ");
		JPanel widgetsButtons = new JPanel();
		//widgetsButtons.setLayout( new BoxLayout(widgetsButtons,BoxLayout.X_AXIS));
		widgetsButtons.setLayout( new GridLayout(1,3) );
		JToggleButton rendererWidgetButton = new JToggleButton("Renderer");
		JToggleButton slicerWidgetButton = new JToggleButton("Slicer");
		JToggleButton cropperWidgetButton = new JToggleButton("Cropper");
		widgetsButtons.add(rendererWidgetButton);
		widgetsButtons.add(slicerWidgetButton);
		widgetsButtons.add(cropperWidgetButton);
		//widgetsPanel.add(widgetsLabel);
		widgetsPanel.add(widgetsButtons);
		
		// view check boxes 
		JPanel viewPanel = new JPanel();
		//JLabel viewLabel = new JLabel("View: ");
		JPanel viewButtons = new JPanel();
		viewButtons.setLayout( new BoxLayout(viewButtons,BoxLayout.X_AXIS));
		JCheckBox outlineButton = new JCheckBox("Outline");
		JCheckBox axesButton = new JCheckBox("Axes");
		viewButtons.add(outlineButton );
		viewButtons.add(axesButton);
		//viewPanel.add(viewLabel);
		viewPanel.add(viewButtons);
		
		buttons = new HashMap<String, JComponent>();
		buttons.put("Widget", widgetModeButton);
		buttons.put("Link", linkedModeButton);
		buttons.put("Camera", cameraModeButton);
		buttons.put("VolRenderer", rendererWidgetButton);
		buttons.put("Slicer", slicerWidgetButton);
		buttons.put("Cropper", cropperWidgetButton);
		buttons.put("Outline", outlineButton);
		buttons.put("Axes", axesButton);
		
		
		// channel controls ///////////////////////////////////////// 
		channelControls = new HashMap<Integer, ChannelControlPanel>();

		
		controlsPanel  = new JPanel();
		//controls.setLayout( new BoxLayout(controls,BoxLayout.Y_AXIS));
		controlsPanel.setPreferredSize(new Dimension(controlWidth, controlsPanel.getPreferredSize().height));
		controlsPanel.add( modePanel );
		controlsPanel.add( widgetsPanel );
		controlsPanel.add( viewPanel );
		
		this.setLayout( new BoxLayout(this, BoxLayout.X_AXIS) );
		this.add(centerPanel, BorderLayout.CENTER);
		this.add(controlsPanel, BorderLayout.EAST);	

		
	}
	
	
	///////////////////////////////////////////////////////////
	// free dimensions control methods ////////////////////////
	
	public void addFreeDimension( DataAxis axis){
		int min = 0;
		int max = (int)axis.sampleNumber;
		FreeDimensionControl freeDimControl = new FreeDimensionControl(axis.name, min, max);
		freeDimensionsPanel.add(freeDimControl);
		freeDimControls.put(axis.name, freeDimControl);
	}
	
	public FreeDimensionControl getFreeDimensions(DataAxis axis){
		if( freeDimControls.keySet().contains(axis.name))
			return freeDimControls.get(axis.name);
		return null;
	}
	
	public void removeFreeDimensions(DataAxis axis){
		if( freeDimControls.keySet().contains(axis.name))
			freeDimensionsPanel.remove( freeDimControls.get(axis.name));
	}
	
	public void repaintFreeDimensions(){
		freeDimensionsPanel.repaint();
	}

	
	///////////////////////////////////////////////////////////
	// channel methods ////////////////////////////////////////
	
	public void addChannelControl(int minR, int maxR, int ch, String lutName){	
		ChannelControlPanel channelControl = new ChannelControlPanel( ch, "ch "+ch, minR, maxR); 
		channelControl.setLutName( lutName );
		controlsPanel.add( channelControl );
		channelControls.put( ch, channelControl );		
	}
	
	public ChannelControlPanel getChannelControl(int ch){	
		if (channelControls.keySet().contains(ch))
			return channelControls.get( ch );		
		return null;
	}

	public void removeChannelControl(int ch){	
		if (channelControls.keySet().contains(ch))
			controlsPanel.remove( channelControls.get( ch ) );
	}

	public void repaintChannelControls(){
		controlsPanel.repaint();
	}

	
	// other ui elements //////////////////////////////////
	
	public vtkJoglCanvasComponent getVtkCanvas(){
		return renWin;
	}
	
	public JComponent getButton(String name){
		if( buttons.keySet().contains(name) )
			return buttons.get(name);
		return null;
	}
	
}
