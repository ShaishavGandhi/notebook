package com.apps.shaishav.notebook.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.apps.shaishav.notebook.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class GPlusLogin extends ActionBarActivity {

    private static final int RC_SIGN_IN = 0;

    // Google client to communicate with Google
    /*private GoogleApiClient mGoogleApiClient;

    private boolean mIntentInProgress;
    private boolean signedInUser;
    private ConnectionResult mConnectionResult;
    private SignInButton signinButton;
    private ImageView image;
    private TextView username, emailLabel;
    private LinearLayout profileFrame, signinFrame;*/
    private EditText userid;
    private EditText password;
    private ProgressDialog pd;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gplus_login);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("RobotoSlab-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        getSupportActionBar().setTitle("Notebook for Quora");
        userid = (EditText)findViewById(R.id.userid);
        password = (EditText)findViewById(R.id.password);




        pd = new ProgressDialog(this);
        pd.setMessage("Logging in...");

        Button login = (Button)findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View v) {
                                         if (userid.getText().toString().length() > 0 && password.getText().toString().length() > 0 && isValidEmail(userid.getText().toString())) {
                                             pd.show();
                                             ParseUser.logInInBackground(userid.getText().toString(), password.getText().toString(), new LogInCallback() {
                                                 @Override
                                                 public void done(ParseUser parseUser, ParseException e) {
                                                     pd.hide();
                                                     if (parseUser != null) {
                                                         SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.shaishav.apps.notebook", Context.MODE_PRIVATE);
                                                         SharedPreferences.Editor editor = prefs.edit();
                                                         editor.putBoolean("signedIn", true);
                                                         editor.putString("email", userid.getText().toString());
                                                         editor.commit();
                                                         Toast.makeText(getApplicationContext(),"Login successful",Toast.LENGTH_SHORT).show();
                                                         Intent letsStart = new Intent(getApplicationContext(), MainActivity.class);
                                                         startActivity(letsStart);
                                                         finish();
                                                     } else {
                                                         Toast.makeText(getApplicationContext(), "Login Unsuccesful", Toast.LENGTH_SHORT).show();
                                                     }
                                                 }

                                             });
                                         }
                                         else{
                                             Toast.makeText(getApplicationContext(),"Invalid input",Toast.LENGTH_SHORT).show();
                                         }
                                     }


                                     ;
                                 });

        Button signUp = (Button)findViewById(R.id.signup);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent signup = new Intent(getApplicationContext(),SignUp.class);
                startActivity(signup);

            }

        });





        //image = (ImageView) findViewById(R.id.image);
        //username = (TextView) findViewById(R.id.username);
        //emailLabel = (TextView) findViewById(R.id.email);

        //profileFrame = (LinearLayout) findViewById(R.id.profileFrame);
        //signinFrame = (LinearLayout) findViewById(R.id.signinFrame);

        Button button = (Button)findViewById(R.id.skip);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(GPlusLogin.this);

                alert.setTitle("Are you sure?");
                alert.setMessage("Skipping signing in will disable cloud backup of all your answers.\n\nNote: You can sign in even after skipping now.");

                // Set an EditText view to get user input


                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert.show();
            }
        });

       // mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Plus.API, Plus.PlusOptions.builder().build()).addScope(Plus.SCOPE_PLUS_LOGIN).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gplus_login, menu);
        return true;

    }
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id==R.id.action_forgot_password){
            Intent intent = new Intent(this,ForgotPassword.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /*  protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }

        if (!mIntentInProgress) {
            // store mConnectionResult
            mConnectionResult = result;

            if (signedInUser) {
                resolveSignInError();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        switch (requestCode) {
            case RC_SIGN_IN:
                if (responseCode == RESULT_OK) {
                    signedInUser = false;

                }
                mIntentInProgress = false;
                if (!mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        signedInUser = false;
        //Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
        getProfileInformation();
    }

    private void updateProfile(boolean isSignedIn) {
        if (isSignedIn) {
            signinFrame.setVisibility(View.GONE);
            profileFrame.setVisibility(View.VISIBLE);

        } else {
            signinFrame.setVisibility(View.VISIBLE);
            profileFrame.setVisibility(View.GONE);
        }
    }

    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

                final String personName = currentPerson.getDisplayName();
                final String personPhotoUrl = currentPerson.getImage().getUrl();
                final String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                String coverPhotoUrl="";
                Person.Cover cover = currentPerson.getCover();

                if(cover!=null){
                    Person.Cover.CoverPhoto coverPhoto = cover.getCoverPhoto();
                    if (coverPhoto != null) {
                        coverPhotoUrl = coverPhoto.getUrl();
                    }

                }
                else{
                    coverPhotoUrl = "https://www.dropbox.com/s/yxpfl3ugqdkvr3e/red_background-compressed.jpg?dl=0";
                }
                //final String cover = currentPerson.getCover().getCoverPhoto().getUrl();

                final String coverUrl = coverPhotoUrl;

                SharedPreferences prefs = this.getSharedPreferences("com.shaishav.apps.notebook", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("email", email);
                editor.putString("username",personName);
                editor.putBoolean("signedIn",true);
                editor.commit();

                /*
                ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
                query.whereEqualTo("email",email);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> scoreList, ParseException e) {
                        if (e == null) {
                            if(scoreList.size()==0){
                                ParseObject answer = new ParseObject("User");
                                answer.put("email",email);
                                answer.put("username",personName);
                                answer.put("cover",coverUrl);
                                answer.put("profile",personPhotoUrl);
                                answer.saveInBackground();
                            }
                        } else {
                            // something went wrong
                        }
                    }
                });*/
/*
                ParseUser user = new ParseUser();
                user.setUsername(email);
                user.setEmail(email);
                user.put("cover",coverUrl);
                user.put("profile",personPhotoUrl);
                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e==null){
                            //Yay
                        }
                        else{
                            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });




                //LoadProfileImage loadProfileImage = new LoadProfileImage();
                new RetrieveTokenTask().execute(email);
                new LoadProfileImage().execute(personPhotoUrl,"profile");
                new LoadProfileImage().execute(coverPhotoUrl,"cover");
                //updateProfile(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
        //updateProfile(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signin:
                if(isNetworkConnected()) {
                    pd.show();
                    googlePlusLogin();
                }
                else
                Toast.makeText(this,"No network detected",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void signIn(View v) {
        googlePlusLogin();
    }
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

    public void logout(View v) {
        googlePlusLogout();
    }

    private void googlePlusLogin() {
        if (!mGoogleApiClient.isConnecting()) {
            signedInUser = true;
            resolveSignInError();
        }
    }

    private void googlePlusLogout() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            updateProfile(false);
        }
    }

    private class LoadProfileImage extends AsyncTask<String,Integer,String> {
        ImageView downloadedImage;

        /*public LoadProfileImage(ImageView image) {
            this.downloadedImage = image;
        }*/
/*
        protected String doInBackground(String... urls) {
            //String url = urls[0];
            //Bitmap icon = null;
            try {
                saveImage(urls[0],urls[1]);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return urls[1];
        }

        protected void onPostExecute(String result) {
           // downloadedImage.setImageBitmap(result);
            if(result.equals("cover")){
                pd.hide();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finishActivity(0);

            }
        }
    }

    public void saveImage(String url,String type){

        try {
            URL imageURL = new URL(url);
            Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

            ContextWrapper cw = new ContextWrapper(this);
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

            //File naming
           /* int start_index = url.indexOf("qimg");
            int last_index = url.indexOf("?convert");

            url = url.substring(start_index+5,last_index-1);
            */
            // Create imageDir
  /*          File mypath=new File(directory,type+".png");

            FileOutputStream fos = null;
            fos = new FileOutputStream(mypath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();


        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private class RetrieveTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String scopes = "oauth2:profile email";
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), accountName, scopes);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UserRecoverableAuthException e) {

                startActivityForResult(e.getIntent(), 1);
            } catch (GoogleAuthException e) {
                //Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
            return token;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SharedPreferences prefs = getApplicationContext().getSharedPreferences("com.shaishav.apps.notebook", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("session-token",s);
            editor.commit();

        }

    }
*/
}
