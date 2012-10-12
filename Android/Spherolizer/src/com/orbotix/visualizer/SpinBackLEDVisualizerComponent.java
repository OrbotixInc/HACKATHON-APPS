/**
 * This class implements the VisualizerEffect interface to tell Sphero spin in place with the Back LED on
 * and switch directions every beat
 *
 * Created by Orbotix Inc.
 * Date: 6/9/12
 *
 * @author Michael DePhillips
 *
 */
package com.orbotix.visualizer;

import android.os.Handler;
import orbotix.robot.base.FrontLEDOutputCommand;
import orbotix.robot.base.RawMotorCommand;
import orbotix.robot.base.Robot;

public class SpinBackLEDVisualizerComponent implements VisualizerEffect{

    private int leftMotorDirection;
    private int rightMotorDirection;

    /**
     * Default constructor, make sure stabilization is off!
     */
    public SpinBackLEDVisualizerComponent(Robot robot) {
        FrontLEDOutputCommand.sendCommand(robot, 1.0f);
        leftMotorDirection = RawMotorCommand.MOTOR_MODE_FORWARD;
        rightMotorDirection = RawMotorCommand.MOTOR_MODE_REVERSE;
        this.spin(robot);
    }

    private void spin(final Robot robot) {
        // set raw motor speed
        RawMotorCommand.sendCommand(robot, leftMotorDirection, 100, rightMotorDirection, 100);

        // Send delayed message on a handler to turn of color
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // turn of color and randomize if the randomize flag is set
                spin(robot);
            }
            // stall for 200 ms
        }, 200);
    }

    /**
     * This command will switch the directions that Sphero is spinning
     * @param robot the Sphero ball to control
     */
    @Override
    public void startEffect(final Robot robot) {
         if( leftMotorDirection == RawMotorCommand.MOTOR_MODE_FORWARD ) {
             leftMotorDirection = RawMotorCommand.MOTOR_MODE_REVERSE;
         }
         else {
             leftMotorDirection = RawMotorCommand.MOTOR_MODE_FORWARD;
         }

        if( rightMotorDirection == RawMotorCommand.MOTOR_MODE_FORWARD ) {
            rightMotorDirection = RawMotorCommand.MOTOR_MODE_REVERSE;
        }
        else {
            rightMotorDirection = RawMotorCommand.MOTOR_MODE_FORWARD;
        }
    }

    @Override
    public void stopEffect(final Robot robot) {}
}