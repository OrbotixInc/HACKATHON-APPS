package com.orbotix;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.view.View;
import android.os.Handler;

import com.orbotix.beatdetection.BeatDetectorByFrequency;
import com.orbotix.visualizer.BlinkVisualizerComponent;
import com.orbotix.visualizer.DriveVisualizerComponent;
import com.orbotix.visualizer.SpinBackLEDVisualizerComponent;
import com.orbotix.visualizer.VisualizerEffect;
import orbotix.robot.base.*;
import org.hermit.android.instruments.SpectrumGauge;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity
{
    // The surface manager for the view.
    private InstrumentPanel mAudioInstrument = null;

    ArrayList<VisualizerEffect> mVisualizerEffects = null;  // Contains the cool effects engines to make Sphero dance
    ArrayList<VisualizerEffect> mVisualizerLowEffects = null;  // Contains the cool effects engines to make Sphero dance
    ArrayList<VisualizerEffect> mVisualizerMidEffects = null;  // Contains the cool effects engines to make Sphero dance
    ArrayList<VisualizerEffect> mVisualizerHighEffects = null;  // Contains the cool effects engines to make Sphero dance

    private ArrayList<Robot> mRobots = null;                    // Sphero!

    // Wake lock used to keep the screen alive.  Null if we aren't going
    // to take a lock; non-null indicates that the lock should be taken
    // while we're actually running.
    private PowerManager.WakeLock wakeLock = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Initialize the microphone
        mAudioInstrument = new InstrumentPanel(this);
        mAudioInstrument.setInstruments(InstrumentPanel.Instruments.SPECTRUM);

        // We want the audio controls to control our sound volume.
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Get our power manager for wake locks.
        if (wakeLock == null)
            wakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        if (!wakeLock.isHeld())
            wakeLock.acquire();

        setContentView(mAudioInstrument);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Take control of connected robots
        mRobots = new ArrayList<Robot>();
        List<Robot> robots = RobotProvider.getDefaultProvider().getControlledRobots();
        for(Robot r : robots){
            if(r.isConnected()){
                mRobots.add(r);
                //FrontLEDOutputCommand.sendCommand(r,1.0f);
            }
        }

        // Initialize audio resources
        MainActivity.this.initVisualizerEffects();

        // Start audio processing
        mAudioInstrument.setOnBeatDetectedListener(onBeatDetectedListener);
        mAudioInstrument.onStart();
    }

    @Override
    protected void onResume() {

        super.onResume();

        // Take the wake lock if we want it.
        if (wakeLock != null && !wakeLock.isHeld())
            wakeLock.acquire();

        mAudioInstrument.onResume();

        // Restart audio process
        mAudioInstrument.surfaceStart();
    }


    @Override
    protected void onPause() {

        super.onPause();

        // Pause audio analysis
        mAudioInstrument.onPause();

        // Let go the wake lock if we have it.
        if (wakeLock != null && wakeLock.isHeld())
            wakeLock.release();
    }

    @Override
    protected void onStop() {

        super.onStop();

        // Disconnect the robots
        for(Robot r: mRobots) {
            if( r != null ) {
                StabilizationCommand.sendCommand(r, true);
                FrontLEDOutputCommand.sendCommand(r, 0.0f);
                RollCommand.sendStop(r);
            }
            //Disconnect Robot
            //RobotProvider.getDefaultProvider().removeAllControls();
            RobotProvider.getDefaultProvider().disconnectControlledRobots();
        }

        // Stop audio analysis
        mAudioInstrument.onStop();
    }

    /**
     * Initialize the effects to be used when a beat is detected
     */
    private void initVisualizerEffects() {

        // Add one sphero effects
        mVisualizerEffects = new ArrayList<VisualizerEffect>();
        mVisualizerEffects.add(new BlinkVisualizerComponent(0, 0, 0));
      //  mVisualizerEffects.add(new SpinBackLEDVisualizerComponent(mRobots.get(0)));

    	// Add low frequency band effects
        mVisualizerLowEffects = new ArrayList<VisualizerEffect>();
        mVisualizerLowEffects.add(new BlinkVisualizerComponent(0, 0, 0));
       // mVisualizerLowEffects.add(new SpinBackLEDVisualizerComponent(mRobots.get(0)));
        //mVisualizerLowEffects.add(new DriveVisualizerComponent());

        // Add mid frequency band effects
        mVisualizerMidEffects = new ArrayList<VisualizerEffect>();
        mVisualizerMidEffects.add(new BlinkVisualizerComponent(0,0,0));
      //  mVisualizerMidEffects.add(new SpinBackLEDVisualizerComponent(mRobots.get(0)));
        //mVisualizerMidEffects.add(new DriveVisualizerComponent());
   //     mVisualizerMidEffects.add(new SpinBackLEDVisualizerComponent(mRobot));

        // Add high frequency band effects
        mVisualizerHighEffects = new ArrayList<VisualizerEffect>();
        mVisualizerHighEffects.add(new BlinkVisualizerComponent(0,0,0   ));
       // mVisualizerHighEffects.add(new SpinBackLEDVisualizerComponent(mRobots.get(0)));
        //mVisualizerHighEffects.add(new DriveVisualizerComponent());
        
        //StabilizationCommand.sendCommand(mRobot, false);
        //mVisualizerEffects.add(new SpinBackLEDVisualizerComponent(mRobot));
       // mVisualizerEffects.add(new RawMotorVisualizerComponent());
    }

    /*
     * Listener for when beats occur in the audio analyser
     */
    private SpectrumGauge.OnBeatDetectedListener onBeatDetectedListener = new SpectrumGauge.OnBeatDetectedListener() {

        @Override
        public void onBeatDetectedOn(float intensity) {
            for( VisualizerEffect effect: mVisualizerEffects ) {
                if( mRobots.size() > 0 ) {
                    ((BlinkVisualizerComponent)effect).setColor(0,0,0,intensity);
                    effect.startEffect(mRobots.get(0));
                }
            }
        }
        @Override
        public void onBeatDetectedOff() {
            for( VisualizerEffect effect: mVisualizerEffects ) {
                if( mRobots.size() > 0 ) {
                    effect.stopEffect(mRobots.get(0));
                }
            }
        }

        @Override
        public void onLowBeatDetectedOn() {
            for( VisualizerEffect effect: mVisualizerLowEffects ) {
                if( mRobots.size() > 1 ) {
                    effect.startEffect(mRobots.get(0));
                }
            }
        }

        @Override
        public void onLowBeatDetectedOff() {
            for( VisualizerEffect effect: mVisualizerLowEffects ) {
                if( mRobots.size() > 1 ) {
                    effect.stopEffect(mRobots.get(0));
                }
            }
        }

        @Override
        public void onMidBeatDetectedOn() {
            for( VisualizerEffect effect: mVisualizerMidEffects ) {
                if( mRobots.size() > 1 ) {
                    effect.startEffect(mRobots.get(1));
                }
            }
        }

        @Override
        public void onMidBeatDetectedOff() {
            for( VisualizerEffect effect: mVisualizerMidEffects ) {
                if( mRobots.size() > 1 ) {
                    effect.stopEffect(mRobots.get(1));
                }
            }
        }

        @Override
        public void onHighBeatDetectedOn() {
            for( VisualizerEffect effect: mVisualizerHighEffects ) {
                if( mRobots.size() > 2 ) {
                    effect.startEffect(mRobots.get(2));
                }
            }
        }

        @Override
        public void onHighBeatDetectedOff() {
            for( VisualizerEffect effect: mVisualizerHighEffects ) {
                if( mRobots.size() > 2 ) {
                    effect.stopEffect(mRobots.get(2));
                }
            }
        }

    };
}
