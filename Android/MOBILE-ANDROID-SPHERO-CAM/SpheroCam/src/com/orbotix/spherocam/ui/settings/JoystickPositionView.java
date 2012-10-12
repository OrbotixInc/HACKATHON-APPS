package com.orbotix.spherocam.ui.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.*;
import com.orbotix.spherocam.R;

/**
 * Created by Orbotix Inc.
 * Author: Adam Williams
 * Date: 11/16/11
 * Time: 10:58 AM
 */
public class JoystickPositionView extends RelativeLayout implements Checkable {

    private ImageView right_joystick_image;
    private ImageView left_joystick_image;
    private TextView  text_view;

    private Position position = Position.RIGHT;

    public JoystickPositionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Inflate settings list item layout xml
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.joystick_position_view, this);

        this.right_joystick_image = (ImageView)this.findViewById(R.id.right_position_image);
        this.left_joystick_image  = (ImageView)this.findViewById(R.id.left_position_image);
        this.text_view            = (TextView)this.findViewById(R.id.text);
    }

    /**
     * Sets this view to the left position
     */
    public void setToLeft(){
        this.position = Position.LEFT;

        this.right_joystick_image.setVisibility(INVISIBLE);
        this.left_joystick_image.setVisibility(VISIBLE);
        this.text_view.setText("LEFT");
    }

    /**
     * Sets this view to the right position
     */
    public void setToRight(){
        this.position = Position.RIGHT;
        
        this.right_joystick_image.setVisibility(VISIBLE);
        this.left_joystick_image.setVisibility(INVISIBLE);
        this.text_view.setText("RIGHT");
    }

    @Override
    public void setChecked(boolean b) {
        if(b){
            this.setToLeft();
        }else{
            this.setToRight();
        }
    }

    @Override
    public boolean isChecked() {
        return (this.position == Position.LEFT);
    }

    /**
     * Toggles the position of this view
     */
    public void toggle(){
        if(this.position == Position.LEFT){
            this.setToRight();
        }else {
            this.setToLeft();
        }
    }

    private enum Position {
        LEFT,
        RIGHT;
    }
}
