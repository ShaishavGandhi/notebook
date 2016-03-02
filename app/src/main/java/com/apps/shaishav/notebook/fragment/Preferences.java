package com.apps.shaishav.notebook.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.shaishav.notebook.R;
import com.apps.shaishav.notebook.service.SyncService;
import com.apps.shaishav.notebook.data.Answers;
import com.apps.shaishav.notebook.data.AnswersDataSource;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Preferences.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Preferences#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Preferences extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    public Preferences() {
        // Required empty public constructor
    }

    private AnswersDataSource answersDataSource;
    private View rowView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rowView= inflater.inflate(R.layout.fragment_preferences, container, false);
        answersDataSource = new AnswersDataSource(getActivity());
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("Preferences");
        SharedPreferences prefs = getActivity().getSharedPreferences("com.shaishav.apps.notebook", Context.MODE_PRIVATE);
        final boolean signedIn = prefs.getBoolean("signedIn",false);
        final SharedPreferences.Editor editor = prefs.edit();
        boolean nightMode=prefs.getBoolean("nightMode",false);
        Switch nightSwitch = (Switch)rowView.findViewById(R.id.switch1);
        nightSwitch.setChecked(nightMode);
        nightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("nightMode",isChecked);
                editor.commit();

            }
        });

        answersDataSource.open();
        List<Answers> unsycned = answersDataSource.getUnsynced();
        List<Answers> total = answersDataSource.getAllComments();
        answersDataSource.close();

        int synced = total.size()-unsycned.size();

        TextView textView = (TextView)rowView.findViewById(R.id.cloudStatus);


        Button button  = (Button)rowView.findViewById(R.id.syncNow);
        if(synced==total.size()) {
            button.setEnabled(false);
            textView.setText("All backed up!");
        }
        else {
            textView.setText(synced + " of " + total.size() + " synced");
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(signedIn) {
                    Intent intent = new Intent(getActivity(), SyncService.class);
                    getActivity().startService(intent);
                    Toast.makeText(getActivity(), "Syncing now...", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getActivity(), "You are not signed in yet.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return rowView;
    }


}
