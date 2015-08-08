package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.util.GolukUtils;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickShareListener implements OnClickListener{
	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;
	private NewestListView mNewestListView;
	
	public ClickShareListener(Context context, VideoSquareInfo info, NewestListView view) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
		this.mNewestListView = view;
	}

	@Override
	public void onClick(View arg0) {
		if("1".equals(mVideoSquareInfo.mVideoEntity.type)) {
			
		}else {
			mNewestListView.showProgressDialog();
		}
		boolean result = GolukApplication.getInstance().getVideoSquareManager()
				.getShareUrl(mVideoSquareInfo.mVideoEntity.videoid, mVideoSquareInfo.mVideoEntity.type);
		if (!result) {
			mNewestListView.closeProgressDialog();
			GolukUtils.showToast(mContext, "网络异常，请检查网络");
		}
	}
	
}
