package com.ridebooker.linkingtalent;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.mock.MockPackageManager;
import android.util.Log;

public class SplashActivity extends AppCompatActivity
{
    private Intent myIntent;
    public static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.e("testing", "Permission is granted");
                myIntent = new Intent(this, MainActivity.class);

                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        startActivity(myIntent);
                        finish();
                    }
                }, 2000);

            } else {


                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                Log.e("testing", "Permission is revoked");
            }
        } else
        { //permission is automatically granted on sdk<23 upon installation
            Log.e("testing", "Permission is already granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSION_ACCESS_COARSE_LOCATION)
        {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED )
            {
                myIntent = new Intent(SplashActivity.this, MainActivity.class);

                        startActivity(myIntent);
                        finish();
            }
            else
            {
                finish();
            }
        }
    }

}
