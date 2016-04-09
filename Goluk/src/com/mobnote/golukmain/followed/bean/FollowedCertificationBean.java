package com.mobnote.golukmain.followed.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FollowedCertificationBean {
	// 机构实名认证标识(蓝V) 是否认证。0：否；1：是。(approvelabel)
	@JSONField(name="isorgcertificated")
	public String isorgcertificated;
	// 机构实名认证(蓝V) 机构认证内容。(approve)
	@JSONField(name="orgcertification")
	public String orgcertification;
	// 个人实名认证标识(黄V) 是否认证。0：否；1：是。(headplusv)
	@JSONField(name="isusercertificated")
	public String isusercertificated;
	// 个人实名认证(黄V) 个人认证内容。(headplusvdes)
	@JSONField(name="usercertification")
	public String usercertification;
	// 达人标识 是否认证。0：否；1：是。(tarento)
	@JSONField(name="isstar")
	public String isstar;
}
