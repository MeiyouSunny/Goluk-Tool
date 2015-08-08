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
		Intent intent = new Intent(mContext, VideoCategoryActivity.class);
		intent.putExtra("type", "2");
		intent.putExtra("attribute", "1");
		intent.putExtra("title", mCategoryDataInfo.name);
		intent.putExtra("id", mCategoryDataInfo.id);
		mContext.startActivity(intent);
	}

}