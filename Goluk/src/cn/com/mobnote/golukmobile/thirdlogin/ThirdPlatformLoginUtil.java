package cn.com.mobnote.golukmobile.thirdlogin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import cn.com.mobnote.util.GolukConfig;
import cn.com.mobnote.util.GolukFileUtils;

import com.alibaba.fastjson.JSON;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.controller.listener.SocializeListeners.UMAuthListener;
import com.umeng.socialize.controller.listener.SocializeListeners.UMDataListener;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.socialize.weixin.controller.UMWXHandler;

public class ThirdPlatformLoginUtil {
	public Context mContext;
	public final UMSocialService mController = UMServiceFactory.getUMSocialService(GolukConfig.LOGIN_DESCRIPTOR);
	private ThirdUserInfoGet mListener;
	
	public ThirdPlatformLoginUtil(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	
	public void setListener(ThirdUserInfoGet listener) {
		mListener = listener;
	}
	/**
     * @功能描述 : 添加微信平台授权登录
     * @return
     */
    public void addWXPlatform() {
        // 注意：在微信授权的时候，必须传递appSecret
 
        // 添加微信平台，APP_ID、APP_SECRET都是在微信开放平台，移动应用通过审核后获取到的
        UMWXHandler wxHandler = new UMWXHandler(mContext, GolukConfig.WX_APPID, GolukConfig.WX_APPSECRET);
 
        wxHandler.setRefreshTokenAvailable(false);
        wxHandler.addToSocialSDK();
 
    }

    /**
     * 授权。如果授权成功，则获取用户信息
     *
     * @param platform
     */
    public void login(SHARE_MEDIA platform) {

        mController.doOauthVerify(mContext, platform,
                new UMAuthListener() {
 
                    @Override
                    public void onStart(SHARE_MEDIA platform) {
                    }
 
                    @Override
                    public void onError(SocializeException e,
                                        SHARE_MEDIA platform) {
                    	if (mListener != null) {
                    		mListener.getUserInfo(false, null, null);
                    	}
                    }
 
                    @Override
                    public void onComplete(Bundle value, SHARE_MEDIA platform) {
                        // 获取uid
                        String uid = value.getString("uid");
                        if (!TextUtils.isEmpty(uid)) {
                            // uid不为空，获取用户信息
                            getUserInfo(platform);
                        } else {
                        	if (mListener != null) {
                        		mListener.getUserInfo(false, null, null);
                        	}
                        }
                    }
 
                    @Override
                    public void onCancel(SHARE_MEDIA platform) {
                    	if (mListener != null) {
                    		mListener.getUserInfo(false, null, null);
                    	}
                    }
                });
    }
 
    /**
     * 获取用户信息
     *
     * @param platform
     */
    public void getUserInfo(SHARE_MEDIA platform) {
        mController.getPlatformInfo(mContext, platform,
                new UMDataListener() {
 
                    @Override
                    public void onStart() {
 
                    }
 
                    @Override
                    public void onComplete(int status, Map<String, Object> info) {
                        if (info != null) {

                            String infoStr = JSON.toJSONString(info);
                            try {
								infoStr = URLEncoder.encode(infoStr, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                        	if (mListener != null) {
                        		mListener.getUserInfo(true, infoStr, "weixin");
                        		GolukFileUtils.saveString(GolukFileUtils.THIRD_USER_INFO, infoStr);
                        		GolukFileUtils.saveString(GolukFileUtils.LOGIN_PLATFORM, "weixin");
                        	}
                        }
                    }

                });
    }
 
    /**
     * 注销本次登陆
     * @param platform
     */
    public void logout(SHARE_MEDIA platform) {
        mController.deleteOauth(mContext, platform,
                new SocializeListeners.SocializeClientListener() {
 
            @Override
            public void onStart() {
 
            }
 
            @Override
            public void onComplete(int status, SocializeEntity entity) {
            }

        });
    }
}
