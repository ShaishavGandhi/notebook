package com.apps.shaishav.notebook.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.shaishav.notebook.R;
import com.apps.shaishav.notebook.data.Answers;

import java.util.List;

/**
 * Created by Shaishav on 25-06-2015.
 */
public class CustomList extends ArrayAdapter<Answers> {

    List<Answers> answers;
    private final Activity context;
    private int lastPosition=-1;
    private SparseBooleanArray mSelectedItemsIds;
    private View rowView;

    public CustomList(Activity context,
                      List<Answers> values) {
        super(context, R.layout.list_single, values);
        this.answers = values;
        this.context = context;
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        rowView = inflater.inflate(R.layout.list_single, null, true);
        SharedPreferences prefs = context.getSharedPreferences("com.shaishav.apps.notebook",Context.MODE_PRIVATE);
        boolean nightMode = prefs.getBoolean("nightMode",false);
        //rowView.setBackgroundColor(Color.parseColor("#fff3f3f3"));
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        TextView txtTitle1 = (TextView) rowView.findViewById(R.id.txt1);
        ImageView imageView = (ImageView)rowView.findViewById(R.id.favIcon);

        if(answers.get(position).getFavorited()){

            imageView.setImageResource(R.drawable.ic_favorite_white_24dp);
            imageView.setColorFilter(Color.parseColor("#F7CA18"));
        }
        txtTitle.setText(Html.fromHtml(answers.get(position).getQuestion()));
        txtTitle1.setText(answers.get(position).getAuthor());

        /*Animation animation = AnimationUtils.loadAnimation(getContext(), (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        rowView.startAnimation(animation);
        lastPosition = position;*/
        if(nightMode){
            rowView.setBackgroundColor(Color.parseColor("#222222"));
            txtTitle.setTextColor(Color.GRAY);
            txtTitle1.setTextColor(Color.GRAY);
        }

        return rowView;
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    private void selectView(int position, boolean value) {
        if (value) {
            mSelectedItemsIds.put(position, value);
            //rowView.setBackgroundColor(Color.RED);
            
        }
        else {
            mSelectedItemsIds.delete(position);
        }
        notifyDataSetChanged();

    }
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;

    }
}
