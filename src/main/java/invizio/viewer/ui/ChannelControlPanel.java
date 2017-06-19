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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


// TODO:
//	- add label to top and bottom slider
//	- add label at the extremity of the slider
//
// ideas:
//	- context menu to change the colorMap
//


public class ChannelControlPanel extends JPanel implements ChangeListener, MyObservable {

	private static final long serialVersionUID = 1L;

	private JCheckBox visibilityCB;
	private JSlider sliderMinIntensity;
	private JSlider sliderMaxIntensity;
	private JSlider sliderGamma;
	private int minRange;
	private int maxRange;
	private int ID;
	private String name;
	private String lutName = "gray";
	private JPanel sliderPanel;
	private boolean oldVisibilityState = true;
	
	private ArrayList<MyObserver> observers = new ArrayList<MyObserver>();
	
	/** Create the panel. */
	public ChannelControlPanel(int ID, String channelName, double min, double max) {
		
		this.ID = ID;
		this.name = channelName;
		this.minRange = (int)min;
		this.maxRange = (int) max;
		int gamma0 = 50;
		
		// visibility panel
		JPanel  visibilityPanel = new JPanel();
		
		
		// slider panel
		sliderPanel = new JPanel();
		sliderPanel.setLayout(new GridLayout(3, 1, 0, 0));
		
		sliderMinIntensity = new JSlider(JSlider.HORIZONTAL, minRange, maxRange, minRange);
		sliderMinIntensity.setUI(new LutSliderUI( sliderMinIntensity, lutName, LutSliderUI.Style.TOP));
		sliderMinIntensity.addChangeListener(this);
		sliderMinIntensity.setFocusable(false);
		sliderPanel.add(sliderMinIntensity);
		
		sliderGamma = new JSlider(JSlider.HORIZONTAL, 0, 100, gamma0);
		sliderGamma.setUI(new LutSliderUI( sliderGamma, lutName, LutSliderUI.Style.MIDDLE));
		sliderGamma.addChangeListener(this);
		sliderGamma.setFocusable(false);
		sliderPanel.add(sliderGamma);

		sliderMaxIntensity = new JSlider(JSlider.HORIZONTAL, minRange, maxRange, maxRange);
		sliderMaxIntensity.setUI(new LutSliderUI( sliderMaxIntensity, lutName, LutSliderUI.Style.BOTTOM));
		sliderMaxIntensity.addChangeListener(this);
		sliderMaxIntensity.setFocusable(false);
		sliderPanel.add(sliderMaxIntensity);
		
        
		// general panel
		//setBorder(new TitledBorder(null, channelName, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(visibilityPanel);
		
		visibilityCB = new JCheckBox(channelName);
		visibilityCB.setSelected(true);
		visibilityCB.setHorizontalAlignment(SwingConstants.CENTER);
		visibilityCB.setVerticalAlignment(SwingConstants.CENTER);
		visibilityCB.addChangeListener(this);
		//visibilityCB.setFocusable( false );
		visibilityPanel.setLayout(new BoxLayout(visibilityPanel, BoxLayout.X_AXIS));
		visibilityPanel.add(visibilityCB);
		add(sliderPanel);
		

	}

	
	
	public int getID() {
		return ID;
	}
	public String getName() {
		return name;
	}
	
	public boolean getVisibility() {
		return visibilityCB.isSelected();
	}
	public void setVisibilityCB(boolean visible) {
		visibilityCB.setSelected(visible);;
	}

	public void setLutName(String lutName) {
		this.lutName = lutName;
		LutSliderUI lutSliderUI = (LutSliderUI)sliderMinIntensity.getUI();
		lutSliderUI.setLutName(lutName);
		lutSliderUI = (LutSliderUI)sliderMaxIntensity.getUI();
		lutSliderUI.setLutName(lutName);
		lutSliderUI = (LutSliderUI)sliderGamma.getUI();
		lutSliderUI.setLutName(lutName);
		this.repaint();
	}
	
