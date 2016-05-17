package com.mobnote.golukmain.newest;

import com.alibaba.fastjson.annotation.JSONField;

public class BannerTextBody {
	@JSONField(name="access")
	public String access;
	@JSONField(name="title")
	public String title;
	@JSONField(name="description")
	public String description;
	@JSONField(name="type")
	public String type;
	@JSONField(name="color")
	public String color;

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

	public void setColor(String color) {
		this.color = color;
	}

	public String getColor() {
		return color;
	}
}
