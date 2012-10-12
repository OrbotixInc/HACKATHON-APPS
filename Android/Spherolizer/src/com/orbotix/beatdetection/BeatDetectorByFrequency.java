/**
 * This class manages the link to the audio playing through the Android device.
 * It uses this link to capture and analyze Fast Fourier Transform data
 * Beats are detected by a change in sound energy in a certain frequency band that exceeds a running average
 * When a beat happens in a certain frequency band occurs, a listener is notified of it
 *
 * As of now the bands are as follows
 * Low (<250 Hz)
 * Mid (>250 Hz and <2.5kHz)
 * High (>2.5kHz)
 *
 * Created by Orbotix Inc.
 * Date: 6/9/12
 *
 * @author Michael DePhillips
 *
 */
package com.orbotix.beatdetection;

import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;

/**
 * {@link android.media.audiofx.Visualizer.OnDataCaptureListener#onWaveFormDataCapture } and
 * {@link android.media.audiofx.Visualizer.OnDataCaptureListener#onFftDataCapture }
 */
public class BeatDetectorByFrequency {
  private static final String TAG = "BeatDetectorByFrequency";

  private Visualizer mVisualizer = null;

  private double mRunningSoundAvg[];             // Adding the total sound energy in one second  (0low, 1mid, 2hi)
  private double mCurrentAvgEnergyOneSec[];      // Average Sound energy in one second (0low, 1mid, 2hi)
  private int mNumberOfSamplesInOneSec;          // Number of samples in one second
  private long mSystemTimeStartSec;              // System time at the start of a one second interval

  // Define the max value for a frequency band
  private static final int LOW_FREQUENCY = 300;
  private static final int MID_FREQUENCY = 2500;
  private static final int HIGH_FREQUENCY = 10000;

  // Beat Detector Listener for other classes to implement
  private OnBeatDetectedListener onBeatDetectedListener = null;

  public BeatDetectorByFrequency() {
     init();
  }

  private void init() {
    mRunningSoundAvg = new double[3];
    mCurrentAvgEnergyOneSec = new double[3];
    mCurrentAvgEnergyOneSec[0] = -1;
    mCurrentAvgEnergyOneSec[1] = -1;
    mCurrentAvgEnergyOneSec[2] = -1;
  }

