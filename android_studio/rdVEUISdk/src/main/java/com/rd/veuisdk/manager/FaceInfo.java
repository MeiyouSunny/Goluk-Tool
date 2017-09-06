package com.rd.veuisdk.manager;

import java.io.File;
import java.io.Serializable;

import android.text.TextUtils;

/**
 * 单个人脸效果
 * 
 * @author JIAN
 * @date 2017-3-18 上午11:19:43
 */
public class FaceInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private String path, title, icon, url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/***
	 * 判断该网络文件是否存在
	 * 
	 * @return
	 */
	public boolean isExists() {
		if (!TextUtils.isEmpty(path)) {
			try {
				File f = new File(path);
				return (f.exists() && f.length() > 0);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return false;
	}

	/**
	 * 支持网络化人脸贴纸 <网络化参数专用>
	 * 
	 * @param path
	 *            .mp3 本地路径
	 * @param icon
	 *            本地图标
	 * @param title
	 *            说明
	 * @param url
	 *            贴纸url
	 */
	public FaceInfo(String path, String title, String icon, String url) {
		super();
		this.path = path;
		this.title = title;
		this.icon = icon;
		this.url = url;
	}

	/**
	 * 
	 * @param path
	 *            .mp3 本地路径
	 * @param icon
	 *            本地图标
	 * @param title
	 *            说明
	 */
	public FaceInfo(String path, String icon, String title) {
		super();
		this.path = path;
		this.title = title;
		this.icon = icon;
	}

	public String getPath() {
		return path;
	}

	/**
	 * 
	 * @param path
	 *            .mp3 本地路径
	 * @return
	 */
	public FaceInfo setPath(String path) {
		this.path = path;
		return this;
	}

	public String getTitle() {
		return title;
	}

	/**
	 * 说明
	 * 
	 * @param title
	 * @return
	 */
	public FaceInfo setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getIcon() {
		return icon;
	}

	/**
	 * 
	 * @param icon
	 *            本地图标
	 * @return
	 */
	public FaceInfo setIcon(String icon) {
		this.icon = icon;
		return this;
	}

}
