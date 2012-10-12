package com.orbotix.spherocam.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import com.flurry.android.FlurryAgent;
import com.orbotix.spherocam.MainActivity;
import com.orbotix.spherocam.R;
import com.orbotix.spherocam.preferences.ControlPref;
import com.orbotix.spherocam.preferences.PreferencesManager;
import orbotix.robot.utilities.SoundManager;

/**
 * Created by Orbotix Inc.
 * User: Adam
 * Date: 11/11/11
 * Time: 4:36 PM
 */
public class SettingsActivity extends Activity {

    /**
     * ID for starting the ControlSettingsActivity for result
     */
    private final static int CONTROL_SETTINGS_ACTIVITY = 0;

    private ControlButtonView[] control_buttons = new ControlButtonView[3];
    private SeekBar volume_bar;
    private JoystickPositionView joystick_toggle;

    private PreferencesManager prefs;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.overridePendingTransition(R.anim.in_from_right, 0);

        this.setContentView(R.layout.settings_activity);

        this.volume_bar             = (SeekBar)this.findViewById(R.id.volume);
        this.joystick_toggle        = (JoystickPositionView)this.findViewById(R.id.joystick_position);

        this.prefs = new PreferencesManager(this);

        //Initialize speed buttons
        final int selected_speed_index = this.prefs.getSelectedDriveControlPref().index;
        this.control_buttons[0] = (ControlButtonView)this.findViewById(R.id.speed_cautious);
        this.control_buttons[1] = (ControlButtonView)this.findViewById(R.id.speed_normal);
        this.control_buttons[2] = (ControlButtonView)this.findViewById(R.id.speed_crazy);

        //Assign control button settings
        this.assignControlPrefs();

        this.selectSpeedButton(selected_speed_index);

        //Initialize volume
        float volume = this.prefs.getVolume();

        this.volume_bar.setMax(100);
        this.volume_bar.setProgress((int)(100 * volume));

        this.volume_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                final float v = (float)i/100.0f;
                SoundManager.setVolume(v);

                prefs.setVolume(v);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //nothing
            }
        });

        //Initialize Joystick Position Toggle
        final boolean joystick_left = this.prefs.getIsJoystickLeft();

        if(joystick_left){
            this.joystick_toggle.setChecked(true);
        }else{
            this.joystick_toggle.setChecked(false);
        }

        //Flurry
        FlurryAgent.onStartSession(this, "H62MI4Q9S6U1YECFVCNH");

        //Always report OK
        this.setResult(RESULT_OK);
    }

    @Override
    public void onActivityResult(int id, int result, Intent i){

        if(result == RESULT_OK){
            if(id == CONTROL_SETTINGS_ACTIVITY){
                this.assignControlPrefs();
            }
        }
    }



    @Override
    public void onStop(){
        super.onStop();
        FlurryAgent.onEndSession(this);


    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(0, R.anim.out_through_right);
    }


    private void selectSpeedButton(int index){

        for(ControlButtonView s : this.control_buttons){
            s.fade();
        }

        this.control_buttons[index].shine();

        this.prefs.setSelectedDriveControlPref(index);
    }

    public void onJoystickClick(View v){

        SoundManager.playSound(MainActivity.SOUND_BUTTON_PRESS);
        this.joystick_toggle.toggle();
        this.prefs.setJoystickLeft(this.joystick_toggle.isChecked());
    }

    /**
     * When the user clicks a ControlButtonView, set the drive control to that style
     * @param v
     */
    public void onControlButtonClick(View v){

        if(v instanceof ControlButtonView){
            ControlButtonView b = (ControlButtonView)v;

            b.checkForDoubleClick();
            
            this.selectSpeedButton(b.getDriveControlPref().index);

            SoundManager.playSound(MainActivity.SOUND_BUTTON_PRESS);
        }
    }

    /**
     * When the user long-clicks a ControlButtonView, select it, and then open the ControlSettingsActivity
     * @param v
     */
    public void onControlDoubleClick(View v){

        if(v instanceof ControlButtonView){

            ControlButtonView b = (ControlButtonView)v;

            Intent i = new Intent(this, ControlSettingsActivity.class);
            i.putExtra(ControlSettingsActivity.EXTRA_INDEX, b.getDriveControlPref().index);

            this.startActivityForResult(i, CONTROL_SETTINGS_ACTIVITY);

            overridePendingTransition(R.anim.flip_in, R.anim.flip_out);
        }
    }

    /**
     * When the user clicks "Roll", send an intent back to the MenuActivity, telling it to send the user
     * back to the MainActivity.
     *
     * @param v
     */
    public void onRollClick(View v){

        SoundManager.playSound(MainActivity.SOUND_BUTTON_PRESS);

        Intent i = new Intent();
        i.putExtra(MenuActivity.EXTRA_ROLL, true);
        this.setResult(RESULT_OK, i);
        this.finish();
        overridePendingTransition(0, R.anim.out_through_bottom);
    }

    private void assignControlPrefs(){

        //Double click listener
        ControlButtonView.OnDoubleClickListener listener = new ControlButtonView.OnDoubleClickListener() {
            @Override
            public void onDoubleClick(ControlButtonView button) {

                onControlDoubleClick(button);

            }
        };

        //Assign control button settings
        for(int i=0;i< this.control_buttons.length;i++){
            this.control_buttons[i].setDriveControlPref(new ControlPref(i, this.prefs));
            this.control_buttons[i].setOnDoubleClickListener(listener);
        }
    }


}
