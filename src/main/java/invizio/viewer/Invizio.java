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

package invizio.viewer;

import invizio.viewer.data.ViewerData;
import invizio.viewer.event.Controller;
import invizio.viewer.ui.ViewerPanel;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import net.imagej.Dataset;
import vtk.vtkNativeLibrary;


public class Invizio{

	ViewerPanel viewerPanel;
	ViewerData data;
	
	/* Load VTK shared libraries (.dll) on startup, print message if not found */
    static 
    {				
    	if (!vtkNativeLibrary.LoadAllNativeLibraries()) 
    	{
    		for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
	    	   if (!lib.IsLoaded()){ 
	    		   System.out.println(lib.GetLibraryName() + " not loaded");
	    	   }
    		}
			System.out.println("Make sure the search path is correct: ");
			System.out.println(System.getProperty("java.library.path"));
        }
        vtkNativeLibrary.DisableOutputWindow(null);
    }
	
	
    public Invizio(Dataset dataset )
    {    	
        data = new ViewerData(dataset);
		initialize();
    }

    public Invizio(Dataset dataset, double[] spacing, String[] axisNames, int[] spaceAxisIdx, int chAxisIdx )
    {
		data = new ViewerData(dataset, spacing, axisNames, spaceAxisIdx, chAxisIdx);
		initialize();    	
    }   

    protected void initialize()
    {
    	viewerPanel = new ViewerPanel();
		Controller controller = new Controller(viewerPanel, data);
		controller.initialize();
    }

    public void show(){
    	JFrame frame = new JFrame("in.vizio : " + data.getName() );
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add( viewerPanel, BorderLayout.CENTER);
        frame.setSize(800, 600);
        //frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
	
}
