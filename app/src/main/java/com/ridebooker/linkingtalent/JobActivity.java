package com.ridebooker.linkingtalent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ridebooker.linkingtalent.datatypes.Job;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JobActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private JobAdapter jobAdapter;

    private String[] companyNames = {"Sony", "Burton", "Apple", "Powerade", "Ridebooker", "Empowered Startups"};
    private String[] jobTitles = {"Chef", "Developer", "Photographer", "Painter", "Doctor", "Server"};



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job);

        recyclerView = (RecyclerView) findViewById(R.id.rv_job_view);
        jobAdapter = new JobAdapter(this, fillDummyData());
        recyclerView.setAdapter(jobAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private ArrayList<Job> fillDummyData()
    {
        ArrayList<Job> jobs = new ArrayList();

        Random rand = new Random();
        for(int i = 0; i < 15; i++)
        {
            Job newJob = new Job(jobTitles[rand.nextInt(5)], companyNames[rand.nextInt(5)]);
            jobs.add(newJob);
        }

        return jobs;
    }
}
