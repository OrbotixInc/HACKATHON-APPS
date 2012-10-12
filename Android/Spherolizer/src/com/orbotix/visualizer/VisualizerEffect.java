/**
 *  An interface that defines the shared method of all cool Sphero Visualizer effects
 *
 * Created by Orbotix Inc.
 * Date: 6/9/12
 *
 * @author Michael DePhillips
 *
 */
package com.orbotix.visualizer;

import android.app.Activity;
import orbotix.robot.base.Robot;

public interface VisualizerEffect {

    // Tells the Visualizer there has been a beat and it should do a command
    public void startEffect(final Robot mRobot);
    public void stopEffect(final Robot mRobot);
}