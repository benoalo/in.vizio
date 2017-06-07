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
import vtk.vtkActor;
import vtk.vtkCubeSource;
import vtk.vtkImageData;
import vtk.vtkOutlineFilter;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;

public class Outliner extends DefaultVizTool{

	public Outliner(ViewerData data) {
		super(data);
		vtkImageData vtkData = data.getVtkImageData();
		int[] dims = vtkData.GetDimensions();
        double[] spacing = vtkData.GetSpacing();
        double[] origin = vtkData.GetOrigin();
        double[] center = new double[dims.length];
        for (int d=0; d<3; d++)
        	center[d] = origin[d] + (dims[d]-1) * spacing[d] / 2 ;
        
        // Create a bounding box
        vtkCubeSource cubeSource = new vtkCubeSource();
        cubeSource.SetCenter( center[0], center[1], center[2]);
        
        cubeSource.SetBounds(origin[0], origin[0]+(dims[0]-1)*spacing[0], origin[1], origin[1]+(dims[1]-1)*spacing[1],origin[2], origin[2]+(dims[2]-1)*spacing[2]);
        cubeSource.Update();
        vtkPolyData boundingBox = cubeSource.GetOutput();	 
        
        // Create a bounding box outline
        vtkOutlineFilter outline = new vtkOutlineFilter();
        outline.SetInputData(boundingBox);
        vtkPolyDataMapper outlineMapper = new vtkPolyDataMapper();
        outlineMapper.SetInputConnection(outline.GetOutputPort());
        prop3D = new vtkActor();
        ((vtkActor) prop3D).SetMapper(outlineMapper);
        ((vtkActor) prop3D).GetProperty().SetColor(0,0,0);
	}

}
