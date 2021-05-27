package com.mobnote.golukmain.videosuqare;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.mobnote.golukmain.newest.NewestListView;
import com.mobnote.golukmain.newest.WonderfulSelectedListView;
import com.mobnote.util.ZhugeUtils;

import androidx.viewpager.widget.PagerAdapter;
import cn.com.tiros.debug.GolukDebugUtils;

public class VideoSquareAdapter extends PagerAdapter {
	private Context mContext = null;
	private WonderfulSelectedListView mWonderfulSelectedListView = null;
	private NewestListView mNewestListView = null;

	public VideoSquareAdapter(Context c) {
		this.mContext = c;
	}

	public WonderfulSelectedListView getWonderfulSelectedListView() {
		return mWonderfulSelectedListView;
	}

	public NewestListView getNewestListView() {
		return mNewestListView;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		GolukDebugUtils.e("", "jyf----Goluk----OOM----VideoSquareAdapter  positon:" + position);
		if (0 == position) {
			mWonderfulSelectedListView = new WonderfulSelectedListView(mContext);
			container.addView(mWonderfulSelectedListView.getView());
			ZhugeUtils.eventWonderfulPage(mContext);
			return mWonderfulSelectedListView.getView();
		} else {
			mNewestListView = new NewestListView(mContext);
			container.addView(mNewestListView.getView());
			return mNewestListView.getView();
		}
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	public void onDestroy() {
		if (null != mWonderfulSelectedListView) {
			mWonderfulSelectedListView.onDestroy();
		}
		if (null != mNewestListView) {
			mNewestListView.onDestroy();
		}

	}

	public void onResume() {
		if (null != mNewestListView) {
			mNewestListView.onResume();
		}

	}

	public void onPause() {
		if (null != mNewestListView) {
			mNewestListView.onPause();
		}

	}

	public void onStop() {

	}

}
