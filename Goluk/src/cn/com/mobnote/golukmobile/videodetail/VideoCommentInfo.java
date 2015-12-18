package cn.com.mobnote.golukmobile.videodetail;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoCommentInfo {
	@JSONField(name="iscomment")
	public String iscomment;
	@JSONField(name="comcount")
	public String comcount;
	@JSONField(name="comlist")
	public List<VideoListInfo> comlist;
}
