package cn.com.mobnote.golukmobile.userinfohome.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class UserinfohomeUserBean {

	/** 用户唯一id **/
	@JSONField(name = "uid")
	public String uid;
	
	/** 性别 **/
	@JSONField(name = "sex")
	public String sex;

	/** 用户昵称 **/
	@JSONField(name = "nickname")
	public String nickname;
	
	@JSONField(name = "headportrait")
	public String headportrait;

	/** 个性签名 **/
	@JSONField(name = "introduce")
	public String introduce;
	
	/** 推荐及奖励 **/
	@JSONField(name = "recom")
	public UserRecomBean recom;
	
	/**  **/
	@JSONField(name = "label")
	public UserLabelBean label;
}
