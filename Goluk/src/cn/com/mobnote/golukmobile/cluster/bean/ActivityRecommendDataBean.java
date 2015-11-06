package cn.com.mobnote.golukmobile.cluster.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class ActivityRecommendDataBean {
	/**返回结果**/
	@JSONField(name="result")
	public String result;
	
	/**操作**/
	@JSONField(name="operation")
	public ClusterHeadBean operation;
	
	/**视频总数**/
	@JSONField(name="videocount")
	public String videocount;
	
	/**视频列表**/
	@JSONField(name="videolist")
	public List<VideoListBean> videolist;
	
}
