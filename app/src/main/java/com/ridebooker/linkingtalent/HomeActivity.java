package com.ridebooker.linkingtalent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ridebooker.linkingtalent.datatypes.TalentChamp;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener
{

    private static final String TAG = "HomeActivity";
    private TextView tvNavName, tvNavEmail, tvHomeName;
    private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;
    private TalentChamp user;

    //Firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Initialize Firebase Variables
        mFirebaseAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupSharedPreferences();
        //getHash();

        //Fill relevent fields with user data
        onSignedInInitialize(mFirebaseAuth.getCurrentUser());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        setupMainNav();

        //check if user is loggin in and start login state listener
        loginState();
    }

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
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        } else
        {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        //unregister sharedpreferences listener
        SharedPreferences prefs = getSharedPreferences(getString(R.string.LOCAL_PREFERENCES), Context.MODE_PRIVATE);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }


    //<editor-fold desc="Menu">

    private void setupMainNav(){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        tvNavName = (TextView) header.findViewById(R.id.tv_nav_header_name);
        tvNavEmail = (TextView) header.findViewById(R.id.tv_nav_header_email);

        tvNavName.setText(user.getName());
        tvNavEmail.setText(user.getEmail());
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
                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_terms:
                Toast.makeText(this, "Terms", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_signout:
                //Toast.makeText(this, "Facebook logged out", Toast.LENGTH_SHORT).show();

                //remove preferences
                SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.LOCAL_PREFERENCES), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.clear();
                editor.apply();

                //log out of firebase
                mFirebaseAuth.signOut();

                //log out of Facebook
                LoginManager.getInstance().logOut();
                //Start the login Activity again
                //Intent i = new Intent(this, LoginActivity.class);
                //startActivity(i);
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

        switch(id){
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_jobs:
                Toast.makeText(this, "View Jobs", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(HomeActivity.this, JobActivity.class);
                startActivity(i);
                break;
            case R.id.nav_search:
                Toast.makeText(this, "Search Jobs", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_profile:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_create:
                Toast.makeText(this, "New job", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "TBC", Toast.LENGTH_SHORT).show();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        //tvNavName.setText(prefs.getString(getString(R.string.key_name), null));
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);

    }

    //</editor-fold>

    public void loginState()
    {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(HomeActivity.this, "Logged in listener", Toast.LENGTH_SHORT).show();
                    onSignedInInitialize(user);
                } else {
                    // User is signed out
                    Toast.makeText(HomeActivity.this, "Logged out listener", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(i);
                }
            }
        };
    }

    private void onSignedInInitialize(FirebaseUser user)
    {
        this.user = new TalentChamp(user.getUid(), user.getDisplayName(), user.getEmail(), "location");
        tvHomeName = (TextView) findViewById(R.id.tv_name);
        tvHomeName.setText(this.user.getName() + "\n \n" + this.user.getId());

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
