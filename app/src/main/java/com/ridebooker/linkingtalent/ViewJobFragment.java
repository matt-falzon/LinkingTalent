package com.ridebooker.linkingtalent;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

    private TextView tvTitle, tvCompany, tvFirstCat, tvSecondCat, tvLocation, tvDescription, tvPayMin, tvPayMax, tvBounty;
    private String key;
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
        tvFirstCat = (TextView) view.findViewById(R.id.view_job_first_cat);
        tvSecondCat = (TextView) view.findViewById(R.id.view_job_second_cat);
        tvLocation = (TextView) view.findViewById(R.id.view_job_location);
        tvDescription = (TextView) view.findViewById(R.id.view_job_description);
        tvPayMin = (TextView) view.findViewById(R.id.view_job_min);
        tvPayMax = (TextView) view.findViewById(R.id.view_job_max);
        tvBounty = (TextView) view.findViewById(R.id.view_job_bounty_value);

        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        populateData(key);
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
                tvFirstCat.setText(viewedJob.getFirstCategory());
                tvSecondCat.setText(viewedJob.getSecondCategory());
                tvLocation.setText(viewedJob.getLocation());
                tvBounty.setText("$" + Integer.toString(viewedJob.getBounty()));
                tvPayMax.setText("$" + Integer.toString(viewedJob.getPayMax()));
                tvPayMin.setText("$" + Integer.toString(viewedJob.getPayMin()));
                tvDescription.setText(viewedJob.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        };
        ref.addValueEventListener(valueEventListener);
    }

}
