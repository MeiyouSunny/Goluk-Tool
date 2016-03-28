package cn.com.mobnote.golukmobile.userbase.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 用户基础信息
 * @author leege100
 * 
 */
public class BaseUserBean {
	
	/** 用户唯一id **/
	@JSONField(name = "uid")
	public String uid;

	/** 用户昵称 **/
	@JSONField(name = "nickname")
	public String nickname;

	/** 用户头像 **/
	@JSONField(name = "avatar")
	public String avatar;

	/** 自定义头像URL **/
	@JSONField(name = "customavatar")
	public String customavatar;

	/** 性别 **/
	@JSONField(name = "sex")
	public String sex;

	/** 个性签名 **/
	@JSONField(name = "introduction")
	public String introduction;

	/** 认证信息 **/
	@JSONField(name = "certification")
	public CertificationBean certification;

}
