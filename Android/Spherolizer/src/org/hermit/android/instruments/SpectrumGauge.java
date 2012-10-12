
/**
 * org.hermit.android.instrument: graphical instruments for Android.
 * <br>Copyright 2009 Ian Cameron Smith
 * 
 * <p>These classes provide input and display functions for creating on-screen
 * instruments of various kinds in Android apps.
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


package org.hermit.android.instruments;


import android.graphics.*;
import android.graphics.Paint.Style;
import org.hermit.android.core.SurfaceRunner;

import java.util.ArrayList;


/**
 * A graphical display which displays the audio spectrum from an
 * {@link org.hermit.android.instruments.AudioAnalyser} instrument.  This class cannot be instantiated
 * directly; get an instance by calling
 * {@link org.hermit.android.instruments.AudioAnalyser#getSpectrumGauge(org.hermit.android.core.SurfaceRunner)}.
 */
public class SpectrumGauge
    extends Gauge
{

	// ******************************************************************** //
	// Constructor.
	// ******************************************************************** //

	/**
	 * Create a SpectrumGauge.  This constructor is package-local, as
	 * public users get these from an {@link org.hermit.android.instruments.AudioAnalyser} instrument.
	 * 
	 * @param	parent		Parent surface.
     * @param   rate        The input sample rate, in samples/sec.
	 */
	SpectrumGauge(SurfaceRunner parent, int rate) {
	    super(parent);
	    nyquistFreq = rate / 2;
        mSystemTimeStartSec = System.currentTimeMillis();
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
        nyquistFreq = rate / 2;
        
        // If we have a size, then we have a background.  Re-draw it
        // to show the new frequency scale.
        if (haveBounds())
            drawBg(bgCanvas, getPaint());
    }
    

    /**
     * Set the size for the label text.
     * 
     * @param   size        Label text size for the gauge.
     */
    public void setLabelSize(float size) {
        labelSize = size;
    }


    /**
     * Get the size for the label text.
     * 
     * @return              Label text size for the gauge.
     */
    public float getLabelSize() {
        return labelSize;
    }


	// ******************************************************************** //
	// Geometry.
	// ******************************************************************** //

    /**
     * This is called during layout when the size of this element has
     * changed.  This is where we first discover our size, so set
     * our geometry to match.
     * 
	 * @param	bounds		The bounding rect of this element within
	 * 						its parent View.
     */
	@Override
    public void setGeometry(Rect bounds) {
	    super.setGeometry(bounds);
	    
	    dispX = bounds.left;
	    dispY = bounds.top;
	    dispWidth = bounds.width();
	    dispHeight = bounds.height();
        
        // Do some layout within the meter.
        int mw = dispWidth;
        int mh = dispHeight;
        if (labelSize == 0f)
            labelSize = mw / 24f;
        
        spectLabY = mh - 4;
        spectGraphMargin = labelSize;
        spectGraphX = spectGraphMargin;
        spectGraphY = 0;
        spectGraphWidth = mw - spectGraphMargin * 2;
        spectGraphHeight = mh - labelSize - 6;

        // Create the bitmap for the spectrum display,
        // and the Canvas for drawing into it.
        specBitmap = getSurface().getBitmap(dispWidth, dispHeight);
        specCanvas = new Canvas(specBitmap);
        
        // Create the bitmap for the background,
        // and the Canvas for drawing into it.
        bgBitmap = getSurface().getBitmap(dispWidth, dispHeight);
        bgCanvas = new Canvas(bgBitmap);
        
        drawBg(bgCanvas, getPaint());
	}


    // ******************************************************************** //
    // Background Drawing.
    // ******************************************************************** //
    
    /**
     * Do the subclass-specific parts of drawing the background
     * for this element.  Subclasses should override
     * this if they have significant background content which they would
     * like to draw once only.  Whatever is drawn here will be saved in
     * a bitmap, which will be rendered to the screen before the
     * dynamic content is drawn.
     * 
     * <p>Obviously, if implementing this method, don't clear the screen when
     * drawing the dynamic part.
     * 
     * @param   canvas      Canvas to draw into.
     * @param   paint       The Paint which was set up in initializePaint().
     */
    private void drawBg(Canvas canvas, Paint paint) {
        canvas.drawColor(0xff000000);
        
        paint.setColor(0xffffff00);
        paint.setStyle(Style.STROKE);

        // Draw the grid.
        final float sx = 0 + spectGraphX;
        final float sy = 0 + spectGraphY;
        final float ex = sx + spectGraphWidth - 1;
        final float ey = sy + spectGraphHeight - 1;
        final float bw = spectGraphWidth - 1;
        final float bh = spectGraphHeight - 1;
        canvas.drawRect(sx, sy, ex, ey, paint);
        for (int i = 1; i < 10; ++i) {
            final float x = (float) i * (float) bw / 10f;
            canvas.drawLine(sx + x, sy, sx + x, sy + bh, paint);
        }
        for (int i = 1; i < RANGE_BELS; ++i) {
            final float y = (float) i * (float) bh / RANGE_BELS;
            canvas.drawLine(sx, sy + y, sx + bw, sy + y, paint);
        }
        
        // Draw the labels below the grid.
        final float ly = 0 + spectLabY;
        paint.setTextSize(labelSize);
        int step = paint.measureText("8.8k") > bw / 10f - 1 ? 2 : 1;
        for (int i = 0; i <= 10; i += step) {
            int f = nyquistFreq * i / 10;
            String text = f >= 10000 ? "" + (f / 1000) + "k" :
                          f >= 1000 ? "" + (f / 1000) + "." + (f / 100 % 10) + "k" :
                          "" + f;
            float tw = paint.measureText(text);
            float lx = sx + i * (float) bw / 10f + 1 - (tw / 2);
            canvas.drawText(text, lx, ly, paint);
        }
    }


    // ******************************************************************** //
    // Data Updates.
    // ******************************************************************** //
    
	/**
	 * New data from the instrument has arrived.  This method is called
	 * on the thread of the instrument.
	 * 
     * @param   data        An array of floats defining the signal power
     *                      at each frequency in the spectrum.
	 */
	final void update(float[] data, float[] dataComplex, long instantEnergy) {
        final Canvas canvas = specCanvas;
        final Paint paint = getPaint();
        
        // Now actually do the drawing.
        synchronized (this) {

            // do FFT calculations!
            doFFTAnalysis(data, dataComplex, instantEnergy);

            updateUI(canvas);


           // canvas.drawBitmap(bgBitmap, 0, 0, paint);
           // if (logFreqScale)
           //     logGraph(data, canvas, paint);
           // else
           //     linearGraph(data, canvas, paint);
        }
    }

    public void updateUI(final Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(0xFFFFFFFF);

        // Print : Low Instant: 4.4404 Avg: 409.09
        canvas.drawText("Low Instant: " + new Double(mInstantFreqEnergy[0]).toString() + " Avg: "
                + new Double(mCurrentAvgFreqEnergyOneSec[0]).toString(), 10, 10, paint);

        canvas.drawText("Mid Instant: " + new Double(mInstantFreqEnergy[1]).toString() + " Avg: "
                + new Double(mCurrentAvgFreqEnergyOneSec[1]).toString(), 10, 50, paint);

        canvas.drawText("Low Instant: " + new Double(mInstantFreqEnergy[2]).toString() + " Avg: "
                + new Double(mCurrentAvgFreqEnergyOneSec[2]).toString(), 10, 100, paint);


    }

    /**
     * This is where you change the code to do different on and off beat analysis
     * @param spectrumData
     * @param spectrumDataComplex
     * @param instantEnergy
     */
    public void doFFTAnalysis(float[] spectrumData, float[] spectrumDataComplex, long instantEnergy) {
        // The data in spectrumData is organized as so
        // the length is half the data buffer and the frequencies are 0-(half sample rate)
        // skip the first frequency bucket since it is not needed for audio processing
        if( (System.currentTimeMillis() - mSystemTimeStartSec) > 2000 ) {

            // Compute the average sound energy of a sample over a 1 second period
            mCurrentAvgEnergyOneSec = mRunningSoundAvg / mNumberOfSamplesInOneSec;
            mCurrentAvgFreqEnergyOneSec[0] = mRunningFreqSoundAvg[0] / mNumberOfSamplesInOneSec;
            mCurrentAvgFreqEnergyOneSec[1] = mRunningFreqSoundAvg[1] / mNumberOfSamplesInOneSec;
            mCurrentAvgFreqEnergyOneSec[2] = mRunningFreqSoundAvg[2] / mNumberOfSamplesInOneSec;

            mMaxInstantEnergy = mTempMaxIntensity;

            // reset variables for next iteration
            mNumberOfSamplesInOneSec = 0;
            mTempMaxIntensity = 0;
            mRunningSoundAvg = 0.0;
            mRunningFreqSoundAvg[0] = 0.0;
            mRunningFreqSoundAvg[1] = 0.0;
            mRunningFreqSoundAvg[2] = 0.0;
            mSystemTimeStartSec = System.currentTimeMillis();

        }

        mRunningSoundAvg += instantEnergy;
        mTempMaxIntensity = Math.max(mTempMaxIntensity, instantEnergy);

        if( instantEnergy > mCurrentAvgEnergyOneSec ) {
            if( !mIsBeatOn ) {
                mIsBeatOn = true;
                if(mMaxInstantEnergy != 0)  {
                    if( onBeatDetectedListener != null ) {
                        fireBeatDetectedEventOn((float)((double)instantEnergy/(double)mMaxInstantEnergy));
                    }
                }
            }
        }
        else {
            if( mIsBeatOn ) {
                if( onBeatDetectedListener != null ) fireBeatDetectedEventOff();
            }
            mIsBeatOn = false;
        }

        int counter = 3;
        double frequency = (nyquistFreq / spectrumData.length)*counter;
        double freqInstantEnergy = 0.0;
        double tempEnergy = 0.0;

        while( frequency < LOW_FREQUENCY_THRESHOLD && counter < spectrumData.length ) {
            tempEnergy = Math.sqrt((spectrumData[counter]*spectrumData[counter])+(spectrumDataComplex[counter]*spectrumDataComplex[counter]));
            freqInstantEnergy += tempEnergy;
            counter++;
            frequency = (nyquistFreq / spectrumData.length)*counter;
        }
        mRunningFreqSoundAvg[0]+=freqInstantEnergy;
        mInstantFreqEnergy[0] = freqInstantEnergy;

        // Check for a low freq beat
        if( freqInstantEnergy >  mCurrentAvgFreqEnergyOneSec[0]*BEAT_THRESHOLD ) {
            // signal beat event to the listener
            if( !mIsLowBeatOn ) {
                mIsLowBeatOn = true;
                if( onBeatDetectedListener != null ) fireLowBeatDetectedEventOn();
            }
        }
        else {
            if( mIsLowBeatOn ) {
                if( onBeatDetectedListener != null ) fireLowBeatDetectedEventOff();
            }
            mIsLowBeatOn = false;
        }

        freqInstantEnergy = 0;
        while( frequency < MID_FREQUENCY_THRESHOLD && counter < spectrumData.length ) {
            tempEnergy = Math.sqrt((spectrumData[counter]*spectrumData[counter])+(spectrumDataComplex[counter]*spectrumDataComplex[counter]));
            freqInstantEnergy += tempEnergy;
            frequency = (nyquistFreq / spectrumData.length)*counter;
            counter++;
        }
        mRunningFreqSoundAvg[1]+=freqInstantEnergy;
        mInstantFreqEnergy[1] = freqInstantEnergy;

        // Check for a mid freq beat
        if( freqInstantEnergy >  mCurrentAvgFreqEnergyOneSec[1]*BEAT_THRESHOLD ) {
            // signal beat event to the listener
            if( !mIsMidBeatOn ) {
                mIsMidBeatOn = true;
                if( onBeatDetectedListener != null ) fireMidBeatDetectedEventOn();
            }
        }
        else {
            if( mIsMidBeatOn ) {
                if( onBeatDetectedListener != null ) fireMidBeatDetectedEventOff();
            }
            mIsMidBeatOn = false;
        }

        freqInstantEnergy = 0;
        while( counter < spectrumData.length ) {
            tempEnergy = Math.sqrt((spectrumData[counter]*spectrumData[counter])+(spectrumDataComplex[counter]*spectrumDataComplex[counter]));
            freqInstantEnergy += tempEnergy;
            frequency = (nyquistFreq / spectrumData.length)*counter;
            counter++;
        }
        mRunningFreqSoundAvg[2]+=freqInstantEnergy;
        mInstantFreqEnergy[2] = freqInstantEnergy;

        // Check for a high freq beat
        if( freqInstantEnergy >  mCurrentAvgFreqEnergyOneSec[2]*BEAT_THRESHOLD ) {
            // signal beat event to the listener
            if( !mIsHighBeatOn ) {
                mIsHighBeatOn = true;
                if( onBeatDetectedListener != null ) fireHighBeatDetectedEventOn();
            }
        }
        else {
            if( mIsHighBeatOn ) {
                if( onBeatDetectedListener != null ) fireHighBeatDetectedEventOff();
            }
            mIsHighBeatOn = false;
        }

        mNumberOfSamplesInOneSec++;
        long specEnd = System.currentTimeMillis();
       // instrumentSurface.setFFTLag(specEnd-specStart);
    }

	   
    /**
     * Draw a linear spectrum graph.
     * 
     * @param   data        An array of floats defining the signal power
     *                      at each frequency in the spectrum.
     * @param  canvas       Canvas to draw into.
     * @param  paint        Paint to draw with.
     */
    private void logGraph(float[] data, Canvas canvas, Paint paint) {
        paint.setStyle(Style.FILL);
        paintColor[1] = 1f;
        paintColor[2] = 1f;
        final int len = data.length;
        final float bw = (float) (spectGraphWidth - 2) / (float) len;
        final float bh = spectGraphHeight - 2;
        final float be = spectGraphY + spectGraphHeight - 1;
        
        // Determine the first and last frequencies we have.
        final float lf = nyquistFreq / len;
        final float rf = nyquistFreq;
        
        // Now, how many octaves is that.  Round down.  Calculate pixels/oct.
        final int octaves = (int) Math.floor(log2(rf / lf)) - 2;
        final float octWidth = (float) (spectGraphWidth - 2) / (float) octaves;
        
        // Calculate the base frequency for the graph, which isn't lf.
        final float bf = rf / (float) Math.pow(2, octaves);
            
        // Element 0 isn't a frequency bucket; skip it.
        for (int i = 1; i < len; ++i) {
            // Cycle the hue angle from 0° to 300°; i.e. red to purple.
            paintColor[0] = (float) i / (float) len * 300f;
            paint.setColor(Color.HSVToColor(paintColor));

            // What frequency bucket are we in.
            final float f = lf * i;

            // For freq f, calculate x.
            final float x = spectGraphX + (float) (log2(f) - log2(bf)) * octWidth;

            // Draw the bar.
            float y = be - (float) (Math.log10(data[i]) / RANGE_BELS + 1f) * bh;
            if (y > be)
                y = be;
            else if (y < spectGraphY)
                y = spectGraphY;
            if (bw <= 1.0f)
                canvas.drawLine(x, y, x, be, paint);
            else
                canvas.drawRect(x, y, x + bw, be, paint);
        }
    }
    
    
    private final double log2(double x) {
        return Math.log(x) / LOG2;
    }
    

	/**
	 * Draw a linear spectrum graph.
	 * 
     * @param   data        An array of floats defining the signal power
     *                      at each frequency in the spectrum.
	 * @param  canvas       Canvas to draw into.
	 * @param  paint        Paint to draw with.
	 */
	private void linearGraph(float[] data, Canvas canvas, Paint paint) {
        paint.setStyle(Style.FILL);
        paintColor[1] = 1f;
        paintColor[2] = 1f;
        final int len = data.length;
        final float bw = (float) (spectGraphWidth - 2) / (float) len;
        final float bh = spectGraphHeight - 2;
        final float be = spectGraphY + spectGraphHeight - 1;
        
        // Element 0 isn't a frequency bucket; skip it.
        for (int i = 1; i < len; ++i) {
            // Cycle the hue angle from 0° to 300°; i.e. red to purple.
            paintColor[0] = (float) i / (float) len * 300f;
            paint.setColor(Color.HSVToColor(paintColor));

            // Draw the bar.
            final float x = spectGraphX + i * bw + 1;
            float y = be - (float) (Math.log10(data[i]) / RANGE_BELS + 1f) * bh;
            if (y > be)
                y = be;
            else if (y < spectGraphY)
                y = spectGraphY;
            if (bw <= 1.0f)
                canvas.drawLine(x, y, x, be, paint);
            else
                canvas.drawRect(x, y, x + bw, be, paint);
        }
	}
	

	// ******************************************************************** //
	// View Drawing.
	// ******************************************************************** //
	
	/**
	 * Do the subclass-specific parts of drawing for this element.
	 * This method is called on the thread of the containing SuraceView.
	 * 
	 * <p>Subclasses should override this to do their drawing.
	 * 
	 * @param	canvas		Canvas to draw into.
	 * @param	paint		The Paint which was set up in initializePaint().
     * @param   now         Nominal system time in ms. of this update.
	 */
	@Override
    protected final void drawBody(Canvas canvas, Paint paint, long now) {
	    // Since drawBody may be called more often than we get audio
	    // data, it makes sense to just draw the buffered image here.
	    synchronized (this) {
	        canvas.drawBitmap(specBitmap, dispX, dispY, null);
	    }
	}

    // ******************************************************************** //
    // Beat Detected Listener
    // ******************************************************************** //

    private void fireBeatDetectedEventOn(float intensity) {
        if( onBeatDetectedListener != null ) {
            onBeatDetectedListener.onBeatDetectedOn(intensity);
        }
    }

    private void fireLowBeatDetectedEventOn() {
        if( onBeatDetectedListener != null ) {
            onBeatDetectedListener.onLowBeatDetectedOn();
        }
    }

    private void fireMidBeatDetectedEventOn() {
        if( onBeatDetectedListener != null ) {
            onBeatDetectedListener.onMidBeatDetectedOn();
        }
    }

    private void fireHighBeatDetectedEventOn() {
        if( onBeatDetectedListener != null ) {
            onBeatDetectedListener.onHighBeatDetectedOn();
        }
    }

    private void fireBeatDetectedEventOff() {
        if( onBeatDetectedListener != null ) {
            onBeatDetectedListener.onBeatDetectedOff();
        }
    }

    private void fireLowBeatDetectedEventOff() {
        if( onBeatDetectedListener != null ) {
            onBeatDetectedListener.onLowBeatDetectedOff();
        }
    }

    private void fireMidBeatDetectedEventOff() {
        if( onBeatDetectedListener != null ) {
            onBeatDetectedListener.onMidBeatDetectedOff();
        }
    }

    private void fireHighBeatDetectedEventOff() {
        if( onBeatDetectedListener != null ) {
            onBeatDetectedListener.onHighBeatDetectedOff();
        }
    }

    // Allows the user to set an Listener and react to the beat detected event
    public void setOnBeatDetectedListener(OnBeatDetectedListener listener) {
        onBeatDetectedListener = listener;
    }

    // Define our custom Listener interface
    public interface OnBeatDetectedListener {
        public abstract void onBeatDetectedOn(float intensity);
        public abstract void onBeatDetectedOff();
        public abstract void onLowBeatDetectedOn();
        public abstract void onMidBeatDetectedOn();
        public abstract void onHighBeatDetectedOn();
        public abstract void onLowBeatDetectedOff();
        public abstract void onMidBeatDetectedOff();
        public abstract void onHighBeatDetectedOff();
    }

	// ******************************************************************** //
	// Class Data.
	// ******************************************************************** //

    // Debugging tag.
	@SuppressWarnings("unused")
	private static final String TAG = "instrument";
	
	// Log of 2.
	private static final double LOG2 = Math.log(2);

    // Vertical range of the graph in bels.
    private static final float RANGE_BELS = 6f;


	// ******************************************************************** //
	// Private Data.
	// ******************************************************************** //
	
    // The Nyquist frequency -- the highest frequency
    // represented in the spectrum data we will be plotting.
    private int nyquistFreq = 0;

    // If true, draw a logarithmic frequency scale.  Otherwise linear.
    private static final boolean logFreqScale = false;

	// Display position and size within the parent view.
    private int dispX = 0;
    private int dispY = 0;
	private int dispWidth = 0;
	private int dispHeight = 0;
    
    // Label text size for the gauge.  Zero means not set yet.
    private float labelSize = 0f;

    // Layout parameters for the VU meter.  Position and size for the
    // bar itself; position and size for the bar labels; position
    // and size for the main readout text.
    private float spectGraphX = 0;
    private float spectGraphY = 0;
    private float spectGraphWidth = 0;
    private float spectGraphHeight = 0;
    private float spectLabY = 0;
    private float spectGraphMargin = 0;

    // Bitmap in which we draw the gauge background,
    // and the Canvas and Paint for drawing into it.
    private Bitmap bgBitmap = null;
    private Canvas bgCanvas = null;

    // Bitmap in which we draw the audio spectrum display,
    // and the Canvas and Paint for drawing into it.
    private Bitmap specBitmap = null;
    private Canvas specCanvas = null;

    // Buffer for calculating the draw colour from H,S,V values.
    private float[] paintColor = { 0, 1, 1 };

    // My Added Variables
    OnBeatDetectedListener onBeatDetectedListener = null;
    long mSystemTimeStartSec = 0;
    double mRunningSoundAvg = 0.0;
    int mNumberOfSamplesInOneSec = 0;
    long mInstantEnergy = 0;
    double mCurrentAvgEnergyOneSec = 0.0;
    long mTempMaxIntensity = 0;
    long mMaxInstantEnergy = 0;

    // For frequency analysis
    double mInstantFreqEnergy[] = {0.0, 0.0, 0.0};
    double mRunningFreqSoundAvg[] = {0.0, 0.0, 0.0};
    long mFreqInstantEnergy[] = {0, 0, 0};
    double mCurrentAvgFreqEnergyOneSec[] = {0.0, 0.0, 0.0};

    private static final double BEAT_THRESHOLD = 1.0;
    private static final int LOW_FREQUENCY_THRESHOLD = 250;
    private static final int MID_FREQUENCY_THRESHOLD = 2000;

    // Status of beats
    private boolean mIsBeatOn = false;
    private boolean mIsLowBeatOn = false;
    private boolean mIsMidBeatOn = false;
    private boolean mIsHighBeatOn = false;

    // BPM Calculation
    private int mBPMBeatFactor = 250;
    private boolean mBPMReady;
    private boolean mBPMFirstPass;
    private int mBPM6SecCounter;
    private long mBPMLastBeatTime[];
    private int  mBPMBeatCounter[];
    long mBPMRunningSoundAvg[];
    long mBPMCurrentEnergyOneSec[];
    private ArrayList<ArrayList> mBPMEntries = new ArrayList<ArrayList>();
}

