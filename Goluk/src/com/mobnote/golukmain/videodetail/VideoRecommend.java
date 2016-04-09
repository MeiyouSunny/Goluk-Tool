package com.mobnote.golukmain.videodetail;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoRecommend {

	/**活动id**/
	@JSONField(name="topicid")
	public String topicid;
	/**活动名称**/
	@JSONField(name="topicname")
	public String topicname;
	/**频道id**/
	@JSONField(name="chanid")
	public String chanid;
	/**频道名称**/
	@JSONField(name="chaname")
	public String chaname;
	/**获奖视频标志**/
	@JSONField(name="isreward")
	public String isreward;
	/**活动奖励标志**/
	@JSONField(name="atflag")
	public String atflag;
	/**活动奖励理由**/
	@JSONField(name="atreason")
	public String atreason;
	/**活动奖励金额**/
	@JSONField(name="atgold")
	public String atgold;
	/**系统奖励标志**/
	@JSONField(name="sysflag")
	public String sysflag;
	/**系统奖励原因**/
	@JSONField(name="sysreason")
	public String sysreason;
	/**系统奖励金额**/
	@JSONField(name="sysgold")
	public String sysgold;
	/**是否推荐**/
	@JSONField(name="isrecommend")
	public String isrecommend;
	/**活动推荐理由**/
	@JSONField(name="reason")
	public String reason;
	
	
}
