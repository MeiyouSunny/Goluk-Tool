package cn.com.mobnote.golukmobile.newest;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import cn.com.mobnote.golukmobile.videosuqare.VideoCategoryActivity;

public class ClickCategoryListener implements OnClickListener {
	private CategoryDataInfo mCategoryDataInfo;
	private Context mContext;

	public ClickCategoryListener(Context context, CategoryDataInfo info) {
		this.mCategoryDataInfo = info;
		this.mContext = context;
	}

	@Override
	public void onClick(View arg0) {
		// 跳转到点播
		Intent intent = new Intent(mContext, VideoCategoryActivity.class);
		intent.putExtra(VideoCategoryActivity.KEY_VIDEO_CATEGORY_TYPE, VideoCategoryActivity.CATEGORY_TYPE_DB);
		intent.putExtra(VideoCategoryActivity.KEY_VIDEO_CATEGORY_ATTRIBUTE, mCategoryDataInfo.id);
		intent.putExtra(VideoCategoryActivity.KEY_VIDEO_CATEGORY_TITLE, mCategoryDataInfo.name);
		mContext.startActivity(intent);
	}

}