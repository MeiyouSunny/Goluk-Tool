package com.mobnote.golukmain.followed.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowedListBean {
	// 0: 视频; 1: 推荐用户
	@JSONField(name="type")
	public String type;
	// type为0时, 有值
	@JSONField(name="followvideo")
	public FollowedVideoObjectBean followvideo;
	// type为1时, 有值
	@JSONField(name="recomuser")
	public List<FollowedRecomUserBean> recomuser;
}
