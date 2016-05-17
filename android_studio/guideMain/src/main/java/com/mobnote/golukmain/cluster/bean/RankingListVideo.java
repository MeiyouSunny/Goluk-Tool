package com.mobnote.golukmain.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class RankingListVideo {
	
	/** 视频唯一id **/
	@JSONField(name="videoid")
	public String videoid;
	
	/**视频描述**/
	@JSONField(name="description")
	public String description;
	
	/**视频封面**/
	@JSONField(name="pictureurl")
	public String pictureurl;
	
	/**视频文件**/
	@JSONField(name="videourl")
	public String videourl;

	/**视频清晰度**/
	@JSONField(name="resolution")
	public String resolution;

	/**是否公开**/
	@JSONField(name="isopen")
	public String isopen;

	/**视频添加时间**/
	@JSONField(name="addtime")
	public String addtime;

	/**视频作者**/
	@JSONField(name="user")
	public RankingListUser user;

	/**点击次数**/
	@JSONField(name="clickcount")
	public int clickcount;

	/**人气值**/
	@JSONField(name="rank")
	public int rank;

}
