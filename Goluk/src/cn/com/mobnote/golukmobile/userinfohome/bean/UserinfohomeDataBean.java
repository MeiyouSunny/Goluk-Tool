package cn.com.mobnote.golukmobile.userinfohome.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class UserinfohomeDataBean {
	
	/** 结果代码 **/
	@JSONField(name = "result")
	public String result;
	
	/** user **/
	@JSONField(name = "user")
	public UserinfohomeUserBean user;
	
	/** 分享视频总数 **/
	@JSONField(name = "sharevideonumber")
	public String sharevideonumber;
	
	/** 精华视频总数 **/
	@JSONField(name = "essencevideonumber")
	public String essencevideonumber;
	
	/** 关注数 **/
	@JSONField(name = "followingnumber")
	public String followingnumber;
	
	/** 粉丝数 **/
	@JSONField(name = "fansnumber")
	public String fansnumber;
	
	/** 新增粉丝数 **/
	@JSONField(name = "newfansnumber")
	public String newfansnumber;
	
	/** 赞你的人总数 **/
	@JSONField(name = "praisemenumber")
	public String praisemenumber;

}
