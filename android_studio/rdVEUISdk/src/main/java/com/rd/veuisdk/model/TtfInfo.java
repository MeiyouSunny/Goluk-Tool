package com.rd.veuisdk.model;

import com.rd.veuisdk.utils.FileUtils;

public class TtfInfo {

	@Override
	public String toString() {
		return "TtfInfo{" +
				"code='" + code + '\'' +
				", url='" + url + '\'' +
				", local_path='" + local_path + '\'' +
				", icon='" + icon + '\'' +
				", id=" + id +
				", timeunix=" + timeunix +
				", index=" + index +
				", bCustomApi=" + bCustomApi +
				'}';
	}

	public String code, url, local_path,icon;
	public int id;
	public long timeunix;
	public int index;
	public	boolean bCustomApi=false;

	/**
	 * 文件是否已下载
	 * 
	 * @return
	 */
	public boolean isdownloaded() {
		return FileUtils.isExist(local_path);
	}

}
