package com.orbotix.spherocam.ui.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.orbotix.spherocam.R;
import com.orbotix.spherocam.preferences.ControlPref;

/**
 * Created by Orbotix Inc.
 * User: Adam Williams
 * Date: 11/11/11
 * Time: 5:21 PM
 */
public class ControlButtonView extends RelativeLayout {

    private final ImageView image_view;
    private final TextView  text_view;
    private ControlPref control_pref;

    private OnDoubleClickListener mDoubleClickListener = null;

    private boolean mSingleClicked = false;

    /**
     * A listener for a double-click event for the ControlButtonView
     */
    public interface OnDoubleClickListener {

        /**
         * When the user double-clicks this ControlButtonView, this onDoubleClick method will
         * be executed.
         * @param button The double-clicked ControlButtonView
         */
        public void onDoubleClick(ControlButtonView button);
    }

    public ControlButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Inflate settings list item layout xml
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.speed_button_view, this);

        this.image_view = (ImageView)this.findViewById(R.id.image);
        this.text_view  = (TextView)this.findViewById(R.id.text);

        if(attrs != null){

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SpeedButtonView);

            if(a.hasValue(R.styleable.SpeedButtonView_image)){
                this.image_view.setImageDrawable(a.getDrawable(R.styleable.SpeedButtonView_image));
            }

            if(a.hasValue(R.styleable.SpeedButtonView_text)){
                this.text_view.setText(a.getString(R.styleable.SpeedButtonView_text));
            }

        }

        this.fade();
    }

    /**
     * If the user hasn't already clicked once, set a double click timer, else run the double click
     * listener.
     */
    public void checkForDoubleClick(){

        if(!mSingleClicked){

            mSingleClicked = true;

            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSingleClicked = false;
                }
            }, 1000);
        }else if(mDoubleClickListener != null){
            mDoubleClickListener.onDoubleClick(this);
            mSingleClicked = false;
        }
    }
    
    public void setDriveControlPref(ControlPref control_pref){
        this.control_pref = control_pref;

        this.text_view.setText(control_pref.name);
    }

    public ControlPref getDriveControlPref(){
        return this.control_pref;
    }

    /**
     * Sets a double click listener to run when the button is double-clicked.
     * @param listener
     */
    public void setOnDoubleClickListener(OnDoubleClickListener listener){
        mDoubleClickListener = listener;
    }

    /**
     * Make this view appear faded
     */
    public void fade(){

        this.text_view.setTextColor(0x55ffffff);
        this.image_view.setImageLevel(0);
        this.invalidate();
    }

    /**
     * Make this view not appear faded
     */
    public void shine(){

        this.text_view.setTextColor(0xffffffff);
        this.image_view.setImageLevel(1);
        this.invalidate();
    }
}
