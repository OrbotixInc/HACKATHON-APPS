package com.orbotix.spherocam.ui.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import com.flurry.android.FlurryAgent;
import com.orbotix.spherocam.MainActivity;
import com.orbotix.spherocam.R;
import com.orbotix.spherocam.preferences.ColorPref;
import com.orbotix.spherocam.preferences.PreferencesManager;
import orbotix.robot.app.ColorPickerActivity;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.RunMacroCommand;
import orbotix.robot.utilities.SoundManager;

/**
 * The Activity that shows the settings menu
 * 
 * Created by Orbotix Inc.
 * User: Adam
 * Date: 11/3/11
 * Time: 4:43 PM
 */
public class MenuActivity extends Activity {

    /**
     * ID for starting share option dialog
     */
    private final int SHARE_OPTION_DIALOG = 0;

    /**
     * ID for showing gallery activity to get a picture
     */
    private final int GET_PICTURE = 0;

    /**
     * ID for showing the gallery activity to get a video
     */
    private final int GET_VIDEO = 1;

    /**
     * ID for showing the ColorPickerActivity to get a color
     */
    private final int COLOR_PICKER_ACTIVITY = 2;

    /**
     * ID for showing the SettingsActivity
     */
    private final int EXTRA_SETTINGS_ACTIVITY = 3;

    /**
     * Intent extra ID for the receiving the robot
     */
    public final static String EXTRA_ROBOT = "com.orbotix.spherocam.menu.robot";

    /**
     * Intent extra for telling this Activity to "roll" back to the MainActivity
     */
    public final static String EXTRA_ROLL = "com.orbotix.spherocam.menu.roll";

    private PreferencesManager prefs;

    private Robot robot;

    private SlideToSleepView slide_to_sleep;
    private MenuListItemView mColorButton;
    private MenuListItemView mSettingsButton;
    private MenuListItemView mShareButton;
    private RelativeLayout mSleepButton;
    private MenuListItemView mGuideButton;

    private boolean showing_slide_to_sleep;

    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        this.overridePendingTransition(R.anim.in_from_bottom, 0);

        this.setContentView(R.layout.menu_activity);

        this.prefs = new PreferencesManager(this);

        Intent i = this.getIntent();
        if(i.hasExtra(EXTRA_ROBOT)){
            final String id = i.getStringExtra(EXTRA_ROBOT);

            if(!id.equals("")){
                this.robot = RobotProvider.getDefaultProvider().findRobot(id);
            }
        }

        mColorButton    = (MenuListItemView)findViewById(R.id.color);
        mSettingsButton = (MenuListItemView)findViewById(R.id.settings);
        mShareButton    = (MenuListItemView)findViewById(R.id.share);
        mSleepButton    = (RelativeLayout)findViewById(R.id.sleep_button);
        mGuideButton    = (MenuListItemView)findViewById(R.id.guide);

        //Flurry
        FlurryAgent.onStartSession(this, "H62MI4Q9S6U1YECFVCNH");

