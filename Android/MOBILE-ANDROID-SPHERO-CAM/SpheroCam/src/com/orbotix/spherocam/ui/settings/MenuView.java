package com.orbotix.spherocam.ui.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import com.orbotix.spherocam.R;

/**
 * A View that shows the collection of SettingsListItemViews that is the settings menu
 *
 * Created by Orbotix Inc.
 * User: Adam
 * Date: 11/3/11
 * Time: 3:49 PM
 */
public class MenuView extends RelativeLayout {


    public MenuView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Inflate settings list item layout xml
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.menu_view, this);
    }
}
