package com.empoweredstartups.linkingtalent.Adapters;

/**
 * Created by mattf on 13/03/2017.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.StorageReference;
import com.empoweredstartups.linkingtalent.MainActivity;
import com.empoweredstartups.linkingtalent.R;
import com.empoweredstartups.linkingtalent.Models.Job;

import java.util.ArrayList;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder>
{

    private static final String TAG = JobAdapter.class.getSimpleName();
    private Context context;
    private int numberOfItems;
    private static int viewHolderCount;
    private ArrayList<Job> jobs;
    final private ListItemClickListener mOnClickListener;
    //final private ListItemClickListener mOnClickListener;

    private LayoutInflater inflator;

    //This constructor will need to take in the DB data for jobs
    public JobAdapter(Context context, ArrayList<Job> jobs, ListItemClickListener listener)
    {
        mOnClickListener = listener;
        this.jobs = jobs;
        this.context = context;
    }

    public void add(Job j)
    {
        jobs.add(j);
    }

    public int getLength()
    {
        return jobs.size();
    }

    @Override
    public JobViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        inflator = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflator.inflate(R.layout.job_row, parent, shouldAttachToParentImmediately);

        return  new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(JobViewHolder holder, int position)
    {
        String jobTitle = jobs.get(position).getTitle();

        if(jobTitle.length() >= 17)
            jobTitle = jobTitle.substring(0, 16) + "...";

        holder.tvJobTitle.setText(jobTitle);
        //holder.tvJobCompany.setText(jobs.get(position).getCompany());
        String bounty = Integer.toString(jobs.get(position).getBounty());
        holder.tvJobBounty.setText("$" + bounty);
        holder.tvJobLocation.setText(jobs.get(position).getLocation());
        StorageReference photoRef = MainActivity.firebaseRootStorageRef.child(jobs.get(position).getId());
        /*
            if(jobs.get(position).getImageName() != null)
            {
                //Log.d("ViewJobFragment", "onDataChange: getting image => " + jobs.get(position).getImageName());
                Glide.with(holder.ivJobImage.getContext())
                        .using(new FirebaseImageLoader())
                        .load(photoRef.child(jobs.get(position).getImageName()))
                        .into(holder.ivJobImage);
            }
        */
        holder.key = jobs.get(position).getKey();
        //holder.ivJobImage.setImageResource(icon id here);
    }

    @Override
    public int getItemCount()
    {
        return jobs.size();
    }

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex, String id);
    }


    /* *************************************
     *          ViewHolder Class           *
     ***************************************/
    public class JobViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener
    {
        TextView tvJobTitle, tvJobCompany, tvJobBounty, tvJobLocation;
        ImageView ivJobImage;
        String key;

        public JobViewHolder(View itemView) {
            super(itemView);

            tvJobTitle = (TextView) itemView.findViewById(R.id.tv_job_title);
            //tvJobCompany = (TextView) itemView.findViewById(R.id.tv_job_company);
            //ivJobImage = (ImageView) itemView.findViewById(R.id.iv_job_image);
            tvJobLocation = (TextView) itemView.findViewById(R.id.tv_job_location);
            tvJobBounty = (TextView) itemView.findViewById(R.id.tv_job_bounty);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            int clickedPos = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPos, key);
        }
    }
}
