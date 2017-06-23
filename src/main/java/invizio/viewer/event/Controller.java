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

package invizio.viewer.event;

import invizio.viewer.data.DataAxis;
import invizio.viewer.data.ViewerData;
import invizio.viewer.ui.CameraUtils;
import invizio.viewer.ui.ChannelControlPanel;
import invizio.viewer.ui.FreeDimensionControl;
import invizio.viewer.ui.ViewerPanel;
import invizio.viewer.vizTools.Cropper;
import invizio.viewer.vizTools.DefaultVizTool;
import invizio.viewer.vizTools.Outliner;
import invizio.viewer.vizTools.Slicer2;
import invizio.viewer.vizTools.VolRenderer;
import invizio.viewer.widget.BoxWidget2;
import invizio.viewer.widget.DefaultWidget;
import invizio.viewer.widget.OrientationMarkerWidget;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBox;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import vtk.vtkBorderWidget;
import vtk.vtkCamera;
import vtk.vtkCanvas;
import vtk.rendering.jogl.*;


public class Controller implements AbstractController {

	ViewerPanel viewerPanel;
	vtkJoglCanvasComponent renWin;
	ViewerData data;
	ArrayList<DefaultVizTool> vizTools = new ArrayList<DefaultVizTool>();
	
	final int delayPriorToNewRender = 100; // in millisecond
	boolean readyToRender = true;
	
	public Controller( ViewerPanel viewerPanel, ViewerData data){
		this.viewerPanel = viewerPanel;
		this.renWin = viewerPanel.getVtkCanvas();
		this.data = data;
	}

	
	public void initialize() {

		renWin.getRenderWindowInteractor().SetDesiredUpdateRate(100);
		renWin.getRenderer().AddObserver("EndEvent", this, "frameRateCallback");
		
		// Setup vizTools
		VolRenderer volRenderer = new VolRenderer( data );
		vizTools.add(volRenderer);
		Outliner outliner = new Outliner( data );
		vizTools.add(outliner);
		Slicer2 slicer = new Slicer2( data );
		vizTools.add(slicer);
		
		
		// create a box widget that will be associated to cropper and slicer
		BoxWidget2 boxWidget = new BoxWidget2(data, renWin.getRenderWindowInteractor());
	
		// create an axes widget that will indicate the  data orientation
		OrientationMarkerWidget axesWidget = new OrientationMarkerWidget(renWin.getRenderWindowInteractor() );
			
		
		// setup cropper
		Cropper cropper = new Cropper();
		cropper.addInput( volRenderer );
		cropper.setPlanes( Cropper.getPlanes( volRenderer.getProp().GetBounds(), volRenderer.getProp().GetUserTransform() , 0.5) );
		//boxWidget.addObserver( cropper );
		
		// put there all the logic common to all version of the viewer 
		// initialize the camera position
        vtkCamera camera = CameraUtils.createCameraCenteredOnDataset(data);
        renWin.getRenderer().SetActiveCamera(camera);
        renWin.getRenderer().ResetCameraClippingRange();
//		vtkBorderWidget borderWidget = new vtkBorderWidget(  );
//		borderWidget.SetDefaultRenderer( renWin.getRenderer() );
//		borderWidget.SetInteractor( renWin.getRenderWindowInteractor() );
//		borderWidget.EnabledOn();
//        
        
        // Setup the background gradient
        renWin.getRenderer().GradientBackgroundOn();
        renWin.getRenderer().SetBackground(.7,.7,.85);
        renWin.getRenderer().SetBackground2(.99,.99,1.0);
        
        
        // add channel control for each dataset channel
        int nCh = (int) data.getChannelNumber();
        ChannelControlCallBack chCallBack = new ChannelControlCallBack();
        for(int ch=0; ch<nCh; ch++)
    	{
            double[] range = data.getDataRange((long)ch);
            int minR = (int)range[0];
            int maxR = (int)range[1];
            viewerPanel.addChannelControl(minR, maxR, ch, data.getLutName(ch));
    		viewerPanel.getChannelControl(ch).addObserver( chCallBack );
    	}
        
        
        // add free dimensions control to the main panel
        FreeDimControlCallBack freeDimCallBack = new FreeDimControlCallBack();
        List<DataAxis> freeDimAxes = data.getFreeAxes();
        for(DataAxis axis : freeDimAxes){
        	if( axis.sampleNumber > 1 ){
	        	viewerPanel.addFreeDimension(axis);
	        	viewerPanel.getFreeDimensions(axis).addObserver(freeDimCallBack);
        	}
        }

        
        
        // renderer button
        JToggleButton volRenButton = (JToggleButton) viewerPanel.getButton("VolRenderer");
        volRenButton.setSelected( true );
        renWin.getRenderer().AddActor( volRenderer.getProp() );
		data.addObserver(volRenderer); // to be consistant with volren button being selected
		boolean observeData = true;
        volRenButton.addActionListener( new VizToolButtonCallback(volRenderer, data, observeData) );
        
        // slicer button
        JToggleButton slicerButton = (JToggleButton) viewerPanel.getButton("Slicer");
        slicerButton.setSelected( false );
        observeData = true;
        slicerButton.addActionListener( new SlicerButtonCallback(slicer, data, boxWidget) );

        // outline checkbox
        JCheckBox outlineCB = (JCheckBox) viewerPanel.getButton("Outline");
        outlineCB.setSelected(false);
        observeData= false;
        outlineCB.addActionListener( new VizToolButtonCallback(outliner, data, observeData));
        
        // box widget button
        JToggleButton widgetButton = (JToggleButton) viewerPanel.getButton("Widget");
        widgetButton.setSelected( false );
        boxWidget.setVisibility(false);
        widgetButton.addActionListener( new WidgetButtonCallback( boxWidget ) );
        
        // cropper button
        JToggleButton cropperButton = (JToggleButton) viewerPanel.getButton("Cropper");
        cropperButton.setSelected( false );
        cropper.setEnabled(false);
        cropperButton.addActionListener( new CropperButtonCallback( cropper, boxWidget ) );
        
        // axes widget button
        JCheckBox axesCB = (JCheckBox) viewerPanel.getButton("Axes");
        axesCB.setSelected(false);
        axesCB.addActionListener( new WidgetButtonCallback( axesWidget ) );
        

	}
	
	
	///////////////////////////////////////////////////////////////////////////
	//  button callbacks  /////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	
	
