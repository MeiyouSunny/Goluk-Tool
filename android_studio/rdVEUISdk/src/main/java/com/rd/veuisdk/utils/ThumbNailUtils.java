package com.rd.veuisdk.utils;

import android.content.Context;
import android.content.res.Resources;

import com.rd.veuisdk.R;

public class ThumbNailUtils {

	public static int THUMB_WIDTH = 160;
	public static int THUMB_HEIGHT = 90;

	private static ThumbNailUtils instance;

	public static ThumbNailUtils getInstance(Context context) {

		if (null == instance) {
			instance = new ThumbNailUtils(context);
		}
		return instance;
	}

	private ThumbNailUtils(Context context) {
		Resources res = context.getResources();
		THUMB_HEIGHT = res.getDimensionPixelSize(R.dimen.thumbnail_height);
		THUMB_WIDTH = res.getDimensionPixelSize(R.dimen.thumbnail_height);

	}

}
