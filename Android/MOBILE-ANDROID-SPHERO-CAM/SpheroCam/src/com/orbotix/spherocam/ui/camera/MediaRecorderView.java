package com.orbotix.spherocam.ui.camera;

import android.content.Context;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.orbotix.spherocam.MainActivity;
import com.orbotix.spherocam.util.FileTools;

import java.io.File;
import java.io.IOException;

/**
 * A SurfaceView that shows the MediaRecorder preview, records video, and saves it to storage.
 *
 * Created by Orbotix Inc.
 * Author: Adam Williams
 * Date: 11/11/11
 * Time: 10:58 AM
 */
public class MediaRecorderView extends SurfaceView implements SurfaceHolder.Callback {

    MediaRecorder recorder;
    SurfaceHolder holder;
    volatile boolean recording = false;
    volatile boolean starting  = false;
    volatile boolean stopping  = false;
    private String last_video_path = "";

    public MediaRecorderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.recorder = new MediaRecorder();

        this.holder = this.getHolder();
        this.holder.addCallback(this);
        this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        //Do nothing
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {

        //Do nothing
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        this.stopRecording();
        this.recorder.release();
    }

    

    /**
     * Shows this view, and starts the video preview
     */
    public void show(){

        this.setVisibility(VISIBLE);

        this.invalidate();

    }

    /**
     * Starts recording to a timestamped 3gp file. Initializes and prepares the media recorder.
     *
     * @return True, if it starting recording. False, if it couldn't because it was already recording, or already starting.
     * @throws IOException
     */
    public boolean startRecording() throws IOException {

        if(this.getCanStartRecording()){



            String filename = FileTools.getTimestampedFileName()+".3gp";

            File file = FileTools.getExternalStorageMediaFile(this.getContext(), filename, Environment.DIRECTORY_MOVIES);

            if(file != null){

                this.starting = true;

                this.recorder.reset();

                this.initRecorder(file);

                this.prepareRecorder();

                this.recorder.start();

                this.last_video_path = file.getPath();

                this.recording = true;
                this.starting = false;
                return true;
            }
        }

        return false;
    }

    /**
     * Stops recording. Releases the media recorder.
     *
     * @return True, if it stopped recording. False, if it was already stopped, or if it couldn't stop at this point.
     */
    public boolean stopRecording(){

        if(this.getCanStopRecording()){


            try{
                this.stopping = true;

                this.recorder.stop();

                if(this.last_video_path != null && !this.last_video_path.equals("")){
                    MediaScannerConnection.scanFile(this.getContext(), new String[]{this.last_video_path}, null, null);
                }

                this.recording = false;
                this.stopping  = false;
                return true;

            } catch (IllegalStateException e){
                Log.e(MainActivity.TAG, "Failed to stop recording. " +
                        "IllegalStateException. Probably not actually recording.", e);
                this.recording = false;
                this.stopping  = false;
                return true;

            } catch (RuntimeException e){
                Log.e(MainActivity.TAG, "Failed to stop recording. User may have clicked too fast.", e);
            }
        }

        this.stopping  = false;
        return false;
    }

    /**
     * Indicates whether this MediaRecorderView is currently recording.
     *
     * @return True, if so
     */
    public boolean getIsRecording(){
        return this.recording;
    }

    /**
     * Indicates whether this MediaRecorderView has starting to record. Doesn't necessarily mean that it is
     * actually recording yet, only whether it has starting the process of recording.
     *
     * @return True, if so
     */
    public boolean getIsStarting(){
        return this.starting;
    }

    /**
     * Indicates whether this MediaRecorderView is in the process of stopping recording. Doesn't necessarily mean
     * that it is actually not recording at the moment, only whether it is stopping.
     *
     * @return True, if so
     */
    public boolean getIsStopping(){
        return this.stopping;
    }

    /**
     * Indicates whether this MediaRecorderView is ready to start recording.
     *
     * @return True, if so
     */
    public boolean getCanStartRecording(){
        return (!this.starting && !this.stopping && !this.recording);
    }

    /**
     * Indicates whether this MediaRecorderView is ready to stop recording.
     *
     * @return True, if so
     */
    public boolean getCanStopRecording(){
        return (!this.starting && !this.stopping && this.recording);
    }

    private void prepareRecorder() throws IOException {

        try {

            this.recorder.setPreviewDisplay(holder.getSurface());

            this.recorder.prepare();

        } catch (IllegalStateException e) {
            throw new IOException("Failed to prepare recorder. IllegalStateException.");
        }
    }

    private void initRecorder(File file) {
        
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);
        recorder.setOutputFile(file.getPath());
        recorder.setMaxDuration(50000); // 50 seconds
        recorder.setMaxFileSize(5000000); // Approximately 5 megabytes
    }

}
