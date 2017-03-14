package com.ridebooker.linkingtalent.datatypes;

import java.util.Date;

/**
 * Created by mattf on 13/03/2017.
 */

public class Job
{
    String title;
    String company;
    String firstCategory;
    String secondCategory;
    int payMin;
    int payMax;
    int bounty;
    String location;
    String description;
    Date postDate;

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

    };

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

    public Date getPostDate()
    {
        return postDate;
    }


}
