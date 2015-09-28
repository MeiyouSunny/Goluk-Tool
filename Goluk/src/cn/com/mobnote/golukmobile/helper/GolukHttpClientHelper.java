package cn.com.mobnote.golukmobile.helper;

import android.content.Context;
import cn.com.mobnote.application.GolukApplication;

/**
 * @描述 Goluk通用HttpClient辅助类
 * @作者 卜长清，buchangqing@goluk.com
 * @日期 2015-09-09
 * @版本 1.0
 */
public class GolukHttpClientHelper extends AsyncHttpClientHelper {
	private GolukApplication mApp = null;
	private Context mContext = null;
	
	public GolukHttpClientHelper() { }
	public GolukHttpClientHelper(Context context, GolukApplication application) {
		mApp = application;
		mContext = context;
		
		initConfig();
	}
	
	/**
	 * 初始化配置
	 */
	private void initConfig() {
		if (mApp != null) {
			if (mApp.mSharedPreUtil != null) {
				// serverflag
				String serverFlag = mApp.mSharedPreUtil.getConfigServerFlag();
				if (serverFlag.equals("nvd")) {
					super.setServer("q.goluk.cn");
				} else if (serverFlag.equals("test")) {
					super.setServer("server.goluk.cn");
				} else if (serverFlag.equals("dev")) {
					super.setServer("svr.goluk.cn");
				} else if (serverFlag.equals("temp")) {
					super.setServer("192.168.2.104:9090");
				}
			}
		}
	}
}
