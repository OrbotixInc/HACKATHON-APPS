package orbotix.drive;

import android.content.SharedPreferences;

public class Preferences {
	public static final String NAME = "orbotix.sphero.preferences";

	// color picker preferences
	private static final String RED_PREFERENCE_KEY = "redValue";
	private static final String GREEN_PREFERENCE_KEY = "greenValue";
	private static final String BLUE_PREFERENCE_KEY = "blueValue";

    // control methods
    public static final int JOYSTICK_CONTROL_METHOD = 101;
    public static final int TILT_CONTROL_METHOD = 102;
    public static final int RC_CONTROL_METHOD = 103;
    private static final String USED_JOYSTICK_KEY = "usedJoystick";
    private static final String USED_TILT_KEY = "usedTilt";
    private static final String USED_RC_KEY = "usedRC";

	// drive preferences
	private static final String SENSITIVITY_SETTING = "sensitivitySetting";
    private static final String VOLUME_KEY = "volume";
	// cautious
	private static final String CAUTIOUS_NAME_KEY = "cautiousName";
	public static final String CAUTIOUS_NAME = "Cautious";
	private static final String CAUTIOUS_MAX_SPEED_KEY = "cautiousMaxSpeed";
	private static final String CAUTIOUS_ROTATION_RATE_KEY = "cautiousRotationRate";
	private static final String CAUTIOUS_BOOST_TIME_KEY = "cautiousBoostTime";
	// comfortable
	private static final String COMFORTABLE_NAME_KEY = "comfortableName";
	public static final String COMFORTABLE_NAME = "Comfortable";
	private static final String COMFORTABLE_MAX_SPEED_KEY = "comfortableMaxSpeed";
	private static final String COMFORTABLE_ROTATION_RATE_KEY = "comfortableRotationRate";
	private static final String COMFORTABLE_BOOST_TIME_KEY = "comfortableBoostTime";
	// crazy
	private static final String CRAZY_NAME_KEY = "crazyName";
	public static final String CRAZY_NAME = "Crazy";
	private static final String CRAZY_MAX_SPEED_KEY = "crazyMaxSpeed";
	private static final String CRAZY_ROTATION_RATE_KEY = "crazyRotationRate";
	private static final String CRAZY_BOOST_TIME_KEY = "crazyBoostTime";

	// application preferences
	private static final String ROBOT_ID_KEY = "robotId";
	private static final String AUTO_HEADING_CORRECTION_ON_KEY = "autoCorrection";
    private static final String CONTROL_METHOD_KEY = "controlMethod";

	// default values
	private static final int DEFAULT_RED = 0;
	private static final int DEFAULT_GREEN = 0;
	private static final int DEFAULT_BLUE = 255;

	private static final String DEFAULT_SENSITIVITY_SETTING = COMFORTABLE_NAME;
	private static final String CAUTIOUS_DEFAULT_NAME = "Cautious";
	private static final String COMFORTABLE_DEFAULT_NAME = "Comfy";
	private static final String CRAZY_DEFAULT_NAME = "Crazy";
	private static final float CAUTIOUS_DEFAULT_MAX_SPEED = 0.5f;
	private static final float COMFORTABLE_DEFAULT_MAX_SPEED = 0.7f;
	private static final float CRAZY_DEFAULT_MAX_SPEED = 0.9f;
	private static final float CAUTIOUS_DEFAULT_ROTATION_RATE = 0.4f;
	private static final float COMFORTABLE_DEFAULT_ROTATION_RATE = 0.6f;
	private static final float CRAZY_DEFAULT_ROTATION_RATE = 0.7f;
	private static final float CAUTIOUS_DEFAULT_BOOST_TIME = 0.4f;
	private static final float COMFORTABLE_DEFAULT_BOOST_TIME = 0.6f;
	private static final float CRAZY_DEFAULT_BOOST_TIME = 1.0f;
    private static final float DEFAULT_VOLUME_SETTING = 0.5f;
	private static final boolean AUTO_HEADING_CORRECTION_ON_DEFAULT = false;

    private static final int DEFAULT_CONTROL_METHOD = JOYSTICK_CONTROL_METHOD;

	private static final String DEFAULT_ROBOT_ID = null;

	private static SharedPreferences systemPreferences = null;
	private static Preferences defaultPreferences = new Preferences();

	public static Preferences getDefaultPreferences() {
		return defaultPreferences;
	}

	public void setSystemPreferences(SharedPreferences pref) {
		systemPreferences = pref;
	}

