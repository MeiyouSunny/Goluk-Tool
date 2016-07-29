package com.mobnote.golukmain.cluster.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class ClusterHeadBean {
	/**结果代码**/
	@JSONField(name="result")
	public String result;
	
	/**活动信息**/
	@JSONField(name="activity")
	public TagActivityBean activity;
	
	/**推荐列表**/
	@JSONField(name="recommendvideo")
	public List<VideoListBean> recommendvideo;
	
	/**最新列表**/
	@JSONField(name="latestvideo")
	public List<VideoListBean> latestvideo;
	
	
}
