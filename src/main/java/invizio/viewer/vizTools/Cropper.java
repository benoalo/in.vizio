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

package invizio.viewer.vizTools;

import invizio.viewer.event.MyObservable;
import invizio.viewer.event.MyObserver;
import invizio.viewer.widget.BoxWidget2;
import invizio.viewer.widget.DefaultWidget;

import java.util.ArrayList;

import vtk.vtkActor;
import vtk.vtkBoxRepresentation;
import vtk.vtkLinearTransform;
import vtk.vtkPlanes;
import vtk.vtkProp3D;
import vtk.vtkTransform;
import vtk.vtkVolume;


public class Cropper implements MyObserver{
	
	boolean enabled=false;
	vtkPlanes planes = new vtkPlanes();
	DefaultWidget widget;
	
	
	public void updateClippingPlane() {
		
		//System.out.println("cropper: updateClipping plane 0");
		if( !enabled ){
			return;
		}
					
		for(AbstractVizTool vizTool: vizTools){
			//System.out.println("cropper: updateClipping plane 1");
			vtkProp3D prop3D = vizTool.getProp(); 
			if( prop3D instanceof vtkActor ){
				//System.out.println("cropper: updateClipping plane 2 actor");

				vtkActor actor = (vtkActor) prop3D;	
				actor.GetMapper().RemoveAllClippingPlanes();
				actor.GetMapper().SetClippingPlanes( planes );
			}
			else if( prop3D instanceof vtkVolume){
				//System.out.println("cropper: updateClipping plane 2 volume");

				vtkVolume actor = (vtkVolume) prop3D;	
				actor.GetMapper().RemoveAllClippingPlanes();
				actor.GetMapper().SetClippingPlanes( planes );
			}

		}
	}
	
	public void removeClippingPlane() {
		for(AbstractVizTool vizTool: vizTools){
			vtkProp3D prop3D = vizTool.getProp(); 
			if( prop3D instanceof vtkActor){
				vtkActor actor = (vtkActor) prop3D;	
				actor.GetMapper().RemoveAllClippingPlanes();
			}
			else if( prop3D instanceof vtkVolume){
				vtkVolume actor = (vtkVolume) prop3D;	
				actor.GetMapper().RemoveAllClippingPlanes();
			}
		}
	}
	
	
	public void setPlanes(vtkPlanes planes)
	{
		this.planes = planes;
		updateClippingPlane();
	}
	
	
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
		if( enabled ){
			updateClippingPlane();
		}
		else{
			removeClippingPlane();
		}
	}
	
	public boolean getEnabled(){
		return enabled;
	}
	
	
	// MyObserver methods: cropper can observe a widget
	@Override
	public void fireEvent(MyObservable observable)
	{
		if( observable instanceof BoxWidget2 ){
			System.out.println("cropper: fired by "+observable.toString());
			
			// TODO: make the firing unavailable for 50 ms 
			BoxWidget2 boxWidget = (BoxWidget2) observable;
			planes = boxWidget.getPlanes();
			updateClippingPlane();
		}
	}

	@Override
	public void needsUpdate(boolean needsUpdate) {	}

	
	// methods to observe vizTools (it is needed as cropper is not a vizTool, merely a modifier of vizTool property)
	ArrayList<AbstractVizTool> vizTools = new ArrayList<AbstractVizTool>(); 
	
	
	public void addInput(AbstractVizTool vizTool)
	{
		vizTools.add(vizTool);
	}

	public void removeInput(AbstractVizTool vizTool)
	{
		vizTools.remove(vizTool);
	}

	public void removeAllInput()
	{
		vizTools = new ArrayList<AbstractVizTool>();
	}

	
	// static helper
	public static vtkPlanes getPlanes(double[] bounds, vtkLinearTransform transform, double scale)
	{
		vtkPlanes planes = new vtkPlanes();
		vtkBoxRepresentation boxRep = new vtkBoxRepresentation();
		boxRep.SetPlaceFactor(scale);
        boxRep.PlaceWidget( bounds );
        boxRep.SetTransform( (vtkTransform) transform );
        boxRep.InsideOutOn();
        
        boxRep.GetPlanes(planes);
		return planes;
	}

	
}
