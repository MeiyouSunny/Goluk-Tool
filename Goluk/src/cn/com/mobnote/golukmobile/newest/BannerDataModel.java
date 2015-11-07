package cn.com.mobnote.golukmobile.newest;

import java.util.ArrayList;

import com.alibaba.fastjson.annotation.JSONField;

public class BannerDataModel {
	@JSONField(name="result")
	public String result;
	@JSONField(name="slides")
	public ArrayList<BannerSlideBody> slides;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public ArrayList<BannerSlideBody> getSlides() {
		return slides;
	}

	public void setSlides(ArrayList<BannerSlideBody> slides) {
		this.slides = slides;
	}
}