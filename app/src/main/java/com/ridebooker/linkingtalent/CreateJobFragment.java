package com.ridebooker.linkingtalent;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.test.mock.MockPackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ridebooker.linkingtalent.Helpers.ImageLoadTask;
import com.ridebooker.linkingtalent.Models.Job;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class CreateJobFragment extends Fragment implements
        ConnectionCallbacks, OnConnectionFailedListener
{

    private static final String TAG = "Create Job Fragment";
    private static final int RC_PHOTO_PICKER = 1234;

    Button btnButton;
    EditText etCompany, etTitle, etDescription;
    SeekBar bountySeekBar;
    ImageButton btnLocation, btnPhotoPicker;
    Spinner minSpinner, maxSpinner, employmentTypeSpinner, spJobCat;
    TextView tvBounty, tvLocation;

    private Uri selectedImageUri;

    protected GoogleApiClient googleApiClient;
    protected Location location;
    public static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    Geocoder geocoder;

    private final Job j = new Job(MainActivity.user.getId());

    public CreateJobFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_create_job, container, false);

        btnButton = (Button) view.findViewById(R.id.create_job_insert_button);
        etCompany = (EditText) view.findViewById(R.id.create_job_company_name);
        etTitle = (EditText) view.findViewById(R.id.create_job_title);
        spJobCat = (Spinner) view.findViewById(R.id.create_job_cat);
        tvLocation = (TextView) view.findViewById(R.id.create_job_location);
        etDescription = (EditText) view.findViewById(R.id.create_job_description);
        bountySeekBar = (SeekBar) view.findViewById(R.id.create_job_seekBar);
        minSpinner = (Spinner) view.findViewById(R.id.create_job_min_spinner);
        maxSpinner = (Spinner) view.findViewById(R.id.create_job_max_spinner);
        employmentTypeSpinner = (Spinner) view.findViewById(R.id.create_job_employment_type);
        tvBounty = (TextView) view.findViewById(R.id.create_job_tv_bounty_value);
        btnLocation = (ImageButton) view.findViewById(R.id.create_job_location_button);
        btnPhotoPicker = (ImageButton) view.findViewById(R.id.create_job_photo_picker);

        initGui();

        buildGoogleApiClient();

        geocoder = new Geocoder(getContext(), Locale.getDefault());
        // Inflate the layout for this fragment
        return view;
    }

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    private void initGui()
    {
        bountySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                //Bounty rounded to nearest 100
                tvBounty.setText("$" + Integer.toString(((progress + 99) / 100 ) * 100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        btnButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkFields())
                {
                    int max, min;
                    min = Integer.parseInt(minSpinner.getSelectedItem().toString());
                    max = Integer.parseInt(maxSpinner.getSelectedItem().toString());

                    j.setTitle(etTitle.getText().toString());
                    j.setCompany(etCompany.getText().toString());
                    j.setCategory(spJobCat.getSelectedItem().toString());
                    j.setPayMin(min);
                    j.setPayMax(max);
                    j.setEmploymentType(employmentTypeSpinner.getSelectedItem().toString());
                    //round to nearest 100
                    j.setBounty(((bountySeekBar.getProgress() + 99) / 100 ) * 100);
                    j.setDescription(etDescription.getText().toString());
                    j.setLocation(tvLocation.getText().toString());

                    //store unique key
                    final String key = MainActivity.dbJobRef.push().getKey();
                    j.setKey(key);


                    //push job to database
                    MainActivity.dbJobRef.child(key).setValue(j);

                   //open the view job fragment passing it the key of this job
                    ((MainActivity)getActivity()).navViewJob(key);

                } else
                {
                    Toast.makeText(getContext(), "Field empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnLocation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new LocationTask().execute();
            }
        });


        btnPhotoPicker.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
    }


    private boolean checkFields()
    {
        if (etDescription.getText().toString().matches("") ||
                tvLocation.getText().toString().matches("") ||
                etTitle.getText().toString().matches("") ||
                etCompany.getText().toString().matches("") ||
                tvBounty.getText().toString().matches(""))
        {
            return false;
        }
        return true;

    }

    //deprecated for async task
    private void uploadImage()
    {
        btnButton.setVisibility(View.INVISIBLE);

        StorageReference photoRef = MainActivity.firebaseRootStorageRef.child(MainActivity.user.getId() + '/');

        UploadTask uploadTask = photoRef.child(selectedImageUri.getLastPathSegment()).putFile(selectedImageUri);
        // Upload file to Firebase Storage
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                Uri downloadUri = taskSnapshot.getDownloadUrl();
                j.setImageUrl(downloadUri.toString());
                j.setImageName(downloadUri.getLastPathSegment().toString());
                btnButton.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(getContext(), "Failed to upload Photo", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        new LocationTask().execute();

    }

    private void checkLocationPermission()
    {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    MY_PERMISSION_ACCESS_COARSE_LOCATION );
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    //Gets the location once the user accepts location permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_ACCESS_COARSE_LOCATION)
        {
            if (grantResults.length == 1 && grantResults[0] == MockPackageManager.PERMISSION_GRANTED )
            {
                new LocationTask().execute();
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == -1) {
            //selectedImageUri is the url for the image on the device
            selectedImageUri = data.getData();
            if(selectedImageUri != null)
               new ImageUploadTask().execute();
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onResume()
    {
        super.onResume();

    }

    @Override
    public void onStop()
    {
        super.onStop();
        googleApiClient.disconnect();
    }

    private class LocationTask extends AsyncTask<Void, Void, Void>{

        List<android.location.Address> addresses;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            checkLocationPermission();

            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (location != null) {
                try
                {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    Log.d(TAG, "onConnected: ");
                    Log.d(TAG, "City: " + addresses.get(0).getLocality());
                    Log.d(TAG, "Country: " + addresses.get(0).getCountryName());
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "No Location Detected: ");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            if (location != null)
                tvLocation.setText(addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName());
            else
                Toast.makeText(getContext(), "Unable to get location, check your network status", Toast.LENGTH_LONG).show();
        }
    }

    private class ImageUploadTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            btnButton.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            btnButton.setVisibility(View.VISIBLE);
            //Toast.makeText(getContext(), "Image uploaded!", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            StorageReference photoRef = MainActivity.firebaseRootStorageRef.child(MainActivity.user.getId() + '/');

            UploadTask uploadTask = photoRef.child(selectedImageUri.getLastPathSegment()).putFile(selectedImageUri);
            // Upload file to Firebase Storage
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    //String imageName  = .toString().split("/")[1];
                    j.setImageUrl(downloadUri.toString());
                    j.setImageName(downloadUri.getLastPathSegment().split("/")[1]);
                    Log.d(TAG, "onSuccess: " + downloadUri.toString() + "\n" + downloadUri.getLastPathSegment().split("/")[1]);
                    new ImageLoadTask(downloadUri.toString(), btnPhotoPicker).execute();
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Toast.makeText(getContext(), "Failed to upload Photo", Toast.LENGTH_SHORT).show();
                }
            });

            return null;
        }

    }



}
