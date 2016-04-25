package com.goluk.videoedit.bean;

/**
 * 视频后期处理背景音乐
 * @author wangli
 *
 */
public class AEMusic {

	boolean isSelected;
	String musicName;
	String musicPath;

	public AEMusic(String name,String path,boolean selected){
		this.musicName = name;
		this.musicPath = path;
		this.isSelected = selected;
	}
	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	public String getMusicName() {
		return musicName;
	}
	public void setMusicName(String musicName) {
		this.musicName = musicName;
	}
	public String getMusicPath() {
		return musicPath;
	}
	public void setMusicPath(String musicPath) {
		this.musicPath = musicPath;
	}

}
