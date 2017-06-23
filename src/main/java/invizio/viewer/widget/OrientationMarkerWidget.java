package invizio.viewer.widget;

import vtk.vtkCanvas;
import vtk.vtkOrientationMarkerWidget;
import vtk.vtkAxesActor;
import vtk.vtkRenderWindowInteractor;

public class OrientationMarkerWidget extends DefaultWidget{

		
	public OrientationMarkerWidget( vtkRenderWindowInteractor renWinInteractor  )
	{
		// Set up the axes widget
		widget = new vtkOrientationMarkerWidget();
		widget.SetInteractor( renWinInteractor );
		
		vtkOrientationMarkerWidget widget2 = (vtkOrientationMarkerWidget) widget;
		widget2.SetOutlineColor( 0.9300, 0.5700, 0.1300 );
		vtkAxesActor axesActor = new vtkAxesActor();
		
		widget2.SetOrientationMarker( axesActor );
		widget2.SetViewport( 0.0, 0.0, 0.2, 0.2 );
		widget2.SetInteractive( 0 );
		
	}
	
	@Override
	public void setVisibility(boolean visible){
		super.setVisibility(visible);
		vtkOrientationMarkerWidget widget2 = (vtkOrientationMarkerWidget) widget;
		widget2.SetInteractive( 0 );
	}	
		
}
