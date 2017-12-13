package com.mobnote.golukmain.thirdshare;

import com.mobnote.application.GolukApplication;
import com.mobnote.util.GolukConfig;
import com.umeng.facebook.CallbackManager;
import com.umeng.facebook.FacebookSdk;
import com.umeng.facebook.share.widget.ShareDialog;

public class FacebookShareHelper {
	public CallbackManager mCallbackManager;
	public ShareDialog mShareDialog;

	private volatile static FacebookShareHelper mInstance;

	private FacebookShareHelper() {
		if(FacebookSdk.isInitialized() == false) {
			FacebookSdk.sdkInitialize(GolukApplication.getInstance());
		}
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
