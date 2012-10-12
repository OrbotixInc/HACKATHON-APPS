package com.orbotix.spherocam.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import com.orbotix.spherocam.preferences.ControlPref;
import com.orbotix.spherocam.preferences.PreferencesManager;
import com.orbotix.spherocam.R;

/**
 * Created by Orbotix Inc.
 * Author: Adam Williams
 * Date: 11/15/11
 * Time: 9:12 AM
 */
public class ControlSettingsActivity extends Activity {

    public static final String EXTRA_INDEX = "com.orbotix.spherocam.controlsettings.index";

    private EditText name_input;
    private SeekBar max_speed_bar;
    private SeekBar rotation_bar;
    private TextView max_speed_value;
    private TextView rotation_value;

    private ControlPref control_pref;
    private ControlPref original_pref;

    private PreferencesManager prefs;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.control_settings_activity);

        //Hold important views
        this.name_input      = (EditText)this.findViewById(R.id.name_input);
        this.max_speed_bar   = (SeekBar)this.findViewById(R.id.max_speed_input);
        this.rotation_bar    = (SeekBar)this.findViewById(R.id.rotation_input);
        this.max_speed_value = (TextView)this.findViewById(R.id.max_speed_value);
        this.rotation_value  = (TextView)this.findViewById(R.id.rotation_value);

        this.prefs = new PreferencesManager(this);

        //Get ControlPrefs
        Intent i = this.getIntent();
        final int control_pref_index = i.getIntExtra(EXTRA_INDEX, 1);
        this.control_pref = new ControlPref(control_pref_index, this.prefs);
        this.original_pref = this.control_pref.clone();

        //Set values for the views
        this.max_speed_bar.setProgress((int)(this.control_pref.max_speed * 100));
        this.setSeekBarTextValue(this.max_speed_bar, this.max_speed_value);
        this.rotation_bar.setProgress((int)(this.control_pref.rotation_rate * 100));
        this.setSeekBarTextValue(this.rotation_bar, this.rotation_value);
        this.name_input.setText(this.control_pref.name);

        //Set seek bar listeners
        this.max_speed_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setSeekBarTextValue(seekBar, max_speed_value);
                control_pref.max_speed = (float)i/100f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }
        });

        this.rotation_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setSeekBarTextValue(seekBar, rotation_value);
                control_pref.rotation_rate = (float)i/100f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }
        });
    }

    private void setSeekBarTextValue(SeekBar seek_bar, TextView value_view){

        final float val = (float)seek_bar.getProgress() / 100f;
        String val_string = String.valueOf(val).substring(0, 3);
        value_view.setText(val_string);
    }

    /**
     * When the user clicks "Reset", reset the values
     * @param v
     */
    public void onResetClick(View v){

        //Set values for the views
        this.max_speed_bar.setProgress((int)(this.original_pref.max_speed * 100));
        this.rotation_bar.setProgress((int)(this.original_pref.rotation_rate * 100));
        this.name_input.setText(this.original_pref.name);
    }

    /**
     * When the user clicks "Done", save the changes
     * @param v
     */
    public void onDoneClick(View v){

        this.control_pref.name = this.name_input.getText().toString();
        this.control_pref.max_speed = (float)this.max_speed_bar.getProgress() / 100f;
        this.control_pref.rotation_rate = (float)this.rotation_bar.getProgress() / 100f;
        this.prefs.save(this.control_pref);

        this.setResult(RESULT_OK);
        this.finish();
        overridePendingTransition(R.anim.flip_in, R.anim.flip_out);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.flip_in, R.anim.flip_out);
    }
}