	class VizToolButtonCallback implements ActionListener
	{
		DefaultVizTool vizTool ;
		ViewerData data;
		boolean dataObserver;
		VizToolButtonCallback(DefaultVizTool vizTool, ViewerData data, boolean dataObserver)
		{
			this.vizTool = vizTool;
			this.data = data;
			this.dataObserver = dataObserver;
		}
		
		public void actionPerformed(ActionEvent e)
		{
    		JToggleButton button = (JToggleButton) e.getSource();
			if (button.isSelected()){
				renWin.getRenderer().AddActor( vizTool.getProp() );
				if(dataObserver)
					data.addObserver(vizTool);
			} 
			else{
				renWin.getRenderer().RemoveActor( vizTool.getProp() );
				if(dataObserver)
					data.removeObserver(vizTool);
			}
			updateRenderWindow();
		}
		
    }
	
	
	class SlicerButtonCallback implements ActionListener
	{
		Slicer2 slicer ;
		ViewerData data;
		BoxWidget2 widget;
		
		SlicerButtonCallback(Slicer2 slicer, ViewerData data, BoxWidget2 widget)
		{
			this.slicer = slicer;
			this.data = data;
			this.widget = widget;
		}
		
		public void actionPerformed(ActionEvent e)
		{
    		JToggleButton button = (JToggleButton) e.getSource();
			if (button.isSelected()){
				renWin.getRenderer().AddActor( slicer.getProp() );
				data.addObserver(slicer);
				widget.addObserver( slicer );
				slicer.setPosition( widget.getCenter());
				slicer.setOrientation( widget.getTransform() );
			} 
			else{
				renWin.getRenderer().RemoveActor( slicer.getProp() );
				data.removeObserver(slicer);
				widget.removeObserver( slicer );
			}
			updateRenderWindow();
		}
    }
	
	class WidgetButtonCallback implements ActionListener
	{
		DefaultWidget widget;
		
		WidgetButtonCallback( DefaultWidget boxWidget)
		{
			this.widget = boxWidget;
		}
		
		public void actionPerformed(ActionEvent e) {
			JToggleButton button = (JToggleButton) e.getSource();
			boolean widgetVisibility = button.isSelected(); 
			if ( widgetVisibility ){	
				widget.setVisibility( true );
			} 
			else{	
				widget.setVisibility( false );
			}
			updateRenderWindow();
		}
    }
	
	
	class CropperButtonCallback implements ActionListener
	{
		Cropper cropper;
		BoxWidget2 widget;
		CropperButtonCallback(Cropper cropper, BoxWidget2 boxWidget)
		{
			this.cropper = cropper;
			this.widget = boxWidget;
		}
		
		public void actionPerformed(ActionEvent e) {
			JToggleButton button = (JToggleButton) e.getSource();
			boolean enabled = button.isSelected();
			cropper.setEnabled( enabled );
			
			if( enabled ){
				widget.addObserver( cropper );
				cropper.setPlanes( widget.getPlanes() );
			}
			else{
				widget.removeObserver( cropper );
			}
			
			updateRenderWindow();
		}
    }
	
	
	
