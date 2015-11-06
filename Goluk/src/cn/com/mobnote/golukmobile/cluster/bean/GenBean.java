package cn.com.mobnote.golukmobile.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class GenBean {
	
	/**频道id**/
	@JSONField(name="channelid")
	public String channelid;
	
	/**活动Id**/
	@JSONField(name="topicid")
	public String topicid;
	
	/**活动名称**/
	@JSONField(name="topicname")
	public String topicname;
	
	/**是否推荐L**/
	@JSONField(name="isrecommend")
	public String isrecommend;
	
	/**是否获奖**/
	@JSONField(name="isreward")
	public String isreward;
	
}
