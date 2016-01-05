package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.util.GolukUtils;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickShareListener implements OnClickListener {
	public interface IClickShareView {
		public void showProgressDialog();

		public void closeProgressDialog();

		public void setWillShareInfo(VideoSquareInfo info);
	}

	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;
	private IClickShareView mNewestListView;
	private IClickShareView mCategoryListView = null;

	public ClickShareListener(Context context, VideoSquareInfo info, IClickShareView view) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
		this.mNewestListView = view;
	}

	public void setCategoryListView(IClickShareView view) {
		mCategoryListView = view;
	}

	private void showDialog() {
		if (null != mNewestListView) {
			mNewestListView.showProgressDialog();
		} else {
			mCategoryListView.showProgressDialog();
		}
	}

	private void closeDialog() {
		if (null != mNewestListView) {
			mNewestListView.closeProgressDialog();
		} else {
			mCategoryListView.closeProgressDialog();
		}
	}

	private void saveCategoryData() {
		if (null != mCategoryListView) {
			mCategoryListView.setWillShareInfo(mVideoSquareInfo);
		}

		if (null != mNewestListView) {
			mNewestListView.setWillShareInfo(mVideoSquareInfo);
		}
	}

	@Override
	public void onClick(View arg0) {
		if (!isCanClick()) {
			// 防止重复点击
			return;
		}
		if ("1".equals(mVideoSquareInfo.mVideoEntity.type)) {
			// 直播分享
			showDialog();
		} else {
			// 点播分享
			showDialog();
		}
		boolean result = GolukApplication.getInstance().getVideoSquareManager()
				.getShareUrl(mVideoSquareInfo.mVideoEntity.videoid, mVideoSquareInfo.mVideoEntity.type);
		if (!result) {
			GolukUtils.cancelTimer();
			GolukUtils.isCanClick = true;
			closeDialog();
			GolukUtils.showToast(mContext, "网络异常，请检查网络");
		} else {
			saveCategoryData();
		}
	}

	/**
	 * 防止重复点击
	 * 
	 * @return
	 * @author jyf
	 */
	public boolean isCanClick() {
		if (GolukUtils.isCanClick) {
			GolukUtils.startTimer(2000);
			return true;
		}

		return false;
	}

}
