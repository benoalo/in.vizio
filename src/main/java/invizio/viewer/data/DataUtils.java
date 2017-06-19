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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.imagej.Dataset;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;



public class DataUtils {
	
	public static DataAxisList getDatasetAxes(Dataset dataset){
		
		int nDim = dataset.numDimensions();
		long[] dims = new long[nDim];
		dataset.dimensions(dims);
		
		DataAxisList axes = new DataAxisList();
		
		for(int d=0; d<nDim; d++){
			
			String name = dataset.axis(d).type().getLabel();
			long sampleNumber = dims[d];
			AxisType axisType = AxisType.OTHER;
			//System.out.println("."+name.toLowerCase()+"!=x.");
			
			if( name.toLowerCase().contentEquals("x") || name.toLowerCase().contentEquals("y") || name.toLowerCase().contentEquals("z") ){
				axisType = AxisType.SPACE;
			}
			else if(name.toLowerCase().contentEquals("channel") ){
				axisType = AxisType.CHANNEL;
			}
			double spacing = dataset.axis(d).calibratedValue(1) - dataset.axis(d).calibratedValue(0); 
			
			DataAxis axis = new DataAxis(name, sampleNumber, spacing, axisType);
			axes.add(axis);
		}
		
		return axes;
	}
	
	
	//  calculate the range
	public static double[] getDataRange(RandomAccessibleInterval<? extends RealType<?>> rai){
		
		// search min and max in chuncks
		int nDim = rai.numDimensions();
		int splitDimension = nDim-1;
		int nThreads = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool( Math.max(1, nThreads/2) );
		
		long[] minChunck = new long[nDim];
		long[] maxChunck = new long[nDim];
		rai.min(minChunck);
		rai.max(maxChunck);
		
		long chunckExtent = ( maxChunck[splitDimension] - minChunck[splitDimension] ) / nThreads; 
		long min0 = rai.min(splitDimension);
		long max0 = rai.max(splitDimension);

		List<Future<double[]>> futures = new ArrayList<Future<double[]>>();
		
		for( int threadId=0; threadId<nThreads; threadId++){
			minChunck[splitDimension] = min0 + threadId*chunckExtent;
			maxChunck[splitDimension] = min0 + (threadId+1)*chunckExtent -1;
			if( threadId+1 == nThreads)
				maxChunck[splitDimension] = max0;
			
			final RandomAccessibleInterval<? extends RealType<?>> raiChunck = Views.interval( rai, new FinalInterval( minChunck, maxChunck ) );
			GetMinMaxWorker worker  = new DataUtils().new GetMinMaxWorker( raiChunck );
			futures.add( executor.submit( worker ) );
		}
		executor.shutdown();
		
		// merge intermediary results
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for(Future<double[]> future : futures){
			try {
				double[] aux = future.get();
				if( aux[0]<min )
					min = aux[0];
				if( aux[1]>max )
					max = aux[1]; 
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} 
		}
		
		
		return new double[] {min,max};
	}
	
	public class GetMinMaxWorker implements Callable<double[]>{

		RandomAccessibleInterval<? extends RealType<?>> rai; 
		
		public GetMinMaxWorker(RandomAccessibleInterval<? extends RealType<?>> rai ){
			this.rai = rai;
		}
		
		public double[] call() throws Exception {
			Cursor<? extends RealType<?>> cursor = Views.iterable(rai).cursor();
			double min = cursor.next().getRealDouble();
			double max = cursor.get().getRealDouble();
			while(cursor.hasNext()){
				double val = cursor.next().getRealDouble(); 
				if( val > max ){
					max = val;
				}
				else if( val < min ){
					min = val;
				}
			}
			return new double[] { min, max};
		}
		
	}
	
	
	
}
