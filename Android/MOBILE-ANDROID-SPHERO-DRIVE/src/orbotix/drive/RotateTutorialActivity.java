package orbotix.drive;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewAnimator;
import com.flurry.android.FlurryAgent;
import orbotix.achievement.AchievementManager;
import orbotix.robot.base.*;
import orbotix.robot.utilities.RotationGestureDetector;
import orbotix.robot.utilities.SoundManager;
import orbotix.robot.widgets.calibration.CalibrationView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Orbotix Inc.
 * User: brandon
 * Date: 11/9/11
 * Time: 2:05 PM
 */
public class RotateTutorialActivity extends Activity {

    private static final int PLACE_SCREEN = 0;
    private static final int TAIL_LIGHT_SCREEN = 1;
    private static final int ROTATE_SCREEN = 2;
    private static final int STRAIGHT_AHEAD_SCREEN = 3;
    private static final int REMEMBER_SCREEN = 4;
    private static final long PLACE_ANIMATION_DELAY = 800;
    private static final long SPIN_ANIMATION_DELAY = 200;
    public static final String EXTRA_SPHERO_ID = "orbotix.sphero.SPHERO_ID";
    public static final String NOT_CONNECTED = "nc";


    private ViewAnimator mainViewAnimator;

    private Robot mRobot;
    private RotationGestureDetector rotationGestureDetector;
    private ImageView dot1, dot2;
    private boolean rotated = false;
    private boolean rotateScreenMode = false;
    private boolean lookingForTwoTouches = false;
    private TextView mRotateTextView;
    private View mArm, mDot;
    private Animation fadeInAnimation, fadeOutAnimation;
    private CalibrationView mCalibrationView;

    private Handler mHandler = new Handler();

