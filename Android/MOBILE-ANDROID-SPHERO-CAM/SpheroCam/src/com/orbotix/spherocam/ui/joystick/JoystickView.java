package com.orbotix.spherocam.ui.joystick;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.orbotix.spherocam.R;
import com.orbotix.spherocam.util.Index;
import com.orbotix.spherocam.util.Resolution;
import orbotix.robot.base.DriveControl;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RotationRateCommand;

/**
 * View that displays the Joystick, and handles the user's interactions with it.
 *
 * Created by Orbotix Inc.
 * User: Adam
 * Date: 10/27/11
 * Time: 2:38 PM
 */
public class JoystickView extends View {


    private final JoystickPuck puck;
    private final JoystickWheel wheel;

    private int puck_radius  = 25;
    private int wheel_radius = 75;

    private int puck_edge_overlap = 30;

    private final float scale;

    private final Index center_point = new Index();

    private final Matrix puck_translation = new Matrix();

    private boolean handleTouchEvents = true;
	private volatile boolean draggingPuck = false;
	private int		draggingPuckPointerId;

    private Robot robot = null;
    private DriveControl drive_control = DriveControl.INSTANCE;

    private float speed = 0.8f;
    private float rotation = 0.7f;

    private Runnable mOnStartRunnable;
    private Runnable mOnDragRunnable;
    private Runnable mOnEndRunnable;

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.scale = Resolution.getScale(context);

        this.puck = new JoystickPuck();
        this.wheel = new JoystickWheel();


