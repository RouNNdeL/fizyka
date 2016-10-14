package com.roundel.fizyka.dropbox;

import java.io.Serializable;

/**
 * Created by RouNdeL on 2016-10-14.
 */
import java.util.*;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.*;

import com.roundel.fizyka.R;

import java.io.*;

public class DropboxEntity implements Serializable
{
    public static int TYPE_FILE = 0;
    public static int TYPE_FOLDER = 1;

    public static int BIG_NOTIFICATION_MAX_LINES = 7;

    private int type;
    private String path;
    private Date date;
    private String mimeType;

    //Constructors
    public DropboxEntity(int type, @NonNull String path, @Nullable String mimeType, @NonNull Date date)
    {
        this.setType(type);
        this.path = path;
        this.date = date;
        this.mimeType = mimeType;
    }
    public DropboxEntity(int type, @NonNull String path, @NonNull Date date)
    {
        this(type, path, null, date);
    }
    public DropboxEntity(int type, @NonNull String path)
    {
        this(type, path, new Date());
    }

    public static List<String> getPathsList(List<DropboxEntity> entities)
    {
        List<String> result = new ArrayList<String>();
        for (DropboxEntity entity : entities)
        {
            result.add(entity.getPath());
        }
        return result;
    }
    public static List<Date> getDatesList(List<DropboxEntity> entities)
    {
        List<Date> result = new ArrayList<Date>();
        for (DropboxEntity entity : entities)
        {
            result.add(entity.getDate());
        }
        return result;
    }
    public static List<String> getMimeTypesList(List<DropboxEntity> entities)
    {
        List<String> result = new ArrayList<String>();
        for (DropboxEntity entity : entities)
        {
            result.add(entity.getMimeType());
        }
        return result;
    }
    public static List<Integer> getTypesList(List<DropboxEntity> entities)
    {
        List<Integer> result = new ArrayList<Integer>();
        for (DropboxEntity entity : entities)
        {
            result.add(entity.getType());
        }
        return result;
    }

    @Nullable
    public static DropboxEntity findByPath(String path, List<DropboxEntity> entities)
    {
        for(DropboxEntity entity : entities)
        {
            if(Objects.equals(entity.getPath(), path)) return entity;
        }
        return null;
    }
    @Nullable
    public static DropboxEntity findByDate(Date date, List<DropboxEntity> entities)
    {
        for(DropboxEntity entity : entities)
        {
            if(Objects.equals(entity.getDate(), date)) return entity;
        }
        return null;
    }

    public static List<DropboxEntity> getNewEntities(List<DropboxEntity> entitiesOld, List<DropboxEntity> entitiesNew)
    {
        List<DropboxEntity> result = new ArrayList<DropboxEntity>();
        List<String> pathsNew = getPathsList(entitiesNew);
        List<String> pathsOld = getPathsList(entitiesOld);
        for (String path : pathsNew)
        {
            if(! pathsOld.contains(path)) result.add(findByPath(path, entitiesNew));
        }
        return result;
    }

    public String toString()
    {
        return "type: "+Integer.toString(this.getType())+" path: "+this.getPath()+" mime: "+this.getMimeType();
    }

    public static String listToString(List<DropboxEntity> entities)
    {
        String result  = "";
        for(DropboxEntity entity : entities)
        {
            result+=entity.toString()+"\n";
        }
        return result;
    }

    public static String getChangelog(List<DropboxEntity> entities, Context context)
    {
        String result = "";
        for(DropboxEntity entity : entities)
        {
            if(entity.getType() == DropboxEntity.TYPE_FILE)
            {
                String[] parts = entity.getPath().split("/");
                if(Objects.equals(parts[parts.length-2], ""))result+="• "+
                        parts[parts.length-1]+
                        " "+context.getString(R.string.notify_changelog_in)+
                        " "+context.getString(R.string.notify_changelog_root)+"\n";
                else result+="• "+
                        parts[parts.length-1]+
                        " "+context.getString(R.string.notify_changelog_in)+
                        " "+parts[parts.length-2]+"\n";
            }
        }
        String[] lines = result.split("\r\n|\r|\n");
        Log.d("Lines", Integer.toString(lines.length));
        if(lines.length > DropboxEntity.BIG_NOTIFICATION_MAX_LINES+1)
        {
            result = "";
            for (int i = 0; i < DropboxEntity.BIG_NOTIFICATION_MAX_LINES; i++)
            {
                result+=lines[i]+"\n";
            }
            result+="... "+String.format(context.getString(R.string.notify_changelog_more), lines.length-DropboxEntity.BIG_NOTIFICATION_MAX_LINES);
        }
        return result;
    }

    //Getters
    public int getType()
    {
        return this.type;
    }
    public String getPath()
    {
        return this.path;
    }
    public Date getDate()
    {
        return this.date;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    //Setters
    public void setType(int type)
    {
        if (Objects.equals(type, TYPE_FILE) || Objects.equals(type, TYPE_FOLDER))
        {
            this.type = type;
        } else
        {
            throw new IllegalArgumentException("Type must be either TYPE_FOLDER or TYPE_FILE");
        }
    }
    public void setPath(String path)
    {
        this.path = path;
    }
    public void setDate(Date date)
    {
        this.date = date;
    }

    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }
}