package com.mobnote.golukmain.thirdshare;

import android.graphics.Bitmap;

public class ThirdShareBean {
	/** 分享地址 */
	public String surl;
	public String curl;
	/** title */
	public String tl;
	/** 描述 */
	public String db;
	public Bitmap bitmap;
	public String realDesc;
	public String videoId;
	/** 0:普通列表分享 1:即刻分享 **/
	public String mShareType = "";
	/**视频文件的本地路径**/
	public String filePath = "";

}