        if(attrs != null){
            TypedArray a = this.getContext().obtainStyledAttributes(attrs, R.styleable.JoystickView);

            //Get puck size
            this.puck_radius = (int)a.getDimension(R.styleable.JoystickView_puck_radius, 25);

            //Get alpha
            this.setAlpha(a.getFloat(R.styleable.JoystickView_alpha, 255));

            //Get edge overlap
            this.puck_edge_overlap = a.getInteger(R.styleable.JoystickView_edge_overlap, 30);
        }
    }

    public void setAlpha(float alpha){
        alpha = (alpha > 1)? 1: alpha;
        alpha = (alpha < 0)? 0: alpha;

        alpha = (255 * alpha);

        this.puck.setAlpha((int)alpha);
        this.wheel.setAlpha((int)alpha);
    }

    public void setSpeed(float speed){
        this.speed = speed;
    }

    public void setRotation(float rotation){
        this.rotation = rotation;

        if(this.robot != null){
            RotationRateCommand.sendCommand(this.robot, rotation);
        }
    }
    
    /**
     * Sets the radius of the puck to the provided radius, in pixels
     * @param radius
     */
    public void setPuckRadius(int radius){
        this.puck_radius = radius;

        this.puck.setRadius(radius);
    }

    /**
     * Resets the puck's position to the middle of the wheel
     */
    public void resetPuck(){
        this.puck.setPosition(new Index(this.center_point));
    }

    public void setRobot(Robot robot){
        this.robot = robot;
    }

    public void setOnStartRunnable(Runnable runnable){
        mOnStartRunnable = runnable;
    }

    public void setOnDragRunnable(Runnable runnable){
        mOnDragRunnable = runnable;
    }

    public void setOnEndRunnable(Runnable runnable){
        mOnEndRunnable = runnable;
    }


    @Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {

        this.center_point.set(this.getMeasuredWidth() /2, this.getMeasuredHeight() / 2);

        if(this.getMeasuredWidth() > this.getMeasuredHeight()){
            this.wheel_radius = (this.getMeasuredWidth() / 2) - this.puck_edge_overlap - 2;
        }else{
            this.wheel_radius = (this.getMeasuredHeight()  / 2) - this.puck_edge_overlap - 2;
        }

        //Check that the puck and wheel are within reasonable limits
        this.wheel_radius = (this.wheel_radius < 3)?3:this.wheel_radius;
        this.puck_radius = (this.puck_radius < this.wheel_radius)? this.puck_radius : (this.wheel_radius / 3);

        this.wheel.setRadius(this.wheel_radius);
        this.setPuckRadius(this.puck_radius);

        this.wheel.setPosition(this.center_point);
        this.puck.setPosition(this.center_point);

        DriveControl.INSTANCE.setJoyStickPadSize(this.wheel.getBounds().width(), this.wheel.getBounds().height());
    }

    @Override
    public void onDraw(Canvas canvas){

        this.wheel.draw(canvas);
        this.puck.draw(canvas);

    }

    /**
     * Indicates whether the user is currently dragging the puck
     * @return True, if so
     */
    public boolean getIsDraggingPuck(){
        return draggingPuck;
    }

    private boolean hitPuck(MotionEvent event, int pointer_index){
        final Rect bounds = this.puck.getBounds();
        final int x = (int)event.getX(pointer_index);
        final int y = (int)event.getY(pointer_index);
        return bounds.contains(x, y);
    }

    /**
     * From a provided current_position Index, returns an Index that contains
     * coordinates that are within the area of the puck wheel.
     *
     * @param current_position
     * @return an Index containing the puck's position
     */
    private Index getValidPuckPosition(Index current_position){

        Index pointer = new Index(current_position);
        Index wheel_center = this.wheel.getPosition();
        Index adj_pointer = new Index(pointer);

        //Set the puck position to within the bounds of the wheel
        if(pointer.x != wheel_center.x || pointer.y != wheel_center.y){

            //reset the drive coords to be the zeroed pointer coords
            adj_pointer.set(pointer);

            //Use the wheel center to zero the pointer coords
            adj_pointer.x = adj_pointer.x - wheel_center.x;
            adj_pointer.y = adj_pointer.y - wheel_center.y;

            double a = Math.abs(adj_pointer.y);
            double b = Math.abs(adj_pointer.x);

            double hyp = Math.hypot(a, b);

            if(hyp > this.wheel_radius){
                final double factor = this.wheel_radius / hyp;

                pointer.x = (int)(adj_pointer.x * factor) + wheel_center.x;
                pointer.y = (int)(adj_pointer.y * factor) + wheel_center.y;
            }
        }

        return pointer;
    }

    /**
     * From a provided Index containing the puck's current position, returns an Index containing
     * a valid coordinate for use with the DriveControl's joystick area.
     *
     * @param current_position
     * @return an Index containing the clipped coordinates
     */
    private Index getDrivePuckPosition(Index current_position){

        Index drive_coord = new Index(current_position);
        Rect bounds = this.wheel.getBounds();

        drive_coord.x = drive_coord.x - bounds.left;
        drive_coord.y = drive_coord.y - bounds.top;

        if(drive_coord.x < 0){
            drive_coord.x = 0;
        }else if(drive_coord.x > bounds.width()){
            drive_coord.x = bounds.width();
        }

        if(drive_coord.y < 0){
            drive_coord.y = 0;
        }else if(drive_coord.y > bounds.height()){
            drive_coord.y = bounds.height();
        }

        return drive_coord;
    }

    @Override
	public boolean onTouchEvent(MotionEvent event) {

		if (!handleTouchEvents) return false;
        boolean handled = false;

        int pointer_index = event.getActionIndex();
        int pointer_id = event.getPointerId(pointer_index);
        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                if(this.robot != null && this.robot.isConnected()){
                    if (hitPuck(event, pointer_index)) {
                        draggingPuck = true;
                        draggingPuckPointerId = pointer_id;
                        handled = true;

                        this.drive_control.setSpeedScale(this.speed);
                        this.drive_control.startDriving(this.getContext(), DriveControl.JOY_STICK);

                    }

                    if(mOnStartRunnable != null){
                        mOnStartRunnable.run();
                    }
                }

                break;

		case MotionEvent.ACTION_MOVE:
			if (draggingPuck && draggingPuckPointerId == pointer_id) {

                Index pointer = new Index((int)event.getX(pointer_index), (int)event.getY(pointer_index));

                //Adjust drive coordinates for driving
                Index drive_coord = this.getDrivePuckPosition(pointer);
                this.drive_control.driveJoyStick(drive_coord.x, drive_coord.y);

                //Set the puck position to within the bounds of the wheel
                pointer.set(this.getValidPuckPosition(pointer));
                this.puck.setPosition(new Index(pointer.x, pointer.y));
                this.invalidate();

                if(mOnDragRunnable != null){
                    mOnDragRunnable.run();
                }

                handled = true;
            }
            break;

        case MotionEvent.ACTION_UP:
            if (draggingPuck && draggingPuckPointerId == pointer_id) {
                this.resetPuck();
                invalidate();

                draggingPuck = false;
                handled = true;

                this.drive_control.stopDriving();

                if(mOnEndRunnable != null){
                    mOnEndRunnable.run();
                }
            }
            break;

        default:
            break;
        }

		return handled;
	}
}
