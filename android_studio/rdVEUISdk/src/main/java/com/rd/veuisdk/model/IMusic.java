package com.rd.veuisdk.model;

/**
 * 音乐(historymusic ,)
 * 
 * @author JIAN
 * 
 */
public interface IMusic {

    public abstract void setPath(String path);

    public abstract String getPath();

    public abstract void setName(String name);

    public abstract String getName();

    public abstract void setDuration(int duration);

    public abstract int getDuration();

    public abstract void setId(long id);

    public abstract long getId();

}
