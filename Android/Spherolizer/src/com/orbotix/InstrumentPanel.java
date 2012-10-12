
/**
 * Audalyzer: an audio analyzer for Android.
 * <br>Copyright 2009-2010 Ian Cameron Smith
 *
 * <p>This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation (see COPYING).
 * 
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */


package com.orbotix;


import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import org.hermit.android.instruments.*;
import org.hermit.dsp.Window;


/**
 * The main audio analyser view.  This class relies on the parent SurfaceRunner
 * class to do the bulk of the animation control.
 */
public class InstrumentPanel
	extends InstrumentSurface
	implements GestureDetector.OnGestureListener,
			   GestureDetector.OnDoubleTapListener
{

    // ******************************************************************** //
    // Public Constants.
    // ******************************************************************** //

    /**
     * Definitions of the available window functions.
     */
    public enum Instruments {
        /** Spectrum Gauge, Power and Wave. */
        SPECTRUM_P_W,
        
        /** Sonagram Gauge, Power and Wave. */
        SONAGRAM_P_W,

        /** Spectrum and Sonagram Gauge. */
        SPECTRUM_SONAGRAM,

        /** Spectrum Gauge. */
        SPECTRUM,
        
        /** Sonagram Gauge. */
        SONAGRAM,
    }


	
    // ******************************************************************** //
    // Constructor.
    // ******************************************************************** //

	/**
	 * Create a WindMeter instance.
	 * 
	 * @param	app			The application context we're running in.
	 */
    public InstrumentPanel(Activity app) {
        super(app, SURFACE_DYNAMIC);
        
        audioAnalyser = new AudioAnalyser(this, this);
               
        addInstrument(audioAnalyser);
        
        // On-screen debug stats display.
        statsCreate(new String[] { "µs FFT", "Skip/s" });

        //Gesture detection
        gesturedetector = new GestureDetector(this);
        gesturedetector.setOnDoubleTapListener(this);
    }

    /**
     * Create a SurfaceRunner instance.
     *
     * @param   app         The application context we're running in.
     * @param   attrs       Layout attributes for this SurfaceRunner.
     */
    public InstrumentPanel(Context app, AttributeSet attrs) {
        super(app, SURFACE_DYNAMIC);

        audioAnalyser = new AudioAnalyser(this, this);

        addInstrument(audioAnalyser);

        // On-screen debug stats display.
        statsCreate(new String[] { "µs FFT", "Skip/s" });

        //Gesture detection
        gesturedetector = new GestureDetector(this);
        gesturedetector.setOnDoubleTapListener(this);
    }

    public void setOnBeatDetectedListener(SpectrumGauge.OnBeatDetectedListener listener) {
        audioAnalyser.setOnBeatDetectedListener(listener);
    }

    // ******************************************************************** //
    // Configuration.
    // ******************************************************************** //

    /**
     * Set the sample rate for this instrument.
     * 
     * @param   rate        The desired rate, in samples/sec.
     */
    public void setSampleRate(int rate) {
        audioAnalyser.setSampleRate(rate);
    }
    

    /**
     * Set the input block size for this instrument.
     * 
     * @param   size        The desired block size, in samples.
     */
    public void setBlockSize(int size) {
        audioAnalyser.setBlockSize(size);
    }
    

    /**
     * Set the spectrum analyser windowing function for this instrument.
     * 
     * @param   func        The desired windowing function.
     *                      Window.Function.BLACKMAN_HARRIS is a good option.
     *                      Window.Function.RECTANGULAR turns off windowing.
     */
    public void setWindowFunc(Window.Function func) {
        audioAnalyser.setWindowFunc(func);
    }
    

    /**
     * Set the decimation rate for this instrument.
     * 
     * @param   rate        The desired decimation.  Only 1 in rate blocks
     *                      will actually be processed.
     */
    public void setDecimation(int rate) {
        audioAnalyser.setDecimation(rate);
    }
    

    /**
     * Set the histogram averaging window for this instrument.
     * 
     * @param   rate        The averaging interval.  1 means no averaging.
     */
    public void setAverageLen(int rate) {
        audioAnalyser.setAverageLen(rate);
    }
    

    /**
     * Enable or disable stats display.
     * 
     * @param   enable        True to display performance stats.
     */
    public void setShowStats(boolean enable) {
        setDebugPerf(enable);
    }
    
    
    /**
     * Set the instruments to display
     * 
     * @param   InstrumentPanel.Intruments        Choose which ones to display.
     */
    public void setInstruments(InstrumentPanel.Instruments i) {
    	currentInstruments=i;
    	loadInstruments(currentInstruments);
    }


    /**
     * Load instruments
     *
     * @param   InstrumentPanel.Intruments        Choose which ones to display.
     */
    private void loadInstruments(InstrumentPanel.Instruments i) {
    	Log.i(TAG, "Load instruments");

    	//Stop surface update
    	onPause();

		//Clear surface events
    	clearGauges();

//    	//Clear analyse events
//   		audioAnalyser.resetGauge();
//
//    	//Destroy last Gauges
//    	sonagramGauge=null;
    	spectrumGauge=null;
//    	powerGauge=null;
//    	waveformGauge=null;
//
//    	//Create instruments, update and refresh
    	//if ((i==InstrumentPanel.Instruments.SPECTRUM)||(i==InstrumentPanel.Instruments.SPECTRUM_SONAGRAM)||(i==InstrumentPanel.Instruments.SPECTRUM_P_W)) {
        spectrumGauge = audioAnalyser.getSpectrumGauge(this);
        addGauge(spectrumGauge);
    	//}
//
//    	if ((i==InstrumentPanel.Instruments.SONAGRAM)||(i==InstrumentPanel.Instruments.SPECTRUM_SONAGRAM)||(i==InstrumentPanel.Instruments.SONAGRAM_P_W)) {
//    			sonagramGauge = audioAnalyser.getSonagramGauge(this);
//   	        	addGauge(sonagramGauge);
//    	}
//
//    	if ((i==InstrumentPanel.Instruments.SPECTRUM_P_W)||(i==InstrumentPanel.Instruments.SONAGRAM_P_W)) {
//            waveformGauge = audioAnalyser.getWaveformGauge(this);
//            addGauge(waveformGauge);
//
//            powerGauge = audioAnalyser.getPowerGauge(this);
//            addGauge(powerGauge);
//    	}

    	//Load current layout in Gauges if they're already define
    	if ((currentWidth>0)&&(currentHeight>0))
    		refreshLayout();

		//Restart
    	onResume();

    	Log.i(TAG, "End instruments loading");
    }


    // ******************************************************************** //
    // Layout Processing.
    // ******************************************************************** //

    /**
     * Lay out the display for a given screen size.
     *
     * @param   width       The new width of the surface.
     * @param   height      The new height of the surface.
     */
    @Override
    protected void layout(int width, int height) {
    	//Save current layout
    	currentWidth=width;
    	currentHeight=height;
    	refreshLayout();
    }


    /**
     * Lay out the display for the current screen size.
     */
    protected void refreshLayout() {
        // Make up some layout parameters.
        int min = Math.min(currentWidth, currentHeight);
        int gutter = min / (min > 400 ? 15 : 20);

        // Calculate the layout based on the screen configuration.
        if (currentWidth > currentHeight)
            layoutLandscape(currentWidth, currentHeight, gutter);
        else
            layoutPortrait(currentWidth, currentHeight, gutter);

        // Set the gauge geometries.
//        if (waveformGauge!=null)
//        	waveformGauge.setGeometry(waveRect);
//        if (spectrumGauge!=null)
//        	spectrumGauge.setGeometry(specRect);
//        if (sonagramGauge!=null)
//        	sonagramGauge.setGeometry(sonaRect);
//        if (powerGauge!=null)
//        	powerGauge.setGeometry(powerRect);
    }


    /**
     * Lay out the display for a given screen size.
     *
     * @param   width       The new width of the surface.
     * @param   height      The new height of the surface.
     * @param   gutter      Spacing to leave between items.
     */
    private void layoutLandscape(int width, int height, int gutter) {
        int x = gutter;
        int y = gutter;

        // Divide the display into two columns.
        int col = (width - gutter * 3) / 2;

    	//Init
//        waveRect = new Rect(0,0,0,0);
//    	specRect = new Rect(0,0,0,0);
//    	sonaRect = new Rect(0,0,0,0);
//    	powerRect = new Rect(0,0,0,0);
//
//        if (waveformGauge!=null) {
//            // Divide the left pane in two.
//            int row = (height - gutter * 3) / 2;
//
//        	//Wave+Spectrum+Power or Wave+Sonagram+Power
//            waveRect = new Rect(x, y, x + col, y + row);
//            y += row + gutter;
//            powerRect = new Rect(x, y, x + col, height - gutter);
//            x += col + gutter;
//            y = gutter;
//
//            //Spectrum or Sonagram fullscreen
//            if (spectrumGauge!=null)
//                specRect = new Rect(x, y, x + col, height - gutter);
//            else
//                sonaRect = new Rect(x, y, x + col, height - gutter);
//        } else if ((spectrumGauge!=null)&&(sonagramGauge!=null)) {
//        	//Spectrum + Sonagram
//            specRect = new Rect(x, y, x + col, height - gutter);
//            x += col + gutter;
//            sonaRect = new Rect(x, y, x + col, height - gutter);
//        } else {
//        	//Spectrum or Sonagram fullscreen
//            if (spectrumGauge!=null)
//                specRect = new Rect(x, y, width - gutter, height - gutter);
//            else
//                sonaRect = new Rect(x, y, width - gutter, height - gutter);
//        }
     }


    /**
     * Lay out the display for a given screen size.
     *
     * @param   width       The new width of the surface.
     * @param   height      The new height of the surface.
     * @param   gutter      Spacing to leave between items.
     */
    private void layoutPortrait(int width, int height, int gutter) {
        int x = gutter;
        int y = gutter;

        // Display one column.
        int col = width - gutter * 2;

    	//Init
//        waveRect = new Rect(0,0,0,0);
//    	specRect = new Rect(0,0,0,0);
//    	sonaRect = new Rect(0,0,0,0);
//    	powerRect = new Rect(0,0,0,0);

//        if (waveformGauge!=null) {
//            // Divide the display into three vertical elements, the
//            // spectrum or sonagram display being double-height.
//            int unit = (height - gutter * 4) / 4;
//
//            //Wave+Spectrum+Power or Wave+Sonagram+Power
//            waveRect = new Rect(x, y, x + col, y + unit);
//            y += unit + gutter;
//
//            if (spectrumGauge!=null)
//            	specRect = new Rect(x, y, x + col, y + unit * 2);
//            else
//            	sonaRect = new Rect(x, y, x + col, y + unit * 2);
//
//            y += unit * 2 + gutter;
//            powerRect = new Rect(x, y, x + col, y + unit);
//        }
//        else if ((spectrumGauge!=null)&&(sonagramGauge!=null)) {
//            // Divide the display into two vertical elements
//            int unit = (height - gutter * 3) / 2;
//
//            //Spectrum + Sonagram
//            specRect = new Rect(x, y, x + col, y + unit);
//            y += unit + gutter;
//            sonaRect = new Rect(x, y, x + col, y + unit);
//        }
//        else {
//            //Spectrum or Sonagram fullscreen
//            if (spectrumGauge!=null)
//                specRect = new Rect(x, y, width - gutter, height - gutter);
//            else
//                sonaRect = new Rect(x, y, width - gutter, height - gutter);
//        }
    }


    // ******************************************************************** //
    // Input Handling.
    // ******************************************************************** //

    /**
	 * Handle key input.
	 *
     * @param	keyCode		The key code.
     * @param	event		The KeyEvent object that defines the
     * 						button action.
     * @return				True if the event was handled, false otherwise.
	 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	return false;
    }


    /**
	 * Handle touchscreen input.
	 *
     * @param	event		The MotionEvent object that defines the action.
     * @return				True if the event was handled, false otherwise.
	 */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gesturedetector.onTouchEvent(event);
    }


    @Override
    public boolean onDown(MotionEvent e) {
        //True for propagation to onFling Event
    	return true;
    }


    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX,float velocityY) {
    	if (isFullScreen)
    		return false;

    	final float ev1x = event1.getX();
    	//final float ev1y = event1.getY();
    	final float ev2x = event2.getX();
    	//final float ev2y = event2.getY();
    	final float xdiff = Math.abs(ev1x - ev2x);
    	//final float ydiff = Math.abs(ev1y - ev2y);
    	final float xvelocity = Math.abs(velocityX);
    	//final float yvelocity = Math.abs(velocityY);

    	if (xvelocity > this.SWIPE_MIN_VELOCITY && xdiff > this.SWIPE_MIN_DISTANCE) {
    		if (ev1x > ev2x) { //Swipe Left
    			Log.i(TAG, "Swipe Left");
    			//Modulo 3 to avoid fullscreen modes (used with onLongPress)
    			currentInstruments= Instruments.values()[(currentInstruments.ordinal()+1)%3];
    			loadInstruments(currentInstruments);
    		} else { //Swipe Right

    			Log.i(TAG, "Swipe Right");
    			//Modulo 3 to avoid fullscreen modes (used with onLongPress)
    			currentInstruments= Instruments.values()[(3+currentInstruments.ordinal()-1)%3];
    			loadInstruments(currentInstruments);
    		}
    	}
    	return false;
    }


    @Override
    public void onLongPress(MotionEvent e) {
    	//Vibrate
    	//vibrator.vibrate(100);

//    	final float x = e.getX();
//        final float y = e.getY();
//		if (isFullScreen) {
//			//Load pref instruments
//			isFullScreen = false;
//			loadInstruments(currentInstruments);
//		} else {
//    		//Load fullscreen instrument
//			if (specRect.contains((int) x, (int) y)) {
//    			isFullScreen = true;
//    			loadInstruments(InstrumentPanel.Instruments.SPECTRUM);
//    		}
//
//    		if (sonaRect.contains((int) x, (int) y)) {
//    			isFullScreen = true;
//    			loadInstruments(InstrumentPanel.Instruments.SONAGRAM);
//    		}
//		}
    }
    

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,float distanceY) {
        return false;
    }


    @Override
    public void onShowPress(MotionEvent e) {
    }


    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
    

    @Override
    public boolean onDoubleTap(MotionEvent e) {
		return false;
    }
    

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
    

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }
    
    
    // ******************************************************************** //
    // Save and Restore.
    // ******************************************************************** //

    /**
     * Save the state of the panel in the provided Bundle.
     * 
     * @param   icicle      The Bundle in which we should save our state.
     */
    protected void saveState(Bundle icicle) {
//      gameTable.saveState(icicle);
    }


    /**
     * Restore the panel's state from the given Bundle.
     * 
     * @param   icicle      The Bundle containing the saved state.
     */
    protected void restoreState(Bundle icicle) {
//      gameTable.pause();
//      gameTable.restoreState(icicle);
    }
    
    
    // ******************************************************************** //
    // Class Data.
    // ******************************************************************** //

    // Debugging tag.
	private static final String TAG = "Audalyzer";

	
	// ******************************************************************** //
	// Private Data.
	// ******************************************************************** //

	//Gesture detection
	private GestureDetector gesturedetector = null;

    final private int SWIPE_MIN_DISTANCE = 100;
    final private int SWIPE_MIN_VELOCITY = 100;


    // The current Intruments in pref.
    private Instruments currentInstruments = null;

    // The current fullscreen state
    private boolean isFullScreen =false;
    
    //Current layout
    private int currentWidth=0;
    private int currentHeight=0;
    
    // Our audio input device.
    private final AudioAnalyser audioAnalyser;
    
//    // The gauges associated with this instrument.
//    private WaveformGauge waveformGauge = null;
    private SpectrumGauge spectrumGauge = null;
//    private SonagramGauge sonagramGauge = null;
//    private PowerGauge powerGauge = null;

    // Bounding rectangles for the waveform, spectrum, sonagram, and VU meter displays.
//    private Rect waveRect = null;
//    private Rect specRect = null;
//    private Rect sonaRect = null;
//    private Rect powerRect = null;

}

