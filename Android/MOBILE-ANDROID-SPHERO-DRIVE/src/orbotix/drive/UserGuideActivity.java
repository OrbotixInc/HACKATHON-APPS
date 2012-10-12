package orbotix.drive;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import com.flurry.android.FlurryAgent;

/**
 * Created by Orbotix Inc.
 * User: brandon
 * Date: 12/30/11
 * Time: 2:00 PM
 */
public class UserGuideActivity extends Activity {
    WebView webview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.user_guide);
        webview = (WebView) findViewById(R.id.WebView);
        webview.loadUrl("file:///android_asset/user_guide.html");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_through_bottom);
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