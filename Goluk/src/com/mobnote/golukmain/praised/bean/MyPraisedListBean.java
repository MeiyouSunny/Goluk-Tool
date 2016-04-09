package com.mobnote.golukmain.praised.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MyPraisedListBean {
	/**请求是否成功**/
	@JSONField(name="success")
	public boolean success;

	/**返回调试信息**/
	@JSONField(name="msg")
	public String msg;

	/**请求返回数据**/
	@JSONField(name="data")
	public MyPraisedListDataBean data;
}
