package com.apps.shaishav.notebook.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.media.Image;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.shaishav.notebook.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shaishav on 28-06-2015.
 */
public class NavigationAdapter extends ArrayAdapter<String>{

   List<Integer> images;
   String[] titles;
   private final Activity context;

    public NavigationAdapter(Activity context,String[] titles){

        super(context, R.layout.navigation_single, titles);
        this.titles = titles;
        this.context = context;

        images = new ArrayList<Integer>();
        images.add(R.drawable.ic_home_black_24dp);
        images.add(R.drawable.ic_favorite_black_24dp);
        images.add(R.drawable.ic_mode_edit_black_24dp);
        images.add(R.drawable.ic_label_black_24dp);
        images.add(R.drawable.ic_settings_black_24dp);
        images.add(R.drawable.ic_star_rate_black_18dp);

        //images.add(R.drawable.ic_home_black_24dp);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.navigation_single, null, true);

        TextView textView = (TextView)rowView.findViewById(R.id.nav_title);
        textView.setText(titles[position]);
        ImageView imageView = (ImageView)rowView.findViewById(R.id.nav_icon);
        imageView.setImageResource(images.get(position));

        return rowView;
    }
}
