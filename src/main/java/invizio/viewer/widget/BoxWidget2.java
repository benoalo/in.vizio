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

package invizio.viewer.widget;


import invizio.viewer.data.ViewerData;
import invizio.viewer.event.MyObserver;
import vtk.vtkBoxRepresentation;
import vtk.vtkBoxWidget2;
import vtk.vtkCanvas;
import vtk.vtkPlanes;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkRenderWindowInteractor;
import vtk.vtkTransform;


/*
 * Remark: when acting on the box widget representation, the box widget internally trigger a 
 * render making it unecessary for the controller to do so.
 */

public class BoxWidget2 extends DefaultWidget{

	vtkBoxWidget2 boxWidget;
	vtkBoxRepresentation boxRep;
	
	
	public BoxWidget2( ViewerData data, vtkCanvas renWin )
	{
		vtkRenderWindowInteractor renWinInteractor = renWin.getRenderWindowInteractor();
		
		boxWidget = new vtkBoxWidget2();
		boxWidget.SetInteractor(renWinInteractor); // to move the box
		boxWidget.AddObserver("InteractionEvent", this, "callBack"); // to trigger event when inteaction with the box happen
		
		boxRep = (vtkBoxRepresentation) boxWidget.GetRepresentation();
		boxRep.SetPlaceFactor(0.5);
		boxRep.PlaceWidget(data.getVtkImageData().GetBounds());
		boxRep.InsideOutOn();
		
		vtkTransform transform = new vtkTransform();
		transform.Identity();
		boxRep.SetTransform( transform );
		//callBack();
	}
	
	
	protected void callBack()
	{
		for( MyObserver observer: observers){ 
			if( observer!=null ){	
				observer.fireEvent( this );
			}
		}
	}

	
	public void setVisibility(boolean visible){
		if( visible ){
			boxWidget.On();
			callBack();
		}
		else{
			boxWidget.Off();
		}
	}
	
	
	public vtkPlanes getPlanes(){
		vtkPlanes planes = new vtkPlanes();
		boxRep.GetPlanes( planes );
		return planes;
	}
	
	public double[] getCenter()
	{
		double[] boxCenter = new double[3];
		vtkPolyData polyData = new vtkPolyData();
		boxRep.GetPolyData(polyData);
		vtkPoints points = polyData.GetPoints();
		int nPoints = points.GetNumberOfPoints();
		for(int i=0; i<3; i++){
			for(int j=0; j<nPoints; j++){
				boxCenter[i] += points.GetPoint(j)[i];
			}
			boxCenter[i] /= nPoints;
		}
		
		return boxCenter;
	}

	
	public vtkTransform getTransform()
	{
		vtkTransform transform = new vtkTransform();
		boxRep.GetTransform(transform);
		return transform;
	}
	
}
