package com.orbotix.spherocam.ui.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.orbotix.spherocam.R;

/**
 * A View that shows a settings menu list item
 *
 * Created by Orbotix Inc.
 * User: Adam
 * Date: 11/3/11
 * Time: 12:51 PM
 */
public class MenuListItemView extends RelativeLayout {

    private ImageView icon;
    private TextView text;

    public MenuListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Inflate settings list item layout xml
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.menu_list_item_view, this);

        this.icon = (ImageView)this.findViewById(R.id.icon);
        this.text = (TextView)this.findViewById(R.id.text);

        this.setBackgroundResource(R.drawable.menu_list_item_bg_state);

        if(attrs != null){
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SettingsListItemView);

            if(a.hasValue(R.styleable.SettingsListItemView_icon)){
                Drawable d = a.getDrawable(R.styleable.SettingsListItemView_icon);
                this.setIconDrawable(d);
            }

            if(a.hasValue(R.styleable.SettingsListItemView_text)){
                this.setText(a.getString(R.styleable.SettingsListItemView_text));
            }
        }
    }

    /**
     * Sets this MenuListItemView's icon to the provided Drawable
     * @param d
     */
    public void setIconDrawable(Drawable d){
        this.icon.setImageDrawable(d);
    }

    /**
     * Sets this MenuListItemView's text to the provided String
     * @param text
     */
    public void setText(String text){
        this.text.setText(text);
    }
}
