package com.mobnote.golukmain.videoshare.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoShareDataBean {
	/**请求是否成功**/
	@JSONField(name="result")
	public String result;
	/**短网址**/
	@JSONField(name="shorturl")
	public String shorturl;
	/**封面地址**/
	@JSONField(name="coverurl")
	public String coverurl;
	/**视频描述**/
	@JSONField(name="describe")
	public String describe;
}
