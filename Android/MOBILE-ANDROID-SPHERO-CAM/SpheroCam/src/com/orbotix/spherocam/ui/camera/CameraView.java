package com.orbotix.spherocam.ui.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.orbotix.spherocam.MainActivity;
import com.orbotix.spherocam.R;
import com.orbotix.spherocam.preferences.CameraPref;
import com.orbotix.spherocam.preferences.PreferencesManager;
import com.orbotix.spherocam.util.Dim;
import com.orbotix.spherocam.util.FileTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * A SurfaceView that shows a Camera preview, takes pictures, and saves them to a file in storage.
 *
 * Created by Orbotix Inc.
 * Author: Adam Williams
 * Date: 11/1/11
 * Time: 4:39 PM
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private Camera camera = null;
    private final PreferencesManager prefs;
    
    private final static int sTimeClickInterval = 2000;
    private long mLastPhotoTime = 0;

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.getHolder().addCallback(this);

        this.prefs = new PreferencesManager(context);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        
        try{
            this.camera = Camera.open();
        }catch(final RuntimeException e){

            //Log error
            Log.e(MainActivity.TAG, "Failed to connect to cam service on surfaceCreated.", e);

            //Crash app runnable
            final Runnable crash_runnable = new Runnable() {
                @Override
                public void run() {

                    Context c = getContext();
                    if (c instanceof Activity){

                        Log.e(MainActivity.TAG, "Failed to connect to camera service. Exiting app.");
                        ((Activity)c).finish();
                    }else{
                        throw new RuntimeException(e);
                    }
                }
            };

            //Show failure alert
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.CameraConnectFailedTitle)
                    .setMessage(R.string.CameraConnectFailedMessage)
                    .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {

                        //Crash app
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            crash_runnable.run();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {

                        //Crash app
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            
                            crash_runnable.run();
                        }
                    })
                    .show();
            
            
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
        //initialize cam
        if(this.camera != null){
            initializeCamera(surfaceHolder, w, h);
            //start cam preview
            this.camera.startPreview();
        }
        
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        try{
            closeCamera();
        }catch(NullPointerException e){
            
            Log.e(MainActivity.TAG, "Failed to close camera. Null Pointer. Probably caused by runtime exception opening camera.", e);
        }
    }

    /**
     * Shows this View
     */
    public void show(){
        this.setVisibility(VISIBLE);
    }

    /**
     * Hides this View
     */
    public void hide(){
        this.setVisibility(INVISIBLE);
    }

    /**
     * Takes a picture of the content on the CameraView
     */
    public void takePicture(){
        
        if(this.camera != null){

            final long time = System.currentTimeMillis();
            
            if(time > mLastPhotoTime +sTimeClickInterval){
                Camera.ShutterCallback shutter_callback = new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {
                        //TODO: sound of shutter, etc.
                    }
                };

                Camera.PictureCallback jpeg_callback = new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, final Camera camera) {

                        String name = FileTools.getTimestampedFileName()+".jpg";

                        saveBytesToFile(bytes, name, Environment.DIRECTORY_PICTURES);

                        post(new Runnable() {
                            @Override
                            public void run() {
                                camera.startPreview();
                            }
                        });
                    }
                };

                //Take the picture
                try{
                    this.camera.takePicture(shutter_callback, null, jpeg_callback);
                }catch (RuntimeException e){
                    Log.e(MainActivity.TAG, "Take picture failed. The user likely clicked twice very fast.");
                }

                mLastPhotoTime = time;
            }
        }
    }

    /**
     * Closes an open camera, if there is one.
     */
    public void closeCamera(){


        if(this.camera != null){
            this.camera.stopPreview();
            this.camera.release();
            this.camera = null;
        }
    }

    /**
     * Initialize the camera for the provided SurfaceHolder and the provided width and height
     *
     * @param holder
     * @param w
     * @param h
     */
    private void initializeCamera(SurfaceHolder holder, int w, int h){

        //Set the preview holder.
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            camera.release();
            camera = null;
            throw new RuntimeException("Couldn't set the camera preview holder. ", e);
        }

        //Set the preview size
        Camera.Parameters camera_params = camera.getParameters();

        Dim s = new Dim();
        if(this.prefs.getHasCameraPref()){

            //Use the one from the prefs
            CameraPref cam_pref = this.prefs.getCameraPref();

            camera_params.setPreviewSize(cam_pref.preview_size.w, cam_pref.preview_size.h);

            try{
                this.camera.setParameters(camera_params);
            }catch (RuntimeException e){

                //If it failed, clear prefs and try to set it as though there were no saved size.
                this.prefs.clearCameraPref();
                Camera.Size size = this.setOptimalPreviewSize(camera_params.getSupportedPreviewSizes(), w, h);
                s.set(size.width, size.height);

                //Record the discovered size, to save time in the future.
                cam_pref = new CameraPref(s, s);
                this.prefs.setCameraPref(cam_pref);
            }
        }else {

            //select nearest supported size

            Camera.Size size = this.setOptimalPreviewSize(camera_params.getSupportedPreviewSizes(), w, h);
            s.set(size.width, size.height);

            //Record the discovered size, to save time in the future.
            CameraPref cam_pref = new CameraPref(s, s);
            this.prefs.setCameraPref(cam_pref);
        }
    }

    /**
     * Sets the best preview size to the camera, based on the provided width and height
     *
     * @param w
     * @param h
     * @return a Dim containing the size it set to the camera
     */
    private Camera.Size setOptimalPreviewSize(List<Camera.Size> sizes, int w, int h){

        Camera.Parameters camera_params = camera.getParameters();

        //Go through all the sizes until one is found, or until all the sizes are exhausted
        Camera.Size size = null;
        while(!sizes.isEmpty() && size == null){

            size = getOptimalPreviewSize(sizes, w, h, true, true);

            if(size != null){
                camera_params.setPreviewSize(size.width, size.height);

                try{
                    this.camera.setParameters(camera_params);
                }catch (RuntimeException e){

                    //If it doesn't accept this size, try again, eliminating this size from the list.
                    Log.e(MainActivity.TAG, "Failed to set preview size: "+size.width+"x"+size.height+". Trying again.");
                    sizes.remove(size);
                    size = null;
                }
            }
        }

        if(size != null){
            Log.d(MainActivity.TAG, "Using camera preview size "+size.width+"x"+size.height);

        }else{
            //TODO: make a list of "safer" sizes that might work as last resorts and try those

            //Try 176x144.
            Log.e(MainActivity.TAG, "Couldn't find camera preview size. Setting to 176x144 as resort.");

            final Dim ret = new Dim(176, 144);

            camera_params.setPreviewSize(ret.w, ret.h);

            try{
                
                this.camera.setParameters(camera_params);
            }catch (RuntimeException e){

                Log.e(MainActivity.TAG, "Couldn't set any preview size. Didn't set a preview size.");
                throw new RuntimeException("Couldn't set a camera preview size.", e);
            }
        }

        return size;
    }

    /**
     * Attempts to find a preview size from a given List of Sizes that matches the aspect
     * ratio of the provided width and height.
     *
     * @param sizes a List of Camera.Size to search through
     * @param w the width of the surface
     * @param h the height of the surface
     * @return A Camera.Size object that is the best fit
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h){
        return this.getOptimalPreviewSize(sizes, w, h, true, true);
    }

    /**
     * Attempts to find a preview size from a given List of Sizes that matches the aspect
     * ratio of the provided width and height.
     *
     * @param sizes a List of Camera.Size to search through
     * @param w the width of the surface
     * @param h the height of the surface
     * @param check_aspect set this to true to evaluate by aspect ratio
     * @param check_size set this to true to evaluate by size ratio
     * @return A Camera.Size object that is the best fit
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h, boolean check_aspect, boolean check_size){

        if(sizes == null){
            return null;
        }

        final double aspect_tolerance = 0.1d;
        final double size_tolerance   = 0.25d;

        double target_ratio = (double) w/h;

        Camera.Size ret = null;

        double min_diff = Double.MAX_VALUE;

        int target_height = h;

        //Try to find best fit
        for(Camera.Size size : sizes){

            //Check aspect ratio
            if(check_aspect){
                double ratio = (double) size.width / size.height;
                if(Math.abs(ratio - target_ratio) > aspect_tolerance){
                    continue;
                }
            }


            //Check size ratio
            if(check_size){
                final double surface_area = (w*h);
                final double size_area    = (size.width * size.height);
                final double pre_ratio    = (surface_area / size_area);
                final double size_ratio = Math.abs(1 - pre_ratio);
                if(size_ratio > size_tolerance){
                    continue;
                }
            }

            //See if this is a better fit
            if(Math.abs(size.height - target_height) < min_diff){
                ret = size;
                min_diff = Math.abs(size.height - target_height);
            }
        }

        //If fit wasn't found, try for an okay fit
        if(ret == null){

            if(check_aspect && check_size){
                ret = this.getOptimalPreviewSize(sizes, w, h, true, false);
            }else if(check_aspect){
                ret = this.getOptimalPreviewSize(sizes, w, h, false, false);
            }
        }

        return ret;
    }

    /**
     * Saves the provided byte array to a file of the given filename in the external storage public media directory.
     * @param bytes
     * @param filename
     */
    private void saveBytesToFile(byte[] bytes, String filename, String subdiretory_type) {

        File file = FileTools.getExternalStorageMediaFile(this.getContext(),  filename, subdiretory_type);

        if(file != null){
            FileOutputStream os = null;
            try {

                os = new FileOutputStream(file);

                //Write to file
                os.write(bytes);
                os.close();

                //Tell the media that the file exists
                MediaScannerConnection.scanFile(this.getContext(), new String[]{file.toString()}, null, null);

            } catch (FileNotFoundException e) {
                Log.e(MainActivity.TAG, "Couldn't save "+filename+", file not found while opening output stream.", e);
            } catch (IOException e) {
                Log.e(MainActivity.TAG, "Couldn't save file, "+filename+", IOException while writing or closing.", e);
            }
        }
    }


}
