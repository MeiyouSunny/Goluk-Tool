package cn.com.mobnote.golukmobile.videosuqare.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class PraiseCancelResultBean {
	/**请求是否成功**/
	@JSONField(name="success")
	public boolean success;

	/**返回调试信息**/
	@JSONField(name="msg")
	public String msg;

	/**结果代码**/
	@JSONField(name="data")
	public PraiseCancelResultDataBean data;
}
