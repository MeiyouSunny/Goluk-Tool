package com.mobnote.golukmain.thirdshare;

import com.mobnote.application.GolukApplication;
import com.mobnote.util.GolukConfig;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;

public class GolukUmConfig {

	// twitter国际
	private static final String TWITTER_APPID_INTERNATIONAL = "FLzjlkpJDyZL5pKgdYxl5jpAY";
	private static final String TWITTER_APPSECRET_INTERNATIONAL = "zThc89YoNntZ25jKSwMaT3vXgNwDhQOz2H449FGIZFrlpElfba";
	// twitter国内
	private static final String TWITTER_APPID_MAINLAND = "bX61Sal2t0iSDXLMxjqAEAA2p";
	private static final String TWITTER_APPSECRET_MAINLAND = "Ris7Fpsj7wx03CdM3Sle6AOgIkYjq4Tosgj6EmseXU63OToVh3";

	public static void UmInit() {
		Config.IsToastTip = false;
		if(GolukApplication.getInstance().isMainland()){
            PlatformConfig.setWeixin(GolukConfig.WX_APPID_MAINLAND, GolukConfig.WX_APPSECRET_MAINLAND);
            PlatformConfig.setQQZone(GolukConfig.QQ_APPID_MAINLAND, GolukConfig.QQ_APPKEY_MAINLAND);
            PlatformConfig.setTwitter(TWITTER_APPID_MAINLAND, TWITTER_APPSECRET_MAINLAND);
        }else{
            PlatformConfig.setWeixin(GolukConfig.WX_APPID_INTERNATIONAL, GolukConfig.WX_APPSECRET_INTERNATIONAL);
            PlatformConfig.setQQZone(GolukConfig.QQ_APPID_INTERNATIONAL, GolukConfig.QQ_APPKEY_INTERNATIONAL);
            PlatformConfig.setTwitter(TWITTER_APPID_INTERNATIONAL, TWITTER_APPSECRET_INTERNATIONAL);
        }

	}

}
