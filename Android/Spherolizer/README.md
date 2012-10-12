![logo](http://update.orbotix.com/developer/sphero-small.png)

# Spherolizer

The goal of the Spherolizer project is to have Sphero autonomously dance and change colors to music. Currently, the only effect that is properly implemented is flashing Sphero random colors to the beat of music.   

**What we already did:**

	1. Connect multiple Spheros
	2. Seperate the music into frequency bands using a Fast Fourier Transform
	3. Flash Spheros a random color when a beat happens in a certain frequency.

**What we want:**

	1. Tail LED turned on and spin Sphero to the beat
	2. Driving in shapes and change directions on significant beats
	3. Find the BPM of a song dynamically

This ReadMe will familiarize you with where you can change code for beat detection and how to add your own dance effects.  The code was hacked together for hack Friday, so it is in no way ideal or organized well.  It is okay, but this is your chance to make it great!  

## Beat Analysis Algorithm

When developing the program, I followed this tutorial on detecting beats [http://www.flipcode.com/misc/BeatDetectionAlgorithms.pdf](http://www.flipcode.com/misc/BeatDetectionAlgorithms.pdf)

I haven't implemented the dynamic threshold analysis.  As of now, a "beat" is just the point at which the current sound energy from the microphone is greater than the average sound energy over 2 seconds of microphone data.  It is not a window, but computed every 2 seconds.  It would help to do a standard deviation on the data and found a threshold that is accurate with every genre of music.  

If you want to change the beat detection code, it is located in the `SpectrumGauge.java` file in the package, `org.hermit.android.instruments`  The code is below:

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
        
Check the function for all the code, but the first section computes the average sound energy every 2 seconds for the microphone data and the frequency data.  The second part of the code, checks to see if the instantaneous sound energy computed from the microphone in real-time is greater than the average.  If it is, it fires a beat detected on listener event.  If it was previously on, and turned off, it fires a beat detected off listener event.  

## Beat Detected Listener

The Main Activity simply needs to set up the Beat Detected Listener and the "instrument" that listens to the music.  I took the sample from the Audalyzer sample code, so that is why they are called instruments.  First you must initalize the mSpectrumGauge Instrument, because it controls the beat analysis algorithm.  The code is as follows:

        // Initialize the microphone
        mAudioInstrument = new InstrumentPanel(this);
        mAudioInstrument.setInstruments(InstrumentPanel.Instruments.SPECTRUM);

        // We want the audio controls to control our sound volume.
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
And then you can set the listener in the `onStart()` method

        // Start audio processing
        mAudioInstrument.setOnBeatDetectedListener(onBeatDetectedListener);
        mAudioInstrument.onStart();

The Beat Detected Listener looks like this.  This is where you can tell the Spheros what to do when a certain beat is detected on the microphone data as a whole, or in the low, mid, or high frequency ranges.  

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

In this implementation, low is less than 200 Hz, mids are from 200-2000Hz, and highs are greater than 2000Hz.

## Adding Visualizer Effects

I have created multiple visualizer effects, but none of them werer really that great except for the `BlinkVisualizerComponent`  So when the application starts, I have all the visualizer effects add that component to blink Sphero to the music.  To add visualizer effects, you do this code.

        // Add one sphero effects
        mVisualizerEffects = new ArrayList<VisualizerEffect>();
        mVisualizerEffects.add(new BlinkVisualizerComponent(0, 0, 0));
        
When the On Beat Detected Listener fires an event, you can tell the visualizer effect to do its thing with this code:

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

## Conclusion

This all I have had the time to do.  But, I think with a more centralized algorithm, and creativity for what makes Sphero break it down with sick dance moves, this app could be incredible.  

If you have any questions, use the forum, or email me at michael@orbotix.com

## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)

	 




