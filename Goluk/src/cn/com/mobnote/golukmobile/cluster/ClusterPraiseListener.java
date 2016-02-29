package cn.com.mobnote.golukmobile.cluster;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.util.GolukUtils;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.View.OnClickListener;

public class ClusterPraiseListener implements OnClickListener {

	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;
	
	public ClusterPraiseListener(Context context, VideoSquareInfo info) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
	}

	@Override
	public void onClick(View arg0) {
		if (!isNetworkConnected()) {
			GolukUtils.showToast(mContext, mContext.getString(R.string.network_error));
			return;
		}

		if("1".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
			mVideoSquareInfo.mVideoEntity.ispraise = "0";
			((ClusterActivity)mContext).sendCancelPraiseRequest(mVideoSquareInfo.mVideoEntity.videoid);
			((ClusterActivity)mContext).updateClickPraiseNumber(true, mVideoSquareInfo);
		}else {
//			GolukApplication.getInstance().getVideoSquareManager().
//			clickPraise("1", mVideoSquareInfo.mVideoEntity.videoid, "1");
			((ClusterActivity)mContext).sendPraiseRequest(mVideoSquareInfo.mVideoEntity.videoid);
			((ClusterActivity)mContext).updateClickPraiseNumber(false, mVideoSquareInfo);
		}
	}
	
	/**
	 * 检查是否有可用网络
	 * @return
	 * @author xuhw
	 * @date 2015年6月5日
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	} 
	
}

