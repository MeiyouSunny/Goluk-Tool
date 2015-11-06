package cn.com.mobnote.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class GlideUtils {

	public static void loadNetHead(Context context, ImageView view, String headUrl, int placeholder) {
		try {
			if (placeholder < 0) {
				Glide.with(context).load(headUrl).transform(new GlideCircleTransform(context)).into(view);
			} else {
				Glide.with(context).load(headUrl).placeholder(placeholder).transform(new GlideCircleTransform(context))
						.into(view);
			}
		} catch (Exception e) {

		}

	}

	public static void loadLocalHead(Context context, ImageView view, int headId) {
		try {
			Glide.with(context).load(headId).transform(new GlideCircleTransform(context)).into(view);
		} catch (Exception e) {

		}

	}

	public static void loadImage(Context context, ImageView view, String neturl, int placeholder) {
		Glide.with(context).load(neturl).placeholder(placeholder).into(view);
	}

}
