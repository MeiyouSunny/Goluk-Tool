package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.videosuqare.CategoryListView;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickPraiseListener implements OnClickListener{
	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;
	private NewestListView mNewestListView;
	private CategoryListView mCategoryListView = null;
	
	public ClickPraiseListener(Context context, VideoSquareInfo info, NewestListView view) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
		this.mNewestListView = view;
	}
	
	public void setCategoryListView(CategoryListView view) {
		mCategoryListView = view;
	}

	@Override
	public void onClick(View arg0) {
		if("1".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
			mVideoSquareInfo.mVideoEntity.ispraise = "0";
			if (null != mNewestListView) {
				mNewestListView.updateClickPraiseNumber(true, mVideoSquareInfo);
			} else {
				mCategoryListView.updateClickPraiseNumber(true, mVideoSquareInfo);
			}
			
		}else {
			GolukApplication.getInstance().getVideoSquareManager().
			clickPraise("1", mVideoSquareInfo.mVideoEntity.videoid, "1");
			
			if (null != mNewestListView) {
				mNewestListView.updateClickPraiseNumber(false, mVideoSquareInfo);
			} else {
				mCategoryListView.updateClickPraiseNumber(false, mVideoSquareInfo);
			}
			
			
		}
	}
	
}