  /**
   * Links the visualizer to a player
   * @param player - MediaPlayer instance to link to
   */
  public void link(MediaPlayer player)
  {
    if(player == null)
    {
      throw new NullPointerException("Cannot link to null MediaPlayer");
    }

    // Create the Visualizer object and attach it to our media player.
    mVisualizer = new Visualizer(player.getAudioSessionId());
    // [1] is the max capture size (phone dependent)
    mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

    // Pass through Visualizer data to this class
    Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener()
    {
      @Override
      public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
          int samplingRate)
      {
        // will never get called
      }

      @Override
      public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
          int samplingRate)
      {
        updateVisualizerFFT(bytes);
      }
    };

    // if you take the max capture rate and divide it by 2 you get the max sampling rate to retain
    // the correct frequencies per Nyquist Theory
    mVisualizer.setDataCaptureListener(captureListener,
        Visualizer.getMaxCaptureRate() / 2, false, true);

    // Enabled Visualizer and disable when we're done with the stream
    mVisualizer.setEnabled(true);
    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
    {
      @Override
      public void onCompletion(MediaPlayer mediaPlayer)
      {
        mVisualizer.setEnabled(false);
      }
    });

    // set start of 1 sec average energy equation
    mSystemTimeStartSec = System.currentTimeMillis();
  }

  /**
   * Call to release the resources used by BeatDetector. Like with the
   * MediaPlayer it is good practice to call this method
   */
  public void release()
  {
    if( mVisualizer != null ) {
        mVisualizer.setEnabled(false);
        mVisualizer.release();
    }
  }

  /**
   *  Makes sure the Visualizer class isn't still processing data when no music is playing
   */
  public void pause() {
      if( mVisualizer != null ) {
          mVisualizer.setEnabled(false);
      }
  }

  /**
   *  Makes sure the Visualizer class is processing data again since music is playing
   */
  public void resume() {
      if( mVisualizer != null ) {
          mVisualizer.setEnabled(true);
      }
  }

    /**
   * Update the audio data and test for a beat
   * {@link android.media.audiofx.Visualizer.OnDataCaptureListener#onWaveFormDataCapture }
   * @param audioBytes the sampled audio waveform
   */
  public void updateVisualizerFFT(byte[] audioBytes) {

    int energySum = 0;

    // The audioBytes of an FFT are returned as followed:
    // If Fs is the sampling frequency retuned by getSamplingRate() the kth frequency is: (k*Fs)/(n/2)
    // First Byte: DC Component (only real)
    energySum += Math.abs(audioBytes[0]);

    // Calculate the average instantaneous energy of the low frequency band
    int k = 2;
    double captureSize = mVisualizer.getCaptureSize()/2;
    int sampleRate = mVisualizer.getSamplingRate()/2000;
    double nextFrequency = ((k/2)*sampleRate)/(captureSize);
    // sum the low frequency band values
    while( nextFrequency < LOW_FREQUENCY ) {
        energySum += Math.sqrt( (audioBytes[k]*audioBytes[k]) * (audioBytes[k+1]*audioBytes[k+1]) );
        k+=2;
        nextFrequency = ((k/2)*sampleRate)/(captureSize);
    }

    double sampleAvgAudioEnergy = (double)energySum / (double)((k*1.0)/2.0);

    mRunningSoundAvg[0] += sampleAvgAudioEnergy;
    
    // Check for a low frequency band beat
    // A beat occurs when the average sound energy of a sample is greater than
    // the average sound energy of a one second part of a song
    // Also make sure the mCurrentAvgEnergy has been set, otherwise its -1 before its first pass
    if( (sampleAvgAudioEnergy >  mCurrentAvgEnergyOneSec[0]) && (mCurrentAvgEnergyOneSec[0] > 0) ) {
        // signal beat event to the listener
        fireBeatDetectedLowEvent();
    }
    
    // Loop through and calculate all mid frequency components
    energySum = 0;
    while( nextFrequency < MID_FREQUENCY ) {
        energySum += Math.sqrt( (audioBytes[k]*audioBytes[k]) * (audioBytes[k+1]*audioBytes[k+1]) );
        k+=2;
        nextFrequency = ((k/2)*sampleRate)/(captureSize);
    }
    
    sampleAvgAudioEnergy = (double)energySum / (double)((k*1.0)/2.0);
    mRunningSoundAvg[1] += sampleAvgAudioEnergy;
    
    // Check for a mid frequency band beat
    if( (sampleAvgAudioEnergy >  mCurrentAvgEnergyOneSec[1]) && (mCurrentAvgEnergyOneSec[1] > 0) ) {
        fireBeatDetectedMidEvent();
    }
    
    // Second Byte: Only imaginary part of the last frequency (include in highs)
    energySum = Math.abs(audioBytes[1]);
    
    while( (nextFrequency < HIGH_FREQUENCY) && (k < audioBytes.length) ) {
        energySum += Math.sqrt( (audioBytes[k]*audioBytes[k]) * (audioBytes[k+1]*audioBytes[k+1]) );
        k+=2;
        nextFrequency = ((k/2)*sampleRate)/(captureSize);
    }
    
    sampleAvgAudioEnergy = (double)energySum / (double)((k*1.0)/2.0);
    mRunningSoundAvg[2] += sampleAvgAudioEnergy;
    
    // Check for a high frequency band beat
    if( (sampleAvgAudioEnergy >  mCurrentAvgEnergyOneSec[2]) && (mCurrentAvgEnergyOneSec[2] > 0) ) {
        fireBeatDetectedHighEvent();
    }

    // Check for a beat
    // A beat occurs when the average sound energy of a sample is greater than
    // the average sound energy of a one second part of a song
//    if( (sampleAvgAudioEnergy >  mCurrentAvgEnergyOneSec) && (mCurrentAvgEnergyOneSec > 0) ) {
//        // signal beat event to the listener
//        fireBeatDetectedLowEvent();
//    }

    mNumberOfSamplesInOneSec++;
    // Check if one second has gone by
    if( (System.currentTimeMillis() - mSystemTimeStartSec) > 1000 ) {
        // Compute the average sound energy of a sample over a 1 second period
        mCurrentAvgEnergyOneSec[0] = mRunningSoundAvg[0] / mNumberOfSamplesInOneSec;
        mCurrentAvgEnergyOneSec[1] = mRunningSoundAvg[1] / mNumberOfSamplesInOneSec;
        mCurrentAvgEnergyOneSec[2] = mRunningSoundAvg[2] / mNumberOfSamplesInOneSec;

        // reset variables for next iteration
        mNumberOfSamplesInOneSec = 0;
        mRunningSoundAvg[0] = 0.0;
        mRunningSoundAvg[1] = 0.0;
        mRunningSoundAvg[2] = 0.0;
        mSystemTimeStartSec = System.currentTimeMillis();
    }
  }

  /**
   * Sends a beat detected low frequency range event to the activity that's listening to our class
   */
  private void fireBeatDetectedLowEvent() {
      if( onBeatDetectedListener != null ) {
          onBeatDetectedListener.onBeatDetectedLow();
      }
  }

  /**
   * Sends a beat detected mid frequency range event to the activity that's listening to our class
   */
  private void fireBeatDetectedMidEvent() {
      if( onBeatDetectedListener != null ) {
          onBeatDetectedListener.onBeatDetectedMid();
      }
  }

  /**
   * Sends a beat detected event high frequency range to the activity that's listening to our class
   */
  private void fireBeatDetectedHighEvent() {
      if( onBeatDetectedListener != null ) {
          onBeatDetectedListener.onBeatDetectedHigh();
      }
  }

    /**
     * Set the receiver of beat detection events
     * @param listener listener to receive beat detection events
     */
  public void setOnBeatDetectedListener(OnBeatDetectedListener listener) {
      onBeatDetectedListener = listener;
  }

    /**
     * Custom Beat Detection Listener interface
     */
  public interface OnBeatDetectedListener {
      public abstract void onBeatDetectedLow();
      public abstract void onBeatDetectedMid();
      public abstract void onBeatDetectedHigh();
  }
}