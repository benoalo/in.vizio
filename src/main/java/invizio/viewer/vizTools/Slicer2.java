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

import invizio.viewer.data.ViewerData;
import invizio.viewer.event.MyObservable;
import invizio.viewer.event.MyObserver;
import invizio.viewer.widget.BoxWidget2;

import java.util.Arrays;
import java.util.HashMap;

import vtk.vtkAlgorithmOutput;
import vtk.vtkColorTransferFunction;
import vtk.vtkImageActor;
import vtk.vtkImageMapToColors;
import vtk.vtkImageMathematics;
import vtk.vtkImageReslice;
import vtk.vtkMatrix4x4;
import vtk.vtkPlanes;
import vtk.vtkTransform;

public class Slicer2 extends DefaultVizTool implements MyObserver{

	vtkImageReslice planeCutter;
	HashMap<Long, vtkImageMapToColors> coloredChannels;
	vtkColorTransferFunction dummyLut;
	double[] initialPosition = new double[3];
			
	public Slicer2(ViewerData data) {
		super(data);
		
		planeCutter = new vtkImageReslice();
		planeCutter.SetInputData( data.getVtkImageData() );
		double[] center = data.getVtkImageData().GetCenter();
		planeCutter.SetResliceAxesOrigin(center);
		planeCutter.SetOutputDimensionality( 2 );
		planeCutter.SetInterpolationModeToLinear();
		
		double[] ux = new double[] {1,0,0};
		double[] uy = new double[] {0,1,0};
		double[] uz = new double[] {0,0,1};
		planeCutter.SetResliceAxesDirectionCosines(ux, uy, uz);
		
		
        coloredChannels = new HashMap<Long, vtkImageMapToColors>();
        long nCh = data.getChannelNumber();
        for(long ch=0; ch<nCh; ch++){
        	vtkImageMapToColors imgMapToColor = new vtkImageMapToColors();
            imgMapToColor.SetInputConnection(planeCutter.GetOutputPort());
	        imgMapToColor.SetActiveComponent( (int)ch );
	        coloredChannels.put( ch , imgMapToColor);
        }
        long ch0=0;
        vtkAlgorithmOutput channelMix = coloredChannels.get(ch0).GetOutputPort();
        if( nCh > 1 ){
        	for(long ch=1; ch<nCh; ch++){
        		vtkImageMathematics maxImage = new vtkImageMathematics();
        		maxImage.SetOperationToMax();
        		maxImage.SetInputConnection(0, channelMix );
        		maxImage.SetInputConnection(1, coloredChannels.get(ch).GetOutputPort() );
        		channelMix = maxImage.GetOutputPort();
	        }
        }

        prop3D = new vtkImageActor();
        ((vtkImageActor)prop3D).GetMapper().SetInputConnection( channelMix );
        
        
        // crop the output
        planeCutter.AutoCropOutputOn();
        vtkPlanes planes = Cropper.getPlanes( data.getVtkImageData().GetBounds(), prop3D.GetUserTransform() , 1.0 );
        ((vtkImageActor)prop3D).GetMapper().SetClippingPlanes( planes );
        
        dummyLut = new vtkColorTransferFunction();
		dummyLut.RemoveAllPoints();
		dummyLut.AddRGBPoint( Double.MIN_VALUE, 0.0, 0.0, 0.0);
		dummyLut.AddRGBPoint( Double.MAX_VALUE, 0.0, 0.0, 0.0);
	}
	
	//TODO: override updateProp(), updateLut(ch), updateChannelVisibility
	@Override
	public void updateProp()
	{
		super.updateProp();
		planeCutter.SetInputData( data.getVtkImageData());
		//updateLuts(); // add data color and opacity transfer function to volume property           
	}
	
	@Override
	public void updateLut(long channel)
	{
		vtkColorTransferFunction colors = data.getLut(channel);
		colors.Modified();
		coloredChannels.get(channel).SetLookupTable( colors  );
	}
	
	
	@Override
	public void updateChannelVisibility(long channel, boolean visible)
	{
		if( !visible ){
			dummyLut.Modified();
			coloredChannels.get(channel).SetLookupTable( dummyLut  );
		}
		else{
			updateLut(channel);
		}
	}
	
