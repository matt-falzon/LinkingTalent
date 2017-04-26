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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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


import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
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
import com.ridebooker.linkingtalent.Helpers.ImageLoadTask;
import com.ridebooker.linkingtalent.datatypes.TalentChamp;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.w3c.dom.Text;

import static android.Manifest.permission.READ_CONTACTS;


public class LoginActivity extends AppCompatActivity
{
    private static final String TAG = "LoginActivity";

    //Linkedin constants
    private static final String host = "api.linkedin.com";
    private static final String topCardUrl = "https://" + host + "/v1/people/~:(id,email-address,formatted-name,phone-numbers,public-profile-url,picture-url,picture-urls::(original))";
    private static final int RC_PHOTO_PICKER = 1234;

    private final static Logger LOGGER = Logger.getLogger(LoginActivity.class.getName());

    //UI References
    private View mProgressView;
    private View mLoginFormView;
    private LoginButton fbLoginButton;
    private ProgressBar progressBar;
    private EditText etEmail, etPassword;
    private PopupWindow popupWindow;
    public ImageView imgProfile;

    private String linkedinUid;
    private Auth0 auth0;
    private UserProfile _profile;
    private Uri selectedImageUri;
    private TalentChamp champ = new TalentChamp();

    CallbackManager cbManager;

    //Firebase Auth
    FirebaseAuth _firebaseAuth = FirebaseAuth.getInstance();
    //private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        cbManager = CallbackManager.Factory.create();
        //Firebase Auth
        _firebaseAuth = FirebaseAuth.getInstance();

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
        etEmail = (EditText) findViewById(R.id.login_email);
        etPassword = (EditText) findViewById(R.id.login_password);
        //mLoginFormView = findViewById(R.id.login_form);
        //mProgressView = findViewById(R.id.login_progress);
        //progressBar = (ProgressBar) findViewById(R.id.login_progress);
        //progressBar.setVisibility(View.INVISIBLE);

        fbLoginButton.setReadPermissions("email", "public_profile");

        setupTextListeners();

    }

    //hides the keyboard if the user clicks the LT logo
    public void onClickImg(View view)
    {
        closeKeyboard();
    }

    private void closeKeyboard()
    {
        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

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
        else if (requestCode == RC_PHOTO_PICKER && resultCode == -1) {
            //selectedImageUri is the url for the image on the device
            selectedImageUri = data.getData();
            if(selectedImageUri != null)
                imgProfile.setImageURI(selectedImageUri);
                //new ImageUploadTask().execute();
        }
    }



    //we dont want the back button pressed in login screen
    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
        super.onBackPressed();
    }

    //<editor-fold desc="Email">

    private void setupTextListeners()
    {
        etPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {


            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    closeKeyboard();
                    emailLogin(etEmail.getText().toString(), etPassword.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    private void emailLogin(String email, String password)
    {
        _firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = _firebaseAuth.getCurrentUser();
                            //updateUI(user);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    //</editor-fold>

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
        _firebaseAuth.signInWithCredential(credential)
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
        parameters.put("scope", "openid name email displayName offline_access ");
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

                        if(CredentialsManager.getCredentials(LoginActivity.this).getIdToken() == null) {
                            // Prompt Login screen.
                            Log.d(TAG, "onCreate: No token exists");

                        }
                        else {
                            validateToken(CredentialsManager.getCredentials(LoginActivity.this).getIdToken(), auth0);
                        }
                    }
                });
    }

    //get Linkedin Session
    private void setUpdateState() {
        LISessionManager sessionManager = LISessionManager.getInstance(getApplicationContext());
        LISession session = sessionManager.getSession();
        boolean accessTokenValid = session.isValid();

        String token = session.getAccessToken().getValue();

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
                        Log.d(TAG, "onSuccess: Current token valid full payload: "  +
                                "\n email : " + payload.getEmail() +
                                "\n name : " + payload.getName() +
                                "\n picture Url : " + payload.getPictureURL());

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
                                        //MainActivity.tokenId = payload.getIdToken(); // New ID Token
                                        //MainActivity.tokenExpires = payload.getExpiresIn();// New ID Token Expire Date
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
        _firebaseAuth.signInWithCustomToken(token)
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
                            FirebaseUser user = _firebaseAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(_profile.getName())
                                    .setPhotoUri(Uri.parse(_profile.getPictureURL()))
                                    .build();
                            if(user != null)
                            {
                                Log.d(TAG, "onComplete: Updating user profile");
                                user.updateProfile(profileUpdates);
                            }

                            Log.d(TAG, "onComplete: signInWithCustomToken successful! \n " +
                                    "Profile name: " + _profile.getName());
                            finish();
                        }
                    }
                });
    }

    //</editor-fold>

    //<editor-fold desc="Email Registration">

    public void register(View view)
    {
        LayoutInflater inflater = (LayoutInflater) LoginActivity.this.getSystemService(LoginActivity.this.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_register,null);

        if(popupWindow != null)
            if(popupWindow.isShowing())
                popupWindow.dismiss();

        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        popupWindow.setFocusable(true);

        // Set an elevation value for popup window
        // Call requires API level 21
        if(Build.VERSION.SDK_INT>=21){
            popupWindow.setElevation(5.0f);
        }

        // Get a reference for the popup view close button
        ImageButton closeButton = (ImageButton) popupView.findViewById(R.id.ib_close);
        imgProfile = (ImageView) popupView.findViewById(R.id.register_profile_img);
        Button btnRegister = (Button) popupView.findViewById(R.id.btn_register_submit);

        btnRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();
            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                popupWindow.dismiss();
            }
        });

        // Finally, show the popup window at the center location of root relative layout
        popupWindow.showAtLocation(view, Gravity.CENTER,0,0);

    }

    public void onClickProfileImage(View view)
    {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
    }

    private class ImageUploadTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute()
        {
            //put a progress dialogue here
            Log.d(TAG, "onPreExecute: Attempting to get image");
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            StorageReference photoRef = MainActivity.firebaseRootStorageRef.child("newUser" + "/");

            UploadTask uploadTask = photoRef.child(selectedImageUri.getLastPathSegment()).putFile(selectedImageUri);
            // Upload file to Firebase Storage
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    champ.setPhotoUri(downloadUri);
                    champ.setPhoto(downloadUri.toString());
                    Log.d(TAG, "onSuccess: image uploaded " + champ.getPhotoUri().toString());
                    new ImageLoadTask(downloadUri.toString(), imgProfile).execute();
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Log.d(TAG, "onFailure: failed to upload image");
                    Toast.makeText(LoginActivity.this, "Failed to upload Photo", Toast.LENGTH_SHORT).show();
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            //imgProfile.setImageURI(champ.getPhotoUri());
            //imgProfile.setVisibility(View.INVISIBLE);
            Log.d(TAG, "onPostExecute: executed ");
        }
    }

    //</editor-fold>
}

