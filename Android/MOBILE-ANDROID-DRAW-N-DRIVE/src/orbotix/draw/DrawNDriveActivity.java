package orbotix.draw;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import com.flurry.android.FlurryAgent;
import orbotix.macro.*;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.*;
import orbotix.robot.utilities.SoundManager;
import orbotix.robot.widgets.calibration.CalibrationView;

import java.util.Random;

/**
 * Created by IntelliJ IDEA. User: brandon Date: 9/27/11 Time: 10:20 AM To change this template use File | Settings |
 * File Templates.
 */
public class DrawNDriveActivity extends Activity implements DrawingView.PathListener {

    public static final int STARTUP_REQUEST = 101;
    private static final int DELAY_BETWEEN_COMMANDS = 250;
    private static final int MINIMUM_PIXEL_DISTANCE = 10;
    private static final float OLD_MACRO_VERSION_NUMBER_THRESHOLD = 0.951f;
    private static final String DRAW_N_DRIVE_PREFERENCES = "orbotix.sphero.draw.PREFERENCES";
    private static final String PREF_RED = "orbotix.sphero.draw.RED";
    private static final String PREF_GREEN = "orbotix.sphero.draw.GREEN";
    private static final String PREF_BLUE = "orbotix.sphero.draw.BLUE";
    private static final int SOUND_BUTTON_PRESS = 101;
    private static final int SOUND_TRAY_SLIDE = 107;
    private static final int SOUND_ROTATE_IN = 108;
    private static final int SOUND_ROTATE_OUT = 109;

    private DrawingView mDrawingView;
    private Robot mRobot;
    private Macro macro, replayMacro;
    private MacroObject macroObject;
    private ImageButton replayButton;
    private PowerManager.WakeLock mWakeLock;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    private boolean stopping = false;

    private Point lastPoint;
    private int lastHeading;
    private int firstHeading;
    private int colorWheelPointer, drawingViewPointer = -1;
    private boolean recording;
    private boolean firstTime;
    private boolean coloring = false;
    private boolean colorWheelHidden = false;
    private boolean animatingColorWheel = false;

    private int calibrationCount;
    private int currentColor;
    private int streamingCommandCount;

    private int calibrationReferenceX;
    private ColorWheelView mColorWheelView;
    private View mColorPickerLayout;
    private float mFirmwareVersion = 0.95f; // gets updated in onActivityResult()
    private AlertDialog mDisconnectDialog;

    private CalibrationView mCalibrationView;
    private Handler mHandler = new Handler();

