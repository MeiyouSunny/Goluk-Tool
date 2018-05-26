package com.rd.veuisdk.fragment;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rd.xpk.editor.modal.ImageObject;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.ui.ExtCircleImageView;

/**
 * 滤镜方式二
 */
public class FilterFragmentEx extends BaseFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * 编辑界面控制类对象
	 */
	private IVideoEditorHandler mHlrVideoEditor;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mHlrVideoEditor = (IVideoEditorHandler) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Log.e("onCreateView", this.toString());
		if (null == mRoot) {
			mRoot = inflater.inflate(R.layout.sp_filter_container, null);
			initJLKFilter();

			findViewById(R.id.sp_rl_filter1).setOnClickListener(
					mspFilterClickView);
			findViewById(R.id.sp_rl_filter2).setOnClickListener(
					mspFilterClickView);
			findViewById(R.id.sp_rl_filter3).setOnClickListener(
					mspFilterClickView);
			findViewById(R.id.sp_rl_filter4).setOnClickListener(
					mspFilterClickView);
			findViewById(R.id.sp_rl_filter5).setOnClickListener(
					mspFilterClickView);
			findViewById(R.id.sp_rl_filter6).setOnClickListener(
					mspFilterClickView);
			findViewById(R.id.sp_rl_filter7).setOnClickListener(
					mspFilterClickView);
			findViewById(R.id.sp_rl_filter8).setOnClickListener(
					mspFilterClickView);
			findViewById(R.id.sp_rl_filter9).setOnClickListener(
					mspFilterClickView);
			findViewById(R.id.sp_rl_filter10).setOnClickListener(
					mspFilterClickView);
		}
		return mRoot;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		// Log.e("onActivityCreated", this.toString());
		if (null != mCheckedView && mCheckedView.getId() != R.id.sp_rl_filter1) {
			resetCheckedItem();
		}
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		mRoot = null;
		// Log.e("onDestroyView", this.toString());
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// Log.e("onDestroy", this.toString());
	}

	/**
	 * 记录被选中的滤镜样式列表
	 */
	private View mCheckedView;

	/**
	 * 滤镜列表图标
	 */
	private ExtCircleImageView mFilter1, mFilter2, mFilter3,
			mFilter4, mFilter5, mFilter6, mFilter7, mFilter8,
			mFilter9, mFilter10;
	/**
	 * 滤镜列表文字描述
	 */
	private TextView mTv1, mTv2, mTv3, mTv4, mTv5, mTv6, mTv7, mTv8, mTv9, mTv10;
	private int mNormalColor = 0, mCheckedColor = 0;

	/**
	 * 初始化滤镜布局
	 */
	private void initJLKFilter() {
		mFilter1 = (ExtCircleImageView) findViewById(R.id.sp_iv_filter1);
		mFilter2 = (ExtCircleImageView) findViewById(R.id.sp_iv_filter2);
		mFilter3 = (ExtCircleImageView) findViewById(R.id.sp_iv_filter3);
		mFilter4 = (ExtCircleImageView) findViewById(R.id.sp_iv_filter4);
		mFilter5 = (ExtCircleImageView) findViewById(R.id.sp_iv_filter5);
		mFilter6 = (ExtCircleImageView) findViewById(R.id.sp_iv_filter6);
		mFilter7 = (ExtCircleImageView) findViewById(R.id.sp_iv_filter7);
		mFilter8 = (ExtCircleImageView) findViewById(R.id.sp_iv_filter8);
		mFilter9 = (ExtCircleImageView) findViewById(R.id.sp_iv_filter9);
		mFilter10 = (ExtCircleImageView) findViewById(R.id.sp_iv_filter10);
		mTv1 = ((TextView) findViewById(R.id.sp_tv_filter1));
		mTv2 = ((TextView) findViewById(R.id.sp_tv_filter2));
		mTv3 = ((TextView) findViewById(R.id.sp_tv_filter3));
		mTv4 = ((TextView) findViewById(R.id.sp_tv_filter4));
		mTv5 = ((TextView) findViewById(R.id.sp_tv_filter5));
		mTv6 = ((TextView) findViewById(R.id.sp_tv_filter6));
		mTv7 = ((TextView) findViewById(R.id.sp_tv_filter7));
		mTv8 = ((TextView) findViewById(R.id.sp_tv_filter8));
		mTv9 = ((TextView) findViewById(R.id.sp_tv_filter9));
		mTv10 = ((TextView) findViewById(R.id.sp_tv_filter10));
		Resources res = getResources();
		if (null != res) {
			mNormalColor = res.getColor(R.color.transparent80_white);
			mCheckedColor = res.getColor(R.color.main_orange);
		}

		// 默认选中"原片"滤镜
		mFilter1.setChecked(true);
		mTv1.setTextColor(mCheckedColor);
	}

	/**
	 * 滤镜选择回调
	 */
	private OnClickListener mspFilterClickView = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int index = -1;
			int checkedId = v.getId();
			mCheckedView = v;
			mFilter1.setChecked(false);
			mFilter2.setChecked(false);
			mFilter3.setChecked(false);
			mFilter4.setChecked(false);
			mFilter5.setChecked(false);
			mFilter6.setChecked(false);
			mFilter7.setChecked(false);
			mFilter8.setChecked(false);
			mFilter9.setChecked(false);
			mFilter10.setChecked(false);

			mTv1.setTextColor(mNormalColor);
			mTv2.setTextColor(mNormalColor);
			mTv3.setTextColor(mNormalColor);
			mTv4.setTextColor(mNormalColor);
			mTv5.setTextColor(mNormalColor);
			mTv6.setTextColor(mNormalColor);
			mTv7.setTextColor(mNormalColor);
			mTv8.setTextColor(mNormalColor);
			mTv9.setTextColor(mNormalColor);
			mTv10.setTextColor(mNormalColor);

			if (checkedId == R.id.sp_rl_filter1) {
				index = 40;
				mFilter1.setChecked(true);
				mTv1.setTextColor(mCheckedColor);
			} else if (checkedId == R.id.sp_rl_filter2) {
				index = 41;
				mFilter2.setChecked(true);
				mTv2.setTextColor(mCheckedColor);
			} else if (checkedId == R.id.sp_rl_filter3) {
				index = ImageObject.FILTER_TYPE_GRAY;
				mFilter3.setChecked(true);
				mTv3.setTextColor(mCheckedColor);
			} else if (checkedId == R.id.sp_rl_filter4) {
				index = 43;
				mFilter4.setChecked(true);
				mTv4.setTextColor(mCheckedColor);
			} else if (checkedId == R.id.sp_rl_filter5) {
				index = 44;
				mFilter5.setChecked(true);
				mTv5.setTextColor(mCheckedColor);
			} else if (checkedId == R.id.sp_rl_filter6) {
				index = 45;
				mFilter6.setChecked(true);
				mTv6.setTextColor(mCheckedColor);
			} else if (checkedId == R.id.sp_rl_filter7) {
				index = 46;
				mFilter7.setChecked(true);
				mTv7.setTextColor(mCheckedColor);
			} else if (checkedId == R.id.sp_rl_filter8) {
				index = 47;
				mFilter8.setChecked(true);
				mTv8.setTextColor(mCheckedColor);
			} else if (checkedId == R.id.sp_rl_filter9) {
				index = 48;
				mFilter9.setChecked(true);
				mTv9.setTextColor(mCheckedColor);
			} else if (checkedId == R.id.sp_rl_filter10) {
				index = 49;
				mFilter10.setChecked(true);
				mTv10.setTextColor(mCheckedColor);
			}
			reload(index);
		}
	};

	public static int mCurrentFilterType = 40;

	/**
	 * 通知编辑界面滤镜改变
	 */
	public void reload(int index) {
		mCurrentFilterType = index;
		mHlrVideoEditor.changeFilterType(mCurrentFilterType);
	}

	public void resetCheckedItem() {

		mspFilterClickView.onClick(mCheckedView);

	}

}
