package cn.com.mobnote.golukmobile.videodetail;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

public class ClickShareListener implements OnClickListener {

	private Context mContext;
	private VideoJson mVideoJson;
	private VideoDetailAdapter mAdapter;
	
	public ClickShareListener(Context context,VideoJson videoJson,VideoDetailAdapter adapter) {
		this.mContext = context;
		this.mVideoJson = videoJson;
		this.mAdapter = adapter;
	}
	
	@Override
	public void onClick(View arg0) {
		boolean result = GolukApplication.getInstance().getVideoSquareManager()
				.getShareUrl(mVideoJson.data.avideo.video.videoid, mVideoJson.data.avideo.video.type);
		GolukDebugUtils.i("detail", "--------result-----Onclick------" + result);
		if (!result) {
			GolukUtils.showToast(mContext, "网络异常，请检查网络");
		} else {
			mAdapter.showLoadingDialog();
		}
	}
	
}
