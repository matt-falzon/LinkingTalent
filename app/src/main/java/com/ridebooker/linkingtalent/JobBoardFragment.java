package com.ridebooker.linkingtalent;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ridebooker.linkingtalent.Adapters.JobAdapter;
import com.ridebooker.linkingtalent.datatypes.Job;

import java.util.ArrayList;

public class JobBoardFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener, JobAdapter.ListItemClickListener
{
    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;

    public JobBoardFragment(){};

    private ArrayList<Job> jobs = new ArrayList();
    private DatabaseReference ref = ((MainActivity)getActivity()).dbJobRef;
    private ValueEventListener valueEventListener;

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
        ref.removeEventListener(valueEventListener);
    }

    private void getJobs()
    {
        valueEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                //ArrayList<Job> newJobs = new ArrayList<>();
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child: children)
                {
                    Job j = child.getValue(Job.class);
                    Log.d("Child", j.getTitle());
                    jobs.add(j);
                    //addJob(j);
                }

                //addJobs(newJobs);
                jobAdapter.notifyItemInserted(jobs.size() - 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        };
        ref.addValueEventListener(valueEventListener);
    }

    private void setGui(View view)
    {
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Create Job", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        return false;
    }

    @Override
    public void onListItemClick(int clickedItemIndex, String jobKey)
    {
        String message = jobKey;

        //Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

        //use key to open job fragment
        //getView().setVisibility(View.GONE);
        ((MainActivity)getActivity()).viewJob(jobKey);

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