	///////////////////////////////////////////////////////////////////////////
	//  channel control callbacks  ////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	class ChannelControlCallBack extends DefaultMyObserver
	{
		Map<Integer, Map<String,Object>> previous; 
		int channel;
		double min;
		double max;
		double gamma;
		boolean visible;
		boolean visibleChanged;
		
		
		ChannelControlCallBack(){
			super(delayPriorToNewRender/2);
			previous = new HashMap<Integer, Map<String,Object>>();
			int nCh = (int)data.getChannelNumber();
			for( int ch=0; ch<nCh; ch++){
				HashMap<String,Object> prevCh = new HashMap<String,Object>();
				prevCh.put("min", -1d);
				prevCh.put("max", -1d);
				prevCh.put("gamma", -1d);
				prevCh.put("visible", true);
				previous.put(ch, prevCh );
			}
		}
		
		@Override
		public void fireEvent(MyObservable observable) {
			
			if( !readyToRender ){
				//forceAnAdditionnalFiring( observable );
				return;
			}
			
			if( observable instanceof ChannelControlPanel )
			{
				// make renderer unavailable for an additional rendering for the next xx millisecond
				waitPriorToNewRender( delayPriorToNewRender );
				
				ChannelControlPanel channelControl = (ChannelControlPanel) observable; 
				channel = channelControl.getID();
				min = channelControl.getMinValue();
				max = channelControl.getMaxValue();
				gamma = channelControl.getGammaValue();
				visible = channelControl.getVisibility();
				
				//System.out.println("minOld:"+minOld+" , maxOld:"+maxOld+" , gamOld:"+gammaOld+" , visOld:"+visible);
				if( stateChanged() ){
				    data.setDisplayParameters(channel, min, max, gamma);
			        data.setVisibility(channel, visible);
			        //System.out.println("visibility changed: "+visibleChanged);
			        //System.out.println("update: "+(visible || visibleChanged));
			        if( visible || visibleChanged ){
			        	updateRenderWindow();
			        	System.out.println("ch:"+channel+" , min:"+min+" , max:"+max+" , gam:"+gamma+" , vis:"+visible);
			        }
			    }
			}
			//readyToRender=true;
			//_needsUpdate = false;
		}
		
		private boolean stateChanged(){
			boolean stateChanged= false;
			
			
			if( min!=(Double)previous.get(channel).get("min") ){
				previous.get(channel).put("min",min);
				stateChanged=true;
			}
			if( max!=(Double)previous.get(channel).get("max") ){
				previous.get(channel).put("max",max);
				stateChanged=true;
			}
			if( gamma!=(Double)previous.get(channel).get("gamma") ){
				previous.get(channel).put("gamma",gamma);
				stateChanged=true;
			}
			visibleChanged=false;
			if( visible!=(Boolean)previous.get(channel).get("visible") ){
				previous.get(channel).put("visible",visible);
				visibleChanged=true;
				stateChanged=true;
			}

			return stateChanged;
		}
		

	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	//  free dim control callback  ///////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	class FreeDimControlCallBack extends DefaultMyObserver
	{
		FreeDimControlCallBack(){
			super(delayPriorToNewRender/2);
		}
		
		@Override
		public void fireEvent(MyObservable observable) {
			
			if( !readyToRender ){
				//forceAnAdditionnalFiring( observable );
				return;
			}
			
			if( observable instanceof FreeDimensionControl ){
				
				// make renderer unavailable for an additional rendering for the next xx millisecond
				waitPriorToNewRender( delayPriorToNewRender );
				
				FreeDimensionControl freeDimControl = (FreeDimensionControl) observable;
				String axisName = freeDimControl.getName();
				int position = freeDimControl.getValue();
				data.setPosition(axisName, position); // updates vtkImageData
				
				updateRenderWindow();
			}
		}
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	//  helper tools  /////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	protected void waitPriorToNewRender( int delay ){
		
		readyToRender = false;
		
		// start a thread that will turn the flag on after delay ms
		ScheduledExecutorService executorTimer = Executors.newScheduledThreadPool(1);
		Runnable task = new Runnable() {
			
			public void run() { readyToRender = true; }
		};
		executorTimer.schedule(task, delay, TimeUnit.MILLISECONDS);
		executorTimer.shutdown();		
		
	}
	
	
	
	protected void updateRenderWindow(){
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Runnable task = new Runnable() {
		   public void run() {
			   			   
			   try {
				   SwingUtilities.invokeAndWait(new Runnable() {
						   public void run() {
							   System.out.println("render!");
							   renWin.Render();
						   }
						});
					//renWin.repaint();
					
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    }
		};
		executor.submit(task);
		executor.shutdown();
	}
	

	protected void frameRateCallback(){
		double renderTime = renWin.getRenderer().GetLastRenderTimeInSeconds();
		System.out.println("" + (1/renderTime) + " fps" );
	}
	
}
