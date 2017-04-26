package com.ridebooker.linkingtalent.datatypes;

import android.net.Uri;

import java.net.URL;

/**
 * Created by mattf on 13/03/2017.
 */

public class TalentChamp
{
    private String id;
    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private String location;
    private boolean isTalentChamp;
    private String photo;
    private Uri photoUri;

    public void setPhotoUri(Uri photoUri)
    {
        this.photoUri = photoUri;
    }

    public Uri getPhotoUri()
    {

        return photoUri;
    }

    public String getName()
    {
        return name;
    }

    public String getEmail()
    {
        return email;
    }

    public String getLocation()
    {
        return location;
    }

    public String getId()
    {
        return id;
    }

    public String getPhoto()
    {
        return photo;
    }

    public void setPhoto(String photo)
    {
        this.photo = photo;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public TalentChamp(String id, String name, String email, String location, boolean isTalentChamp)
    {
        this.id = id;
        this.name = name;
        this.email = email;
        this.location = location;
        this.isTalentChamp = isTalentChamp;
        String[] names = name.split(" ");
        this.firstName = names[0];
        this.lastName = names[1];

    };

    public TalentChamp(){};
}
