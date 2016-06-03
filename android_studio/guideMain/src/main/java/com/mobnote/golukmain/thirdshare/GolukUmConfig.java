package com.mobnote.golukmain.thirdshare;

import com.mobnote.application.GolukApplication;
import com.mobnote.util.GolukConfig;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;

public class GolukUmConfig {

	// twitter国际
	private static final String TWITTER_INTERNATIONAL_APPID = "FLzjlkpJDyZL5pKgdYxl5jpAY";
	private static final String TWITTER_INTERNATIONAL_APPSECRET = "zThc89YoNntZ25jKSwMaT3vXgNwDhQOz2H449FGIZFrlpElfba";
	// twitter国内
	private static final String TWITTER_APPID = "bX61Sal2t0iSDXLMxjqAEAA2p";
	private static final String TWITTER_APPSECRET = "Ris7Fpsj7wx03CdM3Sle6AOgIkYjq4Tosgj6EmseXU63OToVh3";

	public static void UmInit() {
		Config.IsToastTip = false;
		// 微信
		PlatformConfig.setWeixin(GolukConfig.WX_APPID, GolukConfig.WX_APPSECRET);
		PlatformConfig.setQQZone(GolukConfig.QQ_APPID, GolukConfig.QQ_APPKEY);
		if(GolukApplication.getInstance().isMainland()){
			PlatformConfig.setTwitter(TWITTER_APPID, TWITTER_APPSECRET);
		}else{
            PlatformConfig.setTwitter(TWITTER_INTERNATIONAL_APPID, TWITTER_INTERNATIONAL_APPSECRET);
        }
	}

}
