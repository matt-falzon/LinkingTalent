package com.empoweredstartups.linkingtalent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.empoweredstartups.linkingtalent.Models.Job;

/**
 * Created by matt on 2017-06-06.
 */

public class JobInfoFragment extends Fragment {

    private String key;
    private ValueEventListener valueEventListener;
    private Job viewedJob;
    private TextView tvJobInfoTitle, tvJobInfoContent, tvJobInfoRequirements;
    private DatabaseReference ref = ((MainActivity) getActivity()).dbJobRef;
    private BottomNavigationView bottomNavView;
    private static final String LINKING_TALENT_JOB_URL = "https://d1btafwoxo8q34.cloudfront.net/view_job.html?j=";

    public JobInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        key = getArguments().getString("key");

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_job_info, container, false);

        tvJobInfoTitle = (TextView) view.findViewById(R.id.job_info_title);
        tvJobInfoContent = (TextView) view.findViewById(R.id.job_info_content);
        bottomNavView = (BottomNavigationView) view.findViewById(R.id.view_job_bottom_navigation);
        //tvJobInfoRequirements = (TextView) view.findViewById(R.id.job_info_requirements);
        String filler = "Lorem ipsum dolar sit amet, consectetur adipiscing elit. Pellentesque aliquet dapibus tortor id vulputate. In hac habitasse platea dictumist. Etiam at tellas bibendum, pharetra felis nec, lacinia leo \n" +
                "Lorem ipsum dolar sit amet, consectetur adipiscing elit. Pellentesque aliquet dapibus tortor id vulputate. In hac habitasse platea dictumist. Etiam at tellas bibendum, pharetra felis nec, lacinia leo ";

        tvJobInfoContent.setText(filler);
        //tvJobInfoRequirements.setText(filler);
        setupBottomNav(view);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        populateData(key);
    }

    private void populateData(final String key) {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewedJob = dataSnapshot.child(key).getValue(Job.class);
                tvJobInfoTitle.setText(viewedJob.getTitle());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref.addValueEventListener(valueEventListener);
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
                        applyIntent.setType("text/plain");
                        applyIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.pref_email)});
                        applyIntent.putExtra(Intent.EXTRA_SUBJECT, tvJobInfoTitle.getText().toString() + " application");
                        applyIntent.putExtra(Intent.EXTRA_TEXT, "Job application template goes here" );
                        startActivity(applyIntent);
                        break;
                    case R.id.job_action_share:
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, tvJobInfoTitle.getText().toString() + " position");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "I found a " + tvJobInfoTitle.getText().toString() + " role that i thought you may be interested in "
                                + LINKING_TALENT_JOB_URL + viewedJob.getKey());
                        startActivity(Intent.createChooser(shareIntent, "Send to"));
                        break;
                }
                return false;
            }
        });
    }
}
