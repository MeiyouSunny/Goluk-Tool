package com.mobnote.golukmain.videodetail;

import com.alibaba.fastjson.annotation.JSONField;
import com.mobnote.golukmain.cluster.bean.TagTagsBean;

import java.util.List;

public class VideoInfo {
	public static final String VIDEO_TYPE_LIVE = "1";

	@JSONField(name="category")
	public String category;

	@JSONField(name="videoid")
	public String videoid;
	@JSONField(name="type")
	public String type;
	@JSONField(name="sharingtime")
	public String sharingtime;
	@JSONField(name="describe")
	public String describe;
	@JSONField(name="picture")
	public String picture;
	@JSONField(name="clicknumber")
	public String clicknumber;
	@JSONField(name="praisenumber")
	public String praisenumber;
	@JSONField(name="starttime")
	public String starttime;
	@JSONField(name="livetime")
	public String livetime;
	@JSONField(name="livewebaddress")
	public String livewebaddress;
	@JSONField(name="livesdkaddress")
	public String livesdkaddress;
	@JSONField(name="ondemandwebaddress")
	public String ondemandwebaddress;
	@JSONField(name="ondemandsdkaddress")
	public String ondemandsdkaddress;
	@JSONField(name="ispraise")
	public String ispraise;
	@JSONField(name="videodata")
	public VideoDataInfo videodata;
	@JSONField(name="reason")
	public String reason;
	@JSONField(name="comment")
	public VideoCommentInfo comment;
	@JSONField(name="picture_thmb")
	public String picture_thmb;
	/** 地理位置 */
	@JSONField(name="location")
	public String location;
	/**推荐及金币相关**/
	@JSONField(name="gen")
	public VideoRecommend recom;
	@JSONField(name="tags")
	public List<TagTagsBean> tags;
}
