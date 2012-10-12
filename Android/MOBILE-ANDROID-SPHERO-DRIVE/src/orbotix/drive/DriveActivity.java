package orbotix.drive;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewAnimator;
import com.flurry.android.FlurryAgent;
import orbotix.achievement.AchievementManager;
import orbotix.robot.app.ColorPickerActivity;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.*;
import orbotix.robot.utilities.RotationGestureDetector;
import orbotix.robot.utilities.SoundManager;
import orbotix.robot.widgets.CustomPopover;
import orbotix.robot.widgets.calibration.CalibrationView;

import java.util.ArrayList;

public class DriveActivity extends Activity {
    private static final String LOG_TAG = "DriveActivity";
    public static final String TAG = "Orbotix";
    public static final boolean DEBUG = true;
    public static final String PREFERENCE_VOLUME = "orbotix.preference.VOLUME";

    private final static int DEACTIVATED_BUTTON_COLOR = Color.argb(153, 0, 0, 0);
    private final static int DEACTIVATED_TEXT_COLOR = Color.rgb(100, 100, 100);
    public final static String TUTORIAL_PREFERENCES = "orbotix.drive.tutorial.PREFERENCES";

    private final static String PREFERENCE_SHOW_COLOR_CALLOUT = "orbotix.drive.tutorial.COLOR";
    private final static String PREFERENCE_SHOW_SPEED_CALLOUT = "orbotix.drive.tutorial.SPEED";
    private final static String PREFERENCE_SHOW_DRIVE_CALLOUT = "orbotix.drive.tutorial.DRIVE";
    public static final String PREFERENCE_SHOW_TUTORIAL = "orbotix.drive.tutorial.SHOW_AIM_TUTORIAL";

    private static final int VIEW_ANIMATOR_JOYSTICK_POSITION = 0;
    private static final int VIEW_ANIMATOR_TILT_POSITION = 1;
    private static final int VIEW_ANIMATOR_RC_POSITION = 2;

    public static final int SOUND_DRIVE_CONTROL_IN = 101;
    public static final int SOUND_BUTTON_PRESS = 102;
    public static final int SOUND_DROPDOWN_ALERT = 103;
    public static final int SOUND_ITEM_SELECT = 104;
    public static final int SOUND_ROTATE_IN = 105;
    public static final int SOUND_ROTATE_OUT = 106;

    private static final int STARTUP_REQUEST = 2;
    private static final int REQUEST_SHOW_MENU = 3;
    private static final int REQUEST_CHANGE_NAME = 4;
    private static final int REQUEST_SHOW_TUTORIAL = 5;

    public static final String ACTION_SHOW_COLOR_PICKER = "orbotix.sphero.SHOW_COLOR_PICKER";
    public static final String ACTION_SHOW_SENSITIVITY = "orbotix.sphero.SHOW_SENSITIVITY";
    public static final String ACTION_SHOW_LEADERBOARD = "orbotix.sphero.SHOW_LEADERBOARD";
    public static final String ACTION_SHOW_SETTINGS = "orbotix.sphero.SHOW_SETTINGS";
    public static final String EXTRA_SPHERO_NAME = "orbotix.sphero.SPHERO_NAME";

    private Robot mRobot;

    private DriveWheelView driveWheelView;
    private RcControlView rcControlView;
    private ColorIndicatorView colorIndicatorView;
    private ImageButton mDriveControlButton;
    private CustomPopover mDrivePopover, mSensitivityPopover;
    private ImageButton mJoystickButton, mTiltButton, mRCButton;
    private long startDrivingTimeStamp;

    private PowerManager.WakeLock screenWakeLock;

    private DriveControl driveControl = DriveControl.INSTANCE;

    private boolean pause = true;
    private boolean driveControlConfigured = false;
    private boolean showingTutorial = false;

    private int driveMode = DriveControl.JOY_STICK;
    private ViewAnimator driveViewAnimator;

    private Preferences preferences = Preferences.getDefaultPreferences();

    private enum DriveMode {joy, tilt, rc}

    protected DriveMode mode;

    private enum Callout {color, speed, drive}

    private ArrayList<Callout> callouts;
    private boolean calloutShowing = false;
    private CalibrationView mCalibrationView;

    private boolean canCalibrate = true;
    private boolean showingColorPicker = false;

    private Handler mHandler = new Handler();

