package com.apps.shaishav.notebook.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.apps.shaishav.notebook.adapter.AuthorsAdapter;
import com.apps.shaishav.notebook.activity.Curated;
import com.apps.shaishav.notebook.R;
import com.apps.shaishav.notebook.activity.MainActivity;
import com.apps.shaishav.notebook.data.Answers;
import com.apps.shaishav.notebook.data.AnswersDataSource;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Authors.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Authors#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Authors extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    private AnswersDataSource answersDataSource;
    private ListView listView;
    private List<Answers> values;
    private AuthorsAdapter authorsAdapter;
    private View rowView;
    private boolean nightMode;
    private TextView textView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rowView= inflater.inflate(R.layout.fragment_authors, container, false);
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("Authors");
        listView = (ListView)rowView.findViewById(R.id.authorsList);
        textView = (TextView)rowView.findViewById(R.id.emptyText);
        answersDataSource = new AnswersDataSource(getActivity());
        loadData();
        listView.setEmptyView(textView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Answers temp = (Answers)listView.getItemAtPosition(position);
                Intent curatedIntent = new Intent(getActivity(),Curated.class);
                curatedIntent.putExtra("type","author");
                curatedIntent.putExtra("param",temp.getAuthor());
                startActivity(curatedIntent);
            }
        });

        return rowView;
    }

    @Override
    public void onResume(){
        super.onResume();
        loadData();
        toggleNightMode();
    }

    public void loadData(){
        answersDataSource.open();
        values = answersDataSource.getAuthorsOrCategories("author");
        answersDataSource.close();
        authorsAdapter = new AuthorsAdapter(getActivity(),values,"author");
        listView.setAdapter(authorsAdapter);
    }

    public void toggleNightMode(){
        SharedPreferences prefs = getActivity().getSharedPreferences("com.shaishav.apps.notebook", Context.MODE_PRIVATE);
        nightMode = prefs.getBoolean("nightMode",false);
        if(nightMode){
            ((ActionBarActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable((Color.parseColor("#222222"))));
            ((MainActivity)getActivity()).setStatusBarCustomColor("#222222");
            textView.setTextColor(Color.GRAY);
            rowView.setBackgroundColor(Color.parseColor("#222222"));
            //adapter.notifyDataSetChanged();

        }
        else{
            ((ActionBarActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable((Color.parseColor("#A52F2B"))));
            ((MainActivity)getActivity()).setStatusBarCustomColor("#A52F2B");
            textView.setTextColor(Color.GRAY);
            rowView.setBackgroundColor(Color.parseColor("#f3f3f3"));
//            adapter.notifyDataSetChanged();
        }
    }



}
