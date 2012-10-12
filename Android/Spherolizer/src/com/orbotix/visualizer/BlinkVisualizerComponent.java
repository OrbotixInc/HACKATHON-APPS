/**
 * This class implements the VisualizerEffect interface to tell Sphero to blink when doCommand is called
 *
 * Created by Orbotix Inc.
 * Date: 6/9/12
 *
 * @author Michael DePhillips
 *
 */
package com.orbotix.visualizer;

import android.graphics.Color;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;

public class BlinkVisualizerComponent implements VisualizerEffect{

    // Amount of each color to send to Sphero when blinking
    private int mRed;
    private int mGreen;
    private int mBlue;

    private boolean mRandomizeColor;

    /**
     * Construct visualizer to blink a certain color
     * @param red red value (0-255)
     * @param green green value (0-255)
     * @param blue blue value (0-255)
     */
    public BlinkVisualizerComponent(int red, int green, int blue) {
        this.setColor(red, green, blue);
    }

    /**
     * Set the color to blink, any values outside the range will become the low or high
     * If you give this a value 0,0,0 it will do random colors
     * @param red red value (0-255)
     * @param green green value (0-255)
     * @param blue blue value (0-255)
     */
    public void setColor(int red, int green, int blue) {
        // Bound colors by 0 and 255
        mRed = Math.max( 0, Math.min( red, 255 ));
        mGreen = Math.max( 0, Math.min( green, 255 ));
        mBlue = Math.max( 0, Math.min( blue, 255 ));

        // If all values are 0, then randomize the color
        if( mRed == 0 && mGreen == 0 && mBlue == 0 ) {
            mRandomizeColor = true;
            this.randomizeColors();
        }
        else {
            mRandomizeColor = false;
        }
    }

    /**
     * Set the color to blink, any values outside the range will become the low or high
     * If you give this a value 0,0,0 it will do random colors
     * @param red red value (0-255)
     * @param green green value (0-255)
     * @param blue blue value (0-255)
     */
    public void setColor(int red, int green, int blue, float intensity) {
        // Bound colors by 0 and 255
        mRed = Math.max( 0, Math.min( red, 255 ));
        mGreen = Math.max( 0, Math.min( green, 255 ));
        mBlue = Math.max( 0, Math.min( blue, 255 ));

        // If all values are 0, then randomize the color
        if( mRed == 0 && mGreen == 0 && mBlue == 0 ) {
            mRandomizeColor = true;
            this.randomizeColors(intensity);
        }
        else {
            mRandomizeColor = false;
        }
    }

    /**
     * Returns current blinking of red
     * @return the current amount of red the the Visualizer effect is using
     */
    public int getRed() {
        return mRed;
    }

    /**
     * Returns current blinking of green
     * @return the current amount of green the the Visualizer effect is using
     */
    public int getGreen() {
        return mGreen;
    }

    /**
     * Returns current blinking of blue
     * @return the current amount of blue the the Visualizer effect is using
     */
    public int getBlue() {
        return mBlue;
    }

    /**
     * Calculate random color
     * @return a random color from (0-255)
     */
    private void randomizeColors() {
        mRed = (int)(Math.random() * 255);
        mGreen = (int)(Math.random() * 255);
        mBlue = (int)(Math.random() * 255);
    }

    /**
     * Calculate random color
     * @return a random color from (0-255)
     */
    private void randomizeColors(float intensity) {

        if( intensity > 0.8f ) {
            intensity = 1.0f;
            float[] hsv = {(float)(360*Math.random()),(float)Math.random(),intensity};
            int color = Color.HSVToColor(hsv);
            mRed = (int)((color >> 16)&0x000000FF);
            mGreen = (int)((color >> 8)&0x000000FF);
            mBlue = (int)(color&0x000000FF);
        }
        else {
            randomizeColors();
        }
    }

    /**
     * This command will turn on the Sphero RGB light for 200 ms (blink function)
     * @param robot the Sphero ball to control
     */
    @Override
    public void startEffect(final Robot robot) {
        // set color
        RGBLEDOutputCommand.sendCommand(robot, mRed, mGreen, mBlue);
    }

    /**
     * This command will turn on the Sphero RGB light for 200 ms (blink function)
     * @param robot the Sphero ball to control
     */
    @Override
    public void stopEffect(final Robot robot) {
        // set color
        RGBLEDOutputCommand.sendCommand(robot, 0, 0, 0);
        if( mRandomizeColor ) BlinkVisualizerComponent.this.randomizeColors();
    }
}