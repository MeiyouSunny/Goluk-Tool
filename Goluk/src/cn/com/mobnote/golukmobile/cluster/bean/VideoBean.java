package cn.com.mobnote.golukmobile.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoBean {
	
	/**视频唯一id**/
	@JSONField(name="videoid")
	public String videoid;
	
	/**视频类型**/
	@JSONField(name="type")
	public String type;
	
	/**视频上传时间**/
	@JSONField(name="sharingtime")
	public String sharingtime;
	
	/**视频描述**/
	@JSONField(name="describe")
	public String describe;
	
	/**视频图片**/
	@JSONField(name="picture")
	public String picture;

	/**视频图片**/
	@JSONField(name="picture_thmb")
	public String picture_thmb;

	/**点击次数**/
	@JSONField(name="clicknumber")
	public String clicknumber;

	/**点赞次数**/
	@JSONField(name="praisenumber")
	public String praisenumber;


	/**直播起始时间**/
	@JSONField(name="livetime")
	public String livetime;
	
	/**直播起始时间**/
	@JSONField(name="starttime")
	public String starttime;
	

	/**直播web地址**/
	@JSONField(name="livewebaddress")
	public String livewebaddress;

	/**直播sdk地址**/
	@JSONField(name="livesdkaddress")
	public String livesdkaddress;

	/**点播web地址**/
	@JSONField(name="ondemandwebaddress")
	public String ondemandwebaddress;

	/**点播sdk地址**/
	@JSONField(name="ondemandsdkaddress")
	public String ondemandsdkaddress;

	/**是否点过赞**/
	@JSONField(name="ispraise")
	public String ispraise;

	/**直播数据**/
	@JSONField(name="videodata")
	public VideoDataBean videodata;
	
	/**用户当前位置**/
	@JSONField(name="location")
	public String location;
	
	/**推荐理由**/
	@JSONField(name="reason")
	public String reason;
	
	/**视频评论**/
	@JSONField(name="comment")
	public CommentBean comment;
	
}
