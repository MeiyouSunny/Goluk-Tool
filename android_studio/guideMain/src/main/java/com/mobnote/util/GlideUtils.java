package com.mobnote.util;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class GlideUtils {

	public static void loadNetHead(Context context, ImageView view, String headUrl, int placeholder) {
		try {
			if (placeholder < 0) {
				Glide.with(context).load(headUrl).transform(new GlideCircleTransform(context)).into(view);
			} else {
				Glide.with(context)
						.load(headUrl)
						//.diskCacheStrategy(DiskCacheStrategy.NONE)
						.skipMemoryCache(false)
						//.placeholder(placeholder)
						.transform(new GlideCircleTransform(context))
						.into(view);
			}
		} catch (Exception e) {

		}

	}

	public static void loadLocalHead(Context context, ImageView view, int headId) {
		try {
			Glide.with(context)
					.load(headId)
					//.diskCacheStrategy(DiskCacheStrategy.NONE)
					.skipMemoryCache(false)
					.transform(new GlideCircleTransform(context)).into(view);
		} catch (Exception e) {

		}

	}

	public static void loadImage(Context context, ImageView view, String neturl, int placeholder) {
		//Glide.get(context).setMemoryCategory(MemoryCategory.LOW);
		if (placeholder <= 0) {
			Glide.with(context).load(neturl).into(view);
		} else {
			Glide.with(context).load(neturl).placeholder(placeholder).into(view);
		}
	}

	/**
	 * 加载T1SP远程视频(紧急/循环)缩略图
	 */
	public static void loadRemoteT1SPImage(Context context, ImageView view, String neturl, int placeholder) {
		Glide.get(context).setMemoryCategory(MemoryCategory.LOW);
		if (placeholder <= 0) {
			Glide.with(context).load(neturl).diskCacheStrategy(DiskCacheStrategy.ALL).into(view);
		} else {
			//Glide.with(context).load(neturl).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(placeholder).into(view);
			Glide.with(context)
					.load(neturl)
					.centerCrop()
					.dontAnimate()
					.into(view);
		}
	}

	public static void loadLocalImage(Context context, ImageView view, int drawid) {
		Glide.get(context).setMemoryCategory(MemoryCategory.LOW);
		try {
			Glide.with(context).load(drawid).into(view);
		} catch (Exception e) {

		}
	}

	public static void clearMemory(Context context) {
		Glide.get(context).clearMemory();
	}

	public static void loadLocalImage(Context context, ImageView view, String neturl, int placeholder) {
		if (placeholder <= 0) {
			Glide.with(context).load(neturl).skipMemoryCache(true).into(view);
		} else {
			Glide.with(context).load(neturl).skipMemoryCache(true).placeholder(placeholder).into(view);
		}
	}

	public static void loadImage(Context context, Fragment fragment, ImageView view, String neturl, int placeholder) {
		Glide.get(context).setMemoryCategory(MemoryCategory.LOW);
		if (placeholder <= 0) {
			Glide.with(fragment).load(neturl).into(view);
		} else {
			Glide.with(fragment).load(neturl).placeholder(placeholder).into(view);
		}
	}

	public static void loadLocalImage(Context context, Fragment fragment, ImageView view, int drawid) {
		Glide.get(context).setMemoryCategory(MemoryCategory.LOW);
		try {
			Glide.with(fragment).load(drawid).into(view);
		} catch (Exception e) {

		}
	}

	public static void loadLocalImage(Fragment fragment, ImageView view, String neturl, int placeholder) {
		if (placeholder <= 0) {
			Glide.with(fragment).load(neturl).skipMemoryCache(true).into(view);
		} else {
			Glide.with(fragment).load(neturl).skipMemoryCache(true).placeholder(placeholder).into(view);
		}
	}
}
