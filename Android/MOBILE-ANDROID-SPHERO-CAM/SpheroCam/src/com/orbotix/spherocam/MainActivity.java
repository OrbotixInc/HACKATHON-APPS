package com.orbotix.spherocam;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.flurry.android.FlurryAgent;
import com.orbotix.spherocam.preferences.ColorPref;
import com.orbotix.spherocam.preferences.ControlPref;
import com.orbotix.spherocam.preferences.PreferencesManager;
import com.orbotix.spherocam.ui.camera.CameraView;
import com.orbotix.spherocam.ui.camera.MediaRecorderView;
import com.orbotix.spherocam.ui.joystick.JoystickView;
import com.orbotix.spherocam.ui.settings.MenuActivity;
import orbotix.robot.app.ColorPickerActivity;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.*;
import orbotix.robot.utilities.RotationGestureDetector;
import orbotix.robot.utilities.SoundManager;
import orbotix.robot.widgets.ControllerActivity;
import orbotix.robot.widgets.calibration.CalibrationView;

import java.io.IOException;

/**
 * Main Activity where the user is able to drive the Sphero, and can take pictures or video.
 * <p/>
 * Created by Orbotix Inc.
 * Author: Adam Williams
 * Date: 10/27/11
 * Time: 10:42 AM
 */
public class MainActivity extends ControllerActivity {

    /**
     * Tag for logging to Android LogCat
     */
    public final static String TAG = "SpheroCam";

    /**
     * id for starting the StartupActivity for result
     */
    private final static int STARTUP_ACTIVITY = 0;

    /**
     * ID for starting the MenuActivity for result
     */
    private final static int SETTINGS_MENU_ACTIVITY = 1;

    /**
     * ID for playing the button press sound
     */
    public final static int SOUND_BUTTON_PRESS = 0;
    public final static int SOUND_CALIBRATION_IN = 1;
    public final static int SOUND_CALIBRATION_OUT = 2;

    private Robot mRobot = null;

    private JoystickView mJoystick;
    private CameraView mCameraView;
    private CalibrationView mCalibrateView;

    private ImageView mRecordVideoButton;
    private ImageView mTakePictureButton;
    private ImageView mSettingsButton;

    private MediaRecorderView mMediaRecorderView;

    private PreferencesManager prefs;

    /**
     * Detects the rotation for calibrating the mRobot.
     */
    private RotationGestureDetector gesture_detector;

    private BroadcastReceiver mColorChangeReceiver;

    private BroadcastReceiver mRobotConnectedReceiver;

    private BroadcastReceiver mRobotDisconnectedReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpMainView();

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        this.prefs = new PreferencesManager(this);

        //See if the mJoystick needs changes from the preferences
        this.checkJoystickPrefs();

        //Configure mJoystick
        mJoystick.setOnStartRunnable(new Runnable() {
            @Override
            public void run() {
                mCalibrateView.disable();
            }
        });
        mJoystick.setOnEndRunnable(new Runnable() {
            @Override
            public void run() {
                mCalibrateView.enable();
            }
        });

        //Set the broadcast receiver for color changes
        mColorChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                final int red = intent.getIntExtra(ColorPickerActivity.EXTRA_COLOR_RED, 0);
                final int green = intent.getIntExtra(ColorPickerActivity.EXTRA_COLOR_GREEN, 0);
                final int blue = intent.getIntExtra(ColorPickerActivity.EXTRA_COLOR_BLUE, 0);

                final ColorPref color = new ColorPref(red, green, blue);

                prefs.setColor(color);

