package cn.com.mobnote.golukmobile.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageSelectBean {

	/** 1：专题；2：TAG聚合页；3：单视频  **/
	@JSONField(name="type")
	public String type ;
	
	

}
