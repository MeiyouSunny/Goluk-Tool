package com.rd.veuisdk.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.rd.lib.utils.CoreUtils;

/**
 * 屏蔽多点线性布局
 * 
 * @author abreal
 * 
 */
public class NoneMultiTouchLinearLayout extends LinearLayout {
	// private static final String TAG = "NoneMultiTouchLinearLayout";

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public NoneMultiTouchLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (!isInEditMode() && CoreUtils.hasHoneycomb()) {
			this.setMotionEventSplittingEnabled(false);
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if (CoreUtils.hasHoneycomb()) {
			for (int nTmpIndex = 0; nTmpIndex < this.getChildCount(); nTmpIndex++) {
				View v = this.getChildAt(nTmpIndex);
				if (v instanceof ViewGroup) {
					doChangeSplittingSubView((ViewGroup) v);
				}
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void doChangeSplittingSubView(ViewGroup vg) {
		vg.setMotionEventSplittingEnabled(this.isMotionEventSplittingEnabled());
		for (int nTmpIndex = 0; nTmpIndex < vg.getChildCount(); nTmpIndex++) {
			View v = vg.getChildAt(nTmpIndex);
			if (v instanceof ViewGroup) {
				// 递归调用设置多点支持
				doChangeSplittingSubView((ViewGroup) v);
			}
		}
	}
}
