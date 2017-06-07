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

package invizio.viewer.ui;

import invizio.viewer.vizTools.Lut;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

public class LutSliderUI extends BasicSliderUI{

	public enum Style{
		TOP,
		BOTTOM,
		MIDDLE;
	}
	
	private String lutName;
	private Style style;
	private float[] range;
	private float gamma;
	private float span;
	private float thumbFrac = 0.67f;
	
	public LutSliderUI(JSlider slider, String lutName, Style style) {
		super(slider);
		this.lutName = lutName;
		this.range = new float[] {0,1};
		this.style = style;
		this.span = range[1]-range[0];
		this.gamma = 50;
		
	}
	
	public void setLutName(String lutName){
		this.lutName = lutName;
	}
	
	public void setLutGamma(float gamma){
		if( gamma>=100 )
			gamma = 99.9f;
		if( gamma<=0 )
			gamma=0.01f;

		this.gamma = gamma;
	}
	
	public void setLutDisplayRange(float[] range){
		this.range = range;
		range[0] = Math.max(0, range[0]);
		range[0] = Math.min(0.99f, range[0]);
		range[1] = Math.max(0.01f, range[1]);
		range[1] = Math.min(1f, range[1]);
		if( range[0]>-0.01 && range[1]==range[0] )
			range[0]=range[0]-0.01f;
		
		this.span = range[1]-range[0];
	}
	
	@Override
    public void paintTrack(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        
        TreeMap<Float, float[]> lutPoints = Lut.getLutPoints(lutName,30);
        int nPoints = lutPoints.size();
        nPoints += range[0]==0? 0: 1;
        nPoints += range[1]==1? 0: 1;
        
        float[] fracs = new float[nPoints]; 
        Color[] colors = new Color[nPoints];
        int count=0;
        if( range[0]>0 ){
        	fracs[count] = 0;
        	float[] c = lutPoints.firstEntry().getValue();
        	colors[count] = new Color(c[0], c[1], c[2], 0.0f);
        	count++;
        }
        for( Entry<Float,float[]> entry : lutPoints.entrySet() ){
        	float frac = entry.getKey();
        	fracs[count] = range[0]+ span * frac;
        	float[] c = entry.getValue();
        	colors[count] = new Color(c[0], c[1], c[2], (float)Math.pow(frac,gamma/(100-gamma)) );
        	count++;
        }
        if( range[1]<1 ){
        	fracs[count] = 1;
        	float[] c = lutPoints.lastEntry().getValue();
        	colors[count] = new Color(c[0], c[1], c[2], 1.0f);
        }
        
        Rectangle t = trackRect;
        Point2D start = new Point2D.Float(t.x, t.y);
        Point2D end = new Point2D.Float(t.x+t.width, t.y);
        
        LinearGradientPaint lutPaint = new LinearGradientPaint(start, end, fracs, colors);
        Color[] checkerPaint = new Color[] {new Color(0.8f,0.8f,0.8f) , new Color(1f,1f,1f)};
        int size = (int)((1-thumbFrac)*contentRect.height) +1;
        int nSquare = t.width/size;
    	if( style == Style.TOP){
        	for(int i=0; i<nSquare; i++){
        		g2d.setPaint( checkerPaint[i%2] );
        		g2d.fillRect(t.x+i*size, (int)(contentRect.height*thumbFrac), size, size);
        	}
        	g2d.setPaint(lutPaint);
            g2d.fillRect(t.x, (int)(contentRect.height*thumbFrac)	, t.width, contentRect.height-(int)(contentRect.height*thumbFrac));
            g2d.setColor(Color.black);
            g2d.drawLine(t.x, (int)(contentRect.height*thumbFrac), t.x, t.y+t.width);
	        g2d.drawLine(t.x+t.width, (int)(contentRect.height*thumbFrac), t.x+t.width, t.y+t.height);
	        g2d.drawLine(t.x, (int)(contentRect.height*thumbFrac), t.x+t.width, (int)(contentRect.height*thumbFrac));
        }
        else if(style == Style.BOTTOM){
        	for(int i=0; i<nSquare; i++){
        		g2d.setPaint( checkerPaint[i%2] );
        		g2d.fillRect(t.x+i*size, 0, size, size);
        	}
        	
        	g2d.setPaint(lutPaint);
            g2d.fillRect(t.x, 0, t.width, (int)(contentRect.height*(1-thumbFrac)));
            g2d.setColor(Color.black);
            g2d.drawLine(t.x, (int)(contentRect.height*(1-thumbFrac)), t.x, t.y);
	        g2d.drawLine(t.x+t.width, (int)(contentRect.height*(1-thumbFrac)), t.x+t.width, t.y);
	        g2d.drawLine(t.x, (int)(contentRect.height*(1-thumbFrac)), t.x+t.width, (int)(contentRect.height*(1-thumbFrac)));
        }
        else{
        	for(int i=0; i<nSquare; i++){
        		for(int j=0; j<3; j++){
	        		g2d.setPaint( checkerPaint[(i+j+1)%2] );
	        		g2d.fillRect(t.x+i*size, j*size , size, size);
        		}
        	}

        	g2d.setPaint(lutPaint);
            g2d.fillRect(t.x, 0, t.width, contentRect.height);
            g2d.setColor(Color.black);
            g2d.drawLine(t.x, t.y, t.x, t.y+t.height);
        	g2d.drawLine(t.x+t.width, t.y, t.x+t.width, t.y+t.height);
        }
    }

	
    @Override
    public void paintThumb(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        
        thumbRect.width = (int)2*(trackRect.x-contentRect.x); 
        thumbRect.height = (int)(trackRect.x-contentRect.x);
        
        Rectangle t = thumbRect;
        if( style == Style.TOP){
	        g2d.setColor(Color.black);
	        thumbRect.y = (int)(contentRect.height*thumbFrac)-thumbRect.height;
	        int tw2 = t.width / 2;
	        g2d.drawLine(t.x, t.y, t.x + t.width, t.y);
	        g2d.drawLine(t.x, t.y, t.x + tw2, t.y + t.height);
	        g2d.drawLine(t.x + t.width, t.y, t.x + tw2, t.y + t.height);
        }
        else if( style == Style.BOTTOM){
        	thumbRect.y = (int)( contentRect.height*(1-thumbFrac)) ;
        	g2d.setColor(Color.black);
	        int tw2 = t.width / 2;
	        g2d.drawLine(t.x, t.y+t.height, t.x + t.width, t.y+t.height);
	        g2d.drawLine(t.x, t.y+t.height, t.x + tw2, t.y);
	        g2d.drawLine(t.x + t.width, t.y + t.height, t.x + tw2, t.y);
        }
        else{
        	thumbRect.y = (int)( contentRect.height/2-t.height/2) ;
        	g2d.setColor(Color.white);
	        g2d.fillOval(t.x+(int)(t.width*0.25), t.y, (int)(t.width*0.5), t.height);
	        g2d.setColor(Color.black);
	        g2d.drawOval(t.x+(int)(t.width*0.25), t.y, (int)(t.width*0.5), t.height);
        }
        //g2d.setColor(Color.white);
        //g2d.fillRect(t.x, t.y, t.width, t.height);
        //g2d.setColor(Color.black);
        //g2d.drawRect(t.x, t.y, t.width, t.height);
    
        
    }
	
    //@Override
	//public int valueForXPosition(int xpos){
    	
    	
    //}
}
