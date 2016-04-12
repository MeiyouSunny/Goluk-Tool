package cn.com.mobnote.golukmobile.thirdshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.util.GolukConfig;
import cn.com.tiros.debug.GolukDebugUtils;

import com.umeng.socialize.bean.HandlerRequestCode;
import com.umeng.socialize.bean.SocializeConfig;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMVideo;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

public class SharePlatformUtil {

	// 注意：在微信授权的时候，必须传递appSecret
	// wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID

	public Context mContext;
	public final UMSocialService mController = UMServiceFactory.getUMSocialService(GolukConfig.SHARE_DESCRIPTOR);
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
		mController.getConfig().closeToast();
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
		UMWXHandler wxHandler = new UMWXHandler(mContext, GolukConfig.WX_APPID, GolukConfig.WX_APPSECRET);
		wxHandler.showCompressToast(false);
		wxHandler.addToSocialSDK();

		// 支持微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(mContext, GolukConfig.WX_APPID, GolukConfig.WX_APPSECRET);
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
		UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler((Activity) mContext, GolukConfig.QQ_APPID, GolukConfig.QQ_APPKEY);
		qqSsoHandler.addToSocialSDK();

		// 参数1为当前Activity， 参数2为开发者在QQ互联申请的APP ID，参数3为开发者在QQ互联申请的APP kEY.
		QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler((Activity) mContext, GolukConfig.QQ_APPID, GolukConfig.QQ_APPKEY);
		qZoneSsoHandler.addToSocialSDK();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		GolukDebugUtils.e("", "jyf----thirdshare--------SharePlatformUtil----onActivityResult: " + "   requestCode:"
				+ requestCode + "   resultCode:" + resultCode);

		// if (10103 == requestCode && 0 == resultCode) {
		// // QQ
		// GolukUtils.showToast(mContext, "分享成功");
		// } else if (10104 == requestCode && 0 == resultCode) {
		// // QQ 空间
		// GolukUtils.showToast(mContext, "分享成功");
		// }

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
			text = mContext.getString(R.string.str_goluk_wonderful_video);
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

		/** QQ空间需要以下写法，它与上面的QQ,微信写法不一致，否则分享的视频地址都是一样的，有问题 , 这可能是友盟的BUG */

		// qq空间
		QZoneShareContent qzone = new QZoneShareContent();
		// 设置分享文字
		qzone.setShareContent(text);
		qzone.setTitle(ttl);
		qzone.setTargetUrl(videourl);
		qzone.setShareImage(new UMImage(mContext, imageurl));
		mController.setShareMedia(qzone);
	}

	// 是否安装微信
	public boolean isInstallWeiXin() {
		if(mController == null) {
			return false;
		}

		SocializeConfig config = mController.getConfig();
		if(null == config) {
			return false;
		}

		UMSsoHandler handler = config.getSsoHandler(HandlerRequestCode.WX_REQUEST_CODE);
		if(null == handler) {
			return false;
		}
		return handler.isClientInstalled();
//		return mController.getConfig().getSsoHandler(HandlerRequestCode.WX_REQUEST_CODE).isClientInstalled();
	}

	// 是否安装微信
	public boolean isInstallQQ() {
		if(mController == null) {
			return false;
		}

		SocializeConfig config = mController.getConfig();
		if(null == config) {
			return false;
		}

		UMSsoHandler handler = config.getSsoHandler(HandlerRequestCode.QQ_REQUEST_CODE);
		if(null == handler) {
			return false;
		}
		return handler.isClientInstalled();
//		return mController.getConfig().getSsoHandler(HandlerRequestCode.QQ_REQUEST_CODE).isClientInstalled();
	}

	public boolean isSinaWBValid() {
		return mSinaWBUtils.isAccessValid();
	}

}
