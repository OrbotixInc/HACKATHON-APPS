package com.orbotix.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.orbotix.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: michaeldephillips
 * Date: 7/6/12
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class RobotArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private ArrayList<RobotWrapper> robots;
    private ArrayList<String> listStrings;

    public RobotArrayAdapter(Context context, ArrayList<String> values) {
        super(context, R.layout.listelement, values);
        robots = new ArrayList<RobotWrapper>();
        listStrings = values;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.listelement, parent, false);
        TextView view = robots.get(position).getView(context);

        // Change icon based on name
        //String s = values[position];

       // System.out.println(s);

//        if (s.equals("WindowsMobile")) {
//            imageView.setImageResource(R.drawable.windowsmobile_logo);
//        } else if (s.equals("iOS")) {
//            imageView.setImageResource(R.drawable.ios_logo);
//        } else if (s.equals("Blackberry")) {
//            imageView.setImageResource(R.drawable.blackberry_logo);
//        } else {
//            imageView.setImageResource(R.drawable.android_logo);
//        }

        return view;
    }

    public RobotWrapper getItemAtPosition(int position) {
        return robots.get(position);
    }

    /**
     * Gets the first RobotWrapper in this ListView that matches the provided Id String
     * @param id
     * @return
     */
    public RobotWrapper getById(String id){

        for(RobotWrapper w : robots){
            if(w.getId().equals(id)){
                return w;
            }
        }

        return null;
    }

    public void addListable(RobotWrapper wrapper) {
        robots.add(wrapper);
        listStrings.add(new String("BAH"));
        this.notifyDataSetChanged();
    }
}