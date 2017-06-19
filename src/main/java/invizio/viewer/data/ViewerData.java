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

package invizio.viewer.data;

import invizio.viewer.data.DataAxis.AxisType;
import invizio.viewer.vizTools.DefaultVizTool;
import invizio.viewer.vizTools.Lut;
import invizio.viewer.vizTools.LutUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imagej.Dataset;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import vtk.vtkColorTransferFunction;
import vtk.vtkImageData;
import vtk.vtkNativeLibrary;
import vtk.vtkPiecewiseFunction;


// mother class for viewerData
public class ViewerData implements AbstractViewerData{

	 /* Load VTK shared libraries (.dll) on startup, print message if not found */
    static 
    {				
        if (!vtkNativeLibrary.LoadAllNativeLibraries()) 
	{
	       for (vtkNativeLibrary lib : vtkNativeLibrary.values()) 
		{
                	if (!lib.IsLoaded()) 
				System.out.println(lib.GetLibraryName() + " not loaded");    
		}
			
		System.out.println("Make sure the search path is correct: ");
		System.out.println(System.getProperty("java.library.path"));
        }
        vtkNativeLibrary.DisableOutputWindow(null);
    }
    
    
    protected Dataset _dataset;
	protected ImgLib2ToVtkConverter<? extends RealType<?>> _imgToVtkConverter;
	protected RandomAccessibleInterval<? extends RealType<?>> _raiImageData;
	
	protected vtkImageData _vtkImageData;
	
	protected int _nDim = 0;
	protected DataAxisList _axes;
	
	protected Map<Long, String> _lutName;		// lut used per channel
	protected Map<Long, double[]> _dataRange;	// min and max per channel
	protected boolean[] channelNeedsUpdate;
	protected final int _defaultGamma = 50;
	protected boolean[] rangeCheck;
	protected Map<Long, vtkPiecewiseFunction> opacityFunc_PerChannel;
	protected Map<Long, vtkColorTransferFunction> colorTransferFunction_PerChannel;
	protected Map<Long, Boolean> channelVisibility;
	
	
	public ViewerData(){}
	
	public ViewerData(Dataset dataset, double[] spacing, String[] axisNames, int[] spaceAxisIndexes, int channelAxisIndex )
	{
		this._dataset = dataset;
		
		this._nDim = dataset.numDimensions();
		long[] dims = new long[_nDim];
		dataset.dimensions(dims);
		
		_axes = new DataAxisList();
		for(int d=0; d<_nDim; d++){
			String name = axisNames[d];
			
			long sampleNumber = dims[d];
			AxisType axisType = AxisType.OTHER;
			
			if( d == channelAxisIndex ){
				axisType = AxisType.CHANNEL;
			}
			
			for( int spaceAxisIndex : spaceAxisIndexes ){
				if( d == spaceAxisIndex){
					axisType = AxisType.SPACE;
					break;
				}
			}
			DataAxis axis = new DataAxis(name, sampleNumber, spacing[d], axisType);
			_axes.add(axis);
		}
		
		init();
		updateVtkImageData();
		updateDataRange();
		
	}
	
		
	public ViewerData(Dataset dataset, double[] spacing, String[] axisNames, int[] spaceAxisIndexes)
	{
		this._dataset = dataset;
		
		this._nDim = dataset.numDimensions();
		long[] dims = new long[_nDim];
		dataset.dimensions(dims);
		
		_axes = new DataAxisList();
		for(int d=0; d<_nDim; d++){
			String name = axisNames[d];
			
			long sampleNumber = dims[d];
			AxisType axisType = AxisType.OTHER;
						
			for( int spaceAxisIndex : spaceAxisIndexes ){
				if( d == spaceAxisIndex){
					axisType = AxisType.SPACE;
					break;
				}
			}
			DataAxis axis = new DataAxis(name, sampleNumber, spacing[d], axisType);
			_axes.add(axis);
		}
		
		init();
		updateVtkImageData();
		updateDataRange();	
	}

	
	public ViewerData(Dataset dataset)
	{
		this._dataset = dataset;
		
		this._nDim = dataset.numDimensions();
		long[] dims = new long[_nDim];
		dataset.dimensions(dims);
		
		_axes = new DataAxisList();
		//this._posInData = new long[_nDim];
		for(int d=0; d<_nDim; d++){
			
			String name = _dataset.axis(d).type().getLabel();
			long sampleNumber = dims[d];
			AxisType axisType = AxisType.OTHER;
			
			if( name.toLowerCase().contentEquals("x") || name.toLowerCase().contentEquals("y") || name.toLowerCase().contentEquals("z") ){
				if(_axes.getNumberOfSpaceAxis()<3){
					axisType = AxisType.SPACE;
				}
			}
			else if( name.toLowerCase().contentEquals("channel") ){
				axisType = AxisType.CHANNEL;
				//System.out.println("test");
			}
			double spacing = _dataset.axis(d).calibratedValue(1) - _dataset.axis(d).calibratedValue(0); 
			
			DataAxis axis = new DataAxis(name, sampleNumber, spacing, axisType);
			_axes.add(axis);
			System.out.println( "name: " + name +"  ;  is channel: " + name.toLowerCase().contentEquals("channel") );
			System.out.println( axis.toString() );
		}
		
		checkParameters( _nDim, _axes.size(), _axes.getSpaceAxes().size() );
		
		init();
		updateVtkImageData();
		updateDataRange();
		initGrayTransferFunctions();
	}
	
