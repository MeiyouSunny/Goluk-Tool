package cn.com.mobnote.golukmobile.followed.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowListBean {
	// 0: 视频; 1: 推荐用户
	@JSONField(name="type")
	public String type;
	// type为0时, 有值
	@JSONField(name="followvideo")
	public FollowVideoObjectBean followvideo;
	// type为1时, 有值
	@JSONField(name="recomuser")
	public List<FollowRecomUserBean> recomuser;
}
