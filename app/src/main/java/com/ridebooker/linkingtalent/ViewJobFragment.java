package com.ridebooker.linkingtalent;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnClickListener;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ridebooker.linkingtalent.datatypes.Job;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewJobFragment extends Fragment
{

    private TextView tvTitle, tvCompany, tvCategory, tvLocation, tvDescription, tvPay, tvBounty, tvEmploymentType, tvViewMore;
    private ImageView imgJob;
    private String key;
    private BottomNavigationView bottomNavView;
    private DatabaseReference ref = ((MainActivity)getActivity()).dbJobRef;
    private ValueEventListener valueEventListener;

    private Job viewedJob;

    public ViewJobFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        key = getArguments().getString("key");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_view_job, container, false);

        // Inflate the layout for this fragment
        tvTitle = (TextView) view.findViewById(R.id.view_job_title);
        //tvCompany = (TextView) view.findViewById(R.id.view_job_company_name);
        tvCategory = (TextView) view.findViewById(R.id.view_job_category);
        tvLocation = (TextView) view.findViewById(R.id.view_job_location);
        tvDescription = (TextView) view.findViewById(R.id.view_job_description);
        tvPay = (TextView) view.findViewById(R.id.view_job_pay);
        tvBounty = (TextView) view.findViewById(R.id.view_job_bounty_value);
        tvEmploymentType = (TextView) view.findViewById(R.id.view_job_employment_type);
        //imgJob = (ImageView) view.findViewById(R.id.view_job_image);
        bottomNavView = (BottomNavigationView) view.findViewById(R.id.view_job_bottom_navigation);
        tvViewMore = (TextView) view.findViewById(R.id.tv_view_job_view_more);

        setOnClickListeners();

        setupBottomNav(view);

        return view;
    }

    private void setOnClickListeners()
    {
        tvViewMore.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_job_info,null);

                if(MainActivity.popupWindow != null)
                    if(MainActivity.popupWindow.isShowing())
                        MainActivity.popupWindow.dismiss();

                MainActivity.popupWindow = new PopupWindow(
                        popupView,
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT
                );

                MainActivity.popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

                // Set an elevation value for popup window
                // Call requires API level 21
                if(Build.VERSION.SDK_INT>=21){
                    MainActivity.popupWindow.setElevation(5.0f);
                }

                // Get a reference for the popup view close button
                ImageButton closeButton = (ImageButton) popupView.findViewById(R.id.ib_close);

                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Dismiss the popup window
                        MainActivity.popupWindow.dismiss();
                    }
                });

                // Finally, show the popup window at the center location of root relative layout
                MainActivity.popupWindow.showAtLocation(v, Gravity.CENTER,0,0);
            }
        });

    }

    @Override
    public void onStart()
    {
        super.onStart();
        populateData(key);
    }


    private void setupBottomNav(final View view)
    {
        bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                switch (item.getItemId()){
                    case R.id.job_action_apply:
                        Intent applyIntent = new Intent();
                        applyIntent.setAction(Intent.ACTION_SEND);
                        applyIntent.setType("message/rfc822");
                        applyIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"mattf@ridebooker.com"});
                        applyIntent.putExtra(Intent.EXTRA_SUBJECT, tvTitle.getText().toString() + " application");
                        applyIntent.putExtra(Intent.EXTRA_TEXT, "Job application template goes here" );
                        startActivity(applyIntent);
                        break;
                    /* This has been moved to the bottom of the job description scroll view
                    case R.id.job_action_info:
                        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
                        View popupView = inflater.inflate(R.layout.popup_job_info,null);

                        if(MainActivity.popupWindow != null)
                            if(MainActivity.popupWindow.isShowing())
                                MainActivity.popupWindow.dismiss();

                        MainActivity.popupWindow = new PopupWindow(
                                popupView,
                                LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT
                        );

                        MainActivity.popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

                        // Set an elevation value for popup window
                        // Call requires API level 21
                        if(Build.VERSION.SDK_INT>=21){
                            MainActivity.popupWindow.setElevation(5.0f);
                        }

                        // Get a reference for the popup view close button
                        ImageButton closeButton = (ImageButton) popupView.findViewById(R.id.ib_close);

                        closeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Dismiss the popup window
                                MainActivity.popupWindow.dismiss();
                            }
                        });

                        // Finally, show the popup window at the center location of root relative layout
                        MainActivity.popupWindow.showAtLocation(view, Gravity.CENTER,0,0);

                        break;
                        */
                    case R.id.job_action_share:
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, tvTitle.getText().toString() + " position");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "I found a " + tvTitle.getText().toString() + " role that i thought you may be interested in "
                                + "https://d1btafwoxo8q34.cloudfront.net/view_job.html?j=" + viewedJob.getKey());
                        startActivity(Intent.createChooser(shareIntent, "Send to"));
                        break;
                }
                return false;
            }
        });
    }

    private void populateData(final String key)
    {
        valueEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                viewedJob = dataSnapshot.child(key).getValue(Job.class);
                tvTitle.setText(viewedJob.getTitle());
                //tvCompany.setText(viewedJob.getCompany());
                tvCategory.setText(viewedJob.getCategory());
                tvLocation.setText(viewedJob.getLocation());
                tvBounty.setText("Reward" + ": " + "$" + Integer.toString(viewedJob.getBounty()));
                tvPay.setText("Pay" + ": " + "$" + Integer.toString(viewedJob.getPayMin()) + " - $" + Integer.toString(viewedJob.getPayMax()));
                tvDescription.setText(viewedJob.getDescription());
                tvEmploymentType.setText("Employment Type: " + viewedJob.getEmploymentType());

                StorageReference imageRef =  FirebaseStorage.getInstance().getReference().child(viewedJob.getId());
                //check if there is an image with this job
                //no longer getting image
                /*
                if(viewedJob.getImageUrl() != null)
                {
                    Log.d("ViewJobFragment", "onDataChange: getting image => " + viewedJob.getImageName());
                    Glide.with(imgJob.getContext())
                            .using(new FirebaseImageLoader())
                            .load(imageRef.child(viewedJob.getImageName()))
                            .into(imgJob);
                }*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        };
        ref.addValueEventListener(valueEventListener);
    }




}
