package cn.com.mobnote.golukmobile.videodetail;

import com.alibaba.fastjson.annotation.JSONField;

public class VideoAllData {
	@JSONField(name="result")
	public String result;
	@JSONField(name="avideo")
	public VideoSquareDetailInfo avideo;
	@JSONField(name="link")
	public VideoLink link;
	@JSONField(name="head")
	public ZTHead head;
}
