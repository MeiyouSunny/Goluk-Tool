package cn.com.mobnote.golukmobile.xdpush;

import android.content.Context;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

public class XGInit implements XGIOperateCallback {
	/** 开启logcat输出，方便debug，发布时请关闭 */
	private static final boolean isDebug = false;
	private Context mContext = null;
	/** 保存TokenId,在服务端注册成功后，保存在本地 */
	private String mTokenId = "";

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
		// 注册失败
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
			GolukDebugUtils.e("", "jyf----XD----Goluk----XGInit---golukServerRegisterCallBack-----failed:");
			return;
		}
		GolukDebugUtils.e("", "jyf----XD----Goluk----XGInit---golukServerRegisterCallBack-----sucess:");

		// 保存TokenId到本地
		GolukApplication.getInstance().mSharedPreUtil.setTokenId(mTokenId);
	}

	@Override
	public void onSuccess(Object arg0, int arg1) {
		// 信鸽服务器注册成功回调, 可以获取Token
		String token = XGPushConfig.getToken(mContext);
		String localToken = GolukApplication.getInstance().mSharedPreUtil.getTolenId();
		if (token.equals(localToken)) {
			// 本地有Token,说明上传成功过，不需要上传
			GolukDebugUtils.e("", "jyf----XD----Goluk----XGInit---local have-token, Not Upload:");
			return;
		}
		mTokenId = token;
		String json = JsonUtil.getPushRegisterJsonStr(token, "1", "");
		GolukApplication.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_PushReg, json);
		// GolukUtils.showToast(mContext, "token:" + token);
		GolukDebugUtils.e("", "jyf----XD----Goluk----XGInit----token:" + token);
	}
}