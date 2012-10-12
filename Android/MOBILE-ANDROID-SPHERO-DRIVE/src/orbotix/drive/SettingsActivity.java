package orbotix.drive;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.flurry.android.FlurryAgent;
import orbotix.robot.utilities.SoundManager;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends Activity {

    public static final int RESULT_ROLL = 123;
    public static final String EXTRA_FROM_MENU = "orbotix.sphero.FROM_MENU";

	private static final int MAX_VALUE = 100;
	private static final String VALUE_FORMAT = "%1.1f";

    private boolean fromMenu;
	private int maxSpeedProgress;
	private int rotationRateProgress;
	private int boostProgress;
    private ViewAnimator viewAnimator;
    private boolean showingOptions = false;

	// sliders
	private SeekBar maxSpeedSeekBar;
	private SeekBar rotationRateSeekBar;
	private SeekBar boostSeekBar;
    private SeekBar volumeSeekBar;
	private TextView maxSpeedValueTextView;
	private TextView rotationRateValueTextView;
	private TextView boostValueTextView;

	// Name Box
	private EditText nameBox;

	// Sensitivity Buttons
	private SensitivityButton cautiousButton;
	private SensitivityButton comfortableButton;
	private SensitivityButton crazyButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        fromMenu = getIntent().getBooleanExtra(EXTRA_FROM_MENU, false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.options);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		nameBox = (EditText)findViewById(R.id.SensitivityNameBox);
		setupSensitivityButtons();

		Preferences preferences = Preferences.getDefaultPreferences();

		//Setup the SeekBars and value TextViews
		setupMaxSpeed();
		setupRotationRate();
		setupBoostTime();
        setupVolumeSlider();

        String currentSensitivitySetting = preferences.getSensitivitySetting();
		if (currentSensitivitySetting.equalsIgnoreCase(Preferences.COMFORTABLE_NAME)) {
			selectSensitivity(comfortableButton);
		} else if (currentSensitivitySetting.equalsIgnoreCase(Preferences.CRAZY_NAME)) {
			selectSensitivity(crazyButton);
		} else {
			selectSensitivity(cautiousButton);
		}

		linkButtons();
        startMarquees();
	}

    private void setupVolumeSlider() {
        volumeSeekBar = (SeekBar)findViewById(R.id.VolumeSlider);
        volumeSeekBar.setMax(100);
        volumeSeekBar.setProgress((int)(100 * Preferences.getDefaultPreferences().getVolume()));
        volumeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // do nothing
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float newVolume = (float)seekBar.getProgress()/100.0f;
                SoundManager.setVolume(newVolume);
                Preferences.getDefaultPreferences().setVolume(newVolume);
                Map<String, String> map = new HashMap<String, String>(1);
                map.put("NewVolume", Integer.toString(seekBar.getProgress()));
                FlurryAgent.logEvent("Settings-VolumeChanged", map);
            }
        });
    }

    private void startMarquees() {
        findViewById(R.id.CautiousSensitivityLabel).setSelected(true);
        findViewById(R.id.ComfortableSensitivityLabel).setSelected(true);
        findViewById(R.id.CrazySensitivityLabel).setSelected(true);
    }

	private void linkButtons() {

		Button optionsDoneButton = (Button)findViewById(R.id.OptionsDoneButton);
		optionsDoneButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Preferences.getDefaultPreferences().setSensitivityName(nameBox.getText().toString());
                updateSensitivityNames();
                viewAnimator.showPrevious();
                showingOptions = false;
			}

		});

		Button optionsResetButton = (Button)findViewById(R.id.OptionsResetButton);
		optionsResetButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Preferences preferences = Preferences.getDefaultPreferences();
				preferences.resetSensitivity();
				updateProgressBars();
				nameBox.setText(preferences.getSensitivityName(preferences.getSensitivitySetting()));
			}

		});
	}

    private void updateSensitivityNames() {
        cautiousButton.updateDisplayName();
        comfortableButton.updateDisplayName();
        crazyButton.updateDisplayName();
    }

	private void selectSensitivity(SensitivityButton sensitivity) {
		sensitivity.press();
		nameBox.setText(sensitivity.getDisplayName());
		Preferences.getDefaultPreferences().setSensitivitySetting(sensitivity.getSensitivity());
		updateProgressBars();
	}

	private void updateProgressBars() {
		Preferences preferences = Preferences.getDefaultPreferences();
		maxSpeedProgress = (int)(preferences.getMaxSpeed() * (float)MAX_VALUE);
		rotationRateProgress = (int)(preferences.getRotationRate() * (float)MAX_VALUE);
		boostProgress = (int)(preferences.getBoostTime() * (float)MAX_VALUE);

		maxSpeedSeekBar.setProgress(maxSpeedProgress);
		rotationRateSeekBar.setProgress(rotationRateProgress);
		boostSeekBar.setProgress(boostProgress);
	}

	private void setupSensitivityButtons() {
		viewAnimator = (ViewAnimator)findViewById(R.id.OptionsViewAnimator);
		cautiousButton = new SensitivityButton(findViewById(R.id.CautiousLayout), Preferences.CAUTIOUS_NAME);
		comfortableButton = new SensitivityButton(findViewById(R.id.ComfortableLayout), Preferences.COMFORTABLE_NAME);
		crazyButton = new SensitivityButton(findViewById(R.id.CrazyLayout), Preferences.CRAZY_NAME);

		cautiousButton.getButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                SoundManager.playSound(DriveActivity.SOUND_ITEM_SELECT);
				if (cautiousButton.doubleClick()) {
                    FlurryAgent.logEvent("Settings-Level1Customized");
					viewAnimator.showNext();
                    showingOptions = true;
					cautiousButton.unPress();
				} else {
                    FlurryAgent.logEvent("Settings-Level1Set");
					selectSensitivity(cautiousButton);
					comfortableButton.deactivate();
					crazyButton.deactivate();
				}
			}

		});
		comfortableButton.getButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                SoundManager.playSound(DriveActivity.SOUND_ITEM_SELECT);
				if (comfortableButton.doubleClick()) {
                    FlurryAgent.logEvent("Settings-Level2Customized");
					viewAnimator.showNext();
                    showingOptions = true;
					comfortableButton.unPress();
				} else {
                    FlurryAgent.logEvent("Settings-Level2Set");
					selectSensitivity(comfortableButton);
					cautiousButton.deactivate();
					crazyButton.deactivate();
				}
			}

		});
		crazyButton.getButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                SoundManager.playSound(DriveActivity.SOUND_ITEM_SELECT);
				if (crazyButton.doubleClick()) {
                    FlurryAgent.logEvent("Settings-Level3Customized");
					viewAnimator.showNext();
                    showingOptions = true;
					crazyButton.unPress();
				} else {
                    FlurryAgent.logEvent("Settings-Level3Set");
					selectSensitivity(crazyButton);
					cautiousButton.deactivate();
					comfortableButton.deactivate();
				}
			}

		});
	}

    @Override
    public void onBackPressed() {
        SoundManager.playSound(DriveActivity.SOUND_ITEM_SELECT);
        super.onBackPressed();
        finish();
        if (fromMenu) {
            overridePendingTransition(R.anim.in_from_left, R.anim.out_through_right);
        } else {
            overridePendingTransition(R.anim.in_from_bottom, R.anim.out_through_bottom);
        }
    }

	private void setupMaxSpeed() {
		maxSpeedSeekBar = (SeekBar)findViewById(R.id.MaxSpeedSeekBar);
		maxSpeedSeekBar.setMax(MAX_VALUE);

		maxSpeedValueTextView = (TextView)findViewById(R.id.MaxSpeedValueTextView);

		maxSpeedSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				maxSpeedProgress = seekBar.getProgress();
				Preferences.getDefaultPreferences().setMaxSpeed((float)maxSpeedProgress / (float)seekBar.getMax());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser)
			{
				maxSpeedValueTextView.setText(relativeValueString(progress));
			}
		});
	}

	private void setupRotationRate() {
		rotationRateSeekBar = (SeekBar)findViewById(R.id.RateSeekBar);
		rotationRateSeekBar.setMax(MAX_VALUE);

		rotationRateValueTextView = (TextView)findViewById(R.id.RateValueTextView);

		rotationRateSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				rotationRateProgress = seekBar.getProgress();
				Preferences.getDefaultPreferences().setRotationRate((float)rotationRateProgress / (float)seekBar.getMax());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				rotationRateValueTextView.setText(relativeValueString(progress));
			}
		});
	}

	private void setupBoostTime() {
		boostSeekBar = (SeekBar)findViewById(R.id.BoostSeekBar);
		boostSeekBar.setMax(MAX_VALUE);

		boostValueTextView = (TextView)findViewById(R.id.BoostValueTextView);

		boostSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				boostProgress = seekBar.getProgress();
				Preferences.getDefaultPreferences().setBoostTime((float)boostProgress / (float)seekBar.getMax());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				boostValueTextView.setText(relativeValueString(progress));
			}
		});
	}

    public void onRollButtonClicked(View v) {
        SoundManager.playSound(DriveActivity.SOUND_ITEM_SELECT);
        setResult(RESULT_ROLL);
        finish();
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_through_bottom);
    }

	@Override
	protected void onPause() {
		super.onPause();
		Preferences preferences = Preferences.getDefaultPreferences();
		preferences.setMaxSpeed(maxSpeedProgress/(float)MAX_VALUE);
		preferences.setRotationRate(rotationRateProgress/(float)MAX_VALUE);
		preferences.setBoostTime(boostProgress/(float)MAX_VALUE);
	}

	private String relativeValueString(int value) {
		return String.format(VALUE_FORMAT, ((float)value / 100.0f));
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
