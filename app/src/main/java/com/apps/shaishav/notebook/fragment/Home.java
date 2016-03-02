package com.apps.shaishav.notebook.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.shaishav.notebook.adapter.CustomList;
import com.apps.shaishav.notebook.activity.GPlusLogin;
import com.apps.shaishav.notebook.R;
import com.apps.shaishav.notebook.service.SyncService;
import com.apps.shaishav.notebook.activity.Details;
import com.apps.shaishav.notebook.activity.MainActivity;
import com.apps.shaishav.notebook.data.Answers;
import com.apps.shaishav.notebook.data.AnswersDataSource;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.melnykov.fab.FloatingActionButton;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Date;
import java.util.List;

public class Home extends Fragment {

    List<Answers> values;
    private GoogleApiClient mGoogleApiClient;

    private boolean mIntentInProgress;
    private boolean signedInUser;
    private ConnectionResult mConnectionResult;

    private static final int RC_SIGN_IN = 0;

    private AnswersDataSource answersDataSource;
    private ProgressDialog pd;
    private ProgressDialog pb;
    private CustomList adapter;
    private static Intent intent;
    ListView listView;
    private int totalToSync;
    private Answers bufferAnswer;
    private String session_token;
    private String sharedText;
    private List<ParseObject> newAnswers;
    public Home() {
        // Required empty public constructor
    }

    private boolean signedIn;
    private boolean nightMode;

    private View rowView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rowView= inflater.inflate(R.layout.fragment_home, container, false);
        listView = (ListView)rowView.findViewById(R.id.answers);
        setHasOptionsMenu(true);
        TextView emptyText = (TextView)rowView.findViewById(R.id.emptyText);
        FloatingActionButton floatingActionButton = (FloatingActionButton)rowView.findViewById(R.id.fab);
        //android.support.design.widget.FloatingActionButton floatingActionButton = (android.support.design.widget.FloatingActionButton)rowView.findViewById(R.id.fab);
        floatingActionButton.attachToListView(listView);
        answersDataSource = new AnswersDataSource(getActivity());
        pd = new ProgressDialog(getActivity());
        pd.setMessage("Adding..");
        pb = new ProgressDialog(getActivity());
        //pb.setIndeterminate(true);
        pb.setCancelable(false);
        pb.setTitle("Getting your answers...");
        pb.setCanceledOnTouchOutside(false);
        pb.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("Home");

        SharedPreferences prefs = getActivity().getSharedPreferences("com.shaishav.apps.notebook", Context.MODE_PRIVATE);
        signedIn = prefs.getBoolean("signedIn",false);
        nightMode = prefs.getBoolean("nightMode",false);

        intent = getActivity().getIntent();
        String action = intent.getAction();
        String type = intent.getType();


        if (Intent.ACTION_SEND.equals(action) && type != null && (intent.hasExtra(Intent.EXTRA_TEXT))){
            if(((MainActivity)getActivity()).isNetworkConnected()) {
                sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                sharedText = sharedText.substring(sharedText.indexOf("http"));
                answersDataSource.open();
                if(!answersDataSource.checkForExistingAnswer(sharedText)) {
                    answersDataSource.close();
                    pd.show();
                    GetAnswer getAnswer = new GetAnswer();
                    getAnswer.execute(sharedText);

                    intent.removeExtra(Intent.EXTRA_TEXT);
                }
                else{
                    answersDataSource.close();
                    Toast.makeText(getActivity(),"Answer already exists",Toast.LENGTH_LONG).show();
                }
            }
            else{
                Toast.makeText(getActivity(),"No network detected",Toast.LENGTH_SHORT).show();
            }

        }

