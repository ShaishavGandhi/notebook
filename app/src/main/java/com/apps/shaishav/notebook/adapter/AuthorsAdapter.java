package com.apps.shaishav.notebook.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.apps.shaishav.notebook.R;
import com.apps.shaishav.notebook.data.Answers;

import java.util.List;

/**
 * Created by Shaishav on 27-06-2015.
 */
public class AuthorsAdapter extends ArrayAdapter<Answers> {

    List<Answers> answers;
    private final Activity context;
    String type;

    public AuthorsAdapter(Activity context,
                      List<Answers> values,String type) {
        super(context, R.layout.list_single, values);
        this.answers = values;
        this.context = context;
        this.type=type;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.authors_single, null, true);
        //rowView.setBackgroundColor(Color.parseColor("#fff3f3f3"));

        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        if(type.equals("author"))
        txtTitle.setText(answers.get(position).getAuthor());
        else
            txtTitle.setText(answers.get(position).getCategory());

        SharedPreferences prefs = context.getSharedPreferences("com.shaishav.apps.notebook", Context.MODE_PRIVATE);
        boolean nightMode = prefs.getBoolean("nightMode",false);

        if(nightMode){
            rowView.setBackgroundColor(Color.parseColor("#222222"));
            txtTitle.setTextColor(Color.GRAY);
        }

        return rowView;
    }
}