        //Always return OK
        this.setResult(RESULT_OK);



    }

    @Override
    public void onPause(){
        super.onPause();

        mColorButton.setEnabled(false);
        mSettingsButton.setEnabled(false);
        mShareButton.setEnabled(false);
        mSleepButton.setEnabled(false);
        mGuideButton.setEnabled(false);
    }

    @Override
    public void onStop(){
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        
        mColorButton.setEnabled(true);
        mSettingsButton.setEnabled(true);
        mShareButton.setEnabled(true);
        mSleepButton.setEnabled(true);
        mGuideButton.setEnabled(true);
    }



    /**
     * When the user clicks on the "share" option, open the user's gallery.
     * @param v
     */
    public void onShareClick(View v){

        SoundManager.playSound(MainActivity.SOUND_BUTTON_PRESS);

        FlurryAgent.logEvent("Menu-SharePressed");
        
        this.showDialog(this.SHARE_OPTION_DIALOG);

    }

    /**
     * When the user clicks on teh "Settings" option, open the SettingsActivity
     * @param v
     */
    public void onSettingsClick(View v){

        FlurryAgent.logEvent("Menu-SettingsPressed");

        SoundManager.playSound(MainActivity.SOUND_BUTTON_PRESS);
        
        this.showExtraSettingsActivity();
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle opts){

        Dialog d = null;

        if(id == this.SHARE_OPTION_DIALOG){
            d = new ShareOptionDialog(this);
        }

        return d;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(0, R.anim.out_through_bottom);
    }

    @Override
    public void onActivityResult(int id, int result, Intent i){

        if(result == Activity.RESULT_OK){

            if(id == GET_PICTURE){

                this.shareContent(i.getData(), "image/*");

            }else if(id == GET_VIDEO){

                this.shareContent(i.getData(), "video/*");

            }else if(id == COLOR_PICKER_ACTIVITY){

                ColorPref color = new ColorPref(
                        i.getIntExtra(ColorPickerActivity.EXTRA_COLOR_RED, 0),
                        i.getIntExtra(ColorPickerActivity.EXTRA_COLOR_GREEN, 0),
                        i.getIntExtra(ColorPickerActivity.EXTRA_COLOR_BLUE, 0)
                );

                this.prefs.setColor(color);

                if(this.robot != null && this.robot.isConnected()){
                    RGBLEDOutputCommand.sendCommand(this.robot, color.red, color.green, color.blue);
                }

                if(i != null && i.hasExtra(ColorPickerActivity.EXTRA_ROLL) && i.getBooleanExtra(ColorPickerActivity.EXTRA_ROLL, false)){
                    this.finish();
                    overridePendingTransition(0, R.anim.out_through_bottom);
                }
            }


        }

        if(i != null && i.hasExtra(EXTRA_ROLL) && i.getBooleanExtra(EXTRA_ROLL, false)){
            this.finish();
            overridePendingTransition(0, R.anim.out_through_bottom);
        }
    }

    /**
     * When the user clicks on the "Color" option, open the color picker activity
     * @param v
     */
    public void onColorClick(View v){

        SoundManager.playSound(MainActivity.SOUND_BUTTON_PRESS);

        FlurryAgent.logEvent("Menu-ColorPressed");

        Intent i = new Intent(this, ColorPickerActivity.class);

        ColorPref color = this.prefs.getColor();

        i.putExtra(ColorPickerActivity.EXTRA_COLOR_RED, color.red);
        i.putExtra(ColorPickerActivity.EXTRA_COLOR_GREEN, color.green);
        i.putExtra(ColorPickerActivity.EXTRA_COLOR_BLUE, color.blue);

        i.putExtra(ColorPickerActivity.EXTRA_ROLL, true);

        this.startActivityForResult(i, COLOR_PICKER_ACTIVITY);
    }

    /**
     * When the user clicks on the "Sleep" option, show the SlideToSleepView
     * @param v
     */
    public void onSleepClick(View v) {

        SoundManager.playSound(MainActivity.SOUND_BUTTON_PRESS);

        FlurryAgent.logEvent("Menu-SleepPressed");

        if (slide_to_sleep == null) {
            this.slide_to_sleep = (SlideToSleepView)this.findViewById(R.id.slide_to_sleep);
            slide_to_sleep.setOnSleepListener(new SlideToSleepView.OnSleepListener() {
                @Override
                public void onSleep() {
                    if (robot != null) {
                        RunMacroCommand.sendCommand(robot, (byte) 2);
                    }
                    hideSleepSlider();
                }
            });
        }
        if (!showing_slide_to_sleep) {
            showSleepSlider();
        }
    }
    
    public void onGuideClick(View v){

        SoundManager.playSound(MainActivity.SOUND_BUTTON_PRESS);

        FlurryAgent.logEvent("Menu-GuidePressed");

        Intent i = new Intent(this, UserGuideActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        if(showing_slide_to_sleep){
            hideSleepSlider();
        }
        
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && showing_slide_to_sleep) {
            Rect bounds = new Rect(slide_to_sleep.getLeft(),
                    slide_to_sleep.getTop(),
                    slide_to_sleep.getRight(),
                    slide_to_sleep.getBottom());
            if (!bounds.contains((int) event.getX(), (int) event.getY())) {
                hideSleepSlider();
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void showSleepSlider() {
        Animation inAnimation = AnimationUtils.loadAnimation(this, R.anim.dropdown_in);
        slide_to_sleep.setVisibility(View.VISIBLE);
        slide_to_sleep.startAnimation(inAnimation);
        showing_slide_to_sleep = true;
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
                slide_to_sleep.setVisibility(View.INVISIBLE);
                showing_slide_to_sleep = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing
            }
        });
        slide_to_sleep.startAnimation(outAnimation);

    }

    private void shareContent(Uri uri, String mimetype){

        Intent share_i = new Intent(Intent.ACTION_SEND);
        share_i.setType(mimetype);
        share_i.putExtra(Intent.EXTRA_STREAM, uri);
        this.startActivity(share_i);
    }


    private void showPictureContentPicker(){

        this.dismissDialog(SHARE_OPTION_DIALOG);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        this.startActivityForResult(i, GET_PICTURE);

    }

    private void showVideoContentPicker(){

        this.dismissDialog(SHARE_OPTION_DIALOG);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("video/*");
        this.startActivityForResult(i, GET_VIDEO);
    }

    private void showExtraSettingsActivity(){

        Intent i = new Intent(this, SettingsActivity.class);
        this.startActivityForResult(i, EXTRA_SETTINGS_ACTIVITY);
    }

    /**
     * The Dialog to show the sharing options when the user wants to share
     */
    private class ShareOptionDialog extends Dialog {

        public ShareOptionDialog(Context context) {
            super(context);

            this.requestWindowFeature(Window.FEATURE_NO_TITLE);

            this.setContentView(R.layout.share_option_dialog);

            MenuListItemView pictures_item = (MenuListItemView)this.findViewById(R.id.share_picture_item);
            MenuListItemView video_item    = (MenuListItemView)this.findViewById(R.id.share_video_item);

            //Set listeners on the two options
            
            pictures_item.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    showPictureContentPicker();
                }
            });

            video_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showVideoContentPicker();
                }
            });

        }
    }
}
