package com.mobnote.golukmain.thirdshare;

import com.mobnote.util.GolukConfig;
import com.umeng.socialize.PlatformConfig;

public class GolukUmConfig {
	// 注意：在微信授权的时候，必须传递appSecret
	// wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
	private static final String WX_APPID = "wx946eb00563c74a93";
	private static final String WX_APPSECRET = "084f1f352078730a78577a1c6be11614";

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