    private RotationGestureDetector.OnRotationGestureListener gestureListener = new RotationGestureDetector.OnRotationGestureListener() {
        @Override
        public void onRotationGestureStarted(double distance, Point p1, Point p2) {
            // do nothing
        }

        @Override
        public void onRotationGestureRotated(double totalRotationAngle, double distance, Point p1, Point p2) {
            float heading = convertAngleToDegrees(2.0 * totalRotationAngle);
            if (rotateScreenMode) {
                if (!rotated && Math.abs(heading) > 40.0f) {
                    rotated = true;
                    changeRotateTextToAim();
                    mainViewAnimator.getCurrentView().findViewById(R.id.NextButton).setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onRotationGestureEnded(double finalAngle, double distance, Point p1, Point p2) {
            // do nothing
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rotate_tutorial);

        String robotId = getIntent().getStringExtra(EXTRA_SPHERO_ID);
        if (robotId.equalsIgnoreCase(NOT_CONNECTED)) {
            mRobot = null;
        } else {
            mRobot = RobotProvider.getDefaultProvider().findRobot(robotId);
        }

        rotationGestureDetector = new RotationGestureDetector();
        rotationGestureDetector.setOnRotationGestureListener(gestureListener);
        mainViewAnimator = (ViewAnimator)findViewById(R.id.TutorialAnimator);
        dot1 = (ImageView)findViewById(R.id.Dot1);
        dot2 = (ImageView)findViewById(R.id.Dot2);
    }

    private int getRelativeLeft(View myView){
        if(myView.getParent()==myView.getRootView())
            return myView.getLeft();
        else
            return myView.getLeft() + getRelativeLeft((View)myView.getParent());
    }


    private int getRelativeTop(View myView){
        if(myView.getParent()==myView.getRootView())
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View)myView.getParent());
    }

    public void onNextClicked(View v) {
        if (v.getId() == R.id.PlaceNextButton) {
            mHandler.removeCallbacks(animateArmDown);
            mainViewAnimator.setDisplayedChild(TAIL_LIGHT_SCREEN);
            animateDot();
            startSpheroSpinning();
        } else if (v.getId() == R.id.TailNextButton) {
            AbortMacroCommand.sendCommand(mRobot);
            FrontLEDOutputCommand.sendCommand(mRobot, 0.0f);
            mainViewAnimator.setDisplayedChild(ROTATE_SCREEN);
            setupRotateScreen();
        } else if (v.getId() == R.id.NextButton) {
            mainViewAnimator.setDisplayedChild(STRAIGHT_AHEAD_SCREEN);
            Macro macro = new Macro();
            macro.calibrate(0, 0);
            macro.roll(0.6f, 0, 0);
            macro.delay(1000);
            macro.roll(0.0f, 0, 0);
            SaveTemporaryMacroCommand.sendCommand(mRobot, (byte)0, macro.macroBytes());
            RunMacroCommand.sendCommand(mRobot, RunMacroCommand.TEMPORARY_MACRO_ID);
        }
    }

    private void setupRotateScreen() {
        rotated = false;
        lookingForTwoTouches = true;
        rotateScreenMode = true;
        if (mRotateTextView == null) {
            mRotateTextView = (TextView)mainViewAnimator.getCurrentView().findViewById(R.id.AimText);
        }
        if (mCalibrationView == null) {
            mCalibrationView = (CalibrationView)mainViewAnimator.getCurrentView().findViewById(R.id.CalibrationView);
            mCalibrationView.setColor(Color.WHITE);
            mCalibrationView.setCircleColor(Color.WHITE);
            mCalibrationView.setBackgroundColors(0x00000000, 0x00000000);
            mCalibrationView.setRobot(mRobot);
            mCalibrationView.setOnStartRunnable(new Runnable() {
                @Override
                public void run() {
                    dot1.setVisibility(View.INVISIBLE);
                    dot2.setVisibility(View.INVISIBLE);
                    SoundManager.playSound(DriveActivity.SOUND_ROTATE_IN);
                    SoundManager.playMusic(RotateTutorialActivity.this, R.raw.rotate_hum, true);
                }
            });
            mCalibrationView.setOnEndRunnable(new Runnable() {
                @Override
                public void run() {
                    dot1.setVisibility(View.VISIBLE);
                    dot2.setVisibility(View.VISIBLE);
                    SoundManager.stopMusic();
                    SoundManager.playSound(DriveActivity.SOUND_ROTATE_OUT);
                }
            });
            mCalibrationView.enable();
        }
        mainViewAnimator.getCurrentView().findViewById(R.id.NextButton).setVisibility(View.INVISIBLE);
        changeRotateTextToPlace();
    }

    private void changeRotateTextToPlace() {
        if (fadeOutAnimation == null) {
            fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            fadeOutAnimation.setDuration(200);
            fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            fadeInAnimation.setDuration(200);
        }
        mRotateTextView.setText(R.string.Place);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mRotateTextView.startAnimation(fadeInAnimation);
            }
        });
    }

    private void changeRotateTextToSpin() {
        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRotateTextView.setText(R.string.Spin);
                mRotateTextView.startAnimation(fadeInAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing
            }
        });
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mRotateTextView.startAnimation(fadeOutAnimation);
            }
        });
    }

    private void changeRotateTextToAim() {
        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRotateTextView.setText(R.string.Aim);
                mRotateTextView.startAnimation(fadeInAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing
            }
        });
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mRotateTextView.startAnimation(fadeOutAnimation);
            }
        });
    }

    public void onNoClicked(View v) {
        mainViewAnimator.setDisplayedChild(ROTATE_SCREEN);
        setupRotateScreen();
    }

    public void onYesClicked(View v) {
        mainViewAnimator.setDisplayedChild(REMEMBER_SCREEN);
    }

    public void onDoneClicked(View v) {
        finishUp();
    }

    public void onDontShowAgainClicked(View v) {
        SharedPreferences preferences = getSharedPreferences(DriveActivity.TUTORIAL_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(DriveActivity.PREFERENCE_SHOW_TUTORIAL, false);
        editor.commit();
        FlurryAgent.logEvent("clickedDoNotShowMeAgain");
        finishUp();
    }

    ///////////////////////////////////////////
    // arm animations
    ///////////////////////////////////////////

    private Animation armDown, armDisappear;
    private Runnable animateArmDown = new Runnable() {
        @Override
        public void run() {
            mArm.startAnimation(armDisappear);
        }
    };

    private void animateArm() {
        mArm = mainViewAnimator.getCurrentView().findViewById(R.id.Arm);
        armDisappear = AnimationUtils.loadAnimation(this, R.anim.arm_disappear);
        armDisappear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mArm.startAnimation(armDown);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing
            }
        });
        armDown = AnimationUtils.loadAnimation(this, R.anim.arm_down);
        armDown.setInterpolator(new DecelerateInterpolator(1.0f));
        armDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mHandler.postDelayed(animateArmDown, PLACE_ANIMATION_DELAY);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing
            }
        });
        mHandler.postDelayed(animateArmDown, PLACE_ANIMATION_DELAY);
    }

    ////////////////////////////////////////////
    // dot animations
    ////////////////////////////////////////////

    private Animation dotAcross;
    private Runnable animateDot = new Runnable() {
        @Override
        public void run() {
            mDot.setVisibility(View.VISIBLE);
            mDot.startAnimation(dotAcross);
        }
    };

    private void animateDot() {
        mDot = mainViewAnimator.getCurrentView().findViewById(R.id.Dot);
        dotAcross = AnimationUtils.loadAnimation(this, R.anim.dot_across);
        dotAcross.setInterpolator(new AccelerateDecelerateInterpolator());
        dotAcross.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mDot.setVisibility(View.INVISIBLE);
                mHandler.postDelayed(animateDot, SPIN_ANIMATION_DELAY);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing
            }
        });
        mHandler.post(animateDot);
    }

    private byte[] loadMacro(int resId) {
        try {
            InputStream inputStream = getResources().openRawResource(resId);
            byte[] bytes = new byte[inputStream.available()];
            int read = inputStream.read(bytes);
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();

        }
        return null;
    }

    private void startSpheroSpinning() {
        if (mRobot == null) return;
        SaveTemporaryMacroCommand.sendCommand(mRobot, (byte)0, loadMacro(R.raw.calibrate_spin));
        RunMacroCommand.sendCommand(mRobot, RunMacroCommand.TEMPORARY_MACRO_ID);
    }

    public static float convertAngleToDegrees(double angleInRadians) {
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (rotateScreenMode) {
            if (lookingForTwoTouches) {
                if (ev.getPointerCount() >= 2) {
                    lookingForTwoTouches = false;
                    changeRotateTextToSpin();
                }
            } else if (!rotated && ev.getPointerCount() < 2) {
                lookingForTwoTouches = true;
                changeRotateTextToPlace();
            }
        }
        rotationGestureDetector.onTouchEvent(ev);
        if (mCalibrationView != null) {
            mCalibrationView.interpretMotionEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AchievementManager.recordEvent("skipTutorial");
        finishUp();
    }

    private void finishUp() {
        finish();
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_through_bottom);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, "NSNS7AGI2SX52EAZN2EU");
        animateArm();
    }

    @Override
    protected void onStop() {
        super.onStop();
        AbortMacroCommand.sendCommand(mRobot);
        FrontLEDOutputCommand.sendCommand(mRobot, 0.0f);
        FlurryAgent.onEndSession(this);
    }
}