package com.rd.veuisdk.model;

/**
 * 配乐历史
 * 
 * @author JIAN
 * 
 */
public class MusicHistory implements IMusic {

    private String path, name;
    private int duration;
    private long id;

    @Override
    public void setPath(String path) {
	this.path = path;
    }

    @Override
    public String getPath() {
	return path;
    }

    @Override
    public void setName(String name) {
	this.name = name;
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public void setDuration(int duration) {
	this.duration = duration;
    }

    @Override
    public int getDuration() {
	return duration;
    }

    @Override
    public void setId(long id) {
	this.id = id;
    }

    @Override
    public long getId() {
	return id;
    }

    @Override
    public String toString() {
	return "MusicHistory [path=" + path + ", name=" + name + ", duration="
		+ duration + ", id=" + id + "]";
    }

}
