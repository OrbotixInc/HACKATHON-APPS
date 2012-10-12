package orbotix.drive;

import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import orbotix.drive.R;

public class SensitivityButton {

	private final static int DEACTIVATED_BUTTON_COLOR = Color.argb(153, 0, 0, 0);
	private final static int DEACTIVATED_TEXT_COLOR = Color.rgb(100, 100, 100);
	private static final long DOUBLE_CLICK_DELAY = 200; // millis

	private ImageButton mButton;
	private TextView mText;
	private boolean pressedOnce;
	private String mSensitivityName;
	private String mDisplayName;
	private Handler mHandler = new Handler();

	public SensitivityButton(View baseLayout, String sensitivity) {
		mButton = (ImageButton)baseLayout.findViewWithTag(baseLayout.getResources().getString(R.string.sensitivity_button_tag));
		mText = (TextView)baseLayout.findViewWithTag(baseLayout.getResources().getString(R.string.sensitivity_text_tag));
		mSensitivityName = sensitivity;
		updateDisplayName();
		deactivate();
	}

	public void deactivate() {
		mButton.setColorFilter(DEACTIVATED_BUTTON_COLOR);
		mText.setTextColor(DEACTIVATED_TEXT_COLOR);
		unPress();
	}

	public void activate() {
		mButton.clearColorFilter();
		mText.setTextColor(Color.WHITE);
	}

	public ImageButton getButton() {
		return mButton;
	}

	public boolean doubleClick() {
		return pressedOnce;
	}

	public void press() {
		pressedOnce = true;
		activate();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				unPress();
			}

		}, DOUBLE_CLICK_DELAY);
	}

	public void unPress() {
		pressedOnce = false;
	}

	public String getSensitivity() {
		return mSensitivityName;
	}

	public String getDisplayName() {
		return mDisplayName;
	}

	public void setDisplayName(String newDisplayName) {
		mDisplayName = newDisplayName;
	}

    public void updateDisplayName() {
        mDisplayName = Preferences.getDefaultPreferences().getSensitivityName(mSensitivityName);
		mText.setText(mDisplayName);
    }

}
