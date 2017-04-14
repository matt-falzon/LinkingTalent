package com.ridebooker.linkingtalent.Adapters;

/**
 * Created by mattf on 13/03/2017.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;
import com.ridebooker.linkingtalent.MainActivity;
import com.ridebooker.linkingtalent.R;
import com.ridebooker.linkingtalent.datatypes.Job;

import java.util.ArrayList;
import java.util.Random;

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
        int layoutIdForListItem = R.layout.job_row;
        inflator = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflator.inflate(R.layout.job_row, parent, shouldAttachToParentImmediately);

        JobViewHolder holder = new JobViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(JobViewHolder holder, int position)
    {
            holder.tvJobTitle.setText(jobs.get(position).getTitle());
            holder.tvJobCompany.setText(jobs.get(position).getCompany());
            StorageReference photoRef = MainActivity.firebaseRootStorageRef.child(jobs.get(position).getId() + '/');
            if(jobs.get(position).getImageName() != null)
            {
                Glide.with(holder.ivJobImage.getContext())
                        .using(new FirebaseImageLoader())
                        .load(MainActivity.firebaseRootStorageRef.child(jobs.get(position).getImageName()))
                        .into(holder.ivJobImage);
            }

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
        TextView tvJobTitle, tvJobCompany;
        ImageView ivJobImage;
        String key;

        public JobViewHolder(View itemView) {
            super(itemView);

            tvJobTitle = (TextView) itemView.findViewById(R.id.tv_job_title);
            tvJobCompany = (TextView) itemView.findViewById(R.id.tv_job_company);
            ivJobImage = (ImageView) itemView.findViewById(R.id.iv_job_image);

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
