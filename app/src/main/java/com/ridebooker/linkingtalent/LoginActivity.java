package com.ridebooker.linkingtalent;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
/*
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISession;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;*/

import java.io.Console;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity
{
    private static final String TAG = "LoginActivity";

    //Linkedin constants
    private static final String host = "api.linkedin.com";
    private static final String topCardUrl = "https://" + host + "/v1/people/~:(id,email-address,formatted-name,phone-numbers,public-profile-url,picture-url,picture-urls::(original))";


    private final static Logger LOGGER = Logger.getLogger(LoginActivity.class.getName());

    //UI References
    private View mProgressView;
    private View mLoginFormView;
    private LoginButton fbLoginButton;
    private ProgressBar progressBar;

    CallbackManager cbManager;

    //Firebase Auth
    FirebaseAuth mFirebaseAuth;
    //private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        cbManager = CallbackManager.Factory.create();
        //Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        // Set up the login form.
        fbLoginButton = (LoginButton) findViewById(R.id.fb_login_button);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        progressBar = (ProgressBar) findViewById(R.id.login_progress);
        progressBar.setVisibility(View.INVISIBLE);

        fbLoginButton.setReadPermissions("email", "public_profile");


        try
        {
            Log.d(TAG, "File Input Stream");
            FileInputStream serviceAccount = new FileInputStream("path/to/custom_auth.json");
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        setupFacebookCallback();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    /**
     *  Calls the Facebook Callback Methods
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(FacebookSdk.isFacebookRequestCode(requestCode))
        {
            cbManager.onActivityResult(requestCode, resultCode, data);
        }
        else//Linkedin request code
        {
            //LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);
        }
    }



    //we dont want the back button pressed in login screen
    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
        super.onBackPressed();
    }

    //<editor-fold desc="Facebook">

    /*
    Sets up the callback methods for logging into facebook
     */
    private void setupFacebookCallback(){
        fbLoginButton.registerCallback(cbManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                //get facebook profile
                Profile profile = Profile.getCurrentProfile();

                SharedPreferences sharedPrefs = getSharedPreferences(getResources().getString(R.string.LOCAL_PREFERENCES), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();

                //set user preferences
                editor.putString(getResources().getString(R.string.key_user_id), profile.getId());
                editor.putString(getResources().getString(R.string.key_name), profile.getFirstName() + " " + profile.getLastName());
                editor.apply();

                //get access token and take to Firebase
                handleFacebookAccessToken(AccessToken.getCurrentAccessToken());
                //mFirebaseAuth.


            }


            @Override
            public void onCancel() {
                Toast.makeText(getBaseContext(), "Login Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(getBaseContext(), "Login Failed", Toast.LENGTH_SHORT).show();

            }
        });
    }

    //Add facebook user to Firebase Auth
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("", "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w("", "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            progressBar.setVisibility(View.INVISIBLE);
                            finish();
                        }

                    }
                });
    }

    public void onClickFacebook(View view)
    {
        //progressBar.setVisibility(View.VISIBLE);
        fbLoginButton.performClick();
    }

    //</editor-fold>
/*
    //<editor-fold desc="Linkedin">

    public void onClickLinkedin(View view){
        LISessionManager.getInstance(getApplicationContext()).init(this, buildScope(), new AuthListener() {
            @Override
            public void onAuthSuccess() {
                setUpdateState();
                getUserData();
                Toast.makeText(getApplicationContext(), "Linkedin Success!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthError(LIAuthError error) {
                Toast.makeText(getApplicationContext(), "failed " + error.toString(), Toast.LENGTH_LONG).show();
            }
        }, true);
    }

    //get Linkedin Session
    private void setUpdateState() {
        LISessionManager sessionManager = LISessionManager.getInstance(getApplicationContext());
        LISession session = sessionManager.getSession();
        boolean accessTokenValid = session.isValid();
        String token = session.getAccessToken().toString();
        //FirebaseAuth.getInstance().createCustomToken(token);



        signInWithToken(token);
        tvLogin.setText("Your token= " + token);

        System.out.println(token);
    }

    // Build the list of member permissions our LinkedIn session requires
    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.W_SHARE, Scope.R_EMAILADDRESS);
    }

    ///Gets the user data, if successfull passes JSON to parseData
    public void getUserData(){
        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(this, topCardUrl, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse result) {
                try {

                    parseData(result.getResponseDataAsJson());
                    //progress.dismiss();

                } catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onApiError(LIApiError error) {
                Toast.makeText(getApplicationContext(), "LinkedinAPI ERROR", Toast.LENGTH_LONG).show();

            }
        });
    }

    ///Parse the Linkedin JSON Data
    public void  parseData(JSONObject response)
    {
        try
        {
            tvLinkedin.setText("Linkedin \n" + "ID: " + response.get("id") + "\n" +
                    "Name: " + response.get("formattedName").toString() + "\n"
                    + "Email: " + response.get("emailAddress").toString() + "\n"

            );


            //Picasso.with(this).load(response.getString("pictureUrl")).into(profile_pic);

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void signInWithToken(String token){
        mFirebaseAuth.signInWithCustomToken(token)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCustomToken:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCustomToken", task.getException());
                            Toast.makeText(LoginActivity.this, "Linkedin Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    //</editor-fold>
*/
}