    private BroadcastReceiver mDisconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(RobotProvider.ACTION_ROBOT_DISCONNECTED)) {
                robotDisconnected();
            }
        }
    };

    private Runnable startRotating = new Runnable() {
        @Override
        public void run() {
            FlurryAgent.logEvent("Calibrate", true);
            mDrawingView.pauseDrawing();
            mDrawingView.clearCanvas();
            recording = false;
            macro = null;
            SoundManager.playSound(SOUND_ROTATE_IN);
            SoundManager.playMusic(DrawNDriveActivity.this, R.raw.rotate_hum, true);
        }
    };

    private Runnable stopRotating = new Runnable() {
        @Override
        public void run() {
            FlurryAgent.endTimedEvent("Calibrate");
            mDrawingView.resumeDrawing();
            SoundManager.stopMusic();
            SoundManager.playSound(SOUND_ROTATE_OUT);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mCalibrationView = (CalibrationView)findViewById(R.id.CalibrationView);
        mCalibrationView.setColor(Color.WHITE);
        mCalibrationView.setCircleColor(Color.WHITE);
        mCalibrationView.setOnStartRunnable(startRotating);
        mCalibrationView.setOnEndRunnable(stopRotating);
        mCalibrationView.enable();
        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);

        mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "KeepDrawing");
        mDrawingView = (DrawingView)findViewById(R.id.DrawingView);
        mDrawingView.setPathListener(this);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        calibrationCount = 0;
        mColorWheelView = (ColorWheelView)findViewById(R.id.ColorWheel);
        mColorWheelView.setColorEventListener(new ColorWheelView.ColorEventListener() {
            @Override
            public void colorChanged(int newColor) {
                mDrawingView.setColor(newColor);
                if (mFirmwareVersion > OLD_MACRO_VERSION_NUMBER_THRESHOLD) {
                    if (macroObject == null || !macroObject.isRunning()) {
                        changeStandingColor(newColor);
                    } else if (!recording) {
                        macroObject.addCommand(new RGB(Color.red(newColor), Color.green(newColor), Color.blue(newColor), 0));
                        macroObject.playMacro();
                    }
                } else {
                    if (macro == null || !recording) {
                        changeStandingColor(newColor);
                    }
                }
                FlurryAgent.logEvent("ColorChanged");
            }

            @Override
            public void colorTrackingStarted(int pointerId) {
                mCalibrationView.disable();
                colorWheelPointer = pointerId;
                coloring = true;
            }

            @Override
            public void colorTrackingEnded(int pointerId) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCalibrationView.enable();
                    }
                }, 300);
                colorWheelPointer = -1;
                coloring = false;
            }

            @Override
            public void hideButtonPressed() {
                colorWheelButtonPressed();
            }
        });
        replayButton = (ImageButton)findViewById(R.id.ReplayButton);
        mColorPickerLayout = findViewById(R.id.ColorPickerLayout);
        mPreferences = getSharedPreferences(DRAW_N_DRIVE_PREFERENCES, MODE_PRIVATE);
        mEditor = mPreferences.edit();
        int red = mPreferences.getInt(PREF_RED, 0);
        int green = mPreferences.getInt(PREF_GREEN, 147);
        int blue = mPreferences.getInt(PREF_BLUE, 208);
        currentColor = Color.rgb(red, green, blue);
        mColorWheelView.setNewColor(currentColor);
    }

    @Override
    public void onStart() {
        super.onStart();
        mWakeLock.acquire();
        FlurryAgent.onStartSession(this, "UM9US3DLE755AHHQQFZM");
        stopping = false;
        if (mRobot == null || !mRobot.isConnected()) {
            Intent startupIntent = new Intent(this, StartupActivity.class);
            startActivityForResult(startupIntent, STARTUP_REQUEST);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        onUserGuidePressed(null);
        return false;
    }

    public void onUserGuidePressed(View v) {
        Intent userGuideIntent = new Intent(this, UserGuideActivity.class);
        startActivity(userGuideIntent);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_through_bottom);
    }

    private void setupSounds() {
        SoundManager.initialize(this);
        SoundManager.setVolume(0.7f);
        SoundManager.addSound(this, SOUND_BUTTON_PRESS, R.raw.button_press);
        SoundManager.addSound(this, SOUND_TRAY_SLIDE, R.raw.tray_slide);
        SoundManager.addSound(this, SOUND_ROTATE_IN, R.raw.rotate_in);
        SoundManager.addSound(this, SOUND_ROTATE_OUT, R.raw.rotate_out);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mWakeLock.release();
        stopping = true;
        FlurryAgent.onEndSession(this);
        AbortMacroCommand.sendCommand(mRobot);
        RollCommand.sendStop(mRobot);
        try {
            unregisterReceiver(mDisconnectReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RobotProvider.getDefaultProvider().removeAllControls();
        if (mDisconnectDialog != null && mDisconnectDialog.isShowing()) {
            mDisconnectDialog.dismiss();
        }

        if (!isFinishing()) {
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case STARTUP_REQUEST:
                setupSounds();
                if (resultCode == StartupActivity.RESULT_OK) {
                    mRobot = RobotProvider.getDefaultProvider().findRobot(data.getStringExtra(StartupActivity.EXTRA_ROBOT_ID));
                    DeviceMessenger.getInstance().addResponseListener(mRobot, new DeviceMessenger.DeviceResponseListener() {
                        @Override
                        public void onResponse(DeviceResponse response) {
                            if (response instanceof VersioningResponse) {
                                mFirmwareVersion = Float.parseFloat(((VersioningResponse) response).getMainApplicationVersion());
                                DeviceMessenger.getInstance().removeResponseListener(mRobot, this);
                            }
                        }
                    });
                    mCalibrationView.setRobot(mRobot);
                    VersioningCommand.sendCommand(mRobot);
                    RobotProvider.getDefaultProvider().setBroadcastContext(this);
                    IntentFilter disconnectFilter = new IntentFilter(RobotProvider.ACTION_ROBOT_DISCONNECTED);
                    registerReceiver(mDisconnectReceiver, disconnectFilter);
                    // for fast aiming
                    RotationRateCommand.sendCommand(mRobot, 1.0f);
                    RGBLEDOutputCommand.sendCommand(mRobot, Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor));
                }
                break;

            default:
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int pointers = event.getPointerCount();
        for (int i = 0; i < pointers; i++) {
            int id = event.getPointerId(i);
            // touch pointers already down
            if (id == drawingViewPointer) {
                mDrawingView.useTouchEvent(event, i);
                continue;
            } else if (id == colorWheelPointer) {
                mColorWheelView.useTouchEvent(event, i);
                continue;
            }

            // new down touches
            if (mColorWheelView.canUseTouchEvent(event, i)) {
                mColorWheelView.useTouchEvent(event, i);
            } else if (mColorWheelView.canUseTouchEventForButton(event, i)) {
                mColorWheelView.useTouchEvent(event, i);
            } else if (event.getAction() == MotionEvent.ACTION_DOWN && touchesReplayButton(event, i)) {
                onReplayClicked(null);
                FlurryAgent.logEvent("ReplayPressed");
            } else {
                mDrawingView.useTouchEvent(event, i);
            }
        }
        mCalibrationView.interpretMotionEvent(event);

        return true;
    }

    private boolean touchesReplayButton(MotionEvent event, int index) {
        float x = event.getX(index);
        float y = event.getY(index);

        if (x > replayButton.getLeft() && x < replayButton.getRight()) {
            if (y > replayButton.getTop() && y < replayButton.getBottom()) {
                return true;
            }
        }

        return false;
    }

    private void colorWheelButtonPressed() {
        SoundManager.playSound(SOUND_TRAY_SLIDE);
        if (colorWheelHidden) {
            showColorWheel();
        } else {
            hideColorWheel();
        }
    }

    private void showColorWheel() {
        if (animatingColorWheel) return;
        FlurryAgent.logEvent("ColorTrayShown");
        animatingColorWheel = true;
        Animation showAnimation = AnimationUtils.loadAnimation(this, R.anim.show_color_wheel);
        showAnimation.setInterpolator(new DecelerateInterpolator(1.5f));
        showAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                colorWheelHidden = false;
                animatingColorWheel = false;
                mColorWheelView.setHidden(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing
            }
        });
        mColorPickerLayout.setVisibility(View.VISIBLE);
        mColorPickerLayout.startAnimation(showAnimation);
    }

    private void hideColorWheel() {
        if (animatingColorWheel) return;
        FlurryAgent.logEvent("ColorTrayHidden");
        animatingColorWheel = true;
        Animation hideAnimation = AnimationUtils.loadAnimation(this, R.anim.hide_color_wheel);
        hideAnimation.setInterpolator(new DecelerateInterpolator(1.5f));
        hideAnimation.setFillAfter(true);
        hideAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //mColorPickerLayout.setVisibility(View.INVISIBLE);
                colorWheelHidden = true;
                animatingColorWheel = false;
                mColorWheelView.setHidden(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing
            }
        });
        mColorPickerLayout.startAnimation(hideAnimation);

    }

    @Override
    public void pathDidStart(Point point, int pointerId) {
        FlurryAgent.logEvent("DrawShape", true);
        drawingViewPointer = pointerId;
        lastPoint = point;
        recording = true;
        updateColor();
        if (mFirmwareVersion > OLD_MACRO_VERSION_NUMBER_THRESHOLD) {
            if (macroObject == null) {
                macroObject = new MacroObject();
                macroObject.setMode(MacroObject.MacroObjectMode.CachedStreaming);
                macroObject.setRobot(mRobot);
            }
            macroObject.addCommand(new SD1(DELAY_BETWEEN_COMMANDS));
            macroObject.addCommand(new SPD1(0.5f));
            macroObject.addCommand(new RotationRate(0.8f));
            macroObject.addCommand(new SPD2(0.0f));
            macroObject.addCommand(new RGB(Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor), 0));
        } else {
            macro = new Macro();
            macro.setSD1(DELAY_BETWEEN_COMMANDS);
            macro.setSPD1(0.5f); //driving speed
            macro.rotationRate(0.8f);
            macro.setSPD2(0.0f); //Speed for turning
            macro.rgb(Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor), 0);
        }
    }

    private void startCalibration(Point point) {
        if (mRobot == null) {
            return;
        }
        calibrationReferenceX = point.x;
        CalibrateCommand.sendCommand(mRobot, 0.0f);
        FrontLEDOutputCommand.sendCommand(mRobot, 1.0f);

    }

    private void handleCalibration(Point point) {
        if (mRobot == null) {
            return;
        }
        int difference = point.x - calibrationReferenceX;
        double radians = (double)difference / 500.0 * 2 * Math.PI;
        float degrees = convertAngleToDegrees(radians);
        RollCommand.sendCommand(mRobot, degrees, 0.0f, true);
    }

    private void stopCalibration() {
        if (mRobot == null) {
            return;
        }
        CalibrateCommand.sendCommand(mRobot, 0.0f);
        FrontLEDOutputCommand.sendCommand(mRobot, 0.0f);
    }

    @Override
    public void pathDidChange(Point point, float pressure) {
        Random random = new Random();
        //mDrawingView.setColor(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
        handleNormalMode(point, pressure);
    }

    private void sendStreamChunk() {
        SaveMacroCommand.sendCommand(mRobot, (byte)0, SaveMacroCommand.MACRO_STREAMING_DESTINATION, macro.macroBytes());
        macro = new Macro();
        streamingCommandCount = 0;
    }

    private boolean updateColor() {
        int color = mDrawingView.getColor();
        boolean changed = false;
        if (color != currentColor) {
            currentColor = color;
            mEditor.putInt(PREF_RED, Color.red(currentColor));
            mEditor.putInt(PREF_GREEN, Color.green(currentColor));
            mEditor.putInt(PREF_BLUE, Color.blue(currentColor));
            mEditor.commit();
            changed = true;
        }

        return changed;
    }

    private void changeStandingColor(int newColor) {
        RGBLEDOutputCommand.sendCommand(mRobot, Color.red(newColor), Color.green(newColor), Color.blue(newColor));
    }

    private void handleNormalMode(Point point, float pressure) {

        float distanceBetweenLastTwoPoints = distanceBetweenPoint(lastPoint, point);
        if (recording && distanceBetweenLastTwoPoints > MINIMUM_PIXEL_DISTANCE) {

            //Calculate an additional delay based on distance between points
            int delay = (int)((distanceBetweenLastTwoPoints / (MINIMUM_PIXEL_DISTANCE * 2.5)) * DELAY_BETWEEN_COMMANDS) - DELAY_BETWEEN_COMMANDS;

            int heading = Math.round(headingFrom(lastPoint, point));
            if (firstTime) {
                firstHeading = heading;
                lastHeading = firstHeading;
                firstTime = false;
            }
            float lastHeadingDifference = Math.abs(heading - lastHeading);
            if (lastHeadingDifference > 180.0) {
                lastHeadingDifference = 360 - lastHeadingDifference;
            }

            if (updateColor()) {
                if (mFirmwareVersion > OLD_MACRO_VERSION_NUMBER_THRESHOLD) {
                    if (macroObject == null) return;
                    macroObject.addCommand(new RGB(Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor), 0));
                } else {
                    if (macro == null) return;
                    macro.rgb(Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor), 0);
                }
            }

            if (lastHeadingDifference > 45.0) {
                if (mFirmwareVersion > OLD_MACRO_VERSION_NUMBER_THRESHOLD) {
                    macroObject.addCommand(new RollSD1SPD2(lastHeading));
                    macroObject.addCommand(new WaitUntilStop(1000));
                    macroObject.addCommand(new RollSD1SPD2(heading));
                    macroObject.addCommand(new WaitUntilStop(1000));
                    macroObject.addCommand(new Delay(100));
                    macroObject.addCommand(new RollSD1SPD1(heading));
                    if (delay > 0) {
                        macroObject.addCommand(new Delay(delay));
                    }
                } else {
                    macro.rollSD1SPD2(lastHeading);
                    macro.delay(400);
                    macro.rollSD1SPD2(heading);
                    macro.rollSD1SPD1(heading);
                    if(delay > 0) {
                        macro.delay(delay);
                    }
                }
            } else {
                if (mFirmwareVersion > OLD_MACRO_VERSION_NUMBER_THRESHOLD) {
                    macroObject.addCommand(new RollSD1SPD1(heading));
                    if (delay > 0) {
                        macroObject.addCommand(new Delay(delay));
                    }
                } else {
                    macro.rollSD1SPD1(heading);
                    if(delay > 0) {
                        macro.delay(delay);
                    }
                }

            }

            if(mFirmwareVersion < OLD_MACRO_VERSION_NUMBER_THRESHOLD && macro.macroLength() >= 245) {  //be sure our last command is a stop command if macro is full
                macro.rollSD1SPD2(lastHeading);
                mDrawingView.pauseDrawing();
            }

            lastHeading = heading;
            lastPoint = point;
        }
    }

    private float distanceBetweenPoint(Point point1, Point point2) {
        float xDifference = point1.x - point2.x;
        float yDifference = point1.y - point2.y;
        return (float)Math.sqrt(xDifference*xDifference + yDifference*yDifference);
    }

    private float headingFrom(Point point1, Point point2) {
        float xDifference = point2.x - point1.x;
        float yDifference = point2.y - point1.y;

        float radians = (float)(Math.atan2(yDifference, xDifference) + Math.PI / 2.0);
        float degrees = convertAngleToDegrees(radians);

        return degrees;
    }

    @Override
    public void pathDidEnd(Point point, int pointerId) {
        FlurryAgent.endTimedEvent("DrawShape");
        drawingViewPointer = -1;
        if (!recording) {
            // the macro was cancelled and should not be run
            return;
        }


        if (mFirmwareVersion > OLD_MACRO_VERSION_NUMBER_THRESHOLD) {
            if (macroObject == null) return;
            macroObject.addCommand(new RollSD1(0.6f, lastHeading));
            macroObject.addCommand(new RollSD1(0.4f, lastHeading));
            macroObject.addCommand(new RollSD1SPD2(lastHeading));
            macroObject.playMacro();
        } else {
            if (macro == null) return;
            macro.rollSD1(0.6f, lastHeading);
            macro.rollSD1(0.4f, lastHeading);
            macro.rollSD1SPD2(lastHeading);

            Log.d("Orbotix", String.format("Macro Length: %d", macro.macroLength()));
            SaveTemporaryMacroCommand.sendCommand(mRobot, SaveTemporaryMacroCommand.MacroFlagMotorControl, macro.macroBytes());
            RollCommand.sendCommand(mRobot, firstHeading, 0.0f, true);
            RunMacroCommand.sendCommand(mRobot, RunMacroCommand.TEMPORARY_MACRO_ID);
            replayMacro = macro;

        }

        firstTime = true;
        recording = false;
        calibrationCount++;
        if(calibrationCount >= 3) {
            // TODO: show the calibrate message
        }

        mDrawingView.resumeDrawing();
    }

    @Override
    public void pathCancelled() {
        if (recording) {
            FlurryAgent.endTimedEvent("DrawShape");
        }
        drawingViewPointer = -1;
        recording = false;
        if (mRobot != null) {
            AbortMacroCommand.sendCommand(mRobot);
            RollCommand.sendCommand(mRobot, lastHeading, 0.0f, true);
            RGBLEDOutputCommand.sendCommand(mRobot, Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor));
        }
        replayMacro = null;
        if (macroObject != null) {
            macroObject.stopMacro();
            macroObject = null;
        }
    }

    /*@Override
    public void onRotationGestureStarted(double distance, Point p1, Point p2) {
        if (coloring) return;
        FlurryAgent.logEvent("Calibrate", true);
        FrontLEDOutputCommand.sendCommand(mRobot, 1.0f);
        CalibrateCommand.sendCommand(mRobot, 0.0f);
        mDrawingView.pauseDrawing();
        mDrawingView.clearCanvas();
        recording = false;
        macro = null;
    }

    @Override
    public void onRotationGestureRotated(double totalRotationAngle, double distance, Point p1, Point p2) {
        if (coloring) return;
        float heading = convertAngleToDegrees(2.0 * totalRotationAngle);
        RollCommand.sendCommand(mRobot, heading, 0.0f, true);
    }
    
    @Override
    public void onRotationGestureEnded(double finalAngle, double distance, Point p1, Point p2) {
        if (coloring) return;
        FlurryAgent.endTimedEvent("Calibrate");
        FrontLEDOutputCommand.sendCommand(mRobot, 0.0f);
        CalibrateCommand.sendCommand(mRobot, 0.0f);
        mDrawingView.resumeDrawing();
    }*/

    public void onReplayClicked(View v) {
        SoundManager.playSound(SOUND_BUTTON_PRESS);
        if (mFirmwareVersion > OLD_MACRO_VERSION_NUMBER_THRESHOLD ) {
            if (macroObject != null) {
                macroObject.playMacro();
            }
        } else {
            if (replayMacro != null) {
                SaveTemporaryMacroCommand.sendCommand(mRobot, SaveTemporaryMacroCommand.MacroFlagMotorControl, replayMacro.macroBytes());
                RollCommand.sendCommand(mRobot, firstHeading, 0.0f, true);
                RunMacroCommand.sendCommand(mRobot, RunMacroCommand.TEMPORARY_MACRO_ID);
            }
        }
    }

    private void robotDisconnected() {
        if (mRobot != null) {
            try {
                unregisterReceiver(mDisconnectReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            mRobot = null;
            if (!stopping) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                mDisconnectDialog = builder.setTitle(R.string.RobotDisconnected)
                .setMessage(R.string.ConnectionLost)
                .setPositiveButton(R.string.OK, null).create();
                mDisconnectDialog.show();
            }
        }
    }

    private float convertAngleToDegrees(double angleInRadians) {
        float angleInDegrees = (float)Math.toDegrees(angleInRadians);
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

}