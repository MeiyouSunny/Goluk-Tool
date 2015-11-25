package cn.com.mobnote.golukmobile.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class UserBean {
	
	/**用户唯一id**/
	@JSONField(name="uid")
	public String uid;
	
	/**用户昵称**/
	@JSONField(name="nickname")
	public String nickname;
	
	/**用户头像**/
	@JSONField(name="headportrait")
	public String headportrait;
	
	/**自定义头像URL**/
	@JSONField(name="customavatar")
	public String customavatar;
	
	/**性别**/
	@JSONField(name="sex")
	public String sex;
	
	/**认证信息**/
	@JSONField(name="label")
	public UserLabelBean label;
	
}
