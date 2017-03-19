package com.ridebooker.linkingtalent;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ridebooker.linkingtalent.Adapters.JobAdapter;
import com.ridebooker.linkingtalent.datatypes.Job;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JobFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener
{
    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;

    private String[] companyNames = {"Sony", "Burton", "Apple", "Powerade", "Ridebooker", "Empowered Startups"};
    private String[] jobTitles = {"Chef", "Developer", "Photographer", "Painter", "Doctor", "Server"};

    public JobFragment(){};

    private ArrayList<Job> jobs = new ArrayList();
    private DatabaseReference ref = ((MainActivity)getActivity()).dbJobRef;
    private ChildEventListener childEventListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment

        View jobView = inflater.inflate(R.layout.fragment_job, container, false);
        recyclerView = (RecyclerView) jobView.findViewById(R.id.rv_job_view);

        getJobs();

        jobAdapter = new JobAdapter(getActivity(), jobs);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setAdapter(jobAdapter);

        setGui(jobView);

        return jobView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        ref.removeEventListener(childEventListener);
    }

    private ArrayList<Job> fillDummyData()
    {

        Random rand = new Random();
        for(int i = 0; i < 100; i++)
        {
            Job newJob = new Job(jobTitles[rand.nextInt(5)], companyNames[rand.nextInt(5)]);
            jobs.add(newJob);
        }

        return jobs;
    }

    private void getJobs()
    {
        childEventListener = new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Job job = dataSnapshot.getValue(Job.class);
                addJob(job);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s)
            {

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        };
        ref.addChildEventListener(childEventListener);
    }


    // Adds a new job to the recycler view
    public void addJob(Job newJob)
    {
        jobs.add(newJob);
        jobAdapter.notifyItemInserted(jobs.size() - 1);
    }

    public ArrayList<Job> saveJobData()
    {
        Job newJob = new Job("JobTitle", "Company Name");
        ref.push().setValue(newJob);
        return null;
    }

    private void setGui(View view)
    {
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        /*
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        return false;
    }

    /*
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }
    */
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    /*
    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener
    {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/

}
