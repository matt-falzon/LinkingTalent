package com.ridebooker.linkingtalent.datatypes;

import java.net.URL;

/**
 * Created by mattf on 13/03/2017.
 */

public class TalentChamp
{
    private String id;
    private String name;
    private String email;
    private String location;
    private boolean isTalentChamp;
    private URL photo;

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

    public URL getPhoto()
    {
        return photo;
    }

    public TalentChamp(String id, String name, String email, String location, boolean isTalentChamp)
    {
        this.id = id;
        this.name = name;
        this.email = email;
        this.location = location;
        this.isTalentChamp = isTalentChamp;
    };
}
