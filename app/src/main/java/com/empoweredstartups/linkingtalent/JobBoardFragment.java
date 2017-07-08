package com.empoweredstartups.linkingtalent;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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
import com.empoweredstartups.linkingtalent.Adapters.JobAdapter;
import com.empoweredstartups.linkingtalent.Models.Job;

import java.util.ArrayList;

public class JobBoardFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener, JobAdapter.ListItemClickListener
{
    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;

    public JobBoardFragment(){};

    private ArrayList<Job> jobs = new ArrayList();
    private DatabaseReference ref = ((MainActivity)getActivity()).dbJobRef;
    private ChildEventListener childEventListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View jobView = inflater.inflate(R.layout.fragment_job_board, container, false);
        recyclerView = (RecyclerView) jobView.findViewById(R.id.rv_job_view);

        getJobs();
        //create new adapter, this = click listener
        jobAdapter = new JobAdapter(getActivity(), jobs, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setAdapter(jobAdapter);

        //no longer using floating action button
        //setGui(jobView);

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

    private void getJobs()
    {
        childEventListener = new ChildEventListener()
        {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Job job = dataSnapshot.getValue(Job.class);
                jobAdapter.add(job);
                jobAdapter.notifyItemInserted(jobAdapter.getLength());
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

    /*
    private void setGui(View view)
    {
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ((MainActivity)getActivity()).navCreateJob();
            }
        });
    }*/

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        return false;
    }

    @Override
    public void onListItemClick(int clickedItemIndex, String jobKey)
    {
        //stops list items being selected from behind view job fragment
        if(((MainActivity)getActivity()).checkSameFragment("viewJob"))
            return;
        //use key to open job fragment
        ((MainActivity)getActivity()).navViewJob(jobKey);

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