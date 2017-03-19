package com.ridebooker.linkingtalent.datatypes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by mattf on 13/03/2017.
 */

public class Job
{
    private String title;
    private String company;
    private String firstCategory;
    private String secondCategory;
    private int payMin;
    private int payMax;
    private int bounty;
    private String location;
    private String description;
    private String postDate;

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setCompany(String company)
    {
        this.company = company;
    }

    public Job(String title, String company)
    {
        this.title = title;
        this.company = company;
        this.postDate = getPostDate();
    };


    public Job()
    {
        this.title = "null title";
        this.company = "null company";
        this.firstCategory = "null cat 1";
        this.secondCategory = "null cat 2";
        this.location = "null location";
        this.description = "null job description";
        this.postDate = getTime();
        this.payMax = 0;
        this.payMin = 0;
        this.bounty = 0;
    }

    private String getTime()
    {
        String currentDT = DateFormat.getDateTimeInstance().format(new Date());

         return currentDT;
    }

    public String getTitle()
    {
        return title;
    }

    public String getCompany()
    {
        return company;
    }

    public String getFirstCategory()
    {
        return firstCategory;
    }

    public String getSecondCategory()
    {
        return secondCategory;
    }

    public int getPayMin()
    {
        return payMin;
    }

    public int getPayMax()
    {
        return payMax;
    }

    public int getBounty()
    {
        return bounty;
    }

    public String getLocation()
    {
        return location;
    }

    public String getDescription()
    {
        return description;
    }

    public String getPostDate()
    {
        return postDate;
    }

    public void setFirstCategory(String firstCategory)
    {
        this.firstCategory = firstCategory;
    }

    public void setSecondCategory(String secondCategory)
    {
        this.secondCategory = secondCategory;
    }

    public void setPayMin(int payMin)
    {
        this.payMin = payMin;
    }

    public void setPayMax(int payMax)
    {
        this.payMax = payMax;
    }

    public void setBounty(int bounty)
    {
        this.bounty = bounty;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
