package com.mobnote.golukmain.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoDataBean {
	
	/**爱淘客id**/
	@JSONField(name="aid")
	public String aid;
	
	/**视频类型**/
	@JSONField(name="mid")
	public String mid;
	
	@JSONField(name="active")
	public String active;
	
	@JSONField(name="tag")
	public String tag;

	@JSONField(name="open")
	public String open;

	/**经度**/
	@JSONField(name="lon")
	public String lon;

	/**维度**/
	@JSONField(name="lat")
	public String lat;


	/**速度**/
	@JSONField(name="speed")
	public String speed;

	/**是否开启对讲**/
	@JSONField(name="talk")
	public String talk;

	/**是否静音**/
	@JSONField(name="voice")
	public String voice;

	/**视频分类**/
	@JSONField(name="vtype")
	public String vtype;

	/**耗時**/
	@JSONField(name="restime")
	public String restime;

	/**耗流量**/
	@JSONField(name="flux")
	public String flux;

	
}