	public void setPosition(double[] pos){
		// vector to current position
		double[] reference = data.getVtkImageData().GetCenter();
		
		double[] deltaPos = new double[3];
		for(int d=0; d<3; d++){
			deltaPos[d] = pos[d] - reference[d];
		}
		// get current plane normal 
		double[] normal = new double[3];
		double[] ux = new double[3];
		double[] uy = new double[3];
		planeCutter.GetResliceAxesDirectionCosines(ux, uy, normal);

		// newPos = reference + normal * (normal.deltaPos)
		double[] newPos = new double[3];
		double normal_dot_deltaPos = normal[0]*deltaPos[0] + normal[1]*deltaPos[1] + normal[2]*deltaPos[2] ;
		double[] newPos2 = new double[] { 0, 0, normal_dot_deltaPos}; 
		for(int d=0; d<3; d++){
			newPos[d] = reference[d] + normal[d] * normal_dot_deltaPos;
		}
		planeCutter.SetResliceAxesOrigin( newPos );
		((vtkImageActor)prop3D).SetPosition(newPos2);
		
		double[] dPosActor = ((vtkImageActor)prop3D).GetPosition();
		double normal_dot_deltaPosActor = normal[0]*dPosActor[0] + normal[1]*dPosActor[1] + normal[2]*dPosActor[2] ;
		
		System.out.println( "reference: " + Arrays.toString(reference));
		System.out.println( "pos: " + Arrays.toString(pos));
		System.out.println( "delta: " + Arrays.toString(deltaPos));
		System.out.println( "normal: " + Arrays.toString(normal));
		System.out.println( "newPos: " + Arrays.toString(newPos));
		
		System.out.println( "pos box: " + normal_dot_deltaPos );
		System.out.println( "pos Actor: " + normal_dot_deltaPosActor);
			
		System.out.println( "------" );
	}
	
	public void setOrientation( vtkTransform transform)
	{
		vtkMatrix4x4 m = transform.GetMatrix();
		double[] normal = new double[] { m.GetElement(0, 2), m.GetElement(1, 2), m.GetElement(2, 2) };
		normal = vectorNormalisation(normal);
		
		double[] uy = new double[] {0,1,0};
		double[] vx = vectorProduct( uy , normal );
		vx = vectorNormalisation( vx);
		
		double[] vy = vectorProduct( normal, vx );
		vy = vectorNormalisation( vy);
		
		System.out.println( "vx: " + Arrays.toString(vx));
		System.out.println( "vy: " + Arrays.toString(vy));
		System.out.println( "normal: " + Arrays.toString(normal));
		
		planeCutter.SetResliceAxesDirectionCosines( vx,vy,normal );
		
		vtkMatrix4x4 resliceAxes = planeCutter.GetResliceAxes();
		vtkMatrix4x4 matrix = new vtkMatrix4x4(); 
		matrix.DeepCopy(resliceAxes);
		//double[] pos = new double[] { m.GetElement(0, 3), m.GetElement(1, 3), m.GetElement(2, 3) };
		for(int d=0; d<3; d++){
			matrix.SetElement(d, 3, 0);
		}
		((vtkImageActor)prop3D).SetUserMatrix(matrix);
		
	}
	
	public static double[] vectorProduct( double[] u, double[] v){
		double[] w = new double[3];
		w[0] = u[1]*v[2]-u[2]*v[1];
		w[1] = u[2]*v[0]-u[0]*v[2];
		w[2] = u[0]*v[1]-u[1]*v[0];
		return w;
	}
	
	public static double[] vectorNormalisation(double[] u){
		double L=0;
		double[] v= new double[3];
		
		for(int d=0; d<3; d++)
			L += u[d]*u[d];
		L = Math.sqrt(L);
		
		for(int d=0; d<3; d++)
			v[d] = u[d]/L;
			
		return v;
	}
	
	@Override
	public void fireEvent(MyObservable observable) {
		if( observable instanceof BoxWidget2 ){
			//System.out.println("cropper: fired by "+observable.toString());
			
			// TODO: make the firing unavailable for 50 ms 
			BoxWidget2 boxWidget = (BoxWidget2) observable;
			
			//System.out.println("\n______________________________");
			this.setPosition( boxWidget.getCenter() );
			this.setOrientation( boxWidget.getTransform());
			//System.out.println("===============================");
			
		}
	}

	@Override
	public void needsUpdate(boolean needsUpdate) {	}

}
