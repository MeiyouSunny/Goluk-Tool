package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.util.GolukUtils;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 点击用户头像 ，跳转“个人中心”
 * 
 * @author jyf
 */
public class ClickHeadListener implements OnClickListener {
	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;

	public ClickHeadListener(Context context, VideoSquareInfo info) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
	}

	@Override
	public void onClick(View arg0) {
		// 防止重复点击
		if (null != mContext && mContext instanceof BaseActivity) {
			if (!((BaseActivity) mContext).isAllowedClicked()) {
				return;
			}
			((BaseActivity) mContext).setJumpToNext();
		}

		GolukUtils.showToast(mContext, "点击头像 ");

		// Intent intent = new Intent(mContext, VideoDetailActivity.class);
		// intent.putExtra(VideoDetailActivity.VIDEO_ID,
		// mVideoSquareInfo.mVideoEntity.videoid);
		// boolean iscomment = false;
		// if ("1".equals(mVideoSquareInfo.mVideoEntity.iscomment)) {
		// iscomment = true;
		// }
		// intent.putExtra(VideoDetailActivity.VIDEO_ISCAN_COMMENT, iscomment);
		//
		// mContext.startActivity(intent);
	}

}
