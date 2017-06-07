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
import vtk.vtkSmartVolumeMapper;
import vtk.vtkVolume;
import vtk.vtkVolumeProperty;

public class VolRenderer extends DefaultVizTool{
	
	
	vtkVolumeProperty volumeProperty;
	vtkSmartVolumeMapper volumeMapper;
	
	public VolRenderer(ViewerData data)
	{
		super(data);
		// TODO: check that there are 3 spatial dimensions
		
		volumeProperty = new vtkVolumeProperty();
		volumeMapper = new vtkSmartVolumeMapper();
		
		prop3D = new vtkVolume();
        ((vtkVolume) prop3D).SetMapper(volumeMapper);
        ((vtkVolume) prop3D).SetProperty(volumeProperty);
        
        //volumeMapper.SetInterpolationModeToLinear();
        volumeMapper.SetBlendModeToComposite();
        //volumeMapper.SetBlendModeToMaximumIntensity();
        volumeMapper.SetRequestedRenderModeToGPU();
        //volumeMapper.SetRequestedRenderModeToRayCast();
        
        updateProp();
	}
	
	
	@Override
	public void updateProp()
	{
		super.updateProp();
		volumeMapper.RemoveAllInputs();
		volumeMapper.SetInputData( data.getVtkImageData() );
        //updateLuts(); // add data color and opacity transfer function to volume property           
	}
	
	@Override
	public void updateLut(long channel)
	{
		volumeProperty.SetScalarOpacity((int)channel, data.getOpacityFunction(channel) );
		volumeProperty.SetColor((int) channel, data.getLut(channel) );
	}
	
	
	@Override
	public void updateChannelVisibility(long channel, boolean visible){
		double weight = visible ? 1 : 0 ;
		volumeProperty.SetComponentWeight( (int)channel, weight );
	}
	
}
