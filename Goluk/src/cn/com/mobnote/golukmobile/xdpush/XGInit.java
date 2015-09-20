package cn.com.mobnote.golukmobile.xdpush;

import android.content.Context;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

public class XGInit implements XGIOperateCallback {
	/** 开启logcat输出，方便debug，发布时请关闭 */
	private static final boolean isDebug = true;
	private Context mContext = null;

	private boolean isValidTokenId = false;

	public XGInit(Context context) {
		mContext = context;
	}

	public void init() {
		XGPushConfig.enableDebug(mContext, isDebug);
		// 注册接口
		XGPushManager.registerPush(mContext.getApplicationContext(), this);
	}

	@Override
	public void onFail(Object arg0, int arg1, String arg2) {
		// 册失败
		GolukDebugUtils.e("", "jyf----XD----Goluk----XGInit----token: failed");

	}

	/**
	 * 获取token成功后，需要向服务器上报，这个是上报回调
	 * 
	 * @author jyf
	 */
	public void golukServerRegisterCallBack(int success, Object param1, Object param2) {
		if (0 == success) {
			// 注册失败
			return;
		}
	}

	@Override
	public void onSuccess(Object arg0, int arg1) {
		// 注册成功, 获取Token
		String token = XGPushConfig.getToken(mContext);
		String json = JsonUtil.getPushRegisterJsonStr(token, "1", "");
		GolukApplication.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_PushReg, json);

		isValidTokenId = true;

		GolukUtils.showToast(mContext, "token:" + token);

		GolukDebugUtils.e("", "jyf----XD----Goluk----XGInit----token:" + token);

	}
}
