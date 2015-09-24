package cn.com.mobnote.golukmobile.videodetail;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickPraiseListener implements OnClickListener {

	private Context mContext;
	private VideoDetailAdapter mAdapter;
	private VideoJson mVideoJson;

	public ClickPraiseListener(Context mContext, VideoDetailAdapter adapter,VideoJson videoJson) {
		this.mContext = mContext;
		this.mAdapter = adapter;
		this.mVideoJson = videoJson;
	}

	@Override
	public void onClick(View view) {
		if (!UserUtils.isNetDeviceAvailable(mContext)) {
			GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.user_net_unavailable));
		} else {
			String praise = mAdapter.setClickPraise();
			if("0".equals(praise)){
				mAdapter.headHolder.mTextZan.setText(praise);
				mAdapter.headHolder.mTextZan.setVisibility(View.GONE);
				mAdapter.headHolder.mZanImage.setImageResource(R.drawable.videodetail_like);
				mAdapter.headHolder.mTextZan.setTextColor(Color.rgb(136, 136, 136));
				mAdapter.headHolder.mTextZanName.setTextColor(Color.rgb(136, 136, 136));
			}else{
				mAdapter.headHolder.mTextZan.setVisibility(View.VISIBLE);
				mAdapter.headHolder.mTextZan.setText(praise);
				if(mVideoJson.data.avideo.video.ispraise.equals("1")){
					mAdapter.headHolder.mZanImage.setImageResource(R.drawable.videodetail_like_press);
					mAdapter.headHolder.mTextZan.setTextColor(Color.rgb(0x11, 0x63, 0xa2));
					mAdapter.headHolder.mTextZanName.setTextColor(Color.rgb(0x11, 0x63, 0xa2));
				}else{
					mAdapter.headHolder.mZanImage.setImageResource(R.drawable.videodetail_like);
					mAdapter.headHolder.mTextZan.setTextColor(Color.rgb(136, 136, 136));
					mAdapter.headHolder.mTextZanName.setTextColor(Color.rgb(136, 136, 136));
				}
				
			}
			
		}
	}

}
