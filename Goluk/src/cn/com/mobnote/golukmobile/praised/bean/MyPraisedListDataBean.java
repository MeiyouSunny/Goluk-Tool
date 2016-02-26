package cn.com.mobnote.golukmobile.praised.bean;

import java.util.ArrayList;

import com.alibaba.fastjson.annotation.JSONField;

public class MyPraisedListDataBean {

	/**返回调试信息**/
	@JSONField(name="result")
	public String result;

	/**视频列表**/
	@JSONField(name="videolist")
	public ArrayList<MyPraisedVideoBean> videolist;

	/**视频个数**/
	@JSONField(name="praisecount")
	public int praisecount;
}
