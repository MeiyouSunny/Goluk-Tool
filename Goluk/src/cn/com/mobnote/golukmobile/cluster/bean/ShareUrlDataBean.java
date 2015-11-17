package cn.com.mobnote.golukmobile.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class ShareUrlDataBean {
	/**返回结果**/
	@JSONField(name="result")
	public String result;
	
	/**短网址**/
	@JSONField(name="shorturl")
	public String shorturl;
	
	/**封面地址**/
	@JSONField(name="coverurl")
	public String coverurl;
	
	/**活动名称**/
	@JSONField(name="name")
	public String name;
	
	/**活动描述**/
	@JSONField(name="description")
	public String description;
	
	
}
