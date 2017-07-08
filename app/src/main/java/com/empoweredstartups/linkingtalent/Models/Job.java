package com.empoweredstartups.linkingtalent.Models;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by mattf on 13/03/2017.
 */

public class Job
{
    private String id;
    private String key;
    private String title;
    private String company;
    private String category;
    private int payMin;
    private int payMax;
    private int bounty;
    private String location;
    private String description;
    private String postDate;
    private String imageUrl;
    private String imageName;
    private String employmentType;


    public Job(String id, String title, String company)
    {
        this.id = id;
        this.title = title;
        this.company = company;
        this.postDate = getPostDate();
    }

    public Job(String id)
    {
        this.id = id;
    }

    public Job(){}

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {

        this.imageUrl = imageUrl;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setCompany(String company)
    {
        this.company = company;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {

        this.key = key;
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

    public String getCategory()
    {
        return category;
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

    public void setCategory(String category)
    {
        this.category = category;
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

    public String getId()
    {
        return id;
    }

    public String getEmploymentType()
    {
        return employmentType;
    }

    public void setEmploymentType(String employmentType)
    {
        this.employmentType = employmentType;
    }

    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }

    public String getImageName()
    {

        return imageName;
    }
}
