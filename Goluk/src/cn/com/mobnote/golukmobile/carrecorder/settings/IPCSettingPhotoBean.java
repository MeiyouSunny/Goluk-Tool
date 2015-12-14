package cn.com.mobnote.golukmobile.carrecorder.settings;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

public class IPCSettingPhotoBean implements Serializable {
	/** */
	private static final long serialVersionUID = 1L;
	/** 分辨率 */
	@JSONField(name = "resolution")
	public String resolution;
	/** 抓图质量 */
	@JSONField(name = "quality")
	public String quality;
	/** 自动拍照间隔 */
	@JSONField(name = "interval")
	public String interval;
}
