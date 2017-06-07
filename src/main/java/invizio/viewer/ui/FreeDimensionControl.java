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

import invizio.viewer.event.MyObservable;
import invizio.viewer.event.MyObserver;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

// TODO:
//	* add label with slider position info


public class FreeDimensionControl extends JPanel implements AdjustmentListener, MyObservable
{
	private static final long serialVersionUID = 1L;

	private JScrollBar scrollBar;
	private String name;
	private int minRange;
	private int maxRange;
	
	private LinkedList<MyObserver> observers = new LinkedList<MyObserver>();

	public FreeDimensionControl(String name, int min, int max){
		this.name = name;
		this.minRange = min;
		this.maxRange = max;
		int extent = 1;
		int value =0;
		scrollBar = new JScrollBar(JScrollBar.HORIZONTAL, value ,extent,minRange, maxRange);
		scrollBar.addAdjustmentListener(this);
		
		JLabel label = new JLabel(name);
		
		this.setLayout( new BoxLayout(this, BoxLayout.X_AXIS) );
		this.add(label);
		this.add(scrollBar);		
	}

	
	public String getName() {
		return name;
	}
	
	public int getValue() {
		return scrollBar.getValue();
	}
	
	public void setValue(int value) {
		value = Math.max(minRange, value);
		value = Math.min(maxRange, value);
		scrollBar.setValue( value );
		scrollBar.repaint();
	}
	
	
	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		
		scrollBar.removeAdjustmentListener(this);
		
		this.repaint();
		
		for( MyObserver observer: observers){ 
			if( observer!=null ){	
				
				//boolean isSliderAdjusting = false;
				//if( e.getSource() instanceof JSlider){
				//	JSlider slider = (JSlider) e.getSource();
				//	isSliderAdjusting = slider.getValueIsAdjusting();
				//	observer.needsUpdate( !isSliderAdjusting );
				//}
				
				observer.fireEvent( this );
			}
		}
		
		scrollBar.addAdjustmentListener(this);
	}

	
	@Override
	public void addObserver(MyObserver observer) {
		
		this.observers.add( observer );
	}
	
	
}
