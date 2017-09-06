package com.rd.veuisdk.model;

import android.content.Context;

public class CustomData1 {

	// 缩略图空间的图片资源id
	private int imageViewResourceId;
	// 缩略图空间的图片资源的路径
	private String imageViewPicPath;

	// 描述信息
	private String descriptionInfo;

	// 选中标示的图片资源id
	private int selectedFlagResourceId = -1;
	// 详情名字资源id
	private int descriptionResourceId;

	public CustomData1(int imageViewResourceId, String description,
			int selectedResourceId) {

		this.imageViewResourceId = imageViewResourceId;

		this.descriptionInfo = description;

		this.selectedFlagResourceId = selectedResourceId;
	}

	public CustomData1(int imageViewResourceId, int descriptionResourceId,
			int selectedResourceId, String description, String imageViewPicPath) {

		this.imageViewResourceId = imageViewResourceId;

		this.descriptionResourceId = descriptionResourceId;

		this.selectedFlagResourceId = selectedResourceId;

		this.descriptionInfo = description;

		this.imageViewPicPath = imageViewPicPath;
	}

	public int getImageViewResourceId() {
		return imageViewResourceId;
	}

	public String getDescriptionInfo() {
		return getDescriptionInfo(null);
	}

	public String getDescriptionInfo(Context context) {
		if (context != null && descriptionResourceId != 0) {
			descriptionInfo = context.getResources().getString(
					descriptionResourceId);
		}
		return descriptionInfo;
	}

	public int getDescriptionInfoId() {

		return descriptionResourceId;
	}

	public int getSelectedFlagResourceId() {
		return selectedFlagResourceId;
	}

	public String getImageViewPicPath() {
		return imageViewPicPath;
	}

}
