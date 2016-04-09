package com.mobnote.golukmain.videodetail;

import com.alibaba.fastjson.annotation.JSONField;
import com.mobnote.golukmain.cluster.bean.UserLabelBean;

public class VideoUserInfo {
	@JSONField(name = "uid")
	public String uid;
	@JSONField(name = "nickname")
	public String nickname;
	@JSONField(name = "headportrait")
	public String headportrait;
	/** 头像网络地址 */
	@JSONField(name = "customavatar")
	public String customavatar;
	@JSONField(name = "sex")
	public String sex;
	/**认证标识**/
//	/** 蓝V描述 **/
//	@JSONField(name = "approve")
//	public String approve;
//
//	/** 蓝V标识 **/
//	@JSONField(name = "approvelabel")
//	public String approvelabel;
//
//	/** 达人认证 **/
//	@JSONField(name = "tarento")
//	public String tarento;
//
//	/** 黄v标识 **/
//	@JSONField(name = "headplusv")
//	public String headplusv;
//
//	/** 黄V描述 **/
//	@JSONField(name = "headplusvdes")
//	public String headplusvdes;
	
	@JSONField(name = "label")
	public UserLabelBean label;
}
