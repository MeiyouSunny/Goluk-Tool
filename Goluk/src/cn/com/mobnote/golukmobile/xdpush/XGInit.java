package cn.com.mobnote.golukmobile.xdpush;

import android.content.Context;
import cn.com.tiros.debug.GolukDebugUtils;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

public class XGInit implements XGIOperateCallback {
	/** 开启logcat输出，方便debug，发布时请关闭 */
	private static final boolean isDebug = true;
	private Context mContext = null;

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

	@Override
	public void onSuccess(Object arg0, int arg1) {
		// 注册成功, 获取Token
		String token = XGPushConfig.getToken(mContext);

		GolukDebugUtils.e("", "jyf----XD----Goluk----XGInit----token:" + token);

	}

}
