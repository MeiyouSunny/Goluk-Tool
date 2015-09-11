package cn.com.mobnote.golukmobile.helper;

import android.content.Context;
import cn.com.mobnote.application.GolukApplication;

/**
 * @描述 Goluk通用HttpClient辅助类
 * @作者 卜长清，buchangqing@goluk.com
 * @日期 2015-09-09
 * @版本 1.0
 */
public class GolukHttpClientHelper extends HttpClientHelper {
	private GolukApplication mApp;
	private Context mContext;
	
	public GolukHttpClientHelper() {}
	public GolukHttpClientHelper(Context context, GolukApplication application) {
		mApp = application;
		mContext = context;
		
		initConfig();
	}
	
	private void initConfig() {
		if (mApp != null) {
			if (mApp.mSharedPreUtil != null) {
				// serverflag
				String serverFlag = mApp.mSharedPreUtil.getConfigServerFlag();
				if (serverFlag == "nvd") {
					super.setServer("svr.goluk.cn");
				} else if (serverFlag == "test") {
					super.setServer("svr.goluk.cn");
				}
			}
		}
	}
}
