package com.mobnote.golukmain.thirdshare;

import com.mobnote.application.GolukApplication;
import com.mobnote.util.GolukConfig;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareConfig;

public class GolukUmConfig {

	// twitter国际
	private static final String TWITTER_APPID_INTERNATIONAL = "EwGTl7iobCSmvy6PvotqsEKuA";
	private static final String TWITTER_APPSECRET_INTERNATIONAL = "e0xqiVEFYkSQasEBmz9AANfW2VkKC0NUWPvIo9W2cPSpuks64k";
	private static final String VK_APPID_INTERNATIONAL = "5901352";
	private static final String VK_APPSECRET_INTERNATIONAL = "eF8JYTsby9z25dkbqCZQ";

	// twitter国内
	private static final String TWITTER_APPID_MAINLAND = "bX61Sal2t0iSDXLMxjqAEAA2p";
	private static final String TWITTER_APPSECRET_MAINLAND = "Ris7Fpsj7wx03CdM3Sle6AOgIkYjq4Tosgj6EmseXU63OToVh3";

	public static void UmInit() {
		Config.DEBUG = true;
		UMConfigure.init(GolukApplication.getInstance(),UMConfigure.DEVICE_TYPE_PHONE,"");
		if(GolukApplication.getInstance().isMainland()){
			PlatformConfig.setSinaWeibo(GolukConfig.SINA_APP_KEY_MAINLAND, GolukConfig.SINA_APP_KEY_INTERNATIONAL,GolukConfig.REDIRECT_URL);
            PlatformConfig.setWeixin(GolukConfig.WX_APPID_MAINLAND, GolukConfig.WX_APPSECRET_MAINLAND);
            PlatformConfig.setQQZone(GolukConfig.QQ_APPID_MAINLAND, GolukConfig.QQ_APPKEY_MAINLAND);
            PlatformConfig.setTwitter(TWITTER_APPID_MAINLAND, TWITTER_APPSECRET_MAINLAND);
        }else{
            PlatformConfig.setWeixin(GolukConfig.WX_APPID_INTERNATIONAL, GolukConfig.WX_APPSECRET_INTERNATIONAL);
            PlatformConfig.setQQZone(GolukConfig.QQ_APPID_INTERNATIONAL, GolukConfig.QQ_APPKEY_INTERNATIONAL);
            PlatformConfig.setTwitter(TWITTER_APPID_INTERNATIONAL, TWITTER_APPSECRET_INTERNATIONAL);
			PlatformConfig.setVKontakte(VK_APPID_INTERNATIONAL,VK_APPSECRET_INTERNATIONAL);
        }
	}

}
