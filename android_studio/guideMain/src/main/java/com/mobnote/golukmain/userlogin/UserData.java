package com.mobnote.golukmain.userlogin;

import com.alibaba.fastjson.annotation.JSONField;

public class UserData {
	/**请求返回码**/
	@JSONField(name="uid")
	public String uid;// 	用户id  	
	
	/**用户昵称**/
	@JSONField(name="nickname")
	public String nickname;
	
	
	/**用户头像 **/
	@JSONField(name="head")
	public String head;	
	
	/**自定义头像URL**/
	@JSONField(name="customavatar")
	public String customavatar;
	
	/**性别**/
	@JSONField(name="sex")
	public String sex;	
	
	/**介绍**/
	@JSONField(name="desc")
	public String desc;	
	
	/**爱淘客id **/
	@JSONField(name="aid")
	public String aid;
	
	/**爱淘客key **/
	@JSONField(name="key")
	public String key;
	
	/**CC回调地址**/
	@JSONField(name="ccbackurl")
	public String ccbackurl;
	
	/**手机号**/
	@JSONField(name="phone")
	public String phone;
	
	/**第三方登录平台**/
	@JSONField(name="platform")
	public String platform;
	
	/**关注的视频计数**/
	@JSONField(name="followvideo")
	public int followvideo;
	
	/**授权令牌 **/
	@JSONField(name="token")
	public String token;
}
