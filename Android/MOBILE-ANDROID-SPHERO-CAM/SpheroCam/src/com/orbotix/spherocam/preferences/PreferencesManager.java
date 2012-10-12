package com.orbotix.spherocam.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import com.orbotix.spherocam.util.Dim;

/**
 * Created by Orbotix Inc.
 * User: Adam
 * Date: 11/9/11
 * Time: 10:11 AM
 */
public class PreferencesManager {

    public final static String PREFS_ID = "com.orbotix.spherocam.preferences";

    private final SharedPreferences prefs;

    public PreferencesManager(Context context){
        this.prefs = context.getSharedPreferences(PREFS_ID, Context.MODE_PRIVATE);
    }

    /**
     * Saves the provided Color object to the SharedPreferences
     * @param color
     */
    public void setColor(ColorPref color){

        SharedPreferences.Editor editor = this.prefs.edit();

        editor.putInt("color.red", color.red);
        editor.putInt("color.blue", color.blue);
        editor.putInt("color.green", color.green);

        editor.commit();

    }

    /**
     * Get the saved Color from the SharedPreferences
     * @return a Color object containing the rgb values of the color
     */
    public ColorPref getColor(){

        final ColorPref color = new ColorPref(255, 255, 255);

        if(!prefs.contains("color.red")){
            setColor(color);
        }else{
            color.red = this.prefs.getInt("color.red", 0);
            color.green = this.prefs.getInt("color.green", 0);
            color.blue  = this.prefs.getInt("color.blue", 0);
        }
        return color;
    }


    /**
     * Saves the provided CameraPref object to the SharedPreferences
     *
     * @param cam_pref
     */
    public void setCameraPref(CameraPref cam_pref){

        //If the recording size isn't set, set it to the preview size
        if(cam_pref.recording_size.w == 0){
            cam_pref.recording_size.w = cam_pref.preview_size.w;
        }

        if(cam_pref.recording_size.h == 0){
            cam_pref.recording_size.h = cam_pref.preview_size.h;
        }

        SharedPreferences.Editor editor = this.prefs.edit();

        editor.putBoolean("camera.has_pref", true);

        editor.putInt("camera.preview_size.width", cam_pref.preview_size.w);
        editor.putInt("camera.preview_size.height", cam_pref.preview_size.h);
        editor.putInt("camera.recording_size.width", cam_pref.recording_size.w);
        editor.putInt("camera.recording_size.height", cam_pref.recording_size.h);

        editor.commit();
    }

    public void clearCameraPref(){

        SharedPreferences.Editor editor = this.prefs.edit();

        editor.putBoolean("camera.has_pref", false);

        editor.putInt("camera.preview_size.width", 0);
        editor.putInt("camera.preview_size.height", 0);
        editor.putInt("camera.recording_size.width", 0);
        editor.putInt("camera.recording_size.height", 0);

        editor.commit();
    }

    /**
     * Indicates whether a CameraPref has yet been saved to the SharedPrefs
     *
     * @return True, if so
     */
    public boolean getHasCameraPref(){
        return this.prefs.getBoolean("camera.has_pref", false);
    }

    /**
     * Gets the currently saved CameraPref from the SharedPreferences, or gets a CameraPref with all 0's for values
     *
     * @return a CameraPref object containing the saved CameraPref values
     */
    public CameraPref getCameraPref(){

        return new CameraPref(
                new Dim(
                        this.prefs.getInt("camera.preview_size.width", 0),
                        this.prefs.getInt("camera.preview_size.height", 0)
                ), new Dim(
                        this.prefs.getInt("camera.recording_size.width", 0),
                        this.prefs.getInt("camera.recording_size.height", 0)
                )
            );
    }

    public void setVolume(float volume){

        volume = (volume > 1f)?1f:volume;

        SharedPreferences.Editor editor = this.prefs.edit();

        editor.putFloat("volume", volume);

        editor.commit();
    }

    public float getVolume(){

        final float volume;
        if(this.prefs.contains("volume")){
            volume = this.prefs.getFloat("volume", 0.5f);
        }else{
            volume = 0.5f;
            setVolume(volume);
        }

        return volume;
    }

    /**
     * Sets whether the joystick is positioned to the left in the SavedPreferences
     * @param val True, if it is to the left
     */
    public void setJoystickLeft(boolean val){

        SharedPreferences.Editor editor = this.prefs.edit();

        editor.putBoolean("joystick.position.left", val);

        editor.commit();
    }

    /**
     * Indicates whether the joystick has been set to be on the left in the SavedPreferences
     * @return True, if so
     */
    public boolean getIsJoystickLeft(){
        return this.prefs.getBoolean("joystick.position.left", false);
    }

    /**
     * Saves the provided PreferencesSavable to the SharedPreferences
     * @param savable
     */
    public void save(PreferencesSavable savable){
        savable.save(this.prefs);
    }

    /**
     * Fills the provided PreferencesFillable from the SharedPreferences
     * @param fillable
     */
    public void fill(PreferencesFillable fillable){
        fillable.fill(this.prefs);
    }

    /**
     * Sets the index of the currently selected ControlPref.
     * @param index
     */
    public void setSelectedDriveControlPref(int index){

        SharedPreferences.Editor editor = this.prefs.edit();

        editor.putInt("control.selected", index);

        editor.commit();
    }

    /**
     * Gets the currently selected ControlPref
     *
     * @return
     */
    public ControlPref getSelectedDriveControlPref(){
        ControlPref control_pref = null;

        int index = 1;
        if(!this.prefs.contains("control.selected")){
            this.setSelectedDriveControlPref(index);
        }else{
            index = this.prefs.getInt("control.selected", index);
        }

        control_pref = new ControlPref(index, this);

        return control_pref;
    }
}
