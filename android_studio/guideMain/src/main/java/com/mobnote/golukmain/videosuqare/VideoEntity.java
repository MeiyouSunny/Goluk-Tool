package com.mobnote.golukmain.videosuqare;

import com.mobnote.golukmain.cluster.bean.TagTagsBean;
import com.mobnote.golukmain.newest.CommentDataInfo;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 视频广场视频属性信息
 *
 * 2015年4月14日
 *
 * @author xuhw
 */
public class VideoEntity {
	/** 视频类型 */
	public String category;
	/** 视频唯一id */
	public String videoid;
	/** 视频类型：1.直播 2.点播 */
	public String type;
	/** 视频上传时间 视频分享时间 */
	public String sharingtime;
	/* 标准时间戳 */
	public long sharingts;
	/** 视频描述 */
	public String describe;
	/** 视频图片 */
	public String picture;
	/** 点击次数 */
	public String clicknumber;
	/** 点赞次数 */
	public String praisenumber;
	/** 直播起始时间 (不一定有) */
	public String starttime;
	/** 直播时间 (不一定有) */
	public String livetime;
	/** 直播web地址 */
	public String livewebaddress;
	/** 直播sdk地址 */
	public String livesdkaddress;
	/** 点播web地址 */
	public String ondemandwebaddress;
	/** 点播sdk地址 */
	public String ondemandsdkaddress;
	/** 是否点过赞：0.否1.是 */
	public String ispraise;
	/** 此视频发布的地点 格式为 北京市. 朝阳区 */
	public String location;
	/* 标签 */
	public List<TagTagsBean> tags;

	public VideoExtra videoExtra;

	/**
	 * 是否公开到广场 0：否 1：是
	 */
	public String isopen;
	/** 直播数据 */
	public LiveVideoData livevideodata;
	public String reason;
	public String iscomment;
	public String comcount;
	public List<CommentDataInfo> commentList;

	public VideoEntity() {
		this.commentList = new ArrayList<CommentDataInfo>();
		this.tags = new ArrayList<TagTagsBean>();
	}

}
