package cn.com.mobnote.golukmobile.photoalbum;

import java.util.List;

import cn.com.mobnote.golukmobile.promotion.PromotionSelectItem;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class LocalPagerAdapter extends PagerAdapter {
	private Context mContext = null;
	private LocalVideoManager mLocalVideoListView = null;

	private LocalWonderfulVideoListView mWonderfulVideoLiseView = null;
	private LocalWonderfulVideoListView mEmergencyVideoLiseView = null;
	private LocalWonderfulVideoListView mLoopVideoLiseView = null;
	private String from = null;
	private PromotionSelectItem mPromotionSelectItem;

	public LocalPagerAdapter(Context c, LocalVideoManager localVideoListView, String from, PromotionSelectItem item) {
		this.mContext = c;
		mLocalVideoListView = localVideoListView;
		this.from = from;
		mPromotionSelectItem = item;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		// CK Start
//		if (0 == position) {
//			mWonderfulVideoLiseView = new LocalWonderfulVideoListView(mContext, mLocalVideoListView,
//					IPCManagerFn.TYPE_SHORTCUT, from, mPromotionSelectItem);
//			container.addView(mWonderfulVideoLiseView.getRootView());
//			return mWonderfulVideoLiseView.getRootView();
//		} else if (1 == position) {
//			mEmergencyVideoLiseView = new LocalWonderfulVideoListView(mContext, mLocalVideoListView,
//					IPCManagerFn.TYPE_URGENT, from, mPromotionSelectItem);
//			container.addView(mEmergencyVideoLiseView.getRootView());
//			return mEmergencyVideoLiseView.getRootView();
//		} else {
//			mLoopVideoLiseView = new LocalWonderfulVideoListView(mContext, mLocalVideoListView, IPCManagerFn.TYPE_CIRCULATE,
//					from, mPromotionSelectItem);
//			container.addView(mLoopVideoLiseView.getRootView());
//			return mLoopVideoLiseView.getRootView();
//		}
		// CK End
		return null;
	}

	public boolean isWonderfulHasData() {
		if (null == mWonderfulVideoLiseView) {
			return false;
		}
		return mWonderfulVideoLiseView.isHasData();
	}

	public boolean isEmergencyHasData() {
		if (null == mEmergencyVideoLiseView) {
			return false;
		}
		return mEmergencyVideoLiseView.isHasData();
	}

	public boolean isLoopHasData() {
		if (null == mLoopVideoLiseView) {
			return false;
		}
		return mLoopVideoLiseView.isHasData();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		if (1 == position) {
			container.removeView(mWonderfulVideoLiseView.getRootView());
		} else if (0 == position) {
			container.removeView(mEmergencyVideoLiseView.getRootView());
		} else {
			container.removeView(mLoopVideoLiseView.getRootView());
		}
	}

	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	public void flush(int type) {
		if (IPCManagerFn.TYPE_SHORTCUT == type) {
			mWonderfulVideoLiseView.flushList();
		} else if (IPCManagerFn.TYPE_URGENT == type) {
			mEmergencyVideoLiseView.flushList();
		} else {
			mLoopVideoLiseView.flushList();
		}
	}

	public void deleteDataFlush(int type, List<String> deleteData) {
		if (IPCManagerFn.TYPE_SHORTCUT == type) {
			mWonderfulVideoLiseView.deleteListData(deleteData);
		} else if (IPCManagerFn.TYPE_URGENT == type) {
			mEmergencyVideoLiseView.deleteListData(deleteData);
		} else {
			mLoopVideoLiseView.deleteListData(deleteData);
		}
	}

	public void updateData(int type) {
		if (0 == type) {
			mWonderfulVideoLiseView.updateData();
		} else if (1 == type) {
			mEmergencyVideoLiseView.updateData();
		} else {
			mLoopVideoLiseView.updateData();
		}
	}

	public void onResume() {
		if (null != mWonderfulVideoLiseView) {
			mWonderfulVideoLiseView.onResume();
		}

		if (null != mEmergencyVideoLiseView) {
			mEmergencyVideoLiseView.onResume();
		}

		if (null != mLoopVideoLiseView) {
			mLoopVideoLiseView.onResume();
		}

	}

	public void onDestroy() {

	}

}