    private BroadcastReceiver flurryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == StartupActivity.ACTION_FLURRY_LOG_BUY_SPHERO) {
                FlurryAgent.logEvent("BuyButtonOnFirstNoSpheroConnected");
            }
        }
    };

    private BroadcastReceiver mDisconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(RobotProvider.ACTION_ROBOT_DISCONNECTED)) {
                robotDisconnected();
            }
        }
    };

    private BroadcastReceiver mColorChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // update colors
            int red = intent.getIntExtra(ColorPickerActivity.EXTRA_COLOR_RED, 0);
            int green = intent.getIntExtra(ColorPickerActivity.EXTRA_COLOR_GREEN, 0);
            int blue = intent.getIntExtra(ColorPickerActivity.EXTRA_COLOR_BLUE, 0);

            updateColorIndicatorView(red, green, blue);

            try {
                DriveControl.INSTANCE.getRobotControl().setRGBColor(red, green, blue);
            } catch (NullPointerException e) {
                // do nothing, we just don't want the app to crash
                e.printStackTrace();
            }
        }
    };

    private Runnable showNextCallout = new Runnable() {
        @Override
        public void run() {
            if (calloutShowing) {
                mHandler.postDelayed(showNextCallout, 60000);
                return;
            }
            int next = getNextCallout();
            if (next == -1) {
                mHandler.removeCallbacks(showNextCallout);
            } else {
                findViewById(next).setVisibility(View.VISIBLE);
                calloutShowing = true;
                mHandler.postDelayed(showNextCallout, 60000);
            }
        }
    };

    private Runnable startRotating = new Runnable() {
        @Override
        public void run() {
            pauseDrivingForActivity();
            FlurryAgent.logEvent("Calibrate", true);
            SoundManager.playSound(SOUND_ROTATE_IN);
            SoundManager.playMusic(DriveActivity.this, R.raw.rotate_hum, true);
        }
    };

    private Runnable stopRotating = new Runnable() {
        @Override
        public void run() {
            resumeDrivingForActivity();
            FlurryAgent.endTimedEvent("Calibrate");
            SoundManager.stopMusic();
            SoundManager.playSound(SOUND_ROTATE_OUT);
        }
    };

    private RotationGestureDetector.OnRotationGestureListener mOnRotationGestureListener = new RotationGestureDetector.OnRotationGestureListener() {
        @Override
        public void onRotationGestureStarted(double distance, Point p1, Point p2) {
            if (driveMode == DriveMode.rc.ordinal() || !canCalibrate) {
                mCalibrationView.disable();
                return;
            }
            pauseDrivingForActivity();
            FlurryAgent.logEvent("Calibrate", true);
            FrontLEDOutputCommand.sendCommand(mRobot, 1.0f);
            CalibrateCommand.sendCommand(mRobot, 0.0f);
        }

        @Override
        public void onRotationGestureRotated(double totalRotationAngle, double distance, Point p1, Point p2) {
            if (driveMode == DriveMode.rc.ordinal() || !canCalibrate) {
                return;
            }
            float heading = convertAngleToDegrees(2.0 * totalRotationAngle);
            RollCommand.sendCommand(mRobot, heading, 0.0f, true);
        }

        @Override
        public void onRotationGestureEnded(double finalAngle, double distance, Point p1, Point p2) {
            if (driveMode == DriveMode.rc.ordinal() || !canCalibrate) {
                mCalibrationView.enable();
                return;
            }
            resumeDrivingForActivity();
            FlurryAgent.endTimedEvent("Calibrate");
            FrontLEDOutputCommand.sendCommand(mRobot, 0.0f);
            CalibrateCommand.sendCommand(mRobot, 0.0f);
        }
    };

    private DriveWheelView.TouchMotionListener touchMotionListener = new DriveWheelView.TouchMotionListener() {

        @Override
        public void onStart() {
            mCalibrationView.disable();
            if (pause || mRobot == null) return;
            startDrivingTimeStamp = System.currentTimeMillis();
            driveControl.startDriving(DriveActivity.this, DriveControl.JOY_STICK);
        }

        @Override
        public void onMove(float positionX, float positionY) {
            if (pause || mRobot == null) return;
            driveControl.driveJoyStick(positionX, positionY);
            canCalibrate = false;
        }

        @Override
        public void onStop() {
            mCalibrationView.enable();
            if (pause || mRobot == null) return;
            int distance = AchievementManager.getAccumulatedDistanceSinceLastQuery();
            long endTime = System.currentTimeMillis();
            long totalTime = (endTime - startDrivingTimeStamp) / 1000;
            AchievementManager.recordEvent("driveTimeGeneric", (int)totalTime);
            AchievementManager.recordEvent("driveDistanceTotal", distance);
            switch (mode) {
                case joy:
                    AchievementManager.recordEvent("driveTimeJoystick", (int)totalTime);
                    AchievementManager.recordEvent("driveDistanceJoystickTotal", distance);
                    break;
                case tilt:
                    AchievementManager.recordEvent("driveTimeTilt", (int)totalTime);
                    AchievementManager.recordEvent("driveDistanceTiltTotal", distance);
                    break;
            }
            driveControl.stopDriving();
            canCalibrate = true;
        }

        @Override
        public void onBoost() {
            if (pause || mRobot == null) return;
            FlurryAgent.logEvent("Boost");
            AchievementManager.recordEvent("driveBoost");
            float boost_time = Preferences.getDefaultPreferences().getBoostTime();

            String sensitivity = Preferences.getDefaultPreferences().getSensitivitySetting();
            driveControl.getRobotControl().boostUnderControl(boost_time);
        }

    };

    private RcControlView.OnRcUpdateListener rcUpdateListener = new RcControlView.OnRcUpdateListener() {
        @Override
        public void onRCUpdate(float gas, float steering) {
            if (pause || mRobot == null) return;
            startDrivingTimeStamp = System.currentTimeMillis();
            driveControl.driveRc(gas, steering);
        }

        @Override
        public void onStop() {
            if (pause || mRobot == null) return;
            int distance = AchievementManager.getAccumulatedDistanceSinceLastQuery();
            long endTime = System.currentTimeMillis();
            long totalTime = (endTime - startDrivingTimeStamp) / 1000;
            AchievementManager.recordEvent("driveTimeGeneric", (int)totalTime);
            AchievementManager.recordEvent("driveTimeRC", (int)totalTime);
            AchievementManager.recordEvent("driveDistanceRCTotal", distance);
            driveControl.stopDriving();
        }

        @Override
        public void onBoost() {
            if (pause || mRobot == null) return;
            Preferences preferences = Preferences.getDefaultPreferences();
            float boost_time = preferences.getBoostTime();
            driveControl.getRobotControl().boostUnderControl(boost_time);
        }
    };

    private int getNextCallout() {
        if (callouts == null) {
            callouts = new ArrayList<Callout>(3);
            SharedPreferences preferences = getSharedPreferences(TUTORIAL_PREFERENCES, Context.MODE_PRIVATE);
            if (preferences.getBoolean(PREFERENCE_SHOW_COLOR_CALLOUT, true)) {
                callouts.add(Callout.color);
            }
            if (preferences.getBoolean(PREFERENCE_SHOW_SPEED_CALLOUT, true)) {
                callouts.add(Callout.speed);
            }
            if (preferences.getBoolean(PREFERENCE_SHOW_DRIVE_CALLOUT, true)) {
                callouts.add(Callout.drive);
            }
        } else {
            // cycle the first one to the back since it is not our first time in here
            if (callouts.size() > 1) {
                Callout temp = callouts.get(0);
                callouts.remove(temp);
                callouts.add(temp);
            }
        }

        if (callouts.size() > 0) {
            switch (callouts.get(0)) {
                case color:
                    switch (mode) {
                        case tilt:
                            return R.id.TiltColorPickerCallout;
                        case rc:
                            return R.id.RCColorPickerCallout;
                        default:
                            return R.id.JoystickColorPickerCallout;
                    }
                case speed:
                    return R.id.SpeedCallout;
                case drive:
                    return R.id.DriveCallout;
            }
        }
        return -1;
    }

    private void setupRobot(Intent intent) {
        mRobot = RobotProvider.getDefaultProvider().findRobot(intent.getStringExtra(StartupActivity.EXTRA_ROBOT_ID));
        RobotProvider.getDefaultProvider().setBroadcastContext(this);
        IntentFilter disconnectFilter = new IntentFilter(RobotProvider.ACTION_ROBOT_DISCONNECTED);
        registerReceiver(mDisconnectReceiver, disconnectFilter);
        SharedPreferences sharedPreferences = getSharedPreferences(TUTORIAL_PREFERENCES, MODE_PRIVATE);
        if (mRobot != null && sharedPreferences.getBoolean(PREFERENCE_SHOW_TUTORIAL, true)) {
            FlurryAgent.logEvent("openTutorial");
            pauseDrivingForActivity();
            showingTutorial = true;
            Intent tutorialIntent = new Intent(this, RotateTutorialActivity.class);
            if (mRobot != null) {
                tutorialIntent.putExtra(RotateTutorialActivity.EXTRA_SPHERO_ID, mRobot.getUniqueId());
            } else {
                tutorialIntent.putExtra(RotateTutorialActivity.EXTRA_SPHERO_ID, RotateTutorialActivity.NOT_CONNECTED);
            }
            startActivityForResult(tutorialIntent, REQUEST_SHOW_TUTORIAL);
            overridePendingTransition(R.anim.in_from_bottom, R.anim.out_through_bottom);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Orbotix", "Creating");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.drive);
        mCalibrationView = (CalibrationView)findViewById(R.id.CalibrationView);
        mCalibrationView.setColor(Color.WHITE);
        mCalibrationView.setCircleColor(Color.WHITE);
        mCalibrationView.setOnStartRunnable(startRotating);
        mCalibrationView.setOnEndRunnable(stopRotating);
        mCalibrationView.enable();
        driveViewAnimator = (ViewAnimator) findViewById(R.id.DriveViewAnimator);

        preferences.setSystemPreferences(getSharedPreferences(Preferences.NAME, MODE_PRIVATE));
        setupAudio();
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mDriveControlButton = (ImageButton) findViewById(R.id.DriveSelectButton);
        mDrivePopover = new CustomPopover(R.layout.drive_popover, mDriveControlButton);
        mDrivePopover.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                resumeDrivingForActivity();
            }
        });
        mJoystickButton = (ImageButton) mDrivePopover.getContent().findViewById(R.id.DriveButton);
        mTiltButton = (ImageButton) mDrivePopover.getContent().findViewById(R.id.TiltButton);
        mRCButton = (ImageButton) mDrivePopover.getContent().findViewById(R.id.RCButton);

        setupSensitivityButton();
        mHandler.post(showNextCallout);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("Orbotix", "New Intent");
    }

    private void setupAudio() {
        SoundManager.initialize(this);
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SoundManager.setVolume(Preferences.getDefaultPreferences().getVolume());
        SoundManager.addSound(this, SOUND_BUTTON_PRESS, R.raw.sphero_button_press);
        SoundManager.addSound(this, SOUND_DRIVE_CONTROL_IN, R.raw.sphero_drive_wheel_in);
        SoundManager.addSound(this, SOUND_DROPDOWN_ALERT, R.raw.sphero_dropdown_alert);
        SoundManager.addSound(this, SOUND_ITEM_SELECT, R.raw.sphero_item_select);
        SoundManager.addSound(this, SOUND_ROTATE_IN, R.raw.rotate_in);
        SoundManager.addSound(this, SOUND_ROTATE_OUT, R.raw.rotate_out);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (calloutShowing) {
            calloutShowing = false;
            findViewById(R.id.JoystickColorPickerCallout).setVisibility(View.GONE);
            findViewById(R.id.TiltColorPickerCallout).setVisibility(View.GONE);
            findViewById(R.id.RCColorPickerCallout).setVisibility(View.GONE);
            findViewById(R.id.SpeedCallout).setVisibility(View.GONE);
            findViewById(R.id.DriveCallout).setVisibility(View.GONE);
        }

        mCalibrationView.interpretMotionEvent(event);

        /*if (mRotationGestureDetector.onTouchEvent(event) && driveMode != DriveMode.rc.ordinal() && canCalibrate) {
            return true;
        }*/

        return super.dispatchTouchEvent(event);

    }

    private float convertAngleToDegrees(double angleInRadians) {
        float angleInDegrees = (float) Math.toDegrees(angleInRadians);
        if (angleInDegrees >= 0.0 && angleInDegrees < 360.0) {
            return angleInDegrees;
        } else if (angleInDegrees < 0.0) {
            return convertAngleToDegrees(angleInRadians + (2.0 * Math.PI));
        } else if (angleInDegrees > 360.0) {
            return convertAngleToDegrees(angleInRadians - (2.0 * Math.PI));
        } else {
            return Math.abs(angleInDegrees);
        }
    }

    public static void updateDriveOptions() {
        if (Preferences.getDefaultPreferences().getSensitivitySetting().equalsIgnoreCase(Preferences.CRAZY_NAME)) {
            AchievementManager.recordEvent("driveSensitivtyModeCrazy");
        }
        // Set the drive options for Sphero
        try {
            float rate_scale = Preferences.getDefaultPreferences().getRotationRate();
            RotationRateCommand.sendCommand(DriveControl.INSTANCE.getRobotControl().getRobot(), rate_scale);
            float speed_scale = Preferences.getDefaultPreferences().getMaxSpeed();
            DriveControl.INSTANCE.setSpeedScale(speed_scale);
            DriveControl.INSTANCE.getRobotControl().getDriveAlgorithm().speedScale = speed_scale;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private void showColorPicker() {
        if (showingColorPicker) return;
        showingColorPicker = true;
        if (callouts != null) {
            getSharedPreferences(TUTORIAL_PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean(PREFERENCE_SHOW_COLOR_CALLOUT, false).commit();
            callouts.remove(Callout.color);
        }
        FlurryAgent.logEvent("ColorChanged");
        pauseDrivingForActivity();

        IntentFilter filter = new IntentFilter(ColorPickerActivity.ACTION_COLOR_CHANGE);
        registerReceiver(mColorChangeReceiver, filter);

        Intent picker_intent = new Intent(this, ColorPickerActivity.class);
        picker_intent.putExtra(ColorPickerActivity.EXTRA_COLOR_RED, preferences.getRedLEDColor());
        picker_intent.putExtra(ColorPickerActivity.EXTRA_COLOR_GREEN, preferences.getGreenLEDColor());
        picker_intent.putExtra(ColorPickerActivity.EXTRA_COLOR_BLUE, preferences.getBlueLEDColor());
        startActivityForResult(picker_intent, MenuActivity.PICK_COLOR_REQUEST);
        overridePendingTransition(R.anim.dissolve_in, R.anim.dissolve_out);
    }

    public void showMainMenu(View v) {
        SoundManager.playSound(SOUND_BUTTON_PRESS);
        final ImageButton button = (ImageButton)v;
        button.setEnabled(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(true);
            }
        }, 200);
        FlurryAgent.logEvent("Menu");
        pauseDrivingForActivity();

        Intent menu_intent = new Intent(v.getContext(), MenuActivity.class);
        if (mRobot != null) {
            menu_intent.putExtra(StartupActivity.EXTRA_ROBOT_ID, mRobot.getUniqueId());
        }
        startActivityForResult(menu_intent, REQUEST_SHOW_MENU);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_through_bottom);
    }

    public void showDriveSelector(View v) {
        SoundManager.playSound(SOUND_BUTTON_PRESS);
        final ImageButton button = (ImageButton)v;
        button.setEnabled(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(true);
            }
        }, 200);
        if (callouts != null) {
            getSharedPreferences(TUTORIAL_PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean(PREFERENCE_SHOW_DRIVE_CALLOUT, false).commit();
            callouts.remove(Callout.drive);
        }
        pauseDrivingForActivity();
        mDrivePopover.show();
    }

    private void updateDrivePopoverSelection() {
        switch (preferences.getControlMethod()) {
            case Preferences.JOYSTICK_CONTROL_METHOD:
                mJoystickButton.setImageResource(R.drawable.button_joystick_pressed);
                mTiltButton.setImageResource(R.drawable.tilt_button_selector);
                mRCButton.setImageResource(R.drawable.rc_button_selector);
                break;
            case Preferences.TILT_CONTROL_METHOD:
                mTiltButton.setImageResource(R.drawable.button_tilt_pressed);
                mJoystickButton.setImageResource(R.drawable.joystick_button_selector);
                mRCButton.setImageResource(R.drawable.rc_button_selector);
                break;
            case Preferences.RC_CONTROL_METHOD:
                mRCButton.setImageResource(R.drawable.button_rc_pressed);
                mJoystickButton.setImageResource(R.drawable.joystick_button_selector);
                mTiltButton.setImageResource(R.drawable.tilt_button_selector);
                break;
        }
    }

    public void onDriveModeSelected(View v) {
        switch (v.getId()) {
            case R.id.DriveButton:
                changeDriveMode(DriveMode.joy);
                break;
            case R.id.TiltButton:
                changeDriveMode(DriveMode.tilt);
                break;
            case R.id.RCButton:
                changeDriveMode(DriveMode.rc);
                break;
            default:
                changeDriveMode(DriveMode.joy);
                break;
        }
    }

    private void changeDriveMode(DriveMode driveMode) {
        SoundManager.playSound(SOUND_ITEM_SELECT);
        if (driveMode == mode) {
            return;
        } else {
            mode = driveMode;
        }
        endAnalyticsForDriveMode();
        SoundManager.playSound(SOUND_DRIVE_CONTROL_IN);
        switch (driveMode) {
            case joy:
                Preferences.getDefaultPreferences().usedJoystick();
                mDriveControlButton.setImageResource(R.drawable.icon_joystick_white);
                switchToJoystick();
                preferences.setControlMethod(Preferences.JOYSTICK_CONTROL_METHOD);
                break;
            case tilt:
                Preferences.getDefaultPreferences().usedTilt();
                AchievementManager.recordEvent("tiltDriveModeSelect");
                mDriveControlButton.setImageResource(R.drawable.icon_tilt);
                switchToTilt();
                preferences.setControlMethod(Preferences.TILT_CONTROL_METHOD);
                break;
            case rc:
                Preferences.getDefaultPreferences().usedRC();
                AchievementManager.recordEvent("rcDriveModeSelect");
                mDriveControlButton.setImageResource(R.drawable.icon_rc);
                switchToRc();
                preferences.setControlMethod(Preferences.RC_CONTROL_METHOD);
                break;
        }
        mDrivePopover.dismiss();
        updateDrivePopoverSelection();
        if (Preferences.getDefaultPreferences().usedAllDriveModes()) {
            AchievementManager.recordEvent("allDriveModesUsed");
        }
    }

    private void endAnalyticsForDriveMode() {
        switch (driveMode) {
            case DriveControl.JOY_STICK:
                FlurryAgent.endTimedEvent("JoystickDrive");
                break;
            case DriveControl.TILT:
                FlurryAgent.endTimedEvent("TiltDrive");
                break;
            case DriveControl.RC:
                FlurryAgent.endTimedEvent("RCDrive");
                break;
        }
    }

    private void switchToJoystick() {
        if (driveMode == DriveControl.RC) {
            rcControlView.stopControlSystem();
        }
        FlurryAgent.logEvent("JoystickDrive", true);
        mCalibrationView.enable();
        driveWheelView = (DriveWheelView) findViewById(R.id.JoystickWheelView);
        if (!pause) {
            driveControl.stopDriving();
            driveWheelView.updatePuckPosition(0.0f, 0.0f);
        }
        driveWheelView.setTouchMotionListener(touchMotionListener);
        driveMode = DriveControl.JOY_STICK;
        driveViewAnimator.setDisplayedChild(VIEW_ANIMATOR_JOYSTICK_POSITION);
        configureDriveControl();

        // setup color indicator
        colorIndicatorView = (ColorIndicatorView) findViewById(R.id.JoystickColorIndicatorView);
        configureColorIndicator();
    }

    private void switchToTilt() {
        driveWheelView = (DriveWheelView) findViewById(R.id.TiltWheelView);
        FlurryAgent.logEvent("TiltDrive", true);
        mCalibrationView.enable();
        if (!pause) {
            driveControl.stopDriving();
            driveWheelView.updatePuckPosition(0.0f, 0.0f);
        }

        if (driveMode == DriveControl.RC) {
            rcControlView.stopControlSystem();
        }

        driveWheelView.setTouchMotionListener(touchMotionListener);
        driveMode = DriveControl.TILT;
        driveViewAnimator.setDisplayedChild(VIEW_ANIMATOR_TILT_POSITION);
        configureDriveControl();

        // setup color indicator
        colorIndicatorView = (ColorIndicatorView) findViewById(R.id.TiltColorIndicatorView);
        configureColorIndicator();

        if (!pause) {
            driveControl.startDriving(DriveActivity.this, DriveControl.TILT);
        }
    }

    private void switchToRc() {
        FlurryAgent.logEvent("RCDrive", true);
        driveControl.stopDriving();
        mCalibrationView.disable();
        rcControlView = (RcControlView) findViewById(R.id.RCView);
        rcControlView.setOnRcUpdateListener(rcUpdateListener);

        // No need for a on convert listener so we null it which means it need configured again.
        driveControl.setOnConvertListener(null);
        driveControlConfigured = false;

        driveMode = DriveControl.RC;
        driveViewAnimator.setDisplayedChild(VIEW_ANIMATOR_RC_POSITION);

        // setup color indicator
        colorIndicatorView = (ColorIndicatorView) findViewById(R.id.RcColorIndicatorView);
        configureColorIndicator();

        if (!pause) {
            driveControl.startDriving(DriveActivity.this, DriveControl.RC);
            rcControlView.startControlSystem();
        }
    }

    private void configureDriveControl() {
        if (driveControlConfigured) return;

        driveControl.setJoyStickPadSize(driveWheelView.getDrivePadWidth(),
                driveWheelView.getDrivePadHeight());

        driveControl.setBroadcastContext(this);
        driveControl.setOnConvertListener(new DriveAlgorithm.OnConvertListener() {

            @Override
            public void onConvert(double heading, double speed, double speedScale) {
                driveWheelView.updatePuckPosition((float) heading, (float) (speed / speedScale));
            }

        });
        driveControlConfigured = true;
    }

    private void configureColorIndicator() {
        colorIndicatorView.setColor(preferences.getRedLEDColor(), preferences.getGreenLEDColor(), preferences.getBlueLEDColor());
        colorIndicatorView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showColorPicker();
            }
        });
    }

    private void setupSensitivityButton() {
        ((ImageButton) findViewById(R.id.SettingsButton)).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent settingsIntent = new Intent(view.getContext(), SettingsActivity.class);
                startActivityForResult(settingsIntent, MenuActivity.PICK_SKILL_LEVEL_REQUEST);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_through_bottom);
                return true;
            }
        });
    }

    public void onSensitivityButtonClicked(View v) {
        SoundManager.playSound(SOUND_BUTTON_PRESS);
        final ImageButton button = (ImageButton)v;
        button.setEnabled(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(true);
            }
        }, 200);
        if (callouts != null) {
            getSharedPreferences(TUTORIAL_PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean(PREFERENCE_SHOW_SPEED_CALLOUT, false).commit();
            callouts.remove(Callout.speed);
        }
        pauseDrivingForActivity();
        if (mSensitivityPopover == null) {
            mSensitivityPopover = new CustomPopover(R.layout.sensitivity_popover, v);
            mSensitivityPopover.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    resumeDrivingForActivity();
                }
            });
        }
        String currentSetting = Preferences.getDefaultPreferences().getSensitivitySetting();
        if (currentSetting.equalsIgnoreCase(Preferences.CAUTIOUS_NAME)) {
            activateCautiousButton();
        } else if (currentSetting.equalsIgnoreCase(Preferences.COMFORTABLE_NAME)) {
            activateComfortableButton();
        } else {
            activateCrazyButton();
        }
        loadCustomSensitivityNames();
        mSensitivityPopover.show();
    }

    private void loadCustomSensitivityNames() {
        ((TextView) mSensitivityPopover.getContent().findViewById(R.id.CautiousText))
                .setText(Preferences.getDefaultPreferences().getCautiousSensitivityName());
        mSensitivityPopover.getContent().findViewById(R.id.CautiousText).setSelected(true);
        ((TextView) mSensitivityPopover.getContent().findViewById(R.id.ComfortableText))
                .setText(Preferences.getDefaultPreferences().getComfortableSensitivityName());
        mSensitivityPopover.getContent().findViewById(R.id.ComfortableText).setSelected(true);
        ((TextView) mSensitivityPopover.getContent().findViewById(R.id.CrazyText))
                .setText(Preferences.getDefaultPreferences().getCrazySensitivityName());
        mSensitivityPopover.getContent().findViewById(R.id.CrazyText).setSelected(true);
    }

    public void onSensitivityChanged(View v) {
        SoundManager.playSound(SOUND_ITEM_SELECT);
        if (mSensitivityPopover != null) {
            mSensitivityPopover.dismiss();
        }
        switch (v.getId()) {
            case R.id.CautiousButton:
                Preferences.getDefaultPreferences().setSensitivitySetting(Preferences.CAUTIOUS_NAME);
                FlurryAgent.logEvent("HomeScreenSensitivityChangedLevel1");
                activateCautiousButton();
                break;

            case R.id.ComfortableButton:
                Preferences.getDefaultPreferences().setSensitivitySetting(Preferences.COMFORTABLE_NAME);
                FlurryAgent.logEvent("HomeScreenSensitivityChangedLevel2");
                activateComfortableButton();
                break;

            case R.id.CrazyButton:
                Preferences.getDefaultPreferences().setSensitivitySetting(Preferences.CRAZY_NAME);
                FlurryAgent.logEvent("HomeScreenSensitivityChangedLevel3");
                activateCrazyButton();
                break;

            default:
                Preferences.getDefaultPreferences().setSensitivitySetting(Preferences.CAUTIOUS_NAME);
                FlurryAgent.logEvent("HomeScreenSensitivityChangedLevel1");
                activateCautiousButton();
                break;
        }

        updateDriveOptions();
        //changeSettingsButton();
    }

    private void activateCautiousButton() {
        ((ImageButton) mSensitivityPopover.getContent().findViewById(R.id.CautiousButton)).clearColorFilter();
        ((ImageButton) mSensitivityPopover.getContent().findViewById(R.id.ComfortableButton)).setColorFilter(DEACTIVATED_BUTTON_COLOR);
        ((ImageButton) mSensitivityPopover.getContent().findViewById(R.id.CrazyButton)).setColorFilter(DEACTIVATED_BUTTON_COLOR);
    }

    private void activateComfortableButton() {
        ((ImageButton) mSensitivityPopover.getContent().findViewById(R.id.ComfortableButton)).clearColorFilter();
        ((ImageButton) mSensitivityPopover.getContent().findViewById(R.id.CautiousButton)).setColorFilter(DEACTIVATED_BUTTON_COLOR);
        ((ImageButton) mSensitivityPopover.getContent().findViewById(R.id.CrazyButton)).setColorFilter(DEACTIVATED_BUTTON_COLOR);
    }

    private void activateCrazyButton() {
        ((ImageButton) mSensitivityPopover.getContent().findViewById(R.id.CrazyButton)).clearColorFilter();
        ((ImageButton) mSensitivityPopover.getContent().findViewById(R.id.CautiousButton)).setColorFilter(DEACTIVATED_BUTTON_COLOR);
        ((ImageButton) mSensitivityPopover.getContent().findViewById(R.id.ComfortableButton)).setColorFilter(DEACTIVATED_BUTTON_COLOR);
    }

    private void changeSettingsButton() {
        String setting = Preferences.getDefaultPreferences().getSensitivitySetting();
        if (setting.equalsIgnoreCase(Preferences.CAUTIOUS_NAME)) {
            findViewById(R.id.SettingsButton).setBackgroundResource(R.drawable.button_background);
        } else if (setting.equalsIgnoreCase(Preferences.COMFORTABLE_NAME)) {
            findViewById(R.id.SettingsButton).setBackgroundResource(R.drawable.button_yellow);
        } else {
            findViewById(R.id.SettingsButton).setBackgroundResource(R.drawable.button_red);
        }


    }

    private void pauseDrivingForActivity() {
        pause = true;
        driveControl.stopDriving();
        if (driveMode == DriveControl.RC) {
            rcControlView.stopControlSystem();
        }
    }

    private void resumeDrivingForActivity() {
        pause = false;
        driveControl.startDriving(this, driveMode);
        if (driveMode == DriveControl.RC) {
            rcControlView.startControlSystem();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case STARTUP_REQUEST:
                if (resultCode == RESULT_OK) {
                    setupRobot(data);
                    updateDriveOptions();
                    int red = preferences.getRedLEDColor();
                    int green = preferences.getGreenLEDColor();
                    int blue = preferences.getBlueLEDColor();
                    RGBLEDOutputCommand.sendCommand(mRobot, red, green, blue);
                    updateColorIndicatorView(red, green, blue);
                    mCalibrationView.setRobot(mRobot);
                    if (!showingTutorial) {
                        resumeDrivingForActivity();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    pauseDrivingForActivity();
                }

                // if the activtity is resumed after a long time.
                try {
                    unregisterReceiver(flurryReceiver);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                break;
            case MenuActivity.PICK_COLOR_REQUEST:
                showingColorPicker = false;
                if (resultCode == Activity.RESULT_OK) {
                    preferences.setRedLEDColor(data.getIntExtra(ColorPickerActivity.EXTRA_COLOR_RED, 0));
                    preferences.setGreenLEDColor(data.getIntExtra(ColorPickerActivity.EXTRA_COLOR_GREEN, 0));
                    preferences.setBlueLEDColor(data.getIntExtra(ColorPickerActivity.EXTRA_COLOR_BLUE, 0));
                }
                try {
                    unregisterReceiver(mColorChangeReceiver);
                } catch (IllegalArgumentException e) {
                    // do nothing, the receiver was not registered
                }
                updateColorIndicatorView(preferences.getRedLEDColor(), preferences.getGreenLEDColor(), preferences.getBlueLEDColor());
                AchievementManager.recordEvent("driveColorChange", AchievementManager.getColorChangesSinceLastQuery());

                resumeDrivingForActivity();

                break;
            case REQUEST_SHOW_MENU:
                try {
                    unregisterReceiver(mColorChangeReceiver);
                } catch (IllegalArgumentException e) {
                    // do nothing, the receiver was not registered
                }
                // in case the color changed, we need to update the color indicator
                updateColorIndicatorView(preferences.getRedLEDColor(), preferences.getGreenLEDColor(), preferences.getBlueLEDColor());
                resumeDrivingForActivity();
                //changeSettingsButton();
                break;
            case REQUEST_CHANGE_NAME:
                if (resultCode == RESULT_OK) {
                    String newName = data.getStringExtra(EXTRA_SPHERO_NAME);
                    if (newName != null) {
                        // old code and stuff
                    }
                }
                break;
            case REQUEST_SHOW_TUTORIAL:
                showingTutorial = false;
                resumeDrivingForActivity();
                break;
            case MenuActivity.PICK_SKILL_LEVEL_REQUEST:
                DriveActivity.updateDriveOptions();
                //changeSettingsButton();
                break;
        }
    }

    private void updateColorIndicatorView(int red, int green, int blue) {
        colorIndicatorView.setColor(red, green, blue);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        showMainMenu(findViewById(R.id.MenuButton));
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, "NC1ZA74EFRLLGC3RH1IL");
        AchievementManager.setupApplication("sphe58f7717a8d053dd035340646806fe191", "Hg6F7f5qWRi8gLzhiENJ", this);

        // Keep screen on.
        PowerManager power_manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        screenWakeLock = power_manager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                "DriveActivity");
        screenWakeLock.acquire();

        if (!driveControl.getRobotProvider().hasAdapter()) {
            return;
        }

        // launch the start up activity
        Intent startup = new Intent(this, StartupActivity.class);
        IntentFilter flurryFilter = new IntentFilter(StartupActivity.ACTION_FLURRY_LOG_BUY_SPHERO);
        registerReceiver(flurryReceiver, flurryFilter);
        startup.putExtra(StartupActivity.EXTRA_FLURRY_ENABLED, true);
        startActivityForResult(startup, STARTUP_REQUEST);
        switch (preferences.getControlMethod()) {
            case Preferences.JOYSTICK_CONTROL_METHOD:
                changeDriveMode(DriveMode.joy);
                break;
            case Preferences.TILT_CONTROL_METHOD:
                changeDriveMode(DriveMode.tilt);
                break;
            case Preferences.RC_CONTROL_METHOD:
                changeDriveMode(DriveMode.rc);
                break;
            default:
                changeDriveMode(DriveMode.joy);
                break;
        }
    }

    private void robotDisconnected() {
        mRobot = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.RobotDisconnected)
                .setMessage(R.string.ConnectionLost)
                .setPositiveButton(R.string.OK, null).show();
        try {
            unregisterReceiver(mDisconnectReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        AchievementManager.onStop();
        if (DEBUG) Log.d(LOG_TAG, "onStop()");

        if (driveMode == DriveControl.TILT) {
            driveControl.stopDriving();
        } else if (driveMode == DriveControl.RC) {
            rcControlView.stopControlSystem();
        }

        // end of driving
        if (mRobot != null) {
            String robot_id = mRobot.getUniqueId();
            preferences.setRobotId(robot_id);
        }

        try {
            unregisterReceiver(mDisconnectReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        RobotProvider.getDefaultProvider().removeAllControls();
        RobotProvider.getDefaultProvider().disconnectControlledRobots();
        RobotProvider.getDefaultProvider().setBroadcastContext(null);

        if (driveControl != null) {
            driveControl.setBroadcastContext(null);
        }


        // Stop preventing the screen from sleeping
        if (screenWakeLock != null) {
            screenWakeLock.release();
            screenWakeLock = null;
        }
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startActivity(startMain);
    }
}
