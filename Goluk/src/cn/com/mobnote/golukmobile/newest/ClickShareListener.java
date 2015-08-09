package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.videosuqare.CategoryListView;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.util.GolukUtils;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickShareListener implements OnClickListener {
	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;
	private NewestListView mNewestListView;
	private CategoryListView mCategoryListView = null;

	public ClickShareListener(Context context, VideoSquareInfo info, NewestListView view) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
		this.mNewestListView = view;
	}

	public void setCategoryListView(CategoryListView view) {
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
	}

	@Override
	public void onClick(View arg0) {
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
			closeDialog();
			GolukUtils.showToast(mContext, "网络异常，请检查网络");
		} else {
			saveCategoryData();
		}
	}

}
