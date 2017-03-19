package com.ridebooker.linkingtalent;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ridebooker.linkingtalent.datatypes.Job;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment
{
Button dbButton;

    public HomeFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View homeView = inflater.inflate(R.layout.fragment_home, container, false);
        dbButton = (Button) homeView.findViewById(R.id.db_button);
        dbButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Job newJob = new Job("Job Title", "Company name");
                Job blankJob = new Job();
                MainActivity.dbJobRef.push().setValue(blankJob);
                MainActivity.dbJobRef.push().setValue(newJob);
            }
        });
        // Inflate the layout for this fragment
        return homeView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }
}
