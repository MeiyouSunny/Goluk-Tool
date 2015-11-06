package cn.com.mobnote.golukmobile.newest;

import com.alibaba.fastjson.annotation.JSONField;

public class BannerSlideBody {
	@JSONField(name="type")
	public String type;
	@JSONField(name="access")
	public String access;
	@JSONField(name="picture")
	public String picture;
	@JSONField(name="title")
	public String title;
	@JSONField(name="description")
	public String description;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
