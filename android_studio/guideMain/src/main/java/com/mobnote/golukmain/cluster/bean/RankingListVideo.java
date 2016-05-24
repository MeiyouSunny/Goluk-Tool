package com.mobnote.golukmain.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class RankingListVideo {

	/**视频作者**/
	@JSONField(name="user")
	public RankingListUser user;

	/**视频作者**/
	@JSONField(name="video")
	public RankingVideoBean video;



}
