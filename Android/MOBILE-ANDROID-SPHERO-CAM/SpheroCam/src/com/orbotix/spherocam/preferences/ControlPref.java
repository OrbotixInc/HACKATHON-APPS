package com.orbotix.spherocam.preferences;

import android.content.SharedPreferences;

/**
 * A model for a preferences for one of the three selectable drive control modes, stored in the SavedPreferences
 *
 * Created by Orbotix Inc.
 * Author: Adam Williams
 * Date: 11/15/11
 * Time: 9:41 AM
 */
public class ControlPref implements PreferencesSavable, PreferencesFillable, Cloneable {

    public final int index;
    public String name         = Default.COMFY.name;
    public float max_speed     = Default.COMFY.max_speed;
    public float rotation_rate = Default.COMFY.rotation_rate;

    public ControlPref(final int index, String name, float max_speed, float rotation_rate){
        this.index      = index;
        this.name       = name;
        this.max_speed  = max_speed;
        this.rotation_rate = rotation_rate;
    }

    /**
     * Creates an instance of ControlPref that has an index, but is otherwise identical to
     * the default COMFY theme.
     * @param index
     */
    public ControlPref(int index){
        this(index, null);
    }

    /**
     * Creates an instance of ControlPref that is loaded from the provided SharedPreferences
     *
     * @param index
     * @param prefs
     */
    public ControlPref(int index, PreferencesManager prefs){

        index = (index < 0)?0:index;
        index = (index >= Default.values().length)?Default.values().length-1:index;

        this.index = index;

        if(prefs != null){
            prefs.fill(this);
        }

    }

    /**
     * Copy constructor. Creates an identical instance to the provided ControlPref
     *
     * @param subject
     */
    public ControlPref(ControlPref subject){

        this.index         = subject.index;
        this.name          = subject.name;
        this.max_speed     = subject.max_speed;
        this.rotation_rate = subject.rotation_rate;
    }


    @Override
    public void save(SharedPreferences prefs){

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("control."+this.index+".name", this.name);
        editor.putFloat("control."+this.index+".max_speed", this.max_speed);
        editor.putFloat("control."+this.index+".rotation", this.rotation_rate);

        editor.commit();
    }

    @Override
    public void fill(SharedPreferences prefs){

        Default d = Default.values()[this.index];

        this.name = prefs.getString("control."+this.index+".name", d.name);
        this.max_speed = prefs.getFloat("control."+this.index+".max_speed", d.max_speed);
        this.rotation_rate = prefs.getFloat("control."+this.index+".rotation", d.rotation_rate);
    }

    /**
     * The default values for DriveControlPrefs
     */
    public enum Default {

        CAUTIOUS  ("Cautious", 0.6f, 0.7f),
        COMFY ("Comfy", 0.8f, 0.7f),
        CRAZY ("Crazy", 1f, 0.9f);

        public final String name;
        public final float max_speed;
        public final float rotation_rate;

        Default(String name, float max_speed, float rotation_rate){
            this.name = name;
            this.max_speed = max_speed;
            this.rotation_rate = rotation_rate;
        }
    }

    public ControlPref clone(){
        return new ControlPref(this);
    }
}
