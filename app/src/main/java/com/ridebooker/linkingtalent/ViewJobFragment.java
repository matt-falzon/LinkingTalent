package com.ridebooker.linkingtalent;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ridebooker.linkingtalent.datatypes.Job;

import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewJobFragment extends Fragment
{

    private TextView tvTitle, tvCompany, tvCategory, tvLocation, tvDescription, tvPay, tvBounty, tvEmploymentType;
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
        tvCompany = (TextView) view.findViewById(R.id.view_job_company_name);
        tvCategory = (TextView) view.findViewById(R.id.view_job_category);
        tvLocation = (TextView) view.findViewById(R.id.view_job_location);
        tvDescription = (TextView) view.findViewById(R.id.view_job_description);
        tvPay = (TextView) view.findViewById(R.id.view_job_pay);
        tvBounty = (TextView) view.findViewById(R.id.view_job_bounty_value);
        tvEmploymentType = (TextView) view.findViewById(R.id.view_job_employment_type);
        imgJob = (ImageView) view.findViewById(R.id.view_job_image);
        bottomNavView = (BottomNavigationView) view.findViewById(R.id.view_job_bottom_navigation);

        setupBottomNav();

        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        populateData(key);
    }

    private void setupBottomNav()
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
                        applyIntent.putExtra(Intent.EXTRA_TEXT, "I wanna apply for this job" );
                        startActivity(applyIntent);
                        break;
                    case R.id.job_action_send:
                        String contentBody = "Share content";
                        Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sendIntent.setType("text/plain");
                        sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                        sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, contentBody);
                        startActivity(Intent.createChooser(sendIntent, "Send"));
                        break;
                    case R.id.job_action_share:
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, tvTitle.getText().toString() + " application");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "I wanna apply for this job" );
                        startActivity(shareIntent);
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
                tvCompany.setText(viewedJob.getCompany());
                tvCategory.setText(viewedJob.getFirstCategory() + ", " + viewedJob.getSecondCategory());
                tvLocation.setText(viewedJob.getLocation());
                tvBounty.setText("Bounty" + ": " + "$" + Integer.toString(viewedJob.getBounty()));
                tvPay.setText("Pay" + ": " + "$" + Integer.toString(viewedJob.getPayMax()) + " - $" + Integer.toString(viewedJob.getPayMin()));
                tvDescription.setText(viewedJob.getDescription());
                tvEmploymentType.setText("Employment Type: " + viewedJob.getEmploymentType());

                //check if there is an image with this job
                if(viewedJob.getImageUrl() != null)
                {
                    Glide.with(imgJob.getContext())
                            .using(new FirebaseImageLoader())
                            .load(MainActivity.firebaseRootStorageRef.child(viewedJob.getImageUrl()))
                            .into(imgJob);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        };
        ref.addValueEventListener(valueEventListener);
    }

}