	protected void checkParameters( int nSpacing, int nAxisNames, int nSpaceAxis ){
		if( nSpacing != _nDim )
			System.err.print("spacing should have the same dimensions as dataset ("+_nDim+"), found " + nSpacing );
		if( nAxisNames != _nDim )
			System.err.print("axisNames should have the same dimensions as dataset ("+_nDim+"), found " + nAxisNames );	
		if( nSpaceAxis<2 || nSpaceAxis>3 )
			System.err.print("spaceAxisIndexes should be 2 or 3, found " + nSpaceAxis);
		if( _nDim < 2 )
			System.err.print("Dataset dimensions should be superior or equal to 2, found " + _nDim);
	}
	
	
	protected void init(){
	
		// if no channel axis add compensating information		
		if( _axes.getChannelAxis() == null ){
			String name = "_CHANNEL_FICTITIOUS_";
			long sampleNumber = 1;
			double spacing = 1;
			DataAxis axis = new DataAxis(name, sampleNumber, spacing, AxisType.CHANNEL);
			_axes.add(axis);
		}
				
		// determine the minimum and maximum of each channel for the current volume
		_dataRange = new HashMap<Long,double[]>();
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		int nCh = (int)_axes.getChannelAxis().sampleNumber;
		for(long ch=0; ch<nCh; ch++){	
			_dataRange.put( ch, new double[] {(double)min, (double)max} );
		}
		
		// initialize rangeCheck
		List<DataAxis> freeAxes = _axes.getFreeAxes();
		int n=1;
		for( DataAxis axis: freeAxes){
			n *= axis.sampleNumber;
		}
		rangeCheck = new boolean[n*nCh];
		for(int i=0; i<n; i++)
			rangeCheck[i]=false;
		
		// set default lut for each channel
		channelVisibility = new HashMap<Long,Boolean>();
		_lutName = new HashMap<Long,String>();
		channelNeedsUpdate = new boolean[nCh];
		for(int ch=0; ch<nCh; ch++){	
			_lutName.put((long)ch, Lut.getDefaultLUTName( (int)ch) );
			channelNeedsUpdate[ch] = false;
			channelVisibility.put( (long)ch, true);
		}
		System.out.println(_lutName.toString());
		
		opacityFunc_PerChannel = new HashMap<Long, vtkPiecewiseFunction>();
		colorTransferFunction_PerChannel = new HashMap<Long, vtkColorTransferFunction>();
	}
	
	
	protected void updateDataRange(){
		
		long nCh = _axes.getChannelAxis().sampleNumber;
		for(long ch=0; ch<nCh; ch++){
			if( isRangeCheck((int)ch) ){
				//System.out.println("no range update needed :"+ch);
				continue;
			}

			// get min and max per channel at the current position in the dataset
			RandomAccessibleInterval<? extends RealType<?>> rai; 
			if (nCh>1){
				rai = Views.hyperSlice( _raiImageData, (int)_axes.getChannelAxis().index, ch );
			}
			else{
				rai = _raiImageData;
			}
			double[] aux = DataUtils.getDataRange(rai);
			double[] range = _dataRange.get(ch);
			range[0] = Math.min(range[0], aux[0]);
			range[1] = Math.max(range[1], aux[1]);
			_dataRange.put(ch, range);
			setRangeCheck((int)ch);
		}
	}

	
	protected boolean isRangeCheck(int ch){
		int pos = getRangePos(ch);
		return rangeCheck[pos];
	}
	
	protected void setRangeCheck(int ch){
		int pos = getRangePos(ch);
		rangeCheck[pos]=true;
	}
	
	protected int getRangePos( int ch ){
		List<DataAxis> freeAxes = _axes.getFreeAxes();
		int n = (int)_axes.getChannelAxis().sampleNumber;
		int pos = ch;
		for( DataAxis axis: freeAxes){
			pos += n*axis.position; 
			n *= axis.sampleNumber;
		}
		return pos;
	}


	public int[] getDimensions(){
		return _vtkImageData.GetDimensions();
	}
	
	public double[] getSpacing(){
		return _vtkImageData.GetSpacing();
	}	
	
	public List<DataAxis> getFreeAxes()
	{
		return _axes.getFreeAxes();
	}

	public long getChannelNumber()
	{
		return _axes.getChannelAxis().sampleNumber;
	}
	
	public String getLutName(int ch){
		return _lutName.get((long)ch);
	}
	
	public String getAxisName(int d)
	{
		if( d>0 && d<_axes.size() )
			return _axes.get(d).name;
		else
			return null;
	}

	public double[] getDataRange( long channel )
	{
		return _dataRange.get(channel);
	}
	
	public String getName()
	{
		return _dataset.getName();
	}
	
