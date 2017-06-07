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

public class DataAxisList {
	ArrayList<DataAxis> axes;
	
	int channelAxisIndex = -1;
	ArrayList<Integer> spaceAxisIndexes = null;
	
	public DataAxisList(){
		
		axes = new ArrayList<DataAxis>();
		spaceAxisIndexes = new ArrayList<Integer>();
	}
	
	public void add(DataAxis axis){
		axes.add(axis);
		
		axis.index = axes.size()-1;
		
		if( axis.axisType == DataAxis.AxisType.CHANNEL )
			this.channelAxisIndex = axes.size()-1;
		
		if( axis.axisType == DataAxis.AxisType.SPACE )
			if( spaceAxisIndexes.size()>=3 )
				axis.axisType = DataAxis.AxisType.OTHER;
			else
				spaceAxisIndexes.add( axes.size()-1 );
	}
	
	public DataAxis get(int i){
		return axes.get(i);
	}
	
	public DataAxis get(String name){
		for( DataAxis axis : axes )
			if( axis.name == name )
				return axis;
		return null;
	}
	
	public DataAxis getChannelAxis(){
		if( channelAxisIndex == -1 )
			return null;
		return axes.get( channelAxisIndex );
	}
	
	public int getNumberOfSpaceAxis(){
		return spaceAxisIndexes.size();
	}
	
	public List<DataAxis> getSpaceAxes(){
		List<DataAxis> spaceAxes = new ArrayList<DataAxis>();
		for(Integer i : spaceAxisIndexes)
			spaceAxes.add( axes.get(i) );
		return spaceAxes; 
	}
	
	public List<DataAxis> getFreeAxes(){
		List<DataAxis> freeAxes = new ArrayList<DataAxis>();
		for(DataAxis axis : axes)
			if ( axis.axisType == AxisType.OTHER )
				freeAxes.add( axis );
		return freeAxes; 
	}
	
	public int size(){
		return axes.size();
	}
	

	
	
}
