package com.mobnote.golukmain.http;

import com.mobnote.application.GolukApplication;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SharedPrefUtil;

public class HttpCommHeaderBean {

	public String commhdtype = null;
	public String commipcversion = null;
	public String commticket = null;
	public String commuid = null;
	public String commversion = null;
	public String commlocale = null;

	public HttpCommHeaderBean() {
		init();
	}

	private void init() {
		commhdtype = GolukApplication.getInstance().mIPCControlManager.mProduceName;
		commipcversion = SharedPrefUtil.getIPCVersion();
		commticket = SharedPrefUtil.getUserToken();
		commuid = GolukApplication.getInstance().mCurrentUId;
		commversion = GolukUtils.getCommversion();
		commlocale = GolukUtils.getLanguageAndCountry();
	}

}
