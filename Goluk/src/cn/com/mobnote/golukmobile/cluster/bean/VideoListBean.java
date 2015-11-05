package cn.com.mobnote.golukmobile.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoListBean {
	
	/**视频属性**/
	@JSONField(name="video")
	public VideoBean video;
	
	/**用户属性**/
	@JSONField(name="user")
	public UserBean user;
	
}
