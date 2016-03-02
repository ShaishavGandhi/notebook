package com.apps.shaishav.notebook.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.shaishav.notebook.adapter.CustomList;
import com.apps.shaishav.notebook.R;
import com.apps.shaishav.notebook.data.Answers;
import com.apps.shaishav.notebook.data.AnswersDataSource;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class Curated extends ActionBarActivity {

    private AnswersDataSource answersDataSource;
    private ProgressDialog pd;
    private CustomList adapter;
    private static Intent intent;
    ListView listView;
    private Answers bufferAnswer;
    private List<Answers> values;
    private boolean signedIn;
    private boolean nightMode;
    private TextView textView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    String type,param;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curated);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initiliaze calligraphy
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("RobotoSlab-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        SharedPreferences prefs = this.getSharedPreferences("com.shaishav.apps.notebook", Context.MODE_PRIVATE);


        signedIn = prefs.getBoolean("signedIn",false);
        Intent intent= getIntent();
        type = intent.getStringExtra("type");
        param = intent.getStringExtra("param");
        getSupportActionBar().setTitle(param);
        answersDataSource = new AnswersDataSource(this);
        answersDataSource.open();
        values = answersDataSource.getCustom(type,param);
        answersDataSource.close();
        listView = (ListView)findViewById(R.id.answers);
        adapter = new CustomList(this,values);
        textView = (TextView)findViewById(R.id.emptyText);
        listView.setEmptyView(textView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Answers temp=(Answers)listView.getItemAtPosition(position);
                Intent details = new Intent(getApplicationContext(),Details.class);
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
    }

    @Override

    public void onResume(){
        super.onResume();
        loadData();
        toggleNightMode();
    }

    public void loadData(){
        answersDataSource.open();
        values = answersDataSource.getCustom(type,param);
        answersDataSource.close();
        adapter = new CustomList(this,values);
        listView.setAdapter(adapter);
    }

    public void toggleNightMode(){
        FrameLayout rowView = (FrameLayout)findViewById(R.id.frameLayout);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.shaishav.apps.notebook",Context.MODE_PRIVATE);
        nightMode = prefs.getBoolean("nightMode",false);
        if(nightMode){
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable((Color.parseColor("#222222"))));
            setStatusBarCustomColor("#222222");
            textView.setTextColor(Color.GRAY);
            rowView.setBackgroundColor(Color.parseColor("#222222"));
            adapter.notifyDataSetChanged();


        }
        else{
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable((Color.parseColor("#A52F2B"))));
            setStatusBarCustomColor("#A52F2B");
            textView.setTextColor(Color.GRAY);
            rowView.setBackgroundColor(Color.parseColor("#f3f3f3"));
            adapter.notifyDataSetChanged();
        }
    }

    public void setStatusBarCustomColor(String color){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(color));
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
                    Toast.makeText(getApplicationContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
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
                }
                else{
                    answersDataSource.updateFavorited(bufferAnswer.getId(),true);
                    Toast.makeText(getApplicationContext(),"Added to favorites",Toast.LENGTH_SHORT).show();
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
                break;
            case 1:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this answer by " + bufferAnswer.getAuthor() + " to: " + Html.fromHtml(bufferAnswer.getQuestion()));
                shareIntent.putExtra(Intent.EXTRA_TEXT,bufferAnswer.getLink());

                shareIntent.setType("text/plain");
                startActivity(shareIntent);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_curated, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id==android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
