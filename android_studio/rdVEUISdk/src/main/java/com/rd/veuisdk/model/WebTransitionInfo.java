package com.rd.veuisdk.model;

import java.io.Serializable;


public class WebTransitionInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private long id;

    public long getId() {
	return id;
    }

    public void setId(long id) {
	this.id = id;
    }

    public static long getSerialversionuid() {
	return serialVersionUID;
    }

    public String getTransitionName() {
        return transitionName;
    }

    public void setTransitionName(String transitionName) {
        this.transitionName = transitionName;
    }

    public String getTransitionUrl() {
        return transitionUrl;
    }

    public void setTransitionUrl(String transitionUrl) {
        this.transitionUrl = transitionUrl;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
    
    public String getPngPath() {
        return pngPath;
    }

    public void setPngPath(String pngPath) {
        this.pngPath = pngPath;
    }
    
    public String getTransitionPath() {
        return transitionPath;
    }

    public void setTransitionPath(String transitionPath) {
        this.transitionPath = transitionPath;
    }

    public String getArtName() {
        return artName;
    }

    public void setArtName(String artName) {
        this.artName = artName;
    }

    @Override
    public String toString() {
	return "WebMusicInfo [id=" + id + ", transitionName=" + transitionName
		+ ", transitionUrl=" + transitionUrl + ", localPath=" + localPath
		+ ", pngPath=" + pngPath +  ", transitionPath=" + transitionPath +", artName=" + artName + "]";
    }

    private String transitionName, transitionUrl, localPath, pngPath, transitionPath, artName;


}
