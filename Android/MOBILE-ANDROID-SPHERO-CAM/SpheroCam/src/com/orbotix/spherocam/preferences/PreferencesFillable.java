package com.orbotix.spherocam.preferences;

import android.content.SharedPreferences;

/**
 * A type that can be filled from the SharedPreferences
 *
 * Created by Orbotix Inc.
 * Author: Adam Williams
 * Date: 11/15/11
 * Time: 10:53 AM
 */
public interface PreferencesFillable {

    /**
     * Loads this PreferencesFillable from the provided SharedPreferences.
     *
     * @param prefs
     * @return
     */
    public void fill(SharedPreferences prefs);
}
