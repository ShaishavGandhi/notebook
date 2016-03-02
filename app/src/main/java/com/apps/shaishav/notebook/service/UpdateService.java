package com.apps.shaishav.notebook.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.apps.shaishav.notebook.data.Answers;
import com.apps.shaishav.notebook.data.AnswersDataSource;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by Shaishav on 11-07-2015.
 */
public class UpdateService extends IntentService {

    private AnswersDataSource answersDataSource;
    private List<Answers> answersList;
    private SharedPreferences prefs;

    public UpdateService(){
        super("UpdateService");
        answersDataSource = new AnswersDataSource(this);


    }

    @Override
    protected void onHandleIntent(Intent intent) {

        prefs = getApplicationContext().getSharedPreferences("com.shaishav.apps.notebook", Context.MODE_PRIVATE);
        boolean signedIn = prefs.getBoolean("signedIn",false);
        if(signedIn) {
            answersDataSource.open();
            answersList = answersDataSource.getAllComments();
            answersDataSource.close();
            final SharedPreferences.Editor editor = prefs.edit();

            if (answersList.size() > 0)
                new SyncAnswer().execute();


        /*    ParseQuery<ParseObject> query = ParseQuery.getQuery("Answers");

            Answers temp = answersList.get(0);
            favoriteList.add(temp.getFavorited());
            final Gson gson = new Gson();
            editor.putString("favorited_list",gson.toJson(favoriteList));
            editor.commit();

            query.getInBackground(temp.getObjectId(), new GetCallback<ParseObject>() {
                public void done(ParseObject answerObj, ParseException e) {
                    if (e == null) {

                       List<Boolean> tempList = gson.fromJson(prefs.getString("favorited_list","None"),java.util.List.class);
                       answerObj.put("favorited",tempList.get(0));

                       answerObj.saveInBackground();
                       tempList.remove(0);
                       editor.putString("favorited_list",gson.toJson(tempList));
                       editor.commit();
                    }
                }
            });*/
        }

        }

    private class SyncAnswer extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... params) {
            try {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Answers");
                ParseObject parseObject = query.get(answersList.get(0).getObjectId());
                parseObject.put("favorited",answersList.get(0).getFavorited());
                parseObject.saveInBackground();
            }
            catch(ParseException e){
                e.printStackTrace();
                return "";
            }

            return "Done";
        }

        protected void onPostExecute(String result) {
            if(!result.equals("")){
                answersList.remove(0);
                if(answersList.size()>0){
                    new SyncAnswer().execute();
                }
            }
        }
    }

    }

