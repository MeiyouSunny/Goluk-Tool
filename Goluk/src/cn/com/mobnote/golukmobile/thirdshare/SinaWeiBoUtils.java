package cn.com.mobnote.golukmobile.thirdshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.VideoObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.utils.Utility;

public class SinaWeiBoUtils implements WeiboAuthListener, IWeiboHandler.Response {

	private static final String PREFERENCES_NAME = "cm_com_mobnote_weibo_android";
	private static final String KEY_UID = "uid";
	private static final String KEY_ACCESS_TOKEN = "access_token";
	private static final String KEY_EXPIRES_IN = "expires_in";
	private static final String KEY_REFRESH_TOKEN = "refresh_token";

	/** 视频播放测试地址 */
	public static final String TEST_VIDEO_URL = "http://www.meipai.com/media/372071608";
	public static final String TEST_VIDEO_URL2 = "http://video.sina.com.cn/p/sports/cba/v/2013-10-22/144463050817.html";

	private Activity mActivity = null;

	/** 授权对象，注意：SsoHandler 仅当 SDK 支持 SSO 时有效 (微博3.0.0以上版本支持) */
	private SsoHandler mSsoHandler;
	private AuthInfo mAuthInfo;
	/** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能 */
	private Oauth2AccessToken mAccessToken;
	/** 微博分享的接口实例 */
	private IWeiboShareAPI mWeiboShareAPI = null;

	public SinaWeiBoUtils(Activity activity) {
		mActivity = activity;
		initAuth();
		initWeiboShare();
	}

	/**
	 * 初始化新浪授权接口
	 * 
	 * @author jyf
	 * @date 2015年7月20日
	 */
	private void initAuth() {
		mAuthInfo = new AuthInfo(mActivity, SinaWeiBoConstants.APP_KEY, SinaWeiBoConstants.REDIRECT_URL,
				SinaWeiBoConstants.SCOPE);
		mSsoHandler = new SsoHandler(mActivity, mAuthInfo);
	}

	/**
	 * 初始化微博分享接口
	 * 
	 * @author jyf
	 * @date 2015年7月20日
	 */
	private void initWeiboShare() {
		// 创建微博 SDK 接口实例
		mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(mActivity, SinaWeiBoConstants.APP_KEY);
		boolean isSucess = mWeiboShareAPI.registerApp();
		GolukDebugUtils.e("", "SinaWeiBoUtils-----------------------------------register:" + isSucess);
	}

	/**
	 * 判断是否安装了新浪微博客户端
	 * 
	 * @return
	 * @author jyf
	 * @date 2015年7月20日
	 */
	public boolean isInstallClient() {
		return mWeiboShareAPI.isWeiboAppInstalled();
	}

	public int getSupportAPI() {
		return mWeiboShareAPI.getWeiboAppSupportAPI();
	}

	/**
	 * 判断当前授权是否有效
	 * 
	 * @return true/false 有效／无效
	 * @author jyf
	 * @date 2015年7月20日
	 */
	public boolean isAccessValid() {
		mAccessToken = readAccessToken(mActivity);
		return mAccessToken.isSessionValid();
	}

	public void sendSingleMessage(String txt, final String title, final String dec, final String actionUrl,
			final String dataUrl, Bitmap bitmap) {

		// 1. 初始化微博的分享消息
		// 用户可以分享文本、图片、网页、音乐、视频中的一种
		WeiboMessage weiboMessage = new WeiboMessage();
		weiboMessage.mediaObject = getVideoObj(title, dec, actionUrl, dataUrl, bitmap);
		// 2. 初始化从第三方到微博的消息请求
		SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
		// 用transaction唯一标识一个请求
		request.transaction = String.valueOf(System.currentTimeMillis());
		request.message = weiboMessage;

		// 3. 发送请求消息到微博，唤起微博分享界面
		mWeiboShareAPI.sendRequest(mActivity, request);
	}

	public void sendMessage(String txt, final String title, final String dec, final String actionUrl,
			final String dataUrl, Bitmap bitmap, boolean isClient) {
		GolukDebugUtils.e("", "sina-------click----AAAAAAA:  " + mWeiboShareAPI.isWeiboAppSupportAPI());
		GolukDebugUtils.e("", "sina-------click----BBBBBB:  ");

		WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
		weiboMessage.textObject = getTextObj(txt);
		weiboMessage.mediaObject = getVideoObj(title, dec, actionUrl, dataUrl, bitmap);

		// 2. 初始化从第三方到微博的消息请求
		SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
		// 用transaction唯一标识一个请求
		request.transaction = String.valueOf(System.currentTimeMillis());
		request.multiMessage = weiboMessage;
		// 3. 发送请求消息到微博，唤起微博分享界面

		GolukDebugUtils.e("", "sina-------click----CCCCCC:  ");

		mWeiboShareAPI.registerApp();

		if (isClient) {
			GolukDebugUtils.e("", "sina-------click----DDDDD:  ");
			mWeiboShareAPI.sendRequest(mActivity, request);
		} else {
			GolukDebugUtils.e("", "sina-------click----EEEEEE:  ");
			AuthInfo authInfo = new AuthInfo(mActivity, SinaWeiBoConstants.APP_KEY, SinaWeiBoConstants.REDIRECT_URL,
					SinaWeiBoConstants.SCOPE);
			Oauth2AccessToken accessToken = readAccessToken(mActivity);
			String token = "";
			if (accessToken != null) {
				token = accessToken.getToken();
			}
			mWeiboShareAPI.sendRequest(mActivity, request, authInfo, token, new WeiboAuthListener() {

				@Override
				public void onWeiboException(WeiboException arg0) {
				}

				@Override
				public void onComplete(Bundle bundle) {
					Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
					writeAccessToken(mActivity, newToken);
				}

				@Override
				public void onCancel() {
				}
			});
		}
	}

