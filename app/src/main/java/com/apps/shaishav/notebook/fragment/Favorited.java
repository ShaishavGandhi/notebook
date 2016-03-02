package com.apps.shaishav.notebook.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.shaishav.notebook.adapter.CustomList;
import com.apps.shaishav.notebook.R;
import com.apps.shaishav.notebook.activity.Details;
import com.apps.shaishav.notebook.activity.MainActivity;
import com.apps.shaishav.notebook.data.Answers;
import com.apps.shaishav.notebook.data.AnswersDataSource;

import java.util.List;


public class Favorited extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    List<Answers> values;



    private AnswersDataSource answersDataSource;
    private boolean nightMode;
    private ProgressDialog pd;
    private CustomList adapter;
    private static Intent intent;
    ListView listView;
    private View rowView;
    private Answers bufferAnswer;
    private boolean signedIn;
    private TextView emptyText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rowView= inflater.inflate(R.layout.fragment_favorited, container, false);
        listView = (ListView)rowView.findViewById(R.id.answers);
        emptyText = (TextView)rowView.findViewById(R.id.emptyText);
        answersDataSource = new AnswersDataSource(getActivity());
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("Favorites");

        SharedPreferences prefs = getActivity().getSharedPreferences("com.shaishav.apps.notebook", Context.MODE_PRIVATE);
        signedIn = prefs.getBoolean("signedIn",false);

        answersDataSource.open();
        values = answersDataSource.getFavoritedAnswers();
        answersDataSource.close();

        adapter = new CustomList(getActivity(),values);

        listView.setAdapter(adapter);
        listView.setEmptyView(emptyText);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Answers temp=(Answers)listView.getItemAtPosition(position);
                Intent details = new Intent(getActivity(),Details.class);
                details.putExtra("question",temp.getQuestion());
                details.putExtra("answer",temp.getAnswer());
                details.putExtra("favorited",temp.getFavorited());
                details.putExtra("link",temp.getLink());
                details.putExtra("id",temp.getId());
                details.putExtra("author",temp.getAuthor());
                details.putExtra("objectId",temp.getObjectId());
                startActivity(details);
            }
        });
        registerForContextMenu(listView);


        return rowView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.answers) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

            menu.setHeaderTitle("Choose action");
            bufferAnswer = (Answers)listView.getItemAtPosition(info.position);
            if(!bufferAnswer.getFavorited()) {
                String[] menuItems = {"Add to favorites", "Share", "Delete"};
                for (int i = 0; i < menuItems.length; i++) {
                    menu.add(Menu.NONE, i, i, menuItems[i]);
                }
            }
            else{
                String[] menuItems = {"Remove from favorites", "Share", "Delete"};
                for (int i = 0; i < menuItems.length; i++) {
                    menu.add(Menu.NONE, i, i, menuItems[i]);
                }
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        switch(menuItemIndex){
            case 0:
                answersDataSource.open();
                if(bufferAnswer.getFavorited()){
                    answersDataSource.updateFavorited(bufferAnswer.getId(),false);
                    Toast.makeText(getActivity(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                    bufferAnswer.setFavorited(false);
                    adapter.remove(bufferAnswer);
                    adapter.notifyDataSetChanged();
                    /*if(signedIn && !bufferAnswer.getObjectId().equals("not set")){
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Answers");

                        // Retrieve the object by id
                        query.getInBackground(bufferAnswer.getObjectId(), new GetCallback<ParseObject>() {
                            public void done(ParseObject gameScore, ParseException e) {
                                if (e == null) {
                                    gameScore.put("favorited",false);
                                    gameScore.saveEventually();
                                }
                            }
                        });
                    }*/
                }
                else{
                    answersDataSource.updateFavorited(bufferAnswer.getId(),true);
                    Toast.makeText(getActivity(),"Added to favorites",Toast.LENGTH_SHORT).show();
                    bufferAnswer.setFavorited(true);
                    /*if(signedIn && !bufferAnswer.getObjectId().equals("not set")){
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Answers");

                        // Retrieve the object by id
                        query.getInBackground(bufferAnswer.getObjectId(), new GetCallback<ParseObject>() {
                            public void done(ParseObject gameScore, ParseException e) {
                                if (e == null) {
                                    gameScore.put("favorited",true);
                                    gameScore.saveEventually();
                                }
                            }
                        });
                    }*/

                }
                answersDataSource.close();
                break;
            case 1:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this answer by " + bufferAnswer.getAuthor() + " to: " + Html.fromHtml(bufferAnswer.getQuestion()));
                shareIntent.putExtra(Intent.EXTRA_TEXT,bufferAnswer.getLink());

                shareIntent.setType("text/plain");
                getActivity().startActivity(shareIntent);
                break;
            case 2:
                answersDataSource.open();
                answersDataSource.deleteComment(bufferAnswer);
                adapter.remove(bufferAnswer);
                adapter.notifyDataSetChanged();
                answersDataSource.close();
               /* if(signedIn && !bufferAnswer.getObjectId().equals("not set")){
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Answers");

                    // Retrieve the object by id
                    query.getInBackground(bufferAnswer.getObjectId(), new GetCallback<ParseObject>() {
                        public void done(ParseObject gameScore, ParseException e) {
                            if (e == null) {
                                gameScore.deleteEventually();
                            }
                        }
                    });
                }*/
                break;
        }


        return true;
    }

    public void toggleNightMode(){
        SharedPreferences prefs = getActivity().getSharedPreferences("com.shaishav.apps.notebook",Context.MODE_PRIVATE);
        nightMode = prefs.getBoolean("nightMode",false);
        if(nightMode){
            ((ActionBarActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable((Color.parseColor("#222222"))));
            ((MainActivity)getActivity()).setStatusBarCustomColor("#222222");
            emptyText.setTextColor(Color.GRAY);
            rowView.setBackgroundColor(Color.parseColor("#222222"));
            adapter.notifyDataSetChanged();

        }
        else{
            ((ActionBarActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable((Color.parseColor("#A52F2B"))));
            ((MainActivity)getActivity()).setStatusBarCustomColor("#A52F2B");
            emptyText.setTextColor(Color.GRAY);
            rowView.setBackgroundColor(Color.parseColor("#f3f3f3"));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        toggleNightMode();
    }




}
