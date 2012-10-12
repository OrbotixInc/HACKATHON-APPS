/**
 * This class manages the link to the audio playing through the Android device.
 * It uses this link to capture and analyze the audio waveform at a certain frequency
 * Beats are detected by a change in sound energy that exceeds a running average
 * When a beat happens, a listener is notified of it
 *
 * Created by Orbotix Inc.
 * Date: 6/9/12
 *
 * @author Michael DePhillips
 *
 */
package com.orbotix.beatdetection;

import android.graphics.*;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;

/**
 * {@link android.media.audiofx.Visualizer.OnDataCaptureListener#onWaveFormDataCapture } and
 * {@link android.media.audiofx.Visualizer.OnDataCaptureListener#onFftDataCapture }
 */
public class BeatDetector {
  private static final String TAG = "BeatDetector";

  private byte[] mBytes;
  private byte[] mFFTBytes;
  private Visualizer mVisualizer = null;

  private double mRunningSoundAvg;
  private double mCurrentAvgEnergyOneSec;
  private int mNumberOfSamplesInOneSec;
  private long mSystemTimeStartSec;

  // Beat Detector Listener for other classes to implement
  private OnBeatDetectedListener onBeatDetectedListener = null;

  public BeatDetector() {
     init();
  }

  private void init() {
    mBytes = null;
    mFFTBytes = null;
    mRunningSoundAvg = 0.0;
    mCurrentAvgEnergyOneSec = -1.0;
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
        updateVisualizer(bytes);
      }

      @Override
      public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate)
      {
          //will never get called!
      }
    };

    // if you take the max capture rate and divide it by 2 you get the max sampling rate to retain
    // the correct frequencies per Nyquist Theory
    mVisualizer.setDataCaptureListener(captureListener,
        Visualizer.getMaxCaptureRate() / 2, true, false);

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
  public void updateVisualizer(byte[] audioBytes) {

    int energySum = 0;
    // Sum all the sound energy points in the sample
    for( int i = 0; i < audioBytes.length; i++) {
        energySum += audioBytes[i] * audioBytes[i];
    }

    double sampleAvgAudioEnergy = (double)energySum / (double)audioBytes.length;

    mRunningSoundAvg += sampleAvgAudioEnergy;
    mNumberOfSamplesInOneSec++;

    // Check for a beat
    // A beat occurs when the average sound energy of a sample is greater than
    // the average sound energy of a one second part of a song
    if( (sampleAvgAudioEnergy >  mCurrentAvgEnergyOneSec) && (mCurrentAvgEnergyOneSec > 0) ) {
        // signal beat event to the listener
        fireBeatDetectedEvent();
    }

    // Check if one second has gone by
    if( (System.currentTimeMillis() - mSystemTimeStartSec) > 1000 ) {
        // Compute the average sound energy of a sample over a 1 second period
        mCurrentAvgEnergyOneSec = mRunningSoundAvg / mNumberOfSamplesInOneSec;

        // reset variables for next iteration
        mNumberOfSamplesInOneSec = 0;
        mRunningSoundAvg = 0.0;
        mSystemTimeStartSec = System.currentTimeMillis();
    }

    mBytes = audioBytes;
  }

  /**
   * Sends a beat detected event to the activity that's listening to our class
   */
  private void fireBeatDetectedEvent() {
      if( onBeatDetectedListener != null ) {
          onBeatDetectedListener.onBeatDetected();
      }
  }

  // Allows the user to set an Listener and react to the beat detected event
  public void setOnBeatDetectedListener(OnBeatDetectedListener listener) {
      onBeatDetectedListener = listener;
  }

  // Define our custom Listener interface
  public interface OnBeatDetectedListener {
      public abstract void onBeatDetected();
  }
}