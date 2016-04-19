package com.mobnote.golukmain.thirdshare;

import com.mobnote.util.GolukConfig;
import com.umeng.socialize.PlatformConfig;

public class GolukUmConfig {

	// twitter平台
	private static final String TWITTER_APPID = "FLzjlkpJDyZL5pKgdYxl5jpAY";
	private static final String TWITTER_APPSECRET = "zThc89YoNntZ25jKSwMaT3vXgNwDhQOz2H449FGIZFrlpElfba";

	public static void UmInit() {
		// 微信
		PlatformConfig.setWeixin(GolukConfig.WX_APPID, GolukConfig.WX_APPSECRET);
		PlatformConfig.setQQZone(GolukConfig.QQ_APPID, GolukConfig.QQ_APPKEY);
		PlatformConfig.setTwitter(TWITTER_APPID, TWITTER_APPSECRET);
	}

}
