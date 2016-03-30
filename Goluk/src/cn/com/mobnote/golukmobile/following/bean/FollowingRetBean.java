package cn.com.mobnote.golukmobile.following.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowingRetBean {
	
	@JSONField(name="code")
	public int code;
	
	/**返回数据**/
	@JSONField(name="data")
	public FollowingDataBean data;
	
	/**返回调试信息**/
	@JSONField(name="msg")
	public String msg;

}
