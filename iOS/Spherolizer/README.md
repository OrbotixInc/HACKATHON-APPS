![logo](http://update.orbotix.com/developer/sphero-small.png)

# Spherolizer

The goal of the Spherolizer project is to have Sphero autonomously dance and change colors to music. Currently, the only effect that is properly implemented is flashing Sphero random colors to the beat of music.   

**What we already did:**

	1. Flash Spheros a random color when a beat happens on the microphone data

**What we want:**

	1. Tail LED turned on and spin Sphero to the beat
	2. Driving in shapes and change directions on significant beats
	3. Find the BPM of a song dynamically
	4. A UI to have the user able to choose from settings like, which dance moves to do, or sensitivity of beat detection
	5. Seperate the frequency energies and change the beat detection algorithm to use that like the Android version

This ReadMe will familiarize you with where you can change code for beat detection and how to add your own dance effects.  The code was hacked together for hack Friday, so it is in no way ideal or organized well.  It is okay, but this is your chance to make it great!  

## Beat Analysis Algorithm

When developing the program, I followed this tutorial on detecting beats [http://www.flipcode.com/misc/BeatDetectionAlgorithms.pdf](http://www.flipcode.com/misc/BeatDetectionAlgorithms.pdf)

I haven't implemented the dynamic threshold analysis or frequency analysis yet on iOS.  As of now, a "beat" is just the point at which the current sound energy from the microphone is greater than the average sound energy over 2 seconds of microphone data.  It is not a window, but computed every 2 seconds.  It would help to do a standard deviation on the data and found a threshold that is accurate with every genre of music.  

If you want to change the beat detection code, it is located in the `aurioTouchAppDelegate.mm` file in the package.  The code is below:

    -(void) analyzeBeatWithIOData:(AudioBufferList*)data Frames:(UInt32)inNumberFrames {
        uint32_t instantEnergy = 0;
        // try beat detection!
        SInt32* micData = (SInt32*)(data->mBuffers[0].mData);
        for (UInt32 i=0; i < inNumberFrames; i++)
        {
            instantEnergy += abs(micData[i])/1000;
        }
        // Update max sound
        if( instantEnergy > mRunningMaxSound ) mRunningMaxSound = instantEnergy;
        // NSLog(@"Instant: %d", instantEnergy);
        mRunningSoundAvg += instantEnergy;
        mNumberOfSamples++;
        
        // Keep track of the variance
        runningVariance += instantEnergy - mAvgSoundEnergy;
        
        // if a second passed, compute avg energy
        NSTimeInterval timeInterval = [mSecondStart timeIntervalSinceNow];
        if( timeInterval < -2.0 ) {
            averageVariance = runningVariance / mNumberOfSamples;
            mAvgSoundEnergy = mRunningSoundAvg / mNumberOfSamples;
            mMaxSound = mRunningMaxSound;
            mRunningMaxSound = 0;
            NSLog(@"AVERAGE: %d", mAvgSoundEnergy);
            mRunningSoundAvg = 0;
            mNumberOfSamples = 0;
            mSecondStart = [NSDate date];
        }
        
        if( instantEnergy > mAvgSoundEnergy ) {
            NSLog(@"Beat Start");
            // beat detected! so blink ball :)
            if( !beatIsOn ) {
                HSV color;
                color.h = arc4random() % 360;
                color.s = ((double)arc4random() / ARC4RANDOM_MAX);
                float intensity = instantEnergy / mMaxSound;
                if( intensity > 0.8f ) {
                    color.v = 1.0;
                }
                else {
                    color.v = ((double)arc4random() / ARC4RANDOM_MAX);
                }
                RGB colorOutput = [self RGBfromHSV:color];
                [RKRGBLEDOutputCommand sendCommandWithRed:colorOutput.r
                                                    green:colorOutput.g
                                                     blue:colorOutput.b];
                beatIsOn = YES;
                lastIntensity = intensity;
            }
        }
        else {
            if( beatIsOn ) {
                beatIsOn = NO;
                [RKRGBLEDOutputCommand sendCommandWithRed:0.0 green:0.0 blue:0.0];
            }
        }
    }
        
The first section computes the average sound energy every 2 seconds for the microphone data and the frequency data.  The second part of the code, checks to see if the instantaneous sound energy computed from the microphone in real-time is greater than the average.  If it is, then it turns Sphero a random color.  If it was previously on, and turned off, it makes LED have no color.

## Conclusion

This all I have had the time to do.  But, I think with a more advanced algorithm, and creativity for what makes Sphero break it down with sick dance moves, this app could be incredible.  

If you have any questions, use the forum, or email me at michael@orbotix.com

## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)

	 




