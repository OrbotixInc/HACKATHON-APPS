package com.orbotix.spherocam.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.orbotix.spherocam.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Orbotix Inc.
 * @author Adam Williams
 * Date: 11/14/11
 * Time: 9:08 AM
 */
public class FileTools {

    /**
     * Given the provided filename and type, gets a new file inside the external storage media folder for storing
     * media.
     *
     * @param context the Android Context
     * @param filename a filename
     * @param type a type, from the Environment class. eg - Environment.DIRECTORY_PICTURES
     * @return
     */
    public static File getExternalStorageMediaFile(Context context, String filename, String type){

        File path = null;

        File file = null;

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){

            Log.d(MainActivity.TAG, "External storage available. Fetching external storage path.");

            path = new File(
                 Environment.getExternalStoragePublicDirectory(type),
                 "SpheroCam"
            );

            if(!path.exists()){
                if(!path.mkdirs()){
                    Log.e(MainActivity.TAG, "Couldn't create directory for saving media.");
                }
            }

            //Create the file if the path can be found or created
            if(path.exists()){
                FileOutputStream os = null;

                file = new File(path.getPath()+File.separator+filename);
            }

        }else{
            Toast toast = Toast.makeText(context, "No external storage/SD card detected. " +
                    "Please install an SD card to save pictures or video.", Toast.LENGTH_SHORT);
            toast.show();

            Log.d(MainActivity.TAG, "External storage not available.");
        }

        return file;
    }

    /**
     *
     * @return a filename, without the file extension, that is made of the application tag and a timestamp
     */
    public static String getTimestampedFileName(){

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return MainActivity.TAG+"-"+timestamp;
    }
}
