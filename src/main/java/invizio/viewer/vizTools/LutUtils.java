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

import java.util.TreeMap;
import java.util.Map.Entry;

import vtk.vtkColorTransferFunction;
import vtk.vtkPiecewiseFunction;

public class LutUtils {
	
	
	
	public static vtkColorTransferFunction getColorTransferFunction( ViewerData data, long channel, double minD, double maxD, double gammaD )
	{
		double[] range = data.getDataRange(channel);
		double minR = range[0];
		double maxR = range[1];		
		double span = maxD-minD;
		
		TreeMap<Float,float[]> lutPoints = Lut.getLutPoints( data.getLutName((int)channel) );
		vtkColorTransferFunction colorTransferFunction = new vtkColorTransferFunction();
		colorTransferFunction.RemoveAllPoints();
		
		float[] color;
		color = lutPoints.firstEntry().getValue();
		colorTransferFunction.AddRGBPoint(minR , color[0], color[1], color[2]);
		if ( span!=0 )
		{
			for(Entry<Float, float[]> entry : lutPoints.entrySet())
			{
				color = entry.getValue();
				float p = entry.getKey();
				colorTransferFunction.AddRGBPoint(minD+ p*span  , color[0], color[1], color[2]);
			}
		}
		color = lutPoints.lastEntry().getValue();
		colorTransferFunction.AddRGBPoint(maxR , color[0], color[1], color[2]);
		colorTransferFunction.Modified();
		
		return colorTransferFunction;
	}
	
	
	
	
	public static vtkPiecewiseFunction getOpacityFunction( ViewerData data, long channel, double minD, double maxD, double gammaD )
	{
		double[] range = data.getDataRange(channel);
		double minR = range[0];
		double maxR = range[1];		
		double span = maxD-minD;
	
		// update the opacity transfer function
		vtkPiecewiseFunction opacityFunc = new vtkPiecewiseFunction();
		opacityFunc.RemoveAllPoints();
		
		if( gammaD>=100 )
				gammaD = 99.9d;
		if( gammaD<=0 )
				gammaD=0.01;
		double gamma = gammaD / (100 - gammaD); 
		// x/100-x worth 0 in x=0, 1 in x=50, +inf in x=100
		// resulting opacity curve
		// is always 0 at 0 and 1 at 1
		// small x favor opacity to stay close to 0
		// large x favor opacity to quickly become close to 1
		opacityFunc.AddPoint(minR, 0.0);
		if ( span!=0 )
		{
			for( int i=0 ; i<=100; i++ )
			{
				double p = (double)i/100;
				opacityFunc.AddPoint(minD+p*span    , Math.pow(p,gamma) );
			}
			//opacityFunc.AddPoint(max+0.01*(maxR-max), 0.0);
			opacityFunc.AddPoint(maxR, 1.0);
		}
		else{
			opacityFunc.AddPoint(maxR, 0.0);
		}
		opacityFunc.Modified();

		return opacityFunc; 
	}

	
	
}
