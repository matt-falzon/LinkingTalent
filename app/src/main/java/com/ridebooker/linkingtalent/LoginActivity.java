package com.ridebooker.linkingtalent;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
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

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.provider.AuthCallback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.auth0.android.result.Delegation;
import com.auth0.android.result.UserProfile;
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

import java.io.Console;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISession;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;
import com.ridebooker.linkingtalent.Helpers.Credentials.CredentialsManager;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.w3c.dom.Text;

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
    private TextView tvLinkedin;


    private String linkedinUid;
    private Auth0 auth0;
    private UserProfile _profile;

    CallbackManager cbManager;

    //Firebase Auth
    FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    //private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        cbManager = CallbackManager.Factory.create();
        //Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        auth0 = new Auth0(getString(R.string.auth0_client_id), getString(R.string.auth0_domain));

        if(CredentialsManager.getCredentials(this).getIdToken() == null) {
            // Prompt Login screen.
            Log.d(TAG, "onCreate: No token exists");

        }
        else {
            validateToken(CredentialsManager.getCredentials(this).getIdToken(), auth0);
        }


        // Set up the login form.
        fbLoginButton = (LoginButton) findViewById(R.id.fb_login_button);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        progressBar = (ProgressBar) findViewById(R.id.login_progress);
        progressBar.setVisibility(View.INVISIBLE);
        tvLinkedin = (TextView) findViewById(R.id.tv_linkedin);

        fbLoginButton.setReadPermissions("email", "public_profile");

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
            LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);
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
                editor.putString(getResources().getString(R.string.key_user_img), profile.getProfilePictureUri(100, 100).toString());
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

    //<editor-fold desc="Linkedin">

    public void onClickLinkedin(View view){
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("scope", "name email displayName openid offline_access ");
        //parameters.put()
        WebAuthProvider.init(auth0)
                .withConnection("linkedin")
                .withParameters(parameters)
                .start(this, new AuthCallback() {
                    @Override
                    public void onFailure(@NonNull Dialog dialog) {
                        // Show error Dialog to user
                        Log.d(TAG, "onFailure: error logging in");
                    }

                    @Override
                    public void onFailure(AuthenticationException exception) {
                        // Show error to user
                        Log.d(TAG, "onFailure: " + exception.toString());
                    }

                    @Override
                    public void onSuccess(@NonNull Credentials credentials) {
                        // Store credentials
                        // Navigate to your main activity
                        Log.d("success", "token: " + credentials.getAccessToken());
                        CredentialsManager.saveCredentials(LoginActivity.this, credentials);
                    }
                });
    }

    //get Linkedin Session
    private void setUpdateState() {
        LISessionManager sessionManager = LISessionManager.getInstance(getApplicationContext());
        LISession session = sessionManager.getSession();
        boolean accessTokenValid = session.isValid();

        String token = session.getAccessToken().getValue();
        //FirebaseAuth.getInstance().createCustomToken(token);

        tvLinkedin.setText(session.getAccessToken().toString());

        signInWithToken(session.getAccessToken().toString());
        Log.d("Linkedin login", "Your token= " + token);

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

    @Override
    protected void onNewIntent(Intent intent) {
        if (WebAuthProvider.resume(intent)) {
            return;
        }
        super.onNewIntent(intent);
    }

    ///Parse the Linkedin JSON Data
    public void  parseData(JSONObject response)
    {
        try
        {
            Log.d("Linkedin login", "Linkedin \n" + "ID: " + response.get("id").toString() + "\n" +
                    "Name: " + response.get("formattedName").toString() + "\n"
                    + "Email: " + response.get("emailAddress").toString() + "\n");

            linkedinUid = response.get("id").toString();

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void validateToken(final String token, Auth0 auth0)
    {
        // Try to make an automatic login
        Log.d(TAG, "onCreate: Validating current token");
        final AuthenticationAPIClient authClient = new AuthenticationAPIClient(auth0);
        authClient.tokenInfo(token)
                .start(new BaseCallback<UserProfile, AuthenticationException>() {
                    @Override
                    public void onSuccess(UserProfile payload) {
                        // Valid ID
                        Log.d(TAG, "onSuccess: Current token valid \n" + payload.getName() + "\n" + payload.getId());
                        Log.d(TAG, "onSuccess: Refreshing token \n current token:" + token);
                        _profile = payload;
                        //getFirebaseToken(token, authClient);
                        refreshToken(token, authClient);
                    }

                    @Override
                    public void onFailure(AuthenticationException error) {
                        // Invalid ID Scenario
                        Log.d(TAG, "onSuccess: Current token expired");
                        authClient.delegationWithRefreshToken(token)
                                .start(new BaseCallback<Delegation, AuthenticationException>() {

                                    @Override
                                    public void onSuccess(Delegation payload) {
                                        Log.d(TAG, "onSuccess: refreshed token \n token: " + payload.getIdToken());
                                        MainActivity.tokenId = payload.getIdToken(); // New ID Token
                                        MainActivity.tokenExpires = payload.getExpiresIn();// New ID Token Expire Date
                                        try
                                        {
                                            Log.d(TAG, "onSuccess: " + MainActivity.userProfile.getGivenName());
                                        }catch (Exception e){
                                            Log.d(TAG, "onSuccess: failed to get given name" + e.toString());
                                        }

                                    }

                                    @Override
                                    public void onFailure(AuthenticationException error) {
                                        Log.d(TAG, "onFailure: unable to get refresh token");
                                    }
                                });
                    }
                });
    }

    private void refreshToken(String token, final AuthenticationAPIClient client)
    {
        //refresh token
        client.delegationWithIdToken(token)
                .start(new BaseCallback<Delegation, AuthenticationException>() {

                    @Override
                    public void onSuccess(Delegation payload) {
                        Log.d(TAG, "onSuccess: Refreshed token \n token: " + payload.getIdToken());
                        MainActivity.tokenId = payload.getIdToken(); // New ID Token
                        MainActivity.tokenExpires = payload.getExpiresIn(); // New ID Token Expire Date

                        //get token for firebase
                        getFirebaseToken(payload.getIdToken(), client);
                    }

                    @Override
                    public void onFailure(AuthenticationException error) {
                        //Show error to the user
                        Log.d(TAG, "onFailure: Cannot refresh token");
                    }
                });
    }

    private void getFirebaseToken(String token, AuthenticationAPIClient client)
    {
        String apiType = "firebase";
        client.delegationWithIdToken(token, apiType)
                .start(new BaseCallback<Map<String, Object>, AuthenticationException>() {

                    @Override
                    public void onSuccess(Map<String, Object> payload) {
                        Log.d(TAG, "getFirebaseToken onSuccess: \n" + payload.toString()
                                + "\n payload size: " + payload.size() + " keyset: " + payload.keySet());
                        //Your Firebase token will be in payload
                        signInWithToken(payload.get("id_token").toString());
                    }

                    @Override
                    public void onFailure(AuthenticationException error) {
                        //Delegation call failed
                    }
                });
    }

    public void signInWithToken(String token){

        Log.d(TAG, "signInWithToken: attempt to sign in with custom token...");
        mFirebaseAuth.signInWithCustomToken(token)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCustomToken: " + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCustomTokenFailed: ", task.getException());

                        }
                        else
                        {
                            Log.d(TAG, "onComplete: signInWithCustomToken successful! \n " +
                                    "Profile name: " + _profile.getName());

                        }
                    }
                });
    }
    //</editor-fold>

}

