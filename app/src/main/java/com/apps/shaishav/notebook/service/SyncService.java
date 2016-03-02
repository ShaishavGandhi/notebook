package com.apps.shaishav.notebook.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.apps.shaishav.notebook.data.Answers;
import com.apps.shaishav.notebook.data.AnswersDataSource;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Shaishav on 03-07-2015.
 */
public class SyncService extends IntentService {

    private AnswersDataSource answersDataSource;
    private List<Answers> unsynced;
    private String email_user;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SyncService(){
        super("SyncService");
        answersDataSource = new AnswersDataSource(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        answersDataSource.open();
        unsynced = answersDataSource.getUnsynced();
        answersDataSource.close();
        prefs = getApplicationContext().getSharedPreferences("com.shaishav.apps.notebook", Context.MODE_PRIVATE);
        editor = prefs.edit();
        boolean signedIn = prefs.getBoolean("signedIn",false);
        email_user = prefs.getString("email", "Not logged in");
        if(unsynced.size()>0 && signedIn)
        new SyncAnswer().execute();




        }

    private class SyncAnswer extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... params) {
            try {
                Answers temp = unsynced.get(0);
                final ParseObject answer = new ParseObject("Answers");
                answer.put("question", temp.getQuestion());
                answer.put("answer", temp.getAnswer());
                answer.put("link", temp.getLink());
                answer.put("author", temp.getAuthor());
                answer.put("category", temp.getCategory());
                answer.put("email", email_user);
                answer.put("favorited", temp.getFavorited());
                answer.put("local_id", temp.getId());
                answer.setACL(new ParseACL(ParseUser.getCurrentUser()));
                answer.save();
                answersDataSource.open();
                answersDataSource.updateObjectId(answer.getObjectId(),temp.getId());
                answersDataSource.close();
                long createdAtLong = answer.getCreatedAt().getTime();
                editor.putString("last_synced_time", String.valueOf(createdAtLong + 1000));
                editor.commit();
                return "Done";
            }
            catch(ParseException e){
                e.printStackTrace();
                return "";
            }

        }

        protected void onPostExecute(String result) {
            if(!result.equals("")){
                unsynced.remove(0);
                if(unsynced.size()>0){
                    new SyncAnswer().execute();
                }
            }
        }
    }
    }

