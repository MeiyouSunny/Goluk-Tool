package cn.com.mobnote.golukmobile.newest;

import com.alibaba.fastjson.annotation.JSONField;

public class BannerModel {
	@JSONField(name="msg")
	public String msg;
	@JSONField(name="success")
	public boolean success;
	@JSONField(name="data")
	public BannerDataModel data;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public BannerDataModel getData() {
		return data;
	}

	public void setData(BannerDataModel data) {
		this.data = data;
	}
}
