package cn.com.mobnote.golukmobile.videodetail;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserOpenUrlActivity;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickLinkListener implements OnClickListener {

	private Context mContext;
	private VideoJson mVideoJson;
	private VideoDetailAdapter mAdapter;
	public ClickLinkListener(Context context,VideoJson videoJson,VideoDetailAdapter adapter) {
		this.mContext = context;
		this.mVideoJson = videoJson;
		this.mAdapter = adapter;
	}
	
	@Override
	public void onClick(View arg0) {
		if (!UserUtils.isNetDeviceAvailable(mContext)) {
			GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.user_net_unavailable));
		} else {
			if ("1".equals(mVideoJson.data.link.showurl)) {
				Intent mLinkIntent = new Intent(mContext, UserOpenUrlActivity.class);
				mLinkIntent.putExtra("url", mVideoJson.data.link.outurl);
				mContext.startActivity(mLinkIntent);
				mAdapter.cancleTimer();
			}
		}
	}

}
