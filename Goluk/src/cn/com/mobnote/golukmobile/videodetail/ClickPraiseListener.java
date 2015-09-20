package cn.com.mobnote.golukmobile.videodetail;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickPraiseListener implements OnClickListener {

	private Context mContext;
	private VideoDetailAdapter mAdapter;

	public ClickPraiseListener(Context mContext, VideoDetailAdapter adapter) {
		this.mContext = mContext;
		this.mAdapter = adapter;
	}

	@Override
	public void onClick(View view) {
		if (!UserUtils.isNetDeviceAvailable(mContext)) {
			GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.user_net_unavailable));
		} else {
			mAdapter.clickPraise();
		}
	}

}
