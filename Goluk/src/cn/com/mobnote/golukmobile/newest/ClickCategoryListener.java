package cn.com.mobnote.golukmobile.newest;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickCategoryListener implements OnClickListener{
	private CategoryDataInfo mCategoryDataInfo;
	private Context mContext;
	
	public ClickCategoryListener(Context context, CategoryDataInfo info) {
		this.mCategoryDataInfo = info;
		this.mContext = context;
	}

	@Override
	public void onClick(View arg0) {
		Intent intent = new Intent(mContext, VideoCategoryListActivity.class);
		intent.putExtra("title", mCategoryDataInfo.name);
		intent.putExtra("id", mCategoryDataInfo.id);
		mContext.startActivity(intent);
	}
	
}