	/**
	 * 创建文本消息对象。
	 * 
	 * @return 文本消息对象。
	 */
	private TextObject getTextObj(String text) {
		TextObject textObject = new TextObject();
		textObject.text = text;
		return textObject;
	}

	// 获取分享视频信息
	private VideoObject getVideoObj(final String title, final String dec, final String actionUrl, final String dataUrl,
			Bitmap bitmap) {
		// 创建媒体消息
		VideoObject videoObject = new VideoObject();
		videoObject.identify = Utility.generateGUID();
		videoObject.title = title;
		videoObject.description = dec;
		// 设置 Bitmap 类型的图片到视频对象里
		if (null != bitmap) {
			videoObject.setThumbImage(bitmap);
		} else {
			videoObject.setThumbImage(getDefaultShareBitmap());
		}

		videoObject.actionUrl = actionUrl;
		videoObject.dataUrl = dataUrl;
		videoObject.dataHdUrl = dataUrl;
		videoObject.duration = 10;
		videoObject.defaultText = "Vedio 默认文案";
		return videoObject;
	}

	public Bitmap getDefaultShareBitmap() {
		return BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.tacitly_pic);
	}

	// SSO 授权, 仅客户端
	public void authorizeClientSso() {
		mSsoHandler.authorizeClientSso(this);
	}

	// SSO 授权, 仅Web
	public void authorizeWeb() {
		mSsoHandler.authorize(this);
	}

	// SSO 授权, ALL IN ONE 如果手机安装了微博客户端则使用客户端授权,没有则进行网页授权
	public void authorize() {
		mSsoHandler.authorize(this);
	}

	@Override
	public void onCancel() {

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// SSO 授权回调
		// 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResults
		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}

		// if (765 == requestCode && 0 == resultCode) {
		// // 新浪微博分享成功
		// GolukUtils.showToast(mActivity, "分享成功");
		// }

	}

	/**
	 * 从 SharedPreferences 读取 Token 信息。
	 * 
	 * @param context
	 *            应用程序上下文环境
	 * 
	 * @return 返回 Token 对象
	 */
	public static Oauth2AccessToken readAccessToken(Context context) {
		if (null == context) {
			return null;
		}

		Oauth2AccessToken token = new Oauth2AccessToken();
		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
		token.setUid(pref.getString(KEY_UID, ""));
		token.setToken(pref.getString(KEY_ACCESS_TOKEN, ""));
		token.setRefreshToken(pref.getString(KEY_REFRESH_TOKEN, ""));
		token.setExpiresTime(pref.getLong(KEY_EXPIRES_IN, 0));

		return token;
	}

	/**
	 * 保存 Token 对象到 SharedPreferences。
	 * 
	 * @param context
	 *            应用程序上下文环境
	 * @param token
	 *            Token 对象
	 */
	public static void writeAccessToken(Context context, Oauth2AccessToken token) {
		if (null == context || null == token) {
			return;
		}

		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
		Editor editor = pref.edit();
		editor.putString(KEY_UID, token.getUid());
		editor.putString(KEY_ACCESS_TOKEN, token.getToken());
		editor.putString(KEY_REFRESH_TOKEN, token.getRefreshToken());
		editor.putLong(KEY_EXPIRES_IN, token.getExpiresTime());
		editor.commit();
	}

	@Override
	public void onComplete(Bundle values) {
		// 从 Bundle 中解析 Token
		mAccessToken = Oauth2AccessToken.parseAccessToken(values);
		if (mAccessToken.isSessionValid()) {
			// 保存 Token 到 SharedPreferences
			writeAccessToken(mActivity, mAccessToken);
			GolukUtils.showToast(mActivity, "授权成功");
			mWeiboShareAPI.registerApp();
		} else {
			// 以下几种情况，您会收到 Code：
			// 1. 当您未在平台上注册的应用程序的包名与签名时；
			// 2. 当您注册的应用程序包名与签名不正确时；
			// 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
			String code = values.getString("code");
			GolukUtils.showToast(mActivity, "授权失败: " + code);
		}
	}

	@Override
	public void onWeiboException(WeiboException arg0) {

	}

	@Override
	public void onResponse(BaseResponse arg0) {
		// TODO Auto-generated method stub

	}

}
