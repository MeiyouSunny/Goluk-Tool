package cn.com.mobnote.golukmobile.followed.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowedHotVideoBean {
	@JSONField(name="videoid")
	public String videoid;
	@JSONField(name="description")
	public String description;
	@JSONField(name="pictureurl")
	public String pictureurl;
	@JSONField(name="videourl")
	public String videourl;
	// 视频清晰度 480p, 1080p
	@JSONField(name="resolution")
	public String resolution;
	// 视频添加时间 亦是视频分享时间
	@JSONField(name="addtime")
	public String addtime;
	@JSONField(name="commentcount")
	public int commentcount;
	@JSONField(name="clickcount")
	public int clickcount;
	@JSONField(name="likecount")
	public int likecount;
	@JSONField(name="index")
	public String index;
}
