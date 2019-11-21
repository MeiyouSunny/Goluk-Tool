package com.rd.veuisdk.model;

/**
 * 音乐(historymusic ,)
 *
 * @author JIAN
 */
public interface IMusic {

    void setPath(String path);

    String getPath();

    void setName(String name);

    String getName();

    void setDuration(int duration);

    int getDuration();

    void setId(long id);

    long getId();

}
