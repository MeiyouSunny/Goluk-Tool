package com.rd.veuisdk.model;

import java.io.Serializable;

import com.rd.lib.utils.FileUtils;

/**
 * 新版网络配乐
 * 
 * @author ADMIN
 * 
 */
public class WebMusicInfo implements Serializable {

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

	public String getMusicName() {
		return musicName;
	}

	public void setMusicName(String musicName) {
		this.musicName = musicName;
	}

	public String getMusicUrl() {
		return musicUrl;
	}

	public void setMusicUrl(String musicUrl) {
		this.musicUrl = musicUrl;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public String getArtName() {
		return artName;
	}

	public void setArtName(String artName) {
		this.artName = artName;
	}

	private long mduration;

	public long getDuration() {
		return mduration;
	}

	public void setDuration(long mduration) {
		this.mduration = mduration;
	}

	@Override
	public String toString() {
		return "WebMusicInfo [id=" + id + ", musicName=" + musicName
				+ ", musicUrl=" + musicUrl + ", localPath=" + localPath
				+ ", artName=" + artName + "]";
	}

	private String musicName, musicUrl, localPath, artName;

	/**
	 * 判断音频文件是否存在本地，区分是否需要下载
	 * 
	 * @return
	 */
	public boolean exists() {
		return isExist;

	}

	/**
	 * 
	 * 进入fragment之前验证防止频繁new File()
	 * 
	 */
	public void checkExists() {
		isExist = FileUtils.isExist(localPath);
	}

	private boolean isExist = true;

}
