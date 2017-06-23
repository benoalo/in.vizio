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

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Lut {

    final static String[] defaultLutName = new String[] {"red","green","blue","grays", "magenta", "cyan", "yellow"}; 

    public static String getDefaultLUTName(int i)
    {
    	int nDefaultLut = defaultLutName.length;
    	return defaultLutName[i%nDefaultLut]; 
    }
    
    /**
     * 
     * @param lutName
     * @return a map index value to color value. where index belong to [0,1] and the map is sorted by index
     */
    public static TreeMap<Float,float[]> getLutPoints(String lutName)
	{
    	TreeMap<Float, float[]> lutPoints = new TreeMap<Float, float[]>();
		lutName = lutName.toLowerCase();
			
		if (lutName == "grays" ){
			lutPoints.put( 0.0f, new float[] {0f,0f,0f});
			lutPoints.put( 1.0f, new float[] {1f,1f,1f});
		}
		else if( lutName == "magenta"){
			lutPoints.put( 0.0f, new float[] {0f,0f,0f});
			lutPoints.put( 1.0f, new float[] {1f,0f,1f});
		}
		else if( lutName == "red"){
			lutPoints.put( 0.0f, new float[] {0f,0f,0f});
			lutPoints.put( 1.0f, new float[] {1f,0f,0f});
		}
		else if( lutName == "green"){
			lutPoints.put( 0.0f, new float[] {0f,0f,0f});
			lutPoints.put( 1.0f, new float[] {0f,1f,0f});
		}			
		else if( lutName == "blue"){
			lutPoints.put( 0.0f, new float[] {0f,0f,0f});
			lutPoints.put( 1.0f, new float[] {0f,0f,1f});
		}		
		else if( lutName == "cyan"){
			lutPoints.put( 0.0f, new float[] {0f,0f,0f});
			lutPoints.put( 1.0f, new float[] {0f,1f,1f});
		}		
		else if( lutName == "yellow"){
			lutPoints.put( 0.0f, new float[] {0f,0f,0f});
			lutPoints.put( 1.0f, new float[] {1f,1f,0f});
		}		
		return lutPoints;
	}
    
    

    
    public static TreeMap<Float,float[]> getLutPoints(String lutName, int n)
	{
    	TreeMap<Float,float[]> lutPoints = getLutPoints(lutName);
    	Entry<Float,float[]> e0 = lutPoints.firstEntry();
    	Iterator<Entry<Float,float[]>> es = lutPoints.entrySet().iterator();
    	Entry<Float,float[]> e1= es.next();
    	for( int i=1 ; i<(n-1); i++){
    		float f = i/((float)n-1);
    		while(e1.getKey()<f){
    			e0=e1;
    			e1=es.next();
    		}
    		if(e1.getKey()==f)
    			continue;
    		
    		float p = (f-e0.getKey())/(e1.getKey()-e0.getKey());
    		float[] c = new float[3];
    		for( int j=0 ; j<3 ; j++){
    			c[j] = (1-p)*e0.getValue()[j] +p*e1.getValue()[j]; 
    		}
    		lutPoints.put(f, c);
    		//System.out.println("f0:"+e0.getKey()+" c0:"+ArrayUtils.toString(e0.getValue()));
    		//System.out.println("f:"+f+" c:"+ArrayUtils.toString(c));
    	}
    	
    	return lutPoints;
    }
	
	
}
