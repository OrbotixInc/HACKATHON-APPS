/**
 * This class implements the VisualizerEffect interface to tell Sphero to run a raw motor command on a beat
 * So far this class sucks
 *
 * Created by Orbotix Inc.
 * Date: 6/9/12
 *
 * @author Michael DePhillips
 *
 */
package com.orbotix.visualizer;

import android.os.Handler;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.RawMotorCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.StabilizationCommand;

public class RawMotorVisualizerComponent implements VisualizerEffect{

    /**
     * Default constructor, make sure stabilization is off!
     */
    public RawMotorVisualizerComponent() {}

    @Override
    public void stopEffect(final Robot robot) { }

    /**
     * This command will turn on the Sphero RGB light for 200 ms (blink function)
     * @param robot the Sphero ball to control
     */
    @Override
    public void startEffect(final Robot robot) {

        // set raw motor speed
        RawMotorCommand.sendCommand(robot, RawMotorCommand.MOTOR_MODE_FORWARD, 255, RawMotorCommand.MOTOR_MODE_FORWARD, 255);

        // Send delayed message on a handler to turn of color
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // turn of color and randomize if the randomize flag is set
                RawMotorCommand.sendCommand(robot, RawMotorCommand.MOTOR_MODE_OFF, 0, RawMotorCommand.MOTOR_MODE_OFF, 0);
            }
            // stall for 200 ms
        }, 200);
    }
}