        answersDataSource.open();
        values = answersDataSource.getAllComments();
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

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerForContextMenu(v);
                getActivity().openContextMenu(v);

            }
        });


        if(signedIn) {
            Intent serviceIntent = new Intent(getActivity(), SyncService.class);
            getActivity().startService(serviceIntent);


        }
        checkForNewAnswer();
        introduceApp();

        return rowView;
    }

    public void introduceApp(){
        SharedPreferences prefs = getActivity().getSharedPreferences("com.shaishav.apps.notebook", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        boolean introduced = prefs.getBoolean("introduced",false);
        if(!introduced){
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

            alert.setTitle("In this update");
            alert.setMessage("Cloud Backup : \n\nSign up for an account with Notebook for Quora by going to the Sign Up/Login page from the menu on top right. Once you login after creating an account, all your answers will be safely stored in the cloud, available across multiple devices. \n\nNIGHT MODE:\n\nRead answers easily if you're reading in the dark by clicking on Night Mode icon. You can also toggle the Night Mode in the Preferences tab.\n\nBUG FIXES:\n\nEasily add answers now. Error occurred while adding answer can be resolved by clicking on 'Retry'.");

            // Set an EditText view to get user input


            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    editor.putBoolean("introduced",true);
                    editor.commit();
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                    editor.putBoolean("introduced",true);
                    editor.commit();
                }
            });

            alert.show();
        }

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
        if(v.getId()==R.id.fab){
            String[] menuItems = {"Add answer with link", "Add answer through Quora app"};
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        switch(menuItemIndex){
            case 0:
                if(item.getTitle().equals("Add answer with link")){
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                    alert.setTitle("Add answer");
                    alert.setMessage("Copy link of the answer");

                    // Set an EditText view to get user input
                    final EditText input = new EditText(getActivity());
                    alert.setView(input);

                    alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if(((MainActivity)getActivity()).isNetworkConnected()) {

                                sharedText = input.getText().toString();
                                sharedText = sharedText.substring(sharedText.indexOf("http"));
                                answersDataSource.open();
                                if(!answersDataSource.checkForExistingAnswer(sharedText)) {
                                    answersDataSource.close();
                                    GetAnswer getAnswer = new GetAnswer();
                                    pd.show();
                                    getAnswer.execute(sharedText);
                                }
                                // Do something with value!
                            }
                            else{
                                answersDataSource.close();
                                Toast.makeText(getActivity(),"No network detected",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                        }
                    });

                    alert.show();
                }
                else {
                    answersDataSource.open();
                    if (bufferAnswer.getFavorited()) {
                        answersDataSource.updateFavorited(bufferAnswer.getId(), false);
                        Toast.makeText(getActivity(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                        bufferAnswer.setFavorited(false);
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
                    } else {
                        answersDataSource.updateFavorited(bufferAnswer.getId(), true);
                        Toast.makeText(getActivity(), "Added to favorites", Toast.LENGTH_SHORT).show();
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
                    adapter.notifyDataSetChanged();
                    answersDataSource.close();
                }

                break;
            case 1:
                if(item.getTitle().equals("Add answer through Quora app")){
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    PackageManager managerclock = getActivity().getPackageManager();
                    i = managerclock.getLaunchIntentForPackage("com.quora.android");
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(i);
                }
                else {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this answer by " + bufferAnswer.getAuthor() + " to: " + Html.fromHtml(bufferAnswer.getQuestion()));
                    shareIntent.putExtra(Intent.EXTRA_TEXT, bufferAnswer.getLink());

                    shareIntent.setType("text/plain");
                    getActivity().startActivity(shareIntent);
                }
                break;
            case 2:
                answersDataSource.open();
                answersDataSource.deleteComment(bufferAnswer);
                adapter.remove(bufferAnswer);
                adapter.notifyDataSetChanged();
                answersDataSource.close();
                /*if(signedIn && !bufferAnswer.getObjectId().equals("not set")){
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

    private class GetAnswer extends AsyncTask<String, Integer, String> {
        String answer,question,author,category_link,category_text,link;
        protected String doInBackground(String... urls) {

            try {
                link = urls[0];

                int http_index = link.indexOf("http");
                link = link.substring(http_index);
                Document doc = Jsoup.connect(link).followRedirects(true).get();
//                //New starts
//
//                URL url = new URL(link);
//                InputStream is = (InputStream) url.getContent();
//                BufferedReader br = new BufferedReader(new InputStreamReader(is));
//                String line = null;
//                StringBuffer sb = new StringBuffer();
//                while((line = br.readLine()) != null){
//                    sb.append(line);
//                }
//                String htmlContent = sb.toString();
//
//                Document doc = Jsoup.parse(htmlContent);

                //New ends

                Element answerElement = doc.getElementsByClass("ExpandedAnswer").first();
                answer=doc.getElementsByClass("ExpandedAnswer").first().html();
                answer = answer.replaceAll("src=\"\"", "");
                answer = answer.replaceAll("data-src","src");
                question = doc.getElementsByClass("question_link").first().html();
                Elements anonUser = doc.getElementsByClass("anon_user");
                if(anonUser.size()>0)
                    author="Anonymous";
                if(anonUser.size()==0){

                    //author = doc.getElementsByClass("user").second().child(2).child(0).child(1).text();
                    //Elements tempAuthor = doc.getElementsByClass("user");
                    author=doc.getElementsByClass("feed_item_answer_user").first().getElementsByClass("user").first().text();

                    //author = doc.getElementsByClass("user").get(1).text();
                }

                if(author.length()==0)
                    author="Unknown";


                category_link = doc.getElementsByClass("question_link").first().attr("href");

                //New part 2 starts

                Document category = Jsoup.connect("http://www.quora.com"+category_link).followRedirects(true).get();

//                URL urlCat = new URL("http://www.quora.com"+category_link);
//                InputStream isCat = (InputStream) urlCat.getContent();
//                BufferedReader brCat = new BufferedReader(new InputStreamReader(isCat));
//                String lineCat = null;
//                StringBuffer sbCat = new StringBuffer();
//                while((lineCat = brCat.readLine()) != null){
//                    sbCat.append(lineCat);
//                }
//                String htmlContentCat = sbCat.toString();
//
//
//                Document category = Jsoup.parse(htmlContentCat);

                //New part 2 ends

                category_text = category.getElementsByClass("TopicName").last().text();

                Elements images = answerElement.getElementsByTag("img");

                for(int i=0;i<images.size();i++){
                    saveImage(images.get(i).attr("data-src"));
                }



                return "Something";
            }
            catch(SocketTimeoutException e){

                return "Socket";

            }
            catch(Exception e1){
                return "";
            }


        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String result) {
            pd.hide();
            if(result.equals("")){

                //Toast.makeText(getActivity(), "Sorry about that! Couldn't add the answer", Toast.LENGTH_LONG).show();
                Snackbar.make(rowView.findViewById(R.id.snackbarPosition),"Error",Snackbar.LENGTH_LONG)
                        .setAction("Retry",new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pd.show();
                                new GetAnswer().execute(sharedText);
                            }
                        }).show();
            }
            else if(result.equals("Socket")){
                Snackbar.make(rowView.findViewById(R.id.snackbarPosition),"Network Problem",Snackbar.LENGTH_LONG)
                        .setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pd.show();
                                new GetAnswer().execute(sharedText);
                            }
                        }).show();
            }
            else{
                answersDataSource.open();
                Answers addedAnswer=answersDataSource.createComment(question,answer,author,category_text,false,link,"not set");

                answersDataSource.close();

                Toast.makeText(getActivity(),"Added!",Toast.LENGTH_SHORT).show();
                adapter.insert(addedAnswer, 0);
                adapter.notifyDataSetChanged();
                if(signedIn) {
                    Intent serviceIntent = new Intent(getActivity(), SyncService.class);
                    getActivity().startService(serviceIntent);
                }
            }
        }
    }

    public void saveImage(String url){

        try {
            URL imageURL = new URL(url);
            Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

            ContextWrapper cw = new ContextWrapper(getActivity());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

            //File naming
            int start_index = url.indexOf("qimg");
            int last_index = url.indexOf("?convert");

            url = url.substring(start_index+5,last_index);

            // Create imageDir
            File mypath=new File(directory,url+".jpg");

            FileOutputStream fos = null;
            fos = new FileOutputStream(mypath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();


        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home, menu);

        if(signedIn){
            MenuItem item = menu.findItem(R.id.signIn);
            item.setTitle("Log Out");
        }


        MenuItem item = menu.findItem(R.id.action_search);
        SearchView sv = new SearchView(((MainActivity) getActivity()).getSupportActionBar().getThemedContext());
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, sv);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                System.out.println("search query submit");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Answers> searched;
                if(newText.length()==0){
                    searched = values;
                }
                else {
                    answersDataSource.open();
                    searched = answersDataSource.searchComments(newText);
                    answersDataSource.close();

                }
                adapter = new CustomList(getActivity(),searched);
                listView.setAdapter(adapter);

                return false;
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection

        switch (item.getItemId()) {
            case R.id.action_search:



                return true;
            case R.id.signIn:
                if(item.getTitle().equals("Log Out")){
                    if(((MainActivity)getActivity()).isNetworkConnected())
                        ((MainActivity)getActivity()).logout();
                    else
                        Toast.makeText(getActivity(),"No network detected",Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent loginIntent = new Intent(getActivity(),GPlusLogin.class);
                    getActivity().startActivity(loginIntent);
                    getActivity().finish();
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void checkForNewAnswer(){
        if(signedIn && ((MainActivity)getActivity()).isNetworkConnected()) {

            new GetNewAnswerList().execute();
            /*SharedPreferences prefs = getActivity().getSharedPreferences("com.shaishav.apps.notebook", Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = prefs.edit();
            String date_string = prefs.getString("last_synced_time", "none");
            String emailId = prefs.getString("email","not set");


            ParseQuery<ParseObject> query = ParseQuery.getQuery("Answers");

            query.whereEqualTo("email", emailId);
            if(!date_string.equals("none")){
                Date date = new Date(Long.valueOf(date_string).longValue());
                query.whereGreaterThan("createdAt",date);
            }

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if(e==null){

                        if(parseObjects.size()>0)
                            Toast.makeText(getActivity(),"Getting your answers...",Toast.LENGTH_SHORT).show();
                       for(int i=0;i<parseObjects.size();i++){

                            totalToSync = parseObjects.size();
                           if(totalToSync>5){
                               pb.setMax(totalToSync);
                               pb.show();
                           }
                           ParseObject temp = parseObjects.get(i);
                           String unsynced_question = (String)temp.get("question");
                           String unsynced_answer = (String)temp.get("answer");
                           String unsynced_author = (String)temp.get("author");
                           String unsynced_category = (String)temp.get("category");
                           boolean unsynced_favorited = (boolean)temp.get("favorited");
                           String unsynced_link = (String)temp.get("link");

                           //SimpleDateFormat format = new SimpleDateFormat()
                           long createdAtLong = temp.getCreatedAt().getTime();
                           //Date tempDate = new Date(createdAtLong+1000);
                           String unsynced_objectId = (String)temp.getObjectId();
                           new GetLightAnswer().execute(unsynced_link,unsynced_answer,unsynced_question,unsynced_author,unsynced_category,unsynced_objectId,String.valueOf(unsynced_favorited),String.valueOf(createdAtLong+1000),String.valueOf(i));
                       }
                    }
                }
            });*/

        }
    }

    private class GetLightAnswer extends AsyncTask<String, Integer, String> {
        String link,answer,question,author,category,objectId,createdAt;

        boolean favorited;
        int progressSoFar;
        SharedPreferences prefs;
        SharedPreferences.Editor editor;
        protected String doInBackground(String... urls) {

            try {
                prefs = getActivity().getSharedPreferences("com.shaishav.apps.notebook", Context.MODE_PRIVATE);
                editor = prefs.edit();
                ParseObject temp = newAnswers.get(0);
                link = (String)temp.get("link");
                link = link.substring(link.indexOf("http"));
                answersDataSource.open();
                if(!answersDataSource.checkForExistingAnswer(link)) {
                    answersDataSource.close();
                    question = (String) temp.get("question");
                    answer = (String) temp.get("answer");
                    author = (String) temp.get("author");
                    category = (String) temp.get("category");
                    favorited = (boolean) temp.get("favorited");


                    createdAt = String.valueOf(temp.getCreatedAt().getTime());
                    objectId = temp.getObjectId();
                    Document doc = Jsoup.parseBodyFragment(answer);
                    Elements elem = doc.getElementsByTag("img");
                    for (int j = 0; j < elem.size(); j++) {
                        saveImage(elem.get(j).attr("src"));
                    }

                    return "Something";
                }
                else{
                    answersDataSource.close();
                    return "Duplicate";
                }


                /*SharedPreferences prefs = getActivity().getSharedPreferences("com.shaishav.apps.notebook",Context.MODE_PRIVATE);
                editor = prefs.edit();
                link = urls[0];
                answer=urls[1];
                question = urls[2];
                author = urls[3];
                category = urls[4];
                objectId = urls[5];
                createdAt=urls[7];
                favorited = Boolean.valueOf(urls[6]);
                progressSoFar = Integer.parseInt(urls[8]);
                Document doc = Jsoup.parseBodyFragment(answer);
                Elements elem = doc.getElementsByTag("img");
                for(int j=0;j<elem.size();j++){
                    saveImage(elem.get(j).attr("src"));
                }

                return "Something";
                */

            }
            catch(Exception e){
                return "";
            }


        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String result) {
            //pd.hide();
            if(result.equals("")){
                //Toast.makeText(getActivity(),"Sorry about that! Couldn't add the answer",Toast.LENGTH_LONG).show();
            }
            else if(result.equals("Duplicate")){
                //editor.putString("last_synced_time",createdAt);
                //editor.commit();
                newAnswers.remove(0);
                pb.setProgress(totalToSync-newAnswers.size());
                if(newAnswers.size()==0)
                    pb.hide();
                else
                    new GetLightAnswer().execute();
            }
            else{
                answersDataSource.open();
                Answers tempAnswer=answersDataSource.createComment(question,answer,author,category,favorited,link,objectId);
                adapter.insert(tempAnswer,0);
                adapter.notifyDataSetChanged();
                answersDataSource.close();
                editor.putString("last_synced_time",createdAt);
                editor.commit();
                newAnswers.remove(0);
                pb.setProgress(totalToSync-newAnswers.size());
                if(newAnswers.size()==0)
                    pb.hide();
                else
                    new GetLightAnswer().execute();
            }
        }
    }

    public void toggleNightMode(){
        SharedPreferences prefs = getActivity().getSharedPreferences("com.shaishav.apps.notebook",Context.MODE_PRIVATE);
        nightMode = prefs.getBoolean("nightMode",false);
        if(nightMode){
            ((ActionBarActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable((Color.parseColor("#222222"))));
            ((MainActivity)getActivity()).setStatusBarCustomColor("#222222");
            //emptyText.setTextColor(Color.GRAY);
            rowView.setBackgroundColor(Color.parseColor("#222222"));
            adapter.notifyDataSetChanged();

        }
        else{
            ((ActionBarActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable((Color.parseColor("#A52F2B"))));
            ((MainActivity)getActivity()).setStatusBarCustomColor("#A52F2B");
            //emptyText.setTextColor(Color.GRAY);
            rowView.setBackgroundColor(Color.parseColor("#f3f3f3"));
            adapter.notifyDataSetChanged();
        }
    }

    private class GetNewAnswerList extends AsyncTask<List,Integer,List> {


        @Override
        protected List doInBackground(List... params) {
            try {
                SharedPreferences prefs = getActivity().getSharedPreferences("com.shaishav.apps.notebook", Context.MODE_PRIVATE);
                String date_string = prefs.getString("last_synced_time", "none");
                String emailId = prefs.getString("email","not set");
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Answers");

                query.whereEqualTo("email", emailId);
                if (!date_string.equals("none")) {
                    Date date = new Date(Long.valueOf(date_string).longValue());
                    query.whereGreaterThan("createdAt", date);
                }

                newAnswers = query.find();

                return newAnswers;
            }
            catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(List result) {
            if(result!=null){
                if(result.size()>0) {

                    new GetLightAnswer().execute();
                    pb.setMax(result.size());
                    pb.show();
                    totalToSync = result.size();
                }
            }
        }

    }




    @Override
    public void onResume(){
        super.onResume();
        toggleNightMode();
    }


}