	public void setDisplayParameters( long channel, double min, double max, double gamma){
		updateGrayTransferFunctions(channel, min, max, gamma);
	}

		
	public vtkColorTransferFunction getLut(long channel)
	{
		return colorTransferFunction_PerChannel.get( channel );
	}
	
	
	public void setPosition(String axisName, long pos){
		DataAxis axis = _axes.get(axisName);
		if ( axis == null )
			return;

		int axisIndex = axis.index;
		
		if ( axis.position == pos )
			return;
		
		long maxPos = _axes.get(axisIndex).sampleNumber; 
		if ( pos<0 || pos>=maxPos )
			return;
		
		axis.position = pos;
		updateVtkImageData();
		updateDataRange();
					
		for(DefaultVizTool observer : observers ){
			observer.updateProp();
			//observer.updateLuts(); // lut do not change here, one could simply update the data input in the 
		}
	}

	
	public void setVisibility( long channel, boolean visible){
		
		if( visible ){
			if( channelNeedsUpdate[(int)channel] ){
				channelNeedsUpdate[(int)channel] = false;
				_vtkImageData = _imgToVtkConverter.getVtkImageData(_raiImageData,(int)channel);
			}
		}
		
		for(DefaultVizTool observer : observers ){
			//if( visible != channelVisibility.get(channel)){
			observer.updateChannelVisibility(channel, visible);
			//}
		}
		channelVisibility.put( channel, visible );
		
	}
	
	
	protected void updateVtkImageData()
	{
		updateRAIImageData();
		initConverter();
		
		int nCh = (int) _axes.getChannelAxis().sampleNumber;
        for( int ch=0; ch<nCh; ch++){
        	if ( channelVisibility.get( (long)ch ) ){
        		_vtkImageData = _imgToVtkConverter.getVtkImageData(_raiImageData,ch);
        	}
        	else{
        		channelNeedsUpdate[ch]=true;
        	}
        }
        _vtkImageData.Modified();
	}
	
	
	protected void initConverter(){
		// initialize the dataset to vtk converter
		if ( _imgToVtkConverter == null ){
			
			int[] nSample = new int[3];
			double[] spac = new double[3];
			
			List<DataAxis> spaceAxes = _axes.getSpaceAxes();
			int nSpaceDim = spaceAxes.size();
			for( int d=0; d<nSpaceDim; d++ ){
				DataAxis axis = spaceAxes.get(d);
				nSample[d] = (int)(long)axis.sampleNumber ;
				spac[d] = axis.spacing ;
			}
			if( nSpaceDim < 3 ){
				nSample[2] = 1;
				spac[2] = 1;
			}
				
			int nChannel = (int)_axes.getChannelAxis().sampleNumber;
			int chAxisIndex = _axes.getChannelAxis().index;
			_imgToVtkConverter = new ImgLib2ToVtkConverter(nSample, spac, nChannel, chAxisIndex);		
		}
	}
	
	
	protected void updateRAIImageData(){
	    List<DataAxis> freeAxes = _axes.getFreeAxes();
        _raiImageData = _dataset;
        for( DataAxis axis : freeAxes ){
        	_raiImageData = Views.hyperSlice( _dataset, axis.index , axis.position);
        }
	}



	
	
	protected void initGrayTransferFunctions()
	{
		long nChannel = _axes.getChannelAxis().sampleNumber;
		for (long channel=0 ; channel<nChannel ; channel++)
		{
			double[] range = _dataRange.get(channel);
			double min = range[0];
			double max = range[1];
			double gamma = _defaultGamma;
			updateGrayTransferFunctions( channel, min, max, gamma );
		}
			
	}
	
	
	protected void updateGrayTransferFunctions( long channel, double minD, double maxD, double gammaD)
	{
		vtkPiecewiseFunction opacityFunc = LutUtils.getOpacityFunction( this, channel, minD, maxD, gammaD);
		opacityFunc_PerChannel.put( channel, opacityFunc);
		
		vtkColorTransferFunction colorTransferFunction = LutUtils.getColorTransferFunction( this, channel, minD, maxD, gammaD);
		colorTransferFunction_PerChannel.put( channel, colorTransferFunction);
		
		for(DefaultVizTool observer : observers ){
			observer.updateLut( channel ); 
			// TODO: shall I notify the observer that it was modified ?
		}
	}
	

	
	
	
	ArrayList<DefaultVizTool> observers = new ArrayList<DefaultVizTool>();
	
	
	public void addObserver(DefaultVizTool observer) {
		observers.add(observer);
		observer.updateProp();
		observer.updateLuts();
		for(long ch : this.channelVisibility.keySet() ){
			observer.updateChannelVisibility( ch, channelVisibility.get(ch) );
		}
	}
	
	public void removeObserver(DefaultVizTool observer){
		observers.remove( observer );
	}
	
	public vtkImageData getVtkImageData(){
		return _vtkImageData;
	}
	
	public vtkPiecewiseFunction getOpacityFunction(long channel){
		return opacityFunc_PerChannel.get(channel);
	}

	
	

}
