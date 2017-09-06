package com.rd.veuisdk.model;

import android.text.TextUtils;

/**
 * 音乐信息项
 * 
 * @author abreal
 * 
 */
public class MusicItem {

    private int mId;
    private String mTitle;
    private String mTitleSortKey;
    private String mPath;
    private boolean mSelected;
    private boolean mPlaying;
    private String mAssetsName;
    private String art;

    public String getArt() {
	return art;
    }

    public void setArt(String art) {
	this.art = art;
    }

    private boolean mExtFile;
    private long mDuration;
    /**
     * 支持的音乐格式
     */
    public static final String[] MUSIC_FORMAT = new String[] { "aac", "mp3",
	    "mp2", "amr", "ogg", "ac3", "wmv", "wma","wav" };

    // 判断是否是本地文件。是否显示下载

    // private int mStartTime;// 开始时间
    /**
     * 检查是否为可支持的音乐文件
     * 
     * @param strPath
     * @return
     */
    public static boolean checkValidExtMusicFile(String strPath) {

	if (!TextUtils.isEmpty(strPath)) {
	    boolean re = false;
	    int len = MUSIC_FORMAT.length;
	    for (int i = 0; i < len; i++) {

		if (strPath.endsWith(MUSIC_FORMAT[i])) {
		    re = true;
		    break;
		}
	    }
	    return re;

	}
	return false;

    }

    public boolean isExtFile() {
	return mExtFile;
    }

    public void setExtFile(boolean mExtFile) {
	this.mExtFile = mExtFile;
    }

    /**
     * 获取持续时间
     * 
     * @return
     */
    public long getDuration() {
	return mDuration;
    }

    /**
     * 设置持续时间
     * 
     * @param mDuration
     */
    public void setDuration(long mDuration) {
	this.mDuration = mDuration;
    }

    /**
     * 获取内置资源名称
     * 
     * @return
     */
    public String getAssetsName() {
	return mAssetsName;
    }

    /**
     * 设置内置资源名称
     * 
     * @param mAssetsName
     */
    public void setAssetsName(String mAssetsName) {
	this.mAssetsName = mAssetsName;
    }

    /**
     * 是否播放
     * 
     * @return
     */
    public boolean isPlaying() {
	return mPlaying;
    }

    /**
     * 设置是否播放
     * 
     * @param mPlaying
     */
    public void setPlaying(boolean mPlaying) {
	this.mPlaying = mPlaying;
    }

    /**
     * 是否选中
     * 
     * @return
     */
    public boolean isSelected() {
	return mSelected;
    }

    /**
     * 设置是否选中
     */
    public void setSelected(boolean mSelected) {
	this.mSelected = mSelected;
    }

    /**
     * 获取编号
     * 
     * @return
     */
    public int getId() {

	return mId;
    }

    /**
     * 设置编号
     * 
     * @param mId
     */
    public void setId(int mId) {
	this.mId = mId;
    }

    /**
     * 获取标题
     * 
     * @return
     */
    public String getTitle() {
	String strResultTitle;
	if (!TextUtils.isEmpty(mTitle)) {
	    strResultTitle = mTitle;
	} else {
	    strResultTitle = "";
	}
	return strResultTitle;
    }

    /**
     * 设置标题
     * 
     * @param mTitle
     */
    public void setTitle(String mTitle) {
	this.mTitle = mTitle;
    }

    /**
     * 获取标题排序key
     * 
     * @return
     */
    public String getTitleSortKey() {
	return mTitleSortKey;
    }

    /**
     * 设置标题排序key
     * 
     * @param strTitleSortKey
     */
    public void setTitleSortKey(String strTitleSortKey) {
	this.mTitleSortKey = strTitleSortKey;
    }

    /**
     * 获取播放路径
     * 
     * @return
     */
    public String getPath() {

	return mPath;
    }

    /**
     * 设置播放路径
     * 
     * @param mPath
     */
    public void setPath(String mPath) {
	this.mPath = mPath;
    }

    /**
     * 获取标题标序key首个字符
     * 
     * @return
     */
    public char getLetter() {
	if (!TextUtils.isEmpty(mTitleSortKey)) {
	    char cResult = Character.toUpperCase(getTitleSortKey().charAt(0));
	    char cLowResult = Character
		    .toLowerCase(getTitleSortKey().charAt(0));
	    if (cResult < 'A' || (cResult > 'Z' && cLowResult < 'a')) {
		return '*';
	    } else if (cResult > 'Z') {
		return '#';
	    } else {
		return cResult;
	    }
	} else {
	    return '*';
	}
    }

    /**
     * 字符串实例化
     */
    @Override
    public String toString() {
	return String.format("Title:%s,title key:%s,path:%s", mTitle,
		mTitleSortKey, mPath);
    }

}