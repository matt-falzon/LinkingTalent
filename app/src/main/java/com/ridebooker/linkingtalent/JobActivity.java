package com.ridebooker.linkingtalent;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.ridebooker.linkingtalent.Adapters.JobAdapter;
import com.ridebooker.linkingtalent.datatypes.Job;

import java.util.ArrayList;
import java.util.Random;

public class JobActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
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

        setGui();
    }

    private ArrayList<Job> fillDummyData()
    {
        ArrayList<Job> jobs = new ArrayList();

        Random rand = new Random();
        for(int i = 0; i < 100; i++)
        {
            Job newJob = new Job(jobTitles[rand.nextInt(5)], companyNames[rand.nextInt(5)]);
            jobs.add(newJob);
        }

        return jobs;
    }

    private void setGui()
    {
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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
}