	public void setMinRange(double minRange) {
		this.minRange = (int)minRange;
		sliderMinIntensity.setMinimum((int)minRange);
		sliderMaxIntensity.setMinimum((int)minRange);
		sliderMinIntensity.repaint();
		sliderMaxIntensity.repaint();
	}
	public void setMaxRange(double maxRange) {
		this.maxRange = (int)maxRange;
		sliderMinIntensity.setMaximum((int)maxRange);
		sliderMaxIntensity.setMaximum((int)maxRange);
		sliderMinIntensity.repaint();
		sliderMaxIntensity.repaint();
	}

	
	public double getGammaValue() {
		return (double)sliderGamma.getValue();
	}
	public double getMaxValue() {
		return (double) sliderMaxIntensity.getValue();
	}
	public double getMinValue() {
		return (double) sliderMinIntensity.getValue();
	}
	
	

	public void stateChanged(ChangeEvent e) {
		
		if( e.getSource().equals(visibilityCB)){
			if(visibilityCB.isSelected()==oldVisibilityState){
				return;
			}
			else{
				oldVisibilityState = visibilityCB.isSelected();
			}
		}
		
		sliderMinIntensity.removeChangeListener(this);
		sliderMaxIntensity.removeChangeListener(this);
		sliderGamma.removeChangeListener(this);
		
		int min = sliderMinIntensity.getValue();
		int max = sliderMaxIntensity.getValue();
		int gamma = sliderGamma.getValue();
		//System.out.println("min "+min+"  ;  max "+max);
		if ( e.getSource().equals(sliderMinIntensity) )
		{
			if( min>max ){
				max=min;
				min = max-1;
				sliderMinIntensity.setValue(min);
				sliderMaxIntensity.setValue(max);
			}
		}
		else if ( e.getSource().equals(sliderMaxIntensity) )
		{
			if( max<min ){
				min=max;
				max = min+1;
				sliderMinIntensity.setValue(min);
				sliderMaxIntensity.setValue(max);
			}
		}
		float[] range = new float[2];
		range[0] = (min-minRange)/((float)(maxRange-minRange));
		range[1] = (max-minRange)/((float)(maxRange-minRange));
		LutSliderUI lutSliderUI = (LutSliderUI)sliderMinIntensity.getUI();
		lutSliderUI.setLutDisplayRange(range);
		lutSliderUI.setLutGamma(gamma);
		lutSliderUI = (LutSliderUI)sliderMaxIntensity.getUI();
		lutSliderUI.setLutDisplayRange(range);
		lutSliderUI.setLutGamma(gamma);
		lutSliderUI = (LutSliderUI)sliderGamma.getUI();
		lutSliderUI.setLutDisplayRange(range);
		lutSliderUI.setLutGamma(gamma);
		
		
		this.repaint();
		
		sliderMinIntensity.addChangeListener(this);
		sliderMaxIntensity.addChangeListener(this);
		sliderGamma.addChangeListener(this);
		
		for( MyObserver observer: observers){ 
			if( observer!=null ){	
				
				//boolean isSliderAdjusting = false;
				//if( e.getSource() instanceof JSlider){
				//	JSlider slider = (JSlider) e.getSource();
				//	isSliderAdjusting = slider.getValueIsAdjusting();
				//	observer.needsUpdate( !isSliderAdjusting );
				//}
				//else if( e.getSource() instanceof JCheckBox ){
				//	observer.needsUpdate( true );
				//}
					
				observer.fireEvent( this );
			}
		}
		
		

	}
	
	
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JFrame frame = new JFrame();
					frame.setSize(300, 100);
					int ID =1;
					double minI = 0;
					double maxI = 100;
					frame.add( new ChannelControlPanel(ID, "test", minI, maxI) );
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	public void addObserver(MyObserver observer) {
		
		this.observers.add( observer );
		
	}


}
