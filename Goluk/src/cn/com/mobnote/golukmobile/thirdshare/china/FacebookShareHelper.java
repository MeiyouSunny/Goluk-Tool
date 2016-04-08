package cn.com.mobnote.golukmobile.thirdshare.china;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.util.GolukConfig;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.widget.ShareDialog;

public class FacebookShareHelper {
	public CallbackManager mCallbackManager;
	public ShareDialog mShareDialog;

	private volatile static FacebookShareHelper mInstance;

	private FacebookShareHelper() {
		FacebookSdk.sdkInitialize(GolukApplication.getInstance(), GolukConfig.REQUEST_CODE_FACEBOOK_SHARE);
		mCallbackManager = CallbackManager.Factory.create();
	}

	public static FacebookShareHelper getInstance() {
		if (mInstance == null) {
			synchronized (FacebookShareHelper.class) {
				if (mInstance == null) {
					mInstance = new FacebookShareHelper();
				}
			}
		}
		return mInstance;
	}

}
