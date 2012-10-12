package com.orbotix.spherocam.preferences;

import android.content.SharedPreferences;

/**
 * A type that can be saved to the SharedPreferences
 *
 * Created by Orbotix Inc.
 * Author: Adam Williams
 * Date: 11/15/11
 * Time: 10:51 AM
 */
public interface PreferencesSavable {

    /**
     * Saves this PreferencesSavable to the provided SharedPreferences
     *
     * @param prefs
     */
    public void save(SharedPreferences prefs);
}
