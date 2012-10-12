package com.orbotix;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StartActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
    }

    /*
     * Button press method to only detect Lows
     */
    public void onLowsClicked( View vClicked ) {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra(this.getString(R.string.frequencyband),this.getString(R.string.just_lows));
        this.startActivity(intent);
    }
    
    /*
     * Button press method to only detect Mids
     */
    public void onMidsClicked( View vClicked ) {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra(this.getString(R.string.frequencyband),this.getString(R.string.just_mids));
        this.startActivity(intent);
    }
    
    /*
     * Button press method to only detect Highs
     */
    public void onHighsClicked( View vClicked ) {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra(this.getString(R.string.frequencyband),this.getString(R.string.just_highs));
        this.startActivity(intent);
    }
    
    /*
     * Button press method to only detect All frequncies
     */
    public void onAllFrequenciesClicked( View vClicked ) {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra(this.getString(R.string.frequencyband),this.getString(R.string.all_frequencies));
        this.startActivity(intent);
    }
}
