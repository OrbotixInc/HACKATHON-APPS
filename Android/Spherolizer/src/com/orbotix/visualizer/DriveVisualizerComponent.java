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

import orbotix.macro.Delay;
import orbotix.macro.MacroObject;
import orbotix.macro.RotateOverTime;
import orbotix.macro.RotateOverTimeSD1;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RollCommand;

public class DriveVisualizerComponent implements VisualizerEffect{

    // Amount of each color to send to Sphero when blinking
    private MacroObject turn90Macro;

    /**
     * Construct visualizer to make a 90 degree turn
     */
    public DriveVisualizerComponent() {
        turn90Macro = new MacroObject();
        turn90Macro.addCommand(new RotateOverTime(90, 200));
        turn90Macro.addCommand(new Delay(200));
        turn90Macro.setMode(MacroObject.MacroObjectMode.Normal);
    }

    /**
     * This command will turn on the Sphero RGB light for 200 ms (blink function)
     * @param robot the Sphero ball to control
     */
    @Override
    public void startEffect(final Robot robot) {
        // set color
        RollCommand.sendCommand(robot, 0.0f, 0.5f);
        turn90Macro.setRobot(robot);
        turn90Macro.playMacro();
    }

    /**
     * This command will turn on the Sphero RGB light for 200 ms (blink function)
     * @param robot the Sphero ball to control
     */
    @Override
    public void stopEffect(final Robot robot) {
        // do something?
    }
}