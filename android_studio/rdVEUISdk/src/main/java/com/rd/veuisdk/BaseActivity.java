package com.rd.veuisdk;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.veuisdk.utils.SysAlertDialog;

import java.util.HashMap;

public abstract class BaseActivity extends FragmentActivity {
	/**
	 * 获取统计页名称
	 *
	 * @return
	 */
	protected String mStrActivityPageName = "baseActivity";
	private HashMap<String, Object> mObjectByTag = new HashMap<String, Object>();

	public Object getObjectByTag(String tag) {
		return mObjectByTag.get(tag);
	}

	public void setObjectByTag(String key, Object object) {
		this.mObjectByTag.put(key, object);
	}

	/**
	 * 通用响应Click事件
	 *
	 * @param v
	 */
	public void clickView(View v) {

	}

	/**
	 * 文本框设置文本
	 *
	 * @param nTextViewId
	 *            文本框资源Id
	 * @param strText
	 *            文本
	 */
	protected void setText(int nTextViewId, String strText) {
		((TextView) findViewById(nTextViewId)).setText(strText);
	}

	/**
	 * 设置文本
	 *
	 * @param nTextViewId
	 * @param strId
	 */

	protected void setText(int nTextViewId, int strId) {
		((TextView) findViewById(nTextViewId)).setText(strId);
	}

	/**
	 * 设置View是否显示
	 *
	 * @param nViewId
	 * @param bVisiable
	 */
	public void setViewVisibility(int nViewId, boolean bVisiable) {
		setViewVisibility(nViewId, bVisiable ? View.VISIBLE : View.GONE, 0);
	}

	/**
	 * 设置图片资源
	 *
	 * @param nImageViewId
	 *            图片View资源Id
	 * @param nImageResId
	 *            图片资源Id(drawable)
	 */
	protected void setImageViewSrc(int nImageViewId, int nImageResId) {
		((ImageView) findViewById(nImageViewId)).setImageResource(nImageResId);
	}

	/**
	 * 文本框设置文本
	 *
	 * @param v
	 *            fragment
	 * @param nTextViewId
	 * @param strText
	 */
	protected void setText(View v, int nTextViewId, String strText) {
		((TextView) v.findViewById(nTextViewId)).setText(strText);
	}

	/**
	 * 设置fragment中的控件
	 *
	 * @param parent
	 * @param nViewId
	 * @param bVisiable
	 */
	protected void setViewVisibility(View parent, int nViewId, boolean bVisiable) {
		setViewVisibility(parent, nViewId, bVisiable, 0);
	}

	/**
	 * 设置fragment中的控件可见
	 *
	 * @param parent
	 * @param nViewId
	 * @param bVisiable
	 * @param nAnimationResId
	 */

	protected void setViewVisibility(View parent, int nViewId,
			boolean bVisiable, int nAnimationResId) {
		View v = parent.findViewById(nViewId);
		int nSetVisibility = bVisiable ? View.VISIBLE : View.GONE;
		v.clearAnimation();
		if (nAnimationResId > 0 && v.getVisibility() != nSetVisibility) {
			v.setAnimation(AnimationUtils.loadAnimation(this, nAnimationResId));
		}
		v.setVisibility(nSetVisibility);
	}

	/**
	 * 设置View是否显示
	 *
	 * @param nViewId
	 *            View id
	 * @param visibility
	 *            是否显示
	 */
	protected void setViewVisibility(int nViewId, int visibility,
			int nAnimationResId) {
		View v = this.findViewById(nViewId);
		if (null != v) {
			v.clearAnimation();
			if (nAnimationResId > 0 && v.getVisibility() != visibility) {
				v.setAnimation(AnimationUtils.loadAnimation(this,
						nAnimationResId));
			}
			v.setVisibility(visibility);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.gc();
	}

	void onToast(String strMessage) {
		SysAlertDialog.showAutoHideDialog(this, null, strMessage,
				Toast.LENGTH_SHORT);
	}

}
