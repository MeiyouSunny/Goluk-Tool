package cn.com.mobnote.golukmobile;

import android.app.Activity;
import android.content.Context;

import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.SmsShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMVideo;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.SmsHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

public class SharePlatformUtil{
	
	public Context mContext;
	private static final String DESCRIPTOR = "com.umeng.share";
	public final UMSocialService mController = UMServiceFactory.getUMSocialService(DESCRIPTOR);
	
	public SharePlatformUtil(Context context){
		mContext = context;
	}
	
	/**
	 * 配置分享平台参数</br>
	 */
	public void configPlatforms() {
		// 添加新浪SSO授权
		mController.getConfig().setSsoHandler(new SinaSsoHandler());
		mController.getConfig().closeToast();



		// 添加微信、微信朋友圈平台
		addWXPlatform();
		//添加短信
		addSMS();
		//添加腾讯QQ
		addQQQZonePlatform();
	}
	/**
	 * @功能描述 : 添加微信平台分享
	 * @return
	 */
	public void addWXPlatform(){
		// 注意：在微信授权的时候，必须传递appSecret
		// wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
		String appId = "wx493f46bf1a71416f";
		String appSecret = "b572ec9cbd3fac52e138e34eff0b4926";
		// 添加微信平台
		UMWXHandler wxHandler = new UMWXHandler(mContext, appId, appSecret);
		wxHandler.showCompressToast(false);
		wxHandler.addToSocialSDK();
		
		// 支持微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(mContext, appId, appSecret);
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
        String appId = "1104418156";
        String appKey = "G7OfQ0qbqe5OJlUP";
        // 添加QQ支持, 并且设置QQ分享内容的target url
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler((Activity) mContext,appId, appKey);
       
        qqSsoHandler.addToSocialSDK();
    }
	
	/**
	 * 添加短信平台</br>
	 */
	public void addSMS() {
		// 添加短信
		SmsHandler smsHandler = new SmsHandler();
		smsHandler.addToSocialSDK();
	}
	
	/**
	 * 根据不同的平台设置不同的分享内容</br>
	 */
	public void setShareContent(String videourl,String imageurl,String text,String ttl) {
		
		//ttl = "极路客分享";
		if(text == null || "".equals(text)){
			text = "goluk精彩视频";
		}
		UMImage umimage = new UMImage(mContext,imageurl);
		UMVideo video = new UMVideo(videourl);
		video.setThumb(umimage);

		// 配置新浪SSO
		mController.getConfig().setSsoHandler(new SinaSsoHandler());
		
		//微信
		WeiXinShareContent weixinContent = new WeiXinShareContent();
		
		weixinContent.setShareContent(text);
		weixinContent.setTitle(ttl);
		weixinContent.setTargetUrl(videourl);
		weixinContent.setShareImage(umimage);
		mController.setShareMedia(weixinContent);
		
		// 设置朋友圈分享的内容
		CircleShareContent circleMedia = new CircleShareContent();
		circleMedia.setShareContent(text);
		circleMedia.setTitle(ttl);
		circleMedia.setTargetUrl(videourl);
		circleMedia.setShareImage(umimage);
		mController.setShareMedia(circleMedia);

		// 设置短信分享内容
		SmsShareContent sms = new SmsShareContent();
		sms.setShareContent(text+"。"+videourl);
		//sms.setShareImage(umimage);
		mController.setShareMedia(sms);
		
		
		
		//新浪微博分享
		SinaShareContent sinaContent = new SinaShareContent();
		sinaContent.setShareContent(text);
		sinaContent.setTitle(ttl);
		sinaContent.setTargetUrl(videourl);
		sinaContent.setShareMedia(video);
		mController.setShareMedia(sinaContent);
		
		
		//qq分享
		QQShareContent  qqContent = new QQShareContent();
		qqContent.setShareContent(text);
		qqContent.setTitle(ttl);
		qqContent.setTargetUrl(videourl);
		qqContent.setShareImage(umimage);
		mController.setShareMedia(qqContent);
	}
	
}
