package com.mobnote.golukmain.thirdlogin;

import java.net.URLEncoder;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.widget.Toast;
import cn.com.tiros.debug.GolukDebugUtils;

import com.mobnote.golukmain.R;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

public class ThirdPlatformLoginUtil {
	private static final String TAG = "ThirdPlatformLoginUtil";

	public Activity mContext;
	private ThirdUserInfoGet mListener;

	private UMShareAPI mShareAPI = null;

	public ThirdPlatformLoginUtil(Activity context) {

		mContext = context;
		mShareAPI = UMShareAPI.get(mContext);
	}

	public void setListener(ThirdUserInfoGet listener) {
		mListener = listener;
	}

	/**
	 * 授权。如果授权成功，则获取用户信息
	 *
	 * @param platform
	 */
	public void login(SHARE_MEDIA platform) {
		GolukDebugUtils.e("", "three login--------click");
		if (mShareAPI.isInstall(mContext, platform)) {
			mShareAPI.doOauthVerify(mContext, platform, umAuthListener);
		} else {
			GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.str_weixin_no_install));
		}
	}

	private UMAuthListener umAuthListener = new UMAuthListener() {
		@Override
		public void onStart(SHARE_MEDIA share_media) {

		}

		@Override
		public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
			GolukDebugUtils.e("", "three login------umAuthListener--onComplete  platform:" + platform + "  action:"
					+ action + "  data: " + data);
			if (action == 0) {
				mShareAPI.getPlatformInfo(mContext, SHARE_MEDIA.WEIXIN, umAuthListener);
			} else if (action == 2) {
				readThreeUserInfo(data);
			}
		}

		@Override
		public void onError(SHARE_MEDIA platform, int action, Throwable t) {
			Toast.makeText(mContext.getApplicationContext(),  mContext.getResources().getString(R.string.str_authorize_fail), Toast.LENGTH_SHORT).show();
			if (mListener != null) {
				mListener.getUserInfo(false, null, null);
			}
			GolukDebugUtils.e("", "youmeng----goluk----SharePlatformUtil----umAuthListener----onError");
		}

		@Override
		public void onCancel(SHARE_MEDIA platform, int action) {
			Toast.makeText(mContext.getApplicationContext(), mContext.getResources().getString(R.string.str_authorize_cancel), Toast.LENGTH_SHORT).show();
			GolukDebugUtils.e("", "youmeng----goluk----SharePlatformUtil----umAuthListener----onCancel");
			if (mListener != null) {
				mListener.getUserInfo(false, null, null);
			}
		}
	};

	private void readThreeUserInfo(Map<String, String> data) {
		try {
			String jsonData = new JSONObject(data).toString();
			GolukDebugUtils.e("", "three login------umAuthListener--onComplete  jsonData:" + jsonData);
			String infoStr = URLEncoder.encode(jsonData, "utf-8");
			if (mListener != null) {
				mListener.getUserInfo(true, infoStr, "weixin");
				GolukFileUtils.saveString(GolukFileUtils.THIRD_USER_INFO, infoStr);
				GolukFileUtils.saveString(GolukFileUtils.LOGIN_PLATFORM, "weixin");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 注销本次登陆
	 * 
	 * @param platform
	 */
	public void logout(SHARE_MEDIA platform) {
		mShareAPI.deleteOauth(mContext, platform, umAuthListener);

	}
}
