package com.mobnote.golukmain.followed.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowedRecomUserBean {
	@JSONField(name="uid")
	public String uid;
	@JSONField(name="nickname")
	public String nickname;
	@JSONField(name="avatar")
	public String avatar;
	// 自定义头像URL 如果为空的话取“avatar”参数值
	@JSONField(name="customavatar")
	public String customavatar;
	@JSONField(name="sex")
	public String sex;
	@JSONField(name="introduction")
	public String introduction;
	@JSONField(name="certification")
	public FollowedCertificationBean certification;
	// 连接类型 0:未连接;1:关注;2:粉丝;3:互粉(互相关注);
	@JSONField(name="link")
	public int link;
	@JSONField(name="share")
	public int share;
	@JSONField(name="following")
	public int following;
	@JSONField(name="fans")
	public int fans;
	@JSONField(name="index")
	public String index;
	// 围观次数最多的视频
	@JSONField(name="hotvideo")
	public List<FollowedHotVideoBean> hotvideo;

	// Record for the position in recommend user list
	public int position;
	public boolean showAllFollow;
}