	public String getRobotId() {
		return systemPreferences.getString(ROBOT_ID_KEY, DEFAULT_ROBOT_ID);
	}

	public void setRobotId(String id) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putString(ROBOT_ID_KEY, id);
		editor.commit();
	}

	public void setAutoHeadingCorrectionOn(boolean on) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putBoolean(AUTO_HEADING_CORRECTION_ON_KEY, on);
		editor.commit();
	}

	public boolean getAutoHeadingCorrectionOn() {
		return systemPreferences.getBoolean(AUTO_HEADING_CORRECTION_ON_KEY, AUTO_HEADING_CORRECTION_ON_DEFAULT);
	}

	public void resetSensitivity() {
		String currentSensitivitySetting = getSensitivitySetting();
		if (currentSensitivitySetting.equalsIgnoreCase(CAUTIOUS_NAME)) {
			resetCautiousSensitivity();
		} else if (currentSensitivitySetting.equalsIgnoreCase(CRAZY_NAME)) {
			resetCrazySensitivity();
		} else {
			resetComfortableSensitivity();
		}
	}

	public void resetCautiousSensitivity() {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putString(CAUTIOUS_NAME_KEY, CAUTIOUS_NAME);
		editor.putFloat(CAUTIOUS_MAX_SPEED_KEY, CAUTIOUS_DEFAULT_MAX_SPEED);
		editor.putFloat(CAUTIOUS_ROTATION_RATE_KEY, CAUTIOUS_DEFAULT_ROTATION_RATE);
		editor.putFloat(CAUTIOUS_BOOST_TIME_KEY, CAUTIOUS_DEFAULT_BOOST_TIME);
		editor.commit();
	}

	public void resetComfortableSensitivity() {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putString(COMFORTABLE_NAME_KEY, COMFORTABLE_DEFAULT_NAME);
		editor.putFloat(COMFORTABLE_MAX_SPEED_KEY, COMFORTABLE_DEFAULT_MAX_SPEED);
		editor.putFloat(COMFORTABLE_ROTATION_RATE_KEY, COMFORTABLE_DEFAULT_ROTATION_RATE);
		editor.putFloat(COMFORTABLE_BOOST_TIME_KEY, COMFORTABLE_DEFAULT_BOOST_TIME);
		editor.commit();
	}

	public void resetCrazySensitivity() {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putString(CRAZY_NAME_KEY, CRAZY_NAME);
		editor.putFloat(CRAZY_MAX_SPEED_KEY, CRAZY_DEFAULT_MAX_SPEED);
		editor.putFloat(CRAZY_ROTATION_RATE_KEY, CRAZY_DEFAULT_ROTATION_RATE);
		editor.putFloat(CRAZY_BOOST_TIME_KEY, CRAZY_DEFAULT_BOOST_TIME);
		editor.commit();
	}

	// Sensitivity
	public String getSensitivitySetting() {
		return systemPreferences.getString(SENSITIVITY_SETTING, DEFAULT_SENSITIVITY_SETTING);
	}

	public void setSensitivitySetting(String newSetting) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putString(SENSITIVITY_SETTING, newSetting);
		editor.commit();
	}

	public String getSensitivityName(String name) {
		if (name.equalsIgnoreCase(COMFORTABLE_NAME)) {
			return getComfortableSensitivityName();
		} else if (name.equalsIgnoreCase(CRAZY_NAME)) {
			return getCrazySensitivityName();
		} else {
			return getCautiousSensitivityName();
		}
	}

	public String getCautiousSensitivityName() {
		return systemPreferences.getString(CAUTIOUS_NAME_KEY, CAUTIOUS_DEFAULT_NAME);
	}

	public String getComfortableSensitivityName() {
		return systemPreferences.getString(COMFORTABLE_NAME_KEY, COMFORTABLE_DEFAULT_NAME);
	}

	public String getCrazySensitivityName() {
		return systemPreferences.getString(CRAZY_NAME_KEY, CRAZY_DEFAULT_NAME);
	}

	public void setCautiousSensitivityName(String newName) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putString(CAUTIOUS_NAME_KEY, newName);
		editor.commit();
	}

	public void setComfortableSensitivityName(String newName) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putString(COMFORTABLE_NAME_KEY, newName);
		editor.commit();
	}

	public void setCrazySensitivityName(String newName) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putString(CRAZY_NAME_KEY, newName);
		editor.commit();
	}

	public void setSensitivityName(String newName) {
		String currentSensitivitySetting = getSensitivitySetting();
		if (currentSensitivitySetting.equalsIgnoreCase(CAUTIOUS_NAME)) {
			setCautiousSensitivityName(newName);
		} else if (currentSensitivitySetting.equalsIgnoreCase(CRAZY_NAME)) {
			setCrazySensitivityName(newName);
		} else {
			setComfortableSensitivityName(newName);
		}
	}

	// Max Speed
	public float getMaxSpeed() {
		String currentSensitivitySetting = getSensitivitySetting();
		if (currentSensitivitySetting.equalsIgnoreCase(CAUTIOUS_NAME)) {
			return getCautiousMaxSpeed();
		} else if (currentSensitivitySetting.equalsIgnoreCase(CRAZY_NAME)) {
			return getCrazyMaxSpeed();
		} else {
			return getComfortableMaxSpeed();
		}
	}

	public void setMaxSpeed(float newSpeed) {
		String currentSensitivitySetting = getSensitivitySetting();
		if (currentSensitivitySetting.equalsIgnoreCase(CAUTIOUS_NAME)) {
			setCautiousMaxSpeed(newSpeed);
		} else if (currentSensitivitySetting.equalsIgnoreCase(CRAZY_NAME)) {
			setCrazyMaxSpeed(newSpeed);
		} else {
			setComfortableMaxSpeed(newSpeed);
		}
	}

	public float getCautiousMaxSpeed() {
		return systemPreferences.getFloat(CAUTIOUS_MAX_SPEED_KEY, CAUTIOUS_DEFAULT_MAX_SPEED);
	}

	public float getComfortableMaxSpeed() {
		return systemPreferences.getFloat(COMFORTABLE_MAX_SPEED_KEY, COMFORTABLE_DEFAULT_MAX_SPEED);
	}

	public float getCrazyMaxSpeed() {
		return systemPreferences.getFloat(CRAZY_MAX_SPEED_KEY, CRAZY_DEFAULT_MAX_SPEED);
	}

	public void setCautiousMaxSpeed(float newSpeed) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putFloat(CAUTIOUS_MAX_SPEED_KEY, newSpeed);
		editor.commit();
	}

	public void setComfortableMaxSpeed(float newSpeed) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putFloat(COMFORTABLE_MAX_SPEED_KEY, newSpeed);
		editor.commit();
	}

	public void setCrazyMaxSpeed(float newSpeed) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putFloat(CRAZY_MAX_SPEED_KEY, newSpeed);
		editor.commit();
	}

	// Rotation Rate
	public float getRotationRate() {
		String currentSensitivitySetting = getSensitivitySetting();
		if (currentSensitivitySetting.equalsIgnoreCase(CAUTIOUS_NAME)) {
			return getCautiousRotationRate();
		} else if (currentSensitivitySetting.equalsIgnoreCase(CRAZY_NAME)) {
			return getCrazyRotationRate();
		} else {
			return getComfortableRotationRate();
		}
	}

	public void setRotationRate(float newRate) {
		String currentSensitivitySetting = getSensitivitySetting();
		if (currentSensitivitySetting.equalsIgnoreCase(CAUTIOUS_NAME)) {
			setCautiousRotationRate(newRate);
		} else if (currentSensitivitySetting.equalsIgnoreCase(CRAZY_NAME)) {
			setCrazyRotationRate(newRate);
		} else {
			setComfortableRotationRate(newRate);
		}
	}

	public float getCautiousRotationRate() {
		return systemPreferences.getFloat(CAUTIOUS_ROTATION_RATE_KEY, CAUTIOUS_DEFAULT_ROTATION_RATE);
	}

	public float getComfortableRotationRate() {
		return systemPreferences.getFloat(COMFORTABLE_ROTATION_RATE_KEY, COMFORTABLE_DEFAULT_ROTATION_RATE);
	}

	public float getCrazyRotationRate() {
		return systemPreferences.getFloat(CRAZY_ROTATION_RATE_KEY, CRAZY_DEFAULT_ROTATION_RATE);
	}

	public void setCautiousRotationRate(float newRate) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putFloat(CAUTIOUS_ROTATION_RATE_KEY, newRate);
		editor.commit();
	}

	public void setComfortableRotationRate(float newRate) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putFloat(COMFORTABLE_ROTATION_RATE_KEY, newRate);
		editor.commit();
	}

	public void setCrazyRotationRate(float newRate) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putFloat(CRAZY_ROTATION_RATE_KEY, newRate);
		editor.commit();
	}

	// Boost Time
	public float getBoostTime() {
		String currentSensitivitySetting = getSensitivitySetting();
		if (currentSensitivitySetting.equalsIgnoreCase(CAUTIOUS_NAME)) {
			return getCautiousBoostTime();
		} else if (currentSensitivitySetting.equalsIgnoreCase(CRAZY_NAME)) {
			return getCrazyBoostTime();
		} else {
			return getComfortableBoostTime();
		}
	}

	public void setBoostTime(float newTime) {
		String currentSensitivitySetting = getSensitivitySetting();
		if (currentSensitivitySetting.equalsIgnoreCase(CAUTIOUS_NAME)) {
			setCautiousBoostTime(newTime);
		} else if (currentSensitivitySetting.equalsIgnoreCase(CRAZY_NAME)) {
			setCrazyBoostTime(newTime);
		} else {
			setComfortableBoostTime(newTime);
		}
	}

	public float getCautiousBoostTime() {
		return systemPreferences.getFloat(CAUTIOUS_BOOST_TIME_KEY, CAUTIOUS_DEFAULT_BOOST_TIME);
	}

	public float getComfortableBoostTime() {
		return systemPreferences.getFloat(COMFORTABLE_BOOST_TIME_KEY, COMFORTABLE_DEFAULT_BOOST_TIME);
	}

	public float getCrazyBoostTime() {
		return systemPreferences.getFloat(CRAZY_BOOST_TIME_KEY, CRAZY_DEFAULT_BOOST_TIME);
	}

	public void setCautiousBoostTime(float newTime) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putFloat(CAUTIOUS_BOOST_TIME_KEY, newTime);
		editor.commit();
	}

	public void setComfortableBoostTime(float newTime) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putFloat(COMFORTABLE_BOOST_TIME_KEY, newTime);
		editor.commit();
	}

	public void setCrazyBoostTime(float newTime) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putFloat(CRAZY_BOOST_TIME_KEY, newTime);
		editor.commit();
	}

	// LED Colors
	public int getRedLEDColor() {
		return systemPreferences.getInt(RED_PREFERENCE_KEY, DEFAULT_RED);
	}

	public void setRedLEDColor(int newRed) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putInt(RED_PREFERENCE_KEY, newRed);
		editor.commit();
	}

	public int getGreenLEDColor() {
		return systemPreferences.getInt(GREEN_PREFERENCE_KEY, DEFAULT_GREEN);
	}

	public void setGreenLEDColor(int newGreen) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putInt(GREEN_PREFERENCE_KEY, newGreen);
		editor.commit();
	}

	public int getBlueLEDColor() {
		return systemPreferences.getInt(BLUE_PREFERENCE_KEY, DEFAULT_BLUE);
	}

	public void setBlueLEDColor(int newBlue) {
		SharedPreferences.Editor editor = systemPreferences.edit();
		editor.putInt(BLUE_PREFERENCE_KEY, newBlue);
		editor.commit();
	}

    // Control Method
    public void setControlMethod(int controlMethod) {
        SharedPreferences.Editor editor = systemPreferences.edit();
        editor.putInt(CONTROL_METHOD_KEY, controlMethod);
        editor.commit();
    }

    public int getControlMethod() {
        return systemPreferences.getInt(CONTROL_METHOD_KEY, DEFAULT_CONTROL_METHOD);
    }

    // Volume
    public void setVolume(float volume) {
        SharedPreferences.Editor editor = systemPreferences.edit();
        editor.putFloat(VOLUME_KEY, volume);
        editor.commit();
    }

    public float getVolume() {
        return systemPreferences.getFloat(VOLUME_KEY, DEFAULT_VOLUME_SETTING);
    }

    public void usedJoystick() {
        SharedPreferences.Editor editor = systemPreferences.edit();
        editor.putBoolean(USED_JOYSTICK_KEY, true);
        editor.commit();
    }

    public void usedTilt() {
        SharedPreferences.Editor editor = systemPreferences.edit();
        editor.putBoolean(USED_TILT_KEY, true);
        editor.commit();
    }

    public void usedRC() {
        SharedPreferences.Editor editor = systemPreferences.edit();
        editor.putBoolean(USED_RC_KEY, true);
        editor.commit();
    }

    public boolean usedAllDriveModes() {
        if (systemPreferences.getBoolean(USED_JOYSTICK_KEY, false) &&
                systemPreferences.getBoolean(USED_TILT_KEY, false) &&
                systemPreferences.getBoolean(USED_RC_KEY, false)) {
            return true;
        }
        return false;
    }
}
