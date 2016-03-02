package com.apps.shaishav.notebook.activity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.shaishav.notebook.data.AnswersDataSource;
import com.apps.shaishav.notebook.R;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class Details extends ActionBarActivity implements ObservableScrollViewCallbacks{

    private TextView question;
    private TextView answer;
    private TextView author;
    private String question_string,answer_string,link,author_string;
    private boolean favorited;
    private long answer_id;
    private String objectId;
    private boolean signedIn;
    private boolean nightMode;
    private AnswersDataSource answersDataSource;
    private TextToSpeech textToSpeech;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //ObservableScrollView observableScrollView = (ObservableScrollView)findViewById(R.id.scrollViewDetails);
        //observableScrollView.setScrollViewCallbacks(this);

        answersDataSource = new AnswersDataSource(this);


        //Initiliaze calligraphy
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("RobotoSlab-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.shaishav.apps.notebook",Context.MODE_PRIVATE);
        signedIn = sharedPreferences.getBoolean("signedIn",false);
        nightMode = sharedPreferences.getBoolean("nightMode",false);



        Intent intent = getIntent();
        question_string = intent.getStringExtra("question");
        answer_string = intent.getStringExtra("answer");
        link = intent.getStringExtra("link");
        author_string = intent.getStringExtra("author");
        favorited = intent.getBooleanExtra("favorited",false);
        answer_id = intent.getLongExtra("id",0);
        objectId = intent.getStringExtra("objectId");

        answer_string = setHrefs(answer_string);

        question = (TextView)findViewById(R.id.question);
        answer = (TextView)findViewById(R.id.answer);
        author = (TextView)findViewById(R.id.author);
        answer.setMovementMethod(LinkMovementMethod.getInstance());

        question.setText(Html.fromHtml(question_string));
        answer.setText(Html.fromHtml(answer_string,new Html.ImageGetter() {
            Drawable d;
            @Override
            public Drawable getDrawable(String source) {
                try {
                    ContextWrapper cw = new ContextWrapper(getApplicationContext());
                    // path to /data/data/yourapp/app_data/imageDir
                    File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                    // Create imageDir

                    int start_index = source.indexOf("qimg");
                    int last_index = source.indexOf("?convert");

                    source = source.substring(start_index+5,last_index);
                    File f=new File(directory, source+".jpg");
                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));

                    int device_width = getWidth();
                    int image_width = b.getWidth();
                    int image_height = b.getHeight();
                    float aspect_ratio = (float)device_width/image_width;
                    float new_width = image_width*aspect_ratio;
                    float new_height = image_height*aspect_ratio;



                    d = new BitmapDrawable(getResources(),b);
                    d.setBounds(0,0,(int)Math.round(new_width),(int)Math.round(new_height));


                }
                catch (FileNotFoundException e)
                {

                    e.printStackTrace();

                }
                return d;
            }
        },null));
        author.setText(author_string);
        if(nightMode){
            toggleNightMode(true);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(Html.fromHtml(question_string));

        textToSpeech = new TextToSpeech(getApplicationContext(),new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=TextToSpeech.ERROR){
                    textToSpeech.setLanguage(Locale.UK);
                    textToSpeech.setSpeechRate((float)0.85);

                }
            }
        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.share) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this answer by " + author_string + " to: " + Html.fromHtml(question_string));
            shareIntent.putExtra(Intent.EXTRA_TEXT,link);

            shareIntent.setType("text/plain");
            startActivity(shareIntent);
        }
        if(id==R.id.favorite){
            answersDataSource.open();
            if(favorited) {
                answersDataSource.updateFavorited(answer_id, false);
                Toast.makeText(getApplicationContext(),"Removed from favorites",Toast.LENGTH_SHORT).show();

            }
            else{
                answersDataSource.updateFavorited(answer_id, true);
                Toast.makeText(getApplicationContext(),"Added to favorites",Toast.LENGTH_SHORT).show();

            }
            answersDataSource.close();

        }
        if(id==android.R.id.home){
            finish();
        }
        if(id==R.id.nightMode){

            toggleNightMode(true);
        }
        if(id==R.id.recite){

            if(item.getTitle().equals("Recite")) {
                Element doc = Jsoup.parse(answer_string);

                textToSpeech.speak(doc.text(), TextToSpeech.QUEUE_FLUSH, null, null);
                item.setTitle("Pause");
            }
            else{

                if(textToSpeech!=null){
                    item.setTitle("Recite");
                    textToSpeech.stop();

                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public int getWidth(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width;
    }

    public String setHrefs(String answer){

        int start_index = 0;
        StringBuffer sb = new StringBuffer(answer);
        int last_index = answer.indexOf("href=\"/",start_index);
        int count=0;
        while(last_index>0){

            sb.insert(last_index+6+20*count,"http://www.quora.com");
            start_index=last_index;
            last_index = answer.indexOf("href=\"/",start_index+3);
            count++;
        }

        return sb.toString();
    }

    public void toggleNightMode(boolean nightMode){
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.rootLayout);
        ColorDrawable currentBackground = (ColorDrawable)linearLayout.getBackground();
        int color = currentBackground.getColor();
        //int currentTextColor = question.getCurrentTextColor();
        SharedPreferences prefs = this.getSharedPreferences("com.shaishav.apps.notebook",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();


        if(color==Color.parseColor("#f3f3f3") && nightMode){
            editor.putBoolean("nightMode",true);
            editor.commit();
            linearLayout.setBackgroundColor(Color.parseColor("#222222"));
            author.setTextColor(Color.GRAY);
            question.setTextColor(Color.GRAY);
            answer.setTextColor(Color.GRAY);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
            setStatusBarCustomColor("#222222");


        }
        else{
            editor.putBoolean("nightMode",false);
            editor.commit();
            linearLayout.setBackgroundColor(Color.parseColor("#f3f3f3"));
            author.setTextColor(-1979711488);
            question.setTextColor(-1979711488);
            answer.setTextColor(-1979711488);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#A52F2B")));
            setStatusBarCustomColor("#A52F2B");
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
    public void onScrollChanged(int i, boolean b, boolean b2) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        ActionBar ab = getSupportActionBar();
        if (scrollState == ScrollState.UP) {
            if (ab.isShowing()) {
                ab.hide();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (!ab.isShowing()) {
                ab.show();
            }
        }
    }
}

