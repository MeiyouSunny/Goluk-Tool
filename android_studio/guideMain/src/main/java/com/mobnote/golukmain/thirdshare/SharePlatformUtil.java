package com.mobnote.golukmain.thirdshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;
import com.mobnote.util.GolukUtils;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import cn.com.tiros.debug.GolukDebugUtils;

public class SharePlatformUtil {
    public Context mContext;
    public VideoSquareInfo mData = null;
    private UMShareAPI mShareAPI = null;

    public SharePlatformUtil(Context context) {
        mContext = context;
        mShareAPI = UMShareAPI.get(mContext);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        GolukDebugUtils.e("", "jyf----thirdshare--------SharePlatformUtil----onActivityResult: " + "   requestCode:"
                + requestCode + "   resultCode:" + resultCode);
        mShareAPI.onActivityResult(requestCode, resultCode, data);
        // SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResults
    }

    public void setShareData(VideoSquareInfo data) {
        mData = data;
    }

    public boolean isInstallPlatform(SHARE_MEDIA platform) {
        return mShareAPI != null && mShareAPI.isInstall((Activity) mContext, platform);
//        if (null == mShareAPI) {
//            return false;
//        }
//        try {
//            UMSSOHandler handler = mShareAPI.getHandler(platform);
//            boolean isinstall = handler.isInstall();
//            return null == handler || handler.isInstall();
//        }catch (Exception ex) {
//            return true;
//        }
    }

    public boolean isSinaWBValid() {
        return true;
    }

    public static boolean checkShareableWhenNotHotspot(Context context) {
        GolukApplication app = (GolukApplication) context.getApplicationContext();
        if (app.getEnableSingleWifi() && app.isIpcLoginSuccess) {
            GolukUtils.showToast(context, context.getResources().getString(R.string.disconnect_goluk_before_share));
            return false;
        }
        return true;
    }

}
