package com.mobnote.golukmain.follow.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowAllLinkListBean {
	// 关联用户id
	@JSONField(name="linkuid")
	public String linkuid;
	// 0: 未关注; 1: 关注; 2: 互相关注
	@JSONField(name="link")
	public int link;
}
