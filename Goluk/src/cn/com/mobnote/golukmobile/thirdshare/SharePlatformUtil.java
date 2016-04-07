package cn.com.mobnote.golukmobile.thirdshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.tiros.debug.GolukDebugUtils;

import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

public class SharePlatformUtil {
	public Context mContext;
	public SinaWeiBoUtils mSinaWBUtils = null;
	public VideoSquareInfo mData = null;
	private UMShareAPI mShareAPI = null;

	public SharePlatformUtil(Context context) {
		mContext = context;
		mSinaWBUtils = new SinaWeiBoUtils((Activity) mContext);
		mShareAPI = UMShareAPI.get(mContext);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		GolukDebugUtils.e("", "jyf----thirdshare--------SharePlatformUtil----onActivityResult: " + "   requestCode:"
				+ requestCode + "   resultCode:" + resultCode);
		mShareAPI.onActivityResult(requestCode, resultCode, data);
		// SSO 授权回调
		// 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResults
		if (mSinaWBUtils != null) {
			mSinaWBUtils.onActivityResult(requestCode, resultCode, data);
		}
	}

	public void setShareData(VideoSquareInfo data) {
		mData = data;
	}

	public boolean isInstallPlatform(SHARE_MEDIA platform) {
		return mShareAPI.getHandler(platform).isInstall(mContext);
	}

	public boolean isSinaWBValid() {
		return mSinaWBUtils.isAccessValid();
	}

}
