package cn.com.mobnote.golukmobile.followed.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowGenBean {
	@JSONField(name="channelid")
	public String channelid;
	@JSONField(name="chaname")
	public String chaname;
	@JSONField(name="topicid")
	public String topicid;
	@JSONField(name="topicname")
	public String topicname;
	@JSONField(name="tjtime")
	public String tjtime;
	// 0:否; 1:是;
	@JSONField(name="isrecommend")
	public String isrecommend;
	@JSONField(name="reason")
	public String reason;
	// 0:否; 1:是;
	@JSONField(name="isreward")
	public String isreward;
	// 0:否; 1:是;
	@JSONField(name="atflag")
	public String atflag;
	@JSONField(name="atreason")
	public String atreason;
	@JSONField(name="atgold")
	public String atgold;
	@JSONField(name="attime")
	public String attime;
	// 0:否; 1:是;
	@JSONField(name="sysflag")
	public String sysflag;
	@JSONField(name="sysreason")
	public String sysreason;
	@JSONField(name="sysgold")
	public String sysgold;
	@JSONField(name="systime")
	public String systime;
	@JSONField(name="total")
	public String total;
	@JSONField(name="lasttime")
	public String lasttime;
}
