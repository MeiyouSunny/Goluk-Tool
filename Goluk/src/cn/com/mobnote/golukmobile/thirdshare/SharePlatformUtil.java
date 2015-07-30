package cn.com.mobnote.golukmobile.thirdshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;

import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.UMVideo;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

public class SharePlatformUtil {

	// 注意：在微信授权的时候，必须传递appSecret
	// wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
	private static final String WX_APPID = "wxd437f1e0f67dfac1";
	private static final String WX_APPSECRET = "90d4bc4ae1b3dba372dd72d03cc8e82d";

	private static final String QQ_APPID = "1104418156";
	private static final String QQ_APPKEY = "G7OfQ0qbqe5OJlUP";

	public Context mContext;
	private static final String DESCRIPTOR = "com.umeng.share";
	public final UMSocialService mController = UMServiceFactory.getUMSocialService(DESCRIPTOR);
	public SinaWeiBoUtils mSinaWBUtils = null;
	public VideoSquareInfo mData = null;

	public SharePlatformUtil(Context context) {
		mContext = context;
		mSinaWBUtils = new SinaWeiBoUtils((Activity) mContext);
	}

	/**
	 * 配置分享平台参数</br>
	 */
	public void configPlatforms() {
		// 添加微信、微信朋友圈平台
		addWXPlatform();

		// 添加腾讯QQ及ＱＱ空间
		addQQQZonePlatform();
	}

	/**
	 * @功能描述 : 添加微信平台分享
	 * @return
	 */
	public void addWXPlatform() {
		// 添加微信平台
		UMWXHandler wxHandler = new UMWXHandler(mContext, WX_APPID, WX_APPSECRET);
		wxHandler.showCompressToast(false);
		wxHandler.addToSocialSDK();

		// 支持微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(mContext, WX_APPID, WX_APPSECRET);
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();
	}

	/**
	 * @功能描述 : 添加QQ平台支持 QQ分享的内容， 包含四种类型， 即单纯的文字、图片、音乐、视频. 参数说明 : title, summary,
	 *       image url中必须至少设置一个, targetUrl必须设置,网页地址必须以"http://"开头 . title :
	 *       要分享标题 summary : 要分享的文字概述 image url : 图片地址 [以上三个参数至少填写一个] targetUrl
	 *       : 用户点击该分享时跳转到的目标地址 [必填] ( 若不填写则默认设置为友盟主页 )
	 * @return
	 */
	public void addQQQZonePlatform() {
		// 添加QQ支持, 并且设置QQ分享内容的target url
		UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler((Activity) mContext, QQ_APPID, QQ_APPKEY);
		qqSsoHandler.addToSocialSDK();

		// 参数1为当前Activity， 参数2为开发者在QQ互联申请的APP ID，参数3为开发者在QQ互联申请的APP kEY.
		QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler((Activity) mContext, QQ_APPID, QQ_APPKEY);
		qZoneSsoHandler.addToSocialSDK();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// SSO 授权回调
		// 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResults
		if (mSinaWBUtils != null) {
			mSinaWBUtils.onActivityResult(requestCode, resultCode, data);
		}
	}

	public void setShareData(VideoSquareInfo data) {
		mData = data;
	}

	/**
	 * 根据不同的平台设置不同的分享内容</br>
	 */
	public void setShareContent(String videourl, String imageurl, String text, String ttl) {

		// ttl = "极路客分享";
		if (text == null || "".equals(text)) {
			text = "goluk精彩视频";
		}
		UMVideo video = new UMVideo(videourl);
		video.setThumb(imageurl);
		video.setTitle(ttl);

		// 微信
		WeiXinShareContent weixinContent = new WeiXinShareContent(video);
		weixinContent.setShareContent(text);
		mController.setShareMedia(weixinContent);

		// 设置朋友圈分享的内容
		CircleShareContent circleMedia = new CircleShareContent(video);
		circleMedia.setShareContent(text);
		mController.setShareMedia(circleMedia);

		// qq分享
		QQShareContent qqContent = new QQShareContent(video);
		qqContent.setShareContent(text);
		mController.setShareMedia(qqContent);

		// qq空间
		QZoneShareContent qzone = new QZoneShareContent(video);
		// 设置分享文字
		qzone.setShareContent(text);
		mController.setShareMedia(qzone);
	}

}
