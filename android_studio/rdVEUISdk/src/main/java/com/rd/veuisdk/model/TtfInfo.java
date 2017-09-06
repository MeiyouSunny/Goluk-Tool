package com.rd.veuisdk.model;

import com.rd.veuisdk.utils.FileUtils;

public class TtfInfo {

	public String code, url, local_path;
	public int id;
	public long timeunix;
	public int index;

	/**
	 * 文件是否已下载
	 * 
	 * @return
	 */
	public boolean isdownloaded() {
		return FileUtils.isExist(local_path);
	}

}
