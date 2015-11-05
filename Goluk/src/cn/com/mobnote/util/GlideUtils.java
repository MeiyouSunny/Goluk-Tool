package cn.com.mobnote.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

public class GlideUtils {

	public static void loadNetHead(Context context, ImageView view, String headUrl, int placeholder) {
		CenterCrop a = new CenterCrop(Glide.get(context).getBitmapPool());
		if (placeholder < 0) {
			Glide.with(context).load(headUrl).transform(a).into(view);
		} else {
			Glide.with(context).load(headUrl).placeholder(placeholder).transform(a).into(view);
		}
	}

	public static void loadLocalHead(Context context, ImageView view, int headId) {
		CenterCrop a = new CenterCrop(Glide.get(context).getBitmapPool());
		Glide.with(context).load(headId).transform(a).into(view);
	}

	public static void loadImage(Context context, ImageView view, String neturl, int placeholder) {
		Glide.with(context).load(neturl).placeholder(placeholder).into(view);
	}

}