                if (mRobot != null) {
                    RGBLEDOutputCommand.sendCommand(mRobot, color.red, color.green, color.blue);
                }
            }
        };

        //Broadcast receiver for disconnection
        mRobotDisconnectedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(MainActivity.TAG, "Disconnected from robot.");
                robotDisconnected();
            }
        };

        //Broadcast receiver for connecting to mRobot
        mRobotConnectedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent i) {

                Log.d(TAG, "Connect Intent received. Connecting to robot.");
                String sphero_id = i.getStringExtra(RobotProvider.EXTRA_ROBOT_ID);

                if (sphero_id != null && !sphero_id.equals("")) {

                    Log.d(TAG, "Robot ID aquired.");
                    mRobot = RobotProvider.getDefaultProvider().findRobot(sphero_id);

                    Log.d(MainActivity.TAG, "Connected to robot. Registering disconnect receiver.");
                    registerReceiver(mRobotDisconnectedReceiver, new IntentFilter(RobotProvider.ACTION_ROBOT_DISCONNECTED));

                    mJoystick.setRobot(mRobot);
                    mCalibrateView.setRobot(mRobot);

                    applyPreferencesToRobot();
                }
            }
        };


        registerReceiver(mColorChangeReceiver, new IntentFilter(ColorPickerActivity.ACTION_COLOR_CHANGE));
        registerReceiver(mRobotConnectedReceiver, new IntentFilter(RobotProvider.ACTION_ROBOT_CONNECT_SUCCESS));


        //Sound
        SoundManager.initialize(this);
        float volume = this.prefs.getVolume();
        SoundManager.setVolume(volume);
        SoundManager.addSound(this, SOUND_BUTTON_PRESS, R.raw.sphero_button_press);
        SoundManager.addSound(this, SOUND_CALIBRATION_IN, R.raw.calibration_in);
        SoundManager.addSound(this, SOUND_CALIBRATION_OUT, R.raw.calibration_out);

        //Configure CalibrationView
        mCalibrateView.setColor(0xffffffff);
        mCalibrateView.setOnStartRunnable(new Runnable() {
            @Override
            public void run() {
                SoundManager.playSound(SOUND_CALIBRATION_IN);
                SoundManager.playMusic(MainActivity.this, R.raw.calibration_hum, true);
            }
        });
        mCalibrateView.setOnEndRunnable(new Runnable() {
            @Override
            public void run() {
                SoundManager.stopMusic();
                SoundManager.playSound(SOUND_CALIBRATION_OUT);
            }
        });
        mCalibrateView.setDotSize(25);

        //Flurry
        FlurryAgent.onStartSession(this, "H62MI4Q9S6U1YECFVCNH");


        //Show the StartupActivity, if needed, to connect to a Sphero
        if (mRobot == null || !this.mRobot.isConnected()) {
            this.startActivityForResult(new Intent(this, StartupActivity.class), STARTUP_ACTIVITY);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setUpMainView();
    }

    private void setUpMainView() {
        this.setContentView(R.layout.main_activity);
        this.mJoystick = (JoystickView) this.findViewById(R.id.joystick);
        this.mCameraView = (CameraView) this.findViewById(R.id.camera);
        this.mMediaRecorderView = (MediaRecorderView) this.findViewById(R.id.media_recorder_view);
        this.mRecordVideoButton = (ImageView) this.findViewById(R.id.record_video_button);
        mTakePictureButton = (ImageView) findViewById(R.id.take_picture_button);
        mSettingsButton = (ImageView) findViewById(R.id.settings_button);

        mCalibrateView = (CalibrationView) findViewById(R.id.calibration);
    }

    @Override
    public void onActivityResult(int id, int result, Intent i) {

        if (result == RESULT_OK) {
            if (id == SETTINGS_MENU_ACTIVITY) {

                this.checkJoystickPrefs();

                this.applyPreferencesToRobot();

                if (this.mRobot == null || !this.mRobot.isConnected()) {
                    this.startActivityForResult(new Intent(this, StartupActivity.class), STARTUP_ACTIVITY);
                }

                overridePendingTransition(0, R.anim.out_through_bottom);
            } else if (id == STARTUP_ACTIVITY) {

                RobotProvider.getDefaultProvider().setBroadcastContext(MainActivity.this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        onSettingsClick(null);
        return false;
    }

    /**
     * Applies the SavedPreferences to the currently connected Robot
     */
    public void applyPreferencesToRobot() {

        if (this.mRobot != null && this.mRobot.isConnected()) {

            Log.d(TAG, "Applying prefs to robot.");

            final ColorPref color = this.prefs.getColor();

            RGBLEDOutputCommand.sendCommand(this.mRobot, color.red, color.green, color.blue);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "Pausing MainActivity.");

        mSettingsButton.setEnabled(false);
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(TAG, "MainActivity stopping.");
        DriveControl.INSTANCE.stopDriving();

        //Stop recording
        this.mMediaRecorderView.stopRecording();

        if (mRobot != null) {
            Log.d(TAG, "Unregistering disconnect receiver.");

            try {
                unregisterReceiver(mRobotDisconnectedReceiver);
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to unregister disconnect receiver. Likely not registered.", e);
            }

        }

        if (DriveControl.INSTANCE.hasRobotControl()) {
            DriveControl.INSTANCE.disconnectRobot();
        }

        //Unregister receivers
        Log.d(TAG, "Unregistering color and connect receivers.");
        unregisterReceiver(mColorChangeReceiver);
        unregisterReceiver(mRobotConnectedReceiver);

        FlurryAgent.onEndSession(this);
    }

    @Override
    public void onRestart() {
        super.onRestart();

        setUpMainView();

        Log.d(TAG, "MainActivity restarting.");

        this.startActivityForResult(new Intent(this, StartupActivity.class), STARTUP_ACTIVITY);

        Log.d(TAG, "Registering connection and color receivers.");
        registerReceiver(mColorChangeReceiver, new IntentFilter(ColorPickerActivity.ACTION_COLOR_CHANGE));
        registerReceiver(mRobotConnectedReceiver, new IntentFilter(RobotProvider.ACTION_ROBOT_CONNECT_SUCCESS));

        applyPreferencesToRobot();

        //Flurry
        FlurryAgent.onStartSession(this, "H62MI4Q9S6U1YECFVCNH");
    }


    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "MainActivity resuming.");

        mSettingsButton.setEnabled(true);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        mCalibrateView.interpretMotionEvent(event);

        //See if the user is driving and also wants to take a picture or record
        evaluateMultiTouchButtonPress(event);

        return super.dispatchTouchEvent(event);
    }

    /**
     * Checks whether the user is driving and also trying to start recording or take picture
     *
     * @param event
     * @return
     */
    private boolean evaluateMultiTouchButtonPress(MotionEvent event) {

        final int action = event.getActionMasked();
        if (!mCalibrateView.isCalibrating() &&
                mJoystick.getIsDraggingPuck() &&
                (event.getPointerCount() > 1
                        && action == MotionEvent.ACTION_POINTER_1_DOWN)) {
            final Rect rec_area = new Rect();
            final Rect cam_area = new Rect();

            rec_area.set(
                    mRecordVideoButton.getLeft(),
                    mRecordVideoButton.getTop(),
                    mRecordVideoButton.getRight(),
                    mRecordVideoButton.getBottom()
            );

            cam_area.set(
                    mTakePictureButton.getLeft(),
                    mTakePictureButton.getTop(),
                    mTakePictureButton.getRight(),
                    mTakePictureButton.getBottom()
            );

            if (rec_area.contains((int) event.getX(1), (int) event.getY(1))) {
                onRecordVideoClick(mRecordVideoButton);
                return true;
            } else if (cam_area.contains((int) event.getX(1), (int) event.getY(1))) {
                onTakePictureClick(mTakePictureButton);
                return true;
            }

        }

        return false;
    }

    /**
     * When the user clicks on the "take picture" button, take a picture.
     *
     * @param v
     */
    public void onTakePictureClick(View v) {


        if (!this.mMediaRecorderView.getIsRecording() && !this.mMediaRecorderView.getIsStarting()) {

            mTakePictureButton.setEnabled(false);
            this.mCameraView.takePicture();

            mTakePictureButton.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTakePictureButton.setEnabled(true);
                }
            }, 1000);
        }
    }

    /**
     * When the user clicks on the "record video" button, record video.
     *
     * @param v
     */

    public void onRecordVideoClick(View v) {

        v.setClickable(false);

        if (this.mMediaRecorderView.getCanStartRecording()) {

            this.mCameraView.hide();

            //Start recording
            try {
                if (this.mMediaRecorderView.startRecording()) {
                    //Set the record image to on
                    this.mRecordVideoButton.setImageLevel(1);

                    FlurryAgent.logEvent("Menu-VideoRecorded");
                } else {

                    this.mCameraView.show();
                    Log.e(TAG, "Failed to record video. The SD card may not be mounted.");

                    mCameraView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast error_toast = Toast.makeText(
                                    MainActivity.this,
                                    "Failed to record video. Is your SD card not mounted, or is it busy sharing with your computer?",
                                    Toast.LENGTH_LONG);
                            error_toast.show();
                        }
                    }, 1500);

                }

            } catch (IOException e) {

                this.mMediaRecorderView.stopRecording();

                this.mCameraView.show();

                Log.e(TAG, "Failed to record video.", e);

                //Unset image if it failed to record
                this.mRecordVideoButton.setImageLevel(0);

                Toast error_toast = Toast.makeText(this, "Failed to record video.", Toast.LENGTH_SHORT);
                error_toast.show();
            }
        } else if (this.mMediaRecorderView.getCanStopRecording()) {

            //Stop recording
            if (this.mMediaRecorderView.stopRecording()) {
                //Set the record image back to off
                this.mRecordVideoButton.setImageLevel(0);

                this.mCameraView.show();
            }
        }

        v.setClickable(true);
    }

    /**
     * When the user clicks the settings icon, launch the settings activity
     *
     * @param v
     */
    public void onSettingsClick(View v) {

        if (!this.mMediaRecorderView.getIsRecording() && !this.mMediaRecorderView.getIsStarting()) {

            SoundManager.playSound(SOUND_BUTTON_PRESS);

            Intent i = new Intent(this, MenuActivity.class);

            //Store mRobot ID, if exists
            if (this.mRobot != null && this.mRobot.isConnected()) {
                i.putExtra(MenuActivity.EXTRA_ROBOT, this.mRobot.getUniqueId());
            }

            this.startActivityForResult(i, SETTINGS_MENU_ACTIVITY);
        }
    }

    private void checkJoystickPrefs() {

        //Position
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(this.mJoystick.getLayoutParams());

        if (this.prefs.getIsJoystickLeft()) {
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else {
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }

        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        this.mJoystick.setLayoutParams(lp);

        //Joystick control
        ControlPref control_pref = this.prefs.getSelectedDriveControlPref();
        this.mJoystick.setSpeed(control_pref.max_speed);
        this.mJoystick.setRotation(control_pref.rotation_rate);
    }

    private void robotDisconnected() {
        mRobot = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.RobotDisconnected)
                .setMessage(R.string.ConnectionLost)
                .setPositiveButton(R.string.OK, null)
                .show();

        Log.d(TAG, "Robot disconnected. Unregistering receiver.");
        try {
            unregisterReceiver(mRobotDisconnectedReceiver);
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to unregister disconnect receiver. Likely not registered.", e);
        }
    }
}
