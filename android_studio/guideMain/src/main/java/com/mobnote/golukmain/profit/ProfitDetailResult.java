package com.mobnote.golukmain.profit;

import com.alibaba.fastjson.annotation.JSONField;

public class ProfitDetailResult {

	/**封面图片地址**/
	@JSONField(name="url")
	public String url;
	
	/**视频id**/
	@JSONField(name="vid")
	public String vid;
	
	/**获取金币数量**/
	@JSONField(name="gold")
	public String gold;
	
	/**获奖时间**/
	@JSONField(name="time")
	public String time;
	
	/**时间戳**/
	@JSONField(name="timestamp")
	public String timestamp;
	
}
