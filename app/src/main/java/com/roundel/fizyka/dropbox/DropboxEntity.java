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
import java.util.zip.ZipEntry;

public class DropboxEntity implements Serializable
{
    public static int TYPE_FILE = 0;
    public static int TYPE_FOLDER = 1;

    public static int BIG_NOTIFICATION_MAX_LINES = 7;

    private int type;
    private String path;
    private Date date;
    private String mimeType;
    private long size;

    //Constructors
    public DropboxEntity(int type, @NonNull String path, long size, @Nullable String mimeType, @NonNull Date date)
    {
        this.setType(type);
        this.path = path;
        this.date = date;
        this.mimeType = mimeType;
        this.size = size;
    }
    public DropboxEntity(int type, @NonNull String path, @NonNull Date date)
    {
        this(type, path, 0, null, date);
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
        return result;
    }

    public static String formatSize(long longSize, Context context)
    {
        final double SIZE_KILO = 1024;
        final double SIZE_MEGA = SIZE_KILO*SIZE_KILO;
        final double SIZE_GIGA = SIZE_MEGA*SIZE_KILO;
        final double SIZE_TERA = SIZE_GIGA*SIZE_KILO;
        final double SIZE_PETA = SIZE_TERA*SIZE_KILO;
        final double SIZE_EXA = SIZE_MEGA*SIZE_KILO;
        final double size = (double) longSize;
        if(size < SIZE_KILO)
            if(size == 1)
                return String.format(Locale.getDefault(),
                        context.getString(R.string.file_byte),
                        size);
            else
                return String.format(Locale.getDefault(),
                        context.getString(R.string.file_byte_plural),
                        size);
        else if(size < SIZE_MEGA)
            return String.format(Locale.getDefault(),
                    context.getString(R.string.file_kilobyte),
                    size/SIZE_KILO);
        else if(size < SIZE_GIGA)
            return String.format(Locale.getDefault(),
                    context.getString(R.string.file_megabyte),
                    size/SIZE_MEGA);
        else if(size < SIZE_TERA)
            return String.format(Locale.getDefault(),
                    context.getString(R.string.file_gigabyte),
                    size/SIZE_GIGA);
        else if(size < SIZE_PETA)
            return String.format(Locale.getDefault(),
                    context.getString(R.string.file_terabyte),
                    size/SIZE_TERA);
        else if(size < SIZE_EXA)
            return String.format(Locale.getDefault(),
                    context.getString(R.string.file_petabyte),
                    size/SIZE_PETA);
        else
            return String.format(Locale.getDefault(),
                    context.getString(R.string.file_exabyte),
                    size/SIZE_EXA);
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
    public long getSize()
    {
        return this.size;
    }
    public String getMimeType()
    {
        return mimeType;
    }

    public String getName()
    {
        String[] arr = this.path.split("/");
        return Objects.equals(this.path, "/") ?null:arr[arr.length-1];
    }

    public String getParentDirectory()
    {
        if(this.getName() == null) return null;
        return this.path.substring(0, this.path.length()-this.getName().length());
    }

    @Nullable
    public static String getNameFromString(String path)
    {
        String[] arr = path.split("/");
        return Objects.equals(path, "/") ?null:arr[arr.length-1];
    }

    @Nullable
    public static String getParentDirectoryFromString(String path)
    {
        if(DropboxEntity.getNameFromString(path) == null) return null;
        int add = path.charAt(path.length()-1) == '/'?-1:0;
        return path.substring(0, path.length()-DropboxEntity.getNameFromString(path).length()+add);
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
    public void setSize(long size)
    {
        this.size = size;
    }
    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }
}