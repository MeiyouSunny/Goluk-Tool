package com.mobnote.golukmain.newest;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.videosuqare.CategoryListView;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;
import com.mobnote.util.GolukUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickPraiseListener implements OnClickListener {

	public interface IClickPraiseView{
		public void updateClickPraiseNumber(boolean flag, VideoSquareInfo info);
	}
	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;
	private IClickPraiseView mNewestListView;
	private CategoryListView mCategoryListView = null;

	public ClickPraiseListener(Context context, VideoSquareInfo info, IClickPraiseView view) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
		this.mNewestListView = view;
	}

	public void setCategoryListView(CategoryListView view) {
		mCategoryListView = view;
	}

	@Override
	public void onClick(View arg0) {
		if(GolukUtils.isFastDoubleClick()) {
			return;
		}
		if (!isNetworkConnected()) {
			GolukUtils.showToast(mContext, mContext.getString(R.string.network_error));
			return;
		}

		if("1".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
			mVideoSquareInfo.mVideoEntity.ispraise = "0";
			if (null != mNewestListView) {
				if(mNewestListView instanceof NewestListView) {
					((NewestListView)mNewestListView).sendCancelPraiseRequest(mVideoSquareInfo.mVideoEntity.videoid);
				}
				mNewestListView.updateClickPraiseNumber(true, mVideoSquareInfo);
			} else {
				mCategoryListView.sendCancelPraiseRequest(mVideoSquareInfo.mVideoEntity.videoid);
				mCategoryListView.updateClickPraiseNumber(true, mVideoSquareInfo);
			}
		} else {
			if (null != mNewestListView) {
				if(mNewestListView instanceof NewestListView) {
					((NewestListView)mNewestListView).sendPraiseRequest(mVideoSquareInfo.mVideoEntity.videoid);
				}
				mNewestListView.updateClickPraiseNumber(false, mVideoSquareInfo);
			} else {
				mCategoryListView.sendPraiseRequest(mVideoSquareInfo.mVideoEntity.videoid);
				mCategoryListView.updateClickPraiseNumber(false, mVideoSquareInfo);
			}
		}
	}

	/**
	 * ???????????????????????????
	 * @return
	 * @author xuhw
	 * @date 2015???6???5???
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	}
}
