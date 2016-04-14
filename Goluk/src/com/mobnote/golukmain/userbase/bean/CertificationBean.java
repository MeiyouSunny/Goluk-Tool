package com.mobnote.golukmain.userbase.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 认证信息
 * @author leege100
 *
 */
public class CertificationBean {

	/** 机构实名认证标识（蓝V） **/
	@JSONField(name = "isorgcertificated")
	public String isorgcertificated;

	/** 机构实名认证（蓝V） **/
	@JSONField(name = "orgcertification")
	public String orgcertification;

	/** 达人标识 **/
	@JSONField(name = "isstar")
	public String isstar;

	/** 个人实名认证标识（黄V） **/
	@JSONField(name = "isusercertificated")
	public String isusercertificated;

	/** 个人实名认证（黄V） **/
	@JSONField(name = "usercertification")
	public String usercertification;
}

