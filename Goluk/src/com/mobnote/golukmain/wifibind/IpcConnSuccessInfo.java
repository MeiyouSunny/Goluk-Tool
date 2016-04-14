package com.mobnote.golukmain.wifibind;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * IPC连接成功后返回的信息
 * 
 * @author jyf
 */
public class IpcConnSuccessInfo {
	/** IPC类型 G1,G2,T1,T1S */
	@JSONField(name = "productname")
	public String productname;

	@JSONField(name = "product")
	public String product;

	@JSONField(name = "model")
	public String model;

	/** SN号 */
	@JSONField(name = "serial")
	public String serial;

	/** 固件版本 */
	@JSONField(name = "version")
	public String version;
	@JSONField(serialize = false)
	public String lasttime;

}
