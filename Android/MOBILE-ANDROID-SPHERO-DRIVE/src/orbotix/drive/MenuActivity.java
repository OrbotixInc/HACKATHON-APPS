package orbotix.drive;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewAnimator;
import com.flurry.android.FlurryAgent;
import orbotix.achievement.SpheroWorldWebView;
import orbotix.robot.app.ColorPickerActivity;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.widgets.ToggleView;
import orbotix.robot.base.*;
import orbotix.robot.utilities.SoundManager;

/**
 * Shows a full screen menu with options for:
 * <ul>
 *     <li>Settings</li>
 *     <li>Sphero World</li>
 *     <li>Tutorial</li>
 *     <li>Sleep</li>
 * </ul>
 *
 * @author Brandon Dorris
 */
public class MenuActivity extends Activity {

	public static final int PICK_COLOR_REQUEST = 11;
	public static final int PICK_SKILL_LEVEL_REQUEST = 12;

    private static final int FILTER_COLOR = Color.argb(200, 255, 255, 255);
    private boolean infoShowing = false;
    private boolean sleepSliderShowing = false;

    private SlideToSleepView mSlideToSleepView;
    private Robot mRobot;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ((ImageView)view).setColorFilter(FILTER_COLOR);
                    break;

                case MotionEvent.ACTION_UP:
                    ((ImageView)view).clearColorFilter();
                    break;
            }
            return false;
        }
    };

    private ViewAnimator mViewAnimator;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_menu);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mViewAnimator = (ViewAnimator)findViewById(R.id.MenuViewAnimator);
        String name = getCurrentName();
        if (name != null) {
            ((TextView)findViewById(R.id.NameText)).setText(name);
        }
        setupTouchListeners();
        mRobot = RobotProvider.getDefaultProvider().findRobot(getIntent().getStringExtra(StartupActivity.EXTRA_ROBOT_ID));
    }

    private void setupTouchListeners() {
        ((ImageView)findViewById(R.id.SpheroWorldButtonBackground)).setOnTouchListener(mTouchListener);
        ((ImageView)findViewById(R.id.SleepButtonBackground)).setOnTouchListener(mTouchListener);
        ((ImageView)findViewById(R.id.SettingsButtonBackground)).setOnTouchListener(mTouchListener);
        ((ImageView)findViewById(R.id.TutorialButtonBackground)).setOnTouchListener(mTouchListener);
    }

    public void onSpheroWorldButtonClicked(View v) {
        SoundManager.playSound(DriveActivity.SOUND_ITEM_SELECT);
        FlurryAgent.logEvent("Menu-SpheroWorldPressed");
        Intent spheroWorldIntent = new Intent(this, SpheroWorldWebView.class);
        startActivity(spheroWorldIntent);
    }

    public void onSleepButtonClicked(View v) {
        SoundManager.playSound(DriveActivity.SOUND_ITEM_SELECT);
        FlurryAgent.logEvent("Menu-SleepPressed");
        if (mSlideToSleepView == null) {
            mSlideToSleepView = (SlideToSleepView)findViewById(R.id.SlideToSleepView);
            mSlideToSleepView.setOnSleepListener(new SlideToSleepView.OnSleepListener() {
                @Override
                public void onSleep() {
                    if (mRobot != null) {
                        RunMacroCommand.sendCommand(mRobot, (byte)2);
                    }
                    hideSleepSlider();
                }
            });
        }
        if (!sleepSliderShowing) {
            showSleepSlider();
        }
    }

    public void onUserGuideClicked(View v) {
        SoundManager.playSound(DriveActivity.SOUND_ITEM_SELECT);
        FlurryAgent.logEvent("Menu-UserGuidePressed");
        Intent userGuideIntent = new Intent(this, UserGuideActivity.class);
        startActivity(userGuideIntent);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_through_bottom);
    }

    private void showSleepSlider() {
        Animation inAnimation = AnimationUtils.loadAnimation(this, R.anim.dropdown_in);
        mSlideToSleepView.setVisibility(View.VISIBLE);
        mSlideToSleepView.startAnimation(inAnimation);
        sleepSliderShowing = true;
    }

    private void hideSleepSlider() {
        Animation outAnimation = AnimationUtils.loadAnimation(this, R.anim.dropdown_out);
        outAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mSlideToSleepView.setVisibility(View.INVISIBLE);
                sleepSliderShowing = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing
            }
        });
        mSlideToSleepView.startAnimation(outAnimation);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && sleepSliderShowing) {
            Rect bounds = new Rect(mSlideToSleepView.getLeft(),
                    mSlideToSleepView.getTop(),
                    mSlideToSleepView.getRight(),
                    mSlideToSleepView.getBottom());
            if (!bounds.contains((int) event.getX(), (int) event.getY())) {
                hideSleepSlider();
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public void onSettingsButtonClicked(View v) {
        SoundManager.playSound(DriveActivity.SOUND_ITEM_SELECT);
        FlurryAgent.logEvent("Menu-SettingsButtonPressed");
        Intent picker_intent = new Intent(this, SettingsActivity.class);
        picker_intent.putExtra(SettingsActivity.EXTRA_FROM_MENU, true);
    	startActivityForResult(picker_intent, PICK_SKILL_LEVEL_REQUEST);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_through_left);
    }

    public void onTutorialButtonClicked(View v) {
        SoundManager.playSound(DriveActivity.SOUND_ITEM_SELECT);
        FlurryAgent.logEvent("Menu-TutorialButtonPressed");
        Intent tutorialIntent = new Intent(this, RotateTutorialActivity.class);
        if (mRobot != null) {
            tutorialIntent.putExtra(RotateTutorialActivity.EXTRA_SPHERO_ID, mRobot.getUniqueId());
        } else {
            tutorialIntent.putExtra(RotateTutorialActivity.EXTRA_SPHERO_ID, RotateTutorialActivity.NOT_CONNECTED);
        }
        startActivity(tutorialIntent);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_through_bottom);
    }

    public void onInfoButtonClicked(View v) {
        FlurryAgent.logEvent("Menu-InfoPressed");
        infoShowing = true;
        mViewAnimator.showNext();
    }

    public void onInfoDoneButtonClicked(View v) {
        mViewAnimator.showNext();
        infoShowing = false;
    }

    @Override
    public void onBackPressed() {
        SoundManager.playSound(DriveActivity.SOUND_ITEM_SELECT);
        if (infoShowing) {
            mViewAnimator.showNext();
            infoShowing = false;
        } else {
            finish();
            overridePendingTransition(R.anim.in_from_bottom, R.anim.out_through_bottom);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	switch (requestCode) {
    	case PICK_SKILL_LEVEL_REQUEST:
    		DriveActivity.updateDriveOptions();
            if (resultCode == SettingsActivity.RESULT_ROLL) {
                finish();
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_through_bottom);
            }
    		break;
    	case PICK_COLOR_REQUEST:
            if (resultCode == Activity.RESULT_OK) {
                Preferences preferences = Preferences.getDefaultPreferences();
    		    preferences.setRedLEDColor(data.getIntExtra(ColorPickerActivity.EXTRA_COLOR_RED, 0));
                preferences.setGreenLEDColor(data.getIntExtra(ColorPickerActivity.EXTRA_COLOR_GREEN, 0));
                preferences.setBlueLEDColor(data.getIntExtra(ColorPickerActivity.EXTRA_COLOR_BLUE, 0));
            }
    		break;
    	}
    }

    private String getCurrentName() {
        String currentName = null;
        try {
            currentName = DriveControl.INSTANCE.getRobot().getName();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return currentName;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (infoShowing) {
            mViewAnimator.showNext();
            infoShowing = false;
        } else {
            finish();
            overridePendingTransition(R.anim.dissolve_in, R.anim.dissolve_out);
        }
        return false;
    }

    private String getSoftwareVersion() {
        String version = null;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // do nothing
        }
        return version;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, "NC1ZA74EFRLLGC3RH1IL");
    }

    @Override
    protected void onStop() {
        super.onStop();

        FlurryAgent.onEndSession(this);
    }
}