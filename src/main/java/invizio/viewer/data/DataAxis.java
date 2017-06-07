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

public class DataAxis {
	
	public String name;
	public long sampleNumber;
	public double origin;
	public double extent;
	public double spacing;
	public AxisType axisType; 
	public int index=-1;
	public long position=0;
	
	public enum AxisType{
		SPACE(0,"space"),
		CHANNEL(1,"channel"),
		OTHER(2,"other");
		
		final int id;
		final String name;
		
		AxisType(int id, String name){
			this.id=id;
			this.name = name;
		}
		public int getId(){ return id; }
		public String getName(){ return name;}
	}
	
	public DataAxis(String name, long sampleNumber, double spacing, AxisType axisType){
		this.name = name;
		this.sampleNumber = sampleNumber;
		this.spacing = spacing;
		this.axisType = axisType;
		this.origin = 0;
		this.position = 0;
		this.extent = spacing * sampleNumber;
	}
	
	public DataAxis(String name, long sampleNumber, double spacing, AxisType axisType, double origin){
		this.name = name;
		this.sampleNumber = sampleNumber;
		this.spacing = spacing;
		this.axisType = axisType;
		this.origin = origin;
		this.extent = spacing * sampleNumber;
	}
	
	
	public String toString(){
		return "Axis: name,"+name+" ; type,"+axisType.getName() + " ; spacing,"+spacing+" ; nSample,"+sampleNumber ;
	}
}
