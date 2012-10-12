package com.orbotix.spherocam.preferences;

import com.orbotix.spherocam.util.Dim;

/**
 * SavedPreferences values model for the camera
 *
 * Created by Orbotix Inc.
 * User: Adam
 * Date: 11/10/11
 * Time: 12:40 PM
 */
public class CameraPref {

    public final Dim preview_size = new Dim();
    public final Dim recording_size = new Dim();

    public CameraPref(Dim preview_size, Dim recording_size){
        this.preview_size.set(preview_size);
        this.recording_size.set(recording_size);
    }
}
