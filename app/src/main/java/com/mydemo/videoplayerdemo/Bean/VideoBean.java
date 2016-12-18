package com.mydemo.videoplayerdemo.Bean;

import android.database.Cursor;
import android.provider.MediaStore;

import java.io.Serializable;

/**
 * Created by chenlong on 2016/12/16.
 */

public class VideoBean implements Serializable
{
    private String name;
    private int id;
    private long size;
    private long duration;
    private String path;

    public VideoBean()
    {

    }

    public void bindBean(Cursor cursor)
    {
        this.name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME));
        this.id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID));
        this.size = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE));
        this.duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION));
        this.path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }

    public long getDuration()
    {
        return duration;
    }

    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    @Override
    public String toString()
    {
        return "VideoBean{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", size=" + size +
                ", duration=" + duration +
                ", path='" + path + '\'' +
                '}';
    }
}
