package com.ridebooker.linkingtalent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ridebooker.linkingtalent.datatypes.Job;
import com.ridebooker.linkingtalent.datatypes.TalentChamp;
import com.ridebooker.linkingtalent.Helpers.ImageLoadTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener
{

    private static final String TAG = "MainActivity";
    private TextView tvNavName, tvNavEmail;
    private ImageView imgNav;
    private PopupWindow popupWindow;
    //private RelativeLayout mainLayout;
    private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;
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
    public boolean viewingJob = false;

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

        //Navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();



        //check if user is loggin in and start login state listener
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
        tvNavEmail = (TextView) header.findViewById(R.id.tv_nav_header_email);
        imgNav = (ImageView) header.findViewById(R.id.nav_image);
            tvNavName.setText(user.getName());
            tvNavEmail.setText(user.getEmail());
        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.LOCAL_PREFERENCES), Context.MODE_PRIVATE);
        //get facebook image
        new ImageLoadTask(sharedPrefs.getString(getResources().getString(R.string.key_user_img), null), imgNav).execute();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.action_about:
                LayoutInflater aboutInflater = (LayoutInflater) this.getSystemService(this.LAYOUT_INFLATER_SERVICE);
                View aboutPopupView = aboutInflater.inflate(R.layout.popup_about,null);

                if(popupWindow != null)
                    if(popupWindow.isShowing())
                        popupWindow.dismiss();

                popupWindow = new PopupWindow(
                        aboutPopupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

                popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

                // Set an elevation value for popup window
                // Call requires API level 21
                if(Build.VERSION.SDK_INT>=21){
                    popupWindow.setElevation(5.0f);
                }

                // Get a reference for the popup view close button
                ImageButton closeButton = (ImageButton) aboutPopupView.findViewById(R.id.ib_close);

                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Dismiss the popup window
                        popupWindow.dismiss();
                    }
                });

                // Finally, show the popup window at the center location of root relative layout
                popupWindow.showAtLocation(aboutPopupView, Gravity.CENTER,0,0);
                return true;
            case R.id.action_terms:
                LayoutInflater termsInflater = (LayoutInflater) this.getSystemService(this.LAYOUT_INFLATER_SERVICE);
                View popupView = termsInflater.inflate(R.layout.popup_terms,null);

                if(popupWindow != null)
                    if(popupWindow.isShowing())
                        popupWindow.dismiss();

                popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

                popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

                // Set an elevation value for popup window
                // Call requires API level 21
                if(Build.VERSION.SDK_INT>=21){
                    popupWindow.setElevation(5.0f);
                }

                // Get a reference for the popup view close button
                ImageButton termsCloseButton = (ImageButton) popupView.findViewById(R.id.ib_close);

                termsCloseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Dismiss the popup window
                        popupWindow.dismiss();
                    }
                });

                // Finally, show the popup window at the center location of root relative layout
                popupWindow.showAtLocation(popupView, Gravity.CENTER,0,0);
                return true;
            case R.id.action_signout:

                //remove preferences
                SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.LOCAL_PREFERENCES), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.clear();
                editor.apply();

                //log out of firebase
                mFirebaseAuth.signOut();
                //log out of Facebook
                LoginManager.getInstance().logOut();

                return true;
            default:

        }

        return super.onOptionsItemSelected(item);
    }

    //Menu Navigation
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        switch(id){
            case R.id.nav_home:
                HomeFragment homeFrag = new HomeFragment();
                transaction.replace(R.id.content_main, homeFrag, "job_fragment");
                transaction.commit();
                break;
            case R.id.nav_jobs:
                JobBoardFragment jobFrag = new JobBoardFragment();
                transaction.replace(R.id.content_main, jobFrag, "job_fragment");
                transaction.commit();
                break;
            case R.id.nav_search:
                Toast.makeText(this, "Search Jobs", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_profile:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_create:
                createJob();
                break;
            default:
                Toast.makeText(this, "TBC", Toast.LENGTH_SHORT).show();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void createJob()
    {
        viewingJob = true;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        CreateJobFragment createFrag = new CreateJobFragment();
        transaction.replace(R.id.content_main, createFrag, "create_fragment");
        transaction.commit();
    }

    public void viewJob(String jobKey)
    {
        viewingJob = true;

        FragmentManager fragmentManager = getSupportFragmentManager();
        ViewJobFragment viewJobFrag = new ViewJobFragment();
        Bundle b = new Bundle();
        b.putString("key", jobKey);
        viewJobFrag.setArguments(b);
        FragmentTransaction ft = fragmentManager.beginTransaction();;
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        ft.addToBackStack("jobBoard");
        ft.replace(R.id.content_main, viewJobFrag);
        ft.commit();
    }

    private void showHomeFragment()
    {
        HomeFragment frag = new HomeFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.content_main, frag, "home_fragment");
        transaction.commit();
    }


    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if(viewingJob == true)
        {
            viewingJob = false;
            FragmentManager fragmentManager = getSupportFragmentManager();
            JobBoardFragment jobBoardFrag = new JobBoardFragment();
            FragmentTransaction ft = fragmentManager.beginTransaction();;
            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            ft.addToBackStack("jobBoard");
            ft.replace(R.id.content_main, jobBoardFrag);
            ft.commit();

        }
        else
        {
            super.onBackPressed();
        }
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

    private void loginState()
    {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged: Logged in");
                    onSignedInInitialize(user);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: Logged out");
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(i);
                }
            }
        };
    }


    private void onSignedInInitialize(FirebaseUser user)
    {
        this.user = new TalentChamp(user.getUid(), user.getDisplayName(), user.getEmail(), "location", true);
        dbUsersRef.child(user.getUid().toString()).setValue(this.user);
        setupMainNav();

        //Curently using this to stop the home fragment showing
        //when another intent creates a new activity eg photo picker
        if(viewingJob == false)
            showHomeFragment();
    }


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
}
