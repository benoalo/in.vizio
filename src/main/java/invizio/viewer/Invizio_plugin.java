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

import java.io.IOException;

import javax.swing.SwingUtilities;

import net.imagej.Dataset;
import net.imagej.ImageJ;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;



@Plugin(type = Command.class, menuPath = "Plugins>invizio", label="invizio")
public class Invizio_plugin implements Command {

	@Parameter (required=false)
	private	Dataset dataset;
	
	public void run() 
	{
		SwingUtilities.invokeLater(new Runnable() 
		{
			public void run() 
		    {
                //double[] spacing = new double[] {1,1,1,1,1};
            	//String[] axisNames = new String[] {"XX","YY","CC","ZZ"};
            	//int[] spaceAxisIdx = new int[] {0,1,3};
            	Invizio vtkViewer = new Invizio(  dataset );
            	vtkViewer.show();
		    }
		});
	}
	
	public static void main(final String... args){
		
		System.out.println("hello 0");
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		System.out.println("hello 1");
		
		Dataset dataset;
		try {
			dataset = (Dataset) ij.io().open("C:\\Users\\Ben\\workspace\\testImages\\mitosis.tif");
			ij.ui().show(dataset);
			ij.command().run(Invizio_plugin.class, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

		
}



