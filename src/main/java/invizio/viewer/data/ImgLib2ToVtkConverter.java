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


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import vtk.vtkImageData;
import vtk.vtkTypeInt16Array;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;


// convert graylevel multichannel image to vtkImageData for visualisation

// limitations:
//	* image size must be smaller than the maximum size of an array (all channel together)
// 	* image are converted to unsigned  short independant of the entry type
//	* only graylevel images are handled

// ideas:
//	* explore possibility with data streaming in vtk
//	* current system could be improved, in case the number of sample in the last direction is small one could involve
//	  another dimension in the chuncking
//	* handle different data type ( 8 bit, 16bit, 32 bit integer and float and color image conversion)

public class ImgLib2ToVtkConverter<T extends RealType<T>> {
	
	int nChannel;
	int channelDimension;
	double[] spacing;
	int[] dimensions;
	int nDim;
	
	vtkImageData _vtkImageData = null;
	short[] _javaArray = null;
	vtkTypeInt16Array _vtkArray = null;
	
	
	public ImgLib2ToVtkConverter(int[] dimensions, double[] spacing, int nChannel, int channelDimension){
		this.dimensions = dimensions;
		this.spacing = spacing;
		this.nChannel = nChannel;
		this.channelDimension = channelDimension;
		for(int val : dimensions){
			nDim += val > 1 ? 1 : 0 ;
		}
		//this.nDim = dimensions.length;
		initVtkImageData();
		
	}
	
	
	private void initVtkImageData(){
		
		if ( _vtkImageData != null ){
			return;
		}
		
		// initialize the vtk image data
		_vtkImageData = new vtkImageData();
		double[] defaultOrigin = new double[3];
		for( int d=0; d<nDim; d++ ){
			defaultOrigin[d] = -(dimensions[d]-1)*spacing[d]/2;
		}
		
		_vtkImageData.SetDimensions( dimensions );
        _vtkImageData.SetSpacing( spacing );
        _vtkImageData.SetOrigin( defaultOrigin );        
        _vtkImageData.AllocateScalars(4, nChannel); // 4 mean unsigned short , see vtk
        
        
        // initialize the vtk array
        _vtkArray = new vtkTypeInt16Array();
		_vtkArray.SetNumberOfComponents(nChannel);
		int[] dims = _vtkImageData.GetDimensions();
		long nTuples = dims[0] *dims[1] * dims[2]; 
		_vtkArray.SetNumberOfTuples((int) nTuples);
		_vtkArray.Allocate((int)(nChannel*nTuples), 1000);
		
		
		// initialize the java container
		_javaArray = new short[ (int)(nChannel*nTuples) ];
		
	}
	
	
	
	public vtkImageData getVtkImageData(RandomAccessibleInterval<T> raiImageData){
		for( int ch=0; ch<nChannel; ch++){
			updateJavaArray(raiImageData, ch);
		}
		_vtkArray.SetJavaArray(_javaArray);
		_vtkImageData.GetPointData().SetScalars(_vtkArray);
		return _vtkImageData;
	}

	
	
	public vtkImageData getVtkImageData(RandomAccessibleInterval<? extends RealType<?>> _raiImageData, int channel){
		updateJavaArray(_raiImageData, channel);
		_vtkArray.SetJavaArray(_javaArray);
		_vtkImageData.GetPointData().SetScalars(_vtkArray);
		return _vtkImageData;
	}

	
	private void updateJavaArray(RandomAccessibleInterval<? extends RealType<?>> raiImageData, int channel){
		
		if( nChannel==1 ){
			copyRAIToJavaArray( raiImageData, 0 );
		}
		else{
			RandomAccessibleInterval<? extends RealType<?>> raiChannelData = Views.hyperSlice( raiImageData, channelDimension, channel);
			int offset = channel;
			copyRAIToJavaArray( raiChannelData, offset );
		}
		
	}
	
	private void copyRAIToJavaArray( RandomAccessibleInterval<? extends RealType<?>> rai, final int offset0 ){
		
		final int splitDimension = nDim-1;
		final int nThreads = Runtime.getRuntime().availableProcessors();
		//System.out.println("nThreads:"+nThreads+" vtkImageData");
		ExecutorService executor = Executors.newFixedThreadPool(nThreads);
		
		long[] minChunck = new long[nDim];
		long[] maxChunck = new long[nDim];
		rai.min(minChunck);
		rai.max(maxChunck);
		
		long hyperSliceElements = nChannel; 
		int d = 0;
		while( d < splitDimension ){ 
			hyperSliceElements *= rai.dimension(d);
			d++;
		}
			
		long chunckExtent = ( maxChunck[splitDimension] - minChunck[splitDimension] ) / nThreads; 
		long min0 = rai.min(splitDimension);
		long max0 = rai.max(splitDimension);
		for( int threadId=0; threadId<nThreads; threadId++){
			
			final int offset = offset0 +(int)(threadId * chunckExtent * hyperSliceElements);
			minChunck[splitDimension] = min0 + threadId*chunckExtent;
			maxChunck[splitDimension] = min0 + (threadId+1)*chunckExtent -1;
			if( threadId+1 == nThreads)
				maxChunck[splitDimension] = max0;
			
			final RandomAccessibleInterval< ? extends RealType<?> > raiChunck = Views.interval( rai, new FinalInterval( minChunck, maxChunck ) );
			
			executor.submit( 
				new Runnable() { 
					public void run(){
						Cursor<? extends RealType<?>> cursor=  Views.flatIterable(raiChunck).cursor();
						int count = offset;
						while( cursor.hasNext() ){
				            _javaArray[count] = (short)cursor.next().getRealFloat() ;
				            count += nChannel ;
				        }
						//System.out.println("thread"+ Thread.currentThread().getName() + "  ;  slice:"+raiChunck.min(splitDimension)+","+raiChunck.max(splitDimension));
					}
				});
		}
		executor.shutdown();
		try {
			executor.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//System.out.println("are my threads finished?");
	}
}
