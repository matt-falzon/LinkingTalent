package com.ridebooker.linkingtalent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.auth0.android.result.UserProfile;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ridebooker.linkingtalent.Helpers.Credentials.CredentialsManager;
import com.ridebooker.linkingtalent.Models.Job;
import com.ridebooker.linkingtalent.Models.TalentChamp;
import com.ridebooker.linkingtalent.Helpers.ImageLoadTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener, FragmentManager.OnBackStackChangedListener
{

    private static final String TAG = "MainActivity";
    private TextView tvNavName;
    private ImageView imgNav;
    public static PopupWindow popupWindow;
    FragmentManager fragmentManager = getSupportFragmentManager();
    //private RelativeLayout mainLayout;
    public static TalentChamp user;

    //Firebase Auth
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    //Firebase Database
    public static DatabaseReference dbRootRef = FirebaseDatabase.getInstance().getReference();
    public static DatabaseReference dbJobRef = dbRootRef.child("job");
    public static DatabaseReference dbUsersRef = dbRootRef.child("users");
    private ChildEventListener jobChildEventListener;
    //Firebase Storage
    private FirebaseStorage firebaseStorage;
    public static StorageReference firebaseRootStorageRef;
    public static StorageReference firebaseCompanyImageRef;
    public static StorageReference firebaseProfileImageRef;

    public static UserProfile userProfile;
    public static String tokenId;
    public static long tokenExpires;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialize Firebase Variables
        mFirebaseAuth = FirebaseAuth.getInstance();
        //Firebase storage references
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseRootStorageRef = firebaseStorage.getReference();
        firebaseCompanyImageRef = firebaseStorage.getReference().child("company_images");
        firebaseProfileImageRef = firebaseStorage.getReference().child("profile_images");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupSharedPreferences();

        //track stack changes for on change method
        fragmentManager.addOnBackStackChangedListener(this);

        //Navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        setUpDrawer(drawer, toolbar);
        //getHash();
        //check if user is logged in and start login state listener
        loginState();

        //setupMainNav();
    }

    //<editor-fold desc="Lifecycle">
    @Override
    protected void onResume()
    {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        //Listener for whenever data changes in the job board
        dbJobRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        //unregister sharedpreferences listener
        SharedPreferences prefs = getSharedPreferences(getString(R.string.LOCAL_PREFERENCES), Context.MODE_PRIVATE);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    //</editor-fold>

    //<editor-fold desc="Menu">

    private void setupMainNav(){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        tvNavName = (TextView) header.findViewById(R.id.tv_nav_header_name);
        imgNav = (ImageView) header.findViewById(R.id.nav_image);
        tvNavName.setText(user.getFirstName());
        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.LOCAL_PREFERENCES), Context.MODE_PRIVATE);
        //get profile image
        new ImageLoadTask(user.getPhoto().toString(), imgNav).execute();
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //close keyboard
        InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);

        return true;
    }

     top right popup menu, not using for now

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.action_about:

                return true;
            case R.id.action_terms:

                return true;
            case R.id.action_signout:



                return true;
            default:

        }

        return super.onOptionsItemSelected(item);
    }
    */
    //Menu Navigation
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch(id){
            case R.id.nav_share:
                navShare();
                break;
            case R.id.nav_jobs:
                navJobBoard();
                break;
            /*
            case R.id.nav_profile:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_create:
                navCreateJob();
                break;*/
            case R.id.nav_howitworks:
                navHowitworks();
                break;
            case R.id.nav_about:
                navAbout();
                break;
            case R.id.nav_logout:
                //remove preferences
                SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.LOCAL_PREFERENCES), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.clear();
                editor.apply();

                //log out of firebase
                mFirebaseAuth.signOut();
                //clear current user
                user = null;
                //log out of social network
                CredentialsManager.deleteCredentials(this);
                LoginManager.getInstance().logOut();
                break;
            default:
                Toast.makeText(this, "TBC", Toast.LENGTH_SHORT).show();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void navHome()
    {
        //popFragmentStack();
        //if user is trying to open the same fragment return
        if(checkSameFragment("home"))
            return;

        HomeFragment fragment = new HomeFragment();
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.animation_slide_in_right, R.anim.animation_slide_out_left, R.anim.animation_slide_in_right, R.anim.animation_slide_out_left);
        transaction.add(R.id.content_main, fragment, "home fragment");
        transaction.addToBackStack("home");
        transaction.commit();
    }

    private void navJobBoard()
    {
        //popFragmentStack();
        //if user is trying to open the same fragment return
        if(checkSameFragment("jobBoard"))
            return;

        JobBoardFragment fragment = new JobBoardFragment();
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.animation_slide_in_right, R.anim.animation_slide_out_left, R.anim.animation_slide_in_right, R.anim.animation_slide_out_left);
        transaction.add(R.id.content_main, fragment, "job fragment");
        transaction.addToBackStack("jobBoard");
        transaction.commit();
    }

    public void navCreateJob()
    {
        //if user is trying to open the same fragment return
        if(checkSameFragment("createJob"))
            return;

        //if the user did not navigate here from the job board pop the fragment stack
        //if(fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName() != "jobBoard")
            //popFragmentStack();

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.animation_slide_in_right, R.anim.animation_slide_out_left, R.anim.animation_slide_in_right, R.anim.animation_slide_out_left);
        CreateJobFragment fragment = new CreateJobFragment();
        transaction.add(R.id.content_main, fragment, "create_fragment");
        transaction.addToBackStack("createJob");
        transaction.commit();
    }

    private void navShare()
    {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Linking Talent App");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Share content goes here \n .\n .. \n ... \n .. \n .");
        startActivity(Intent.createChooser(shareIntent, "Send to"));
    }

    public void navJobInfo(String jobKey)
    {
        fragmentManager = getSupportFragmentManager();
        JobInfoFragment fragment = new JobInfoFragment();

        //create bundle with job key
        Bundle b = new Bundle();
        b.putString("key", jobKey);
        fragment.setArguments(b);

        FragmentTransaction ft = fragmentManager.beginTransaction();;
        ft.setCustomAnimations(R.anim.animation_slide_in_right, R.anim.animation_slide_out_left, R.anim.animation_slide_in_right, R.anim.animation_slide_out_left);
        ft.add(R.id.content_main, fragment);
        ft.addToBackStack("jobInfo");
        ft.commit();
    }

    public void navViewJob(String jobKey)
    {
        fragmentManager = getSupportFragmentManager();
        ViewJobFragment fragment = new ViewJobFragment();

        //create bundle with job key
        Bundle b = new Bundle();
        b.putString("key", jobKey);
        fragment.setArguments(b);

        FragmentTransaction ft = fragmentManager.beginTransaction();;
        ft.setCustomAnimations(R.anim.animation_slide_in_right, R.anim.animation_slide_out_left, R.anim.animation_slide_in_right, R.anim.animation_slide_out_left);
        ft.add(R.id.content_main, fragment);
        ft.addToBackStack("viewJob");
        ft.commit();
    }

    public void navHowitworks()
    {
        fragmentManager = getSupportFragmentManager();
        HowItWorksFragment fragment = new HowItWorksFragment();

        FragmentTransaction ft = fragmentManager.beginTransaction();;
        ft.setCustomAnimations(R.anim.animation_slide_in_right, R.anim.animation_slide_out_left, R.anim.animation_slide_in_right, R.anim.animation_slide_out_left);
        ft.add(R.id.content_main, fragment);
        ft.addToBackStack("howitworks");
        ft.commit();
    }


    private void navAbout()
    {
        fragmentManager = getSupportFragmentManager();
        AboutFragment fragment = new AboutFragment();

        FragmentTransaction ft = fragmentManager.beginTransaction();;
        ft.setCustomAnimations(R.anim.animation_slide_in_right, R.anim.animation_slide_out_left, R.anim.animation_slide_in_right, R.anim.animation_slide_out_left);
        ft.add(R.id.content_main, fragment);
        ft.addToBackStack("about");
        ft.commit();
    }
    //Checks if the fragment the user is trying to open is the same as the current fragment
    public boolean checkSameFragment(String fragmentClicked)
    {
        fragmentManager = getSupportFragmentManager();
        if(fragmentManager.getBackStackEntryCount() > 0)
        {
            String fragmentTag = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
            if (fragmentTag.equals(fragmentClicked))
                return true;
            else
                return false;
        }
        return false;
    }

    //pops the fragment stack when selecting a new root stack
    private void popFragmentStack()
    {
        fragmentManager = this.getSupportFragmentManager();
        for(int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
            fragmentManager.popBackStack();
        }
    }


    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        //If the popup window is open close it
        try{
            if(popupWindow.isShowing())
            {
                popupWindow.dismiss();
                return;
            }
        }catch(NullPointerException e){
            Log.d(TAG, "onBackPressed: " + e);
        }

        //if we are at the end of the fragment stack close the app
        if(fragmentManager.getBackStackEntryCount() == 1)
        {
            finish();
        }
        else //Otherwise pop stack
        {
            fragmentManager.popBackStack();
        }
    }

    private void setUpDrawer( DrawerLayout drawer, Toolbar toolbar)
    {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                super.onDrawerOpened(drawerView);
            }
        };



        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    //</editor-fold>

    //<editor-fold desc="Database">
    private void attachDatabaseReadListener() {
        if (jobChildEventListener == null) {
            jobChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Job newJob = dataSnapshot.getValue(Job.class);

                }
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                public void onCancelled(DatabaseError databaseError) {}
            };
            dbRootRef.addChildEventListener(jobChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (jobChildEventListener != null) {
            dbRootRef.removeEventListener(jobChildEventListener);
            jobChildEventListener = null;
        }
    }

    //</editor-fold>

    //<editor-fold desc="SharedPreferences">
    private void setupSharedPreferences() {
        /* not using for now over a private preferences
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        */

        //Ensures the users preferences are private
        SharedPreferences prefs = getSharedPreferences(getString(R.string.LOCAL_PREFERENCES), Context.MODE_PRIVATE);
        // Register the listener
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);

    }

    //</editor-fold>

    //</editor-fold desc="login initialization">
    private void loginState()
    {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                //Toast.makeText(MainActivity.this, user.getDisplayName(), Toast.LENGTH_SHORT).show();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged: Logged in");
                    onSignedInInitialize(user);
                } else {
                    // User is signed out
                    mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
                    Log.d(TAG, "onAuthStateChanged: Logged out");
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(i);
                }
            }
        };
    }

    private void onSignedInInitialize(FirebaseUser firebaseUser)
    {
        user = new TalentChamp(firebaseUser.getUid(), firebaseUser.getDisplayName(), firebaseUser.getEmail());
        user.setPhoto(firebaseUser.getPhotoUrl().toString());

        Query query = dbUsersRef.equalTo(firebaseUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                //TalentChamp t = dataSnapshot.getValue(TalentChamp.class);
                //Log.d(TAG, "Got TC: " + t.getName() + " " + t.getEmail());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        String[] names = user.getName().split(" ");
        user.setFirstName(names[0]);
        user.setLastName(names[1]);

        //sync user data
        //dbUsersRef.child(firebaseUser.getUid()).setValue(user);

        setupMainNav();

        //stops app randomly launching a second home fragment
        if(!checkSameFragment("jobBoard"))
        {
            JobBoardFragment fragment = new JobBoardFragment();
            fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.animation_slide_in_right, R.anim.animation_slide_out_left, R.anim.animation_slide_in_right, R.anim.animation_slide_out_left);
            transaction.add(R.id.content_main, fragment, "job board fragment");
            transaction.addToBackStack("jobBoard");
            transaction.commit();
        }
    }

    //</editor-fold>

    private void getHash(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.ridebooker.linkingtalent",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                String s = Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    @Override
    public void onBackStackChanged()
    {
        //log stack
        for(int i = 0; i < fragmentManager.getBackStackEntryCount(); i++)
        {
            if(i == 0)
                Log.d(TAG, "onBackStackChanged: " + fragmentManager.getBackStackEntryAt(i) + " stack count: " + fragmentManager.getBackStackEntryCount());
            else
                System.out.println(fragmentManager.getBackStackEntryAt(i));
        }

    }


}
