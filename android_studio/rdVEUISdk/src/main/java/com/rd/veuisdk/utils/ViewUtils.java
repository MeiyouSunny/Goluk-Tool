package com.rd.veuisdk.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class ViewUtils {

    private ViewUtils() {
    }

    /**
     * 播放器暂停按钮淡出的动画
     * 
     * @param context
     * @param view
     *            播放器暂停按钮
     */
    public static void fadeOut(final Context context, final View view) {
	if (view.getVisibility() == View.VISIBLE) {
	    view.postDelayed(new Runnable() {
		public void run() {
		    Animation an = AnimationUtils.loadAnimation(context,
			    android.R.anim.fade_out);
		    view.clearAnimation();
		    view.setAnimation(an);
		    view.setVisibility(View.INVISIBLE);
		}
	    }, 500);
	}
    }
}
