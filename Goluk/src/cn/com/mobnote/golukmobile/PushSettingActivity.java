package cn.com.mobnote.golukmobile;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import cn.com.mobnote.golukmobile.live.LiveDialogManager;
import cn.com.mobnote.golukmobile.live.LiveDialogManager.ILiveDialogManagerFn;
import cn.com.mobnote.golukmobile.xdpush.SettingBean;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;

public class PushSettingActivity extends BaseActivity implements OnClickListener, ILiveDialogManagerFn {

	public static final String TAG = "PushSettingActivity";
	private Button mCanCommentBtn = null;
	private Button mCanPariseBtn = null;

	/** 是否允许评论 */
	private boolean mIsCanComment = true;
	/** 是否允许点赞 */
	private boolean isCanParise = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		getWindow().setContentView(R.layout.pushsetting);
		mBaseApp.setContext(this, TAG);
		initView();
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);
		getConfigFromServer();
	}

	private void initView() {
		mCanCommentBtn = (Button) findViewById(R.id.notify_setting_comment_btn);
		mCanPariseBtn = (Button) findViewById(R.id.notify_setting_prise_btn);
		mCanCommentBtn.setOnClickListener(this);
		mCanPariseBtn.setOnClickListener(this);
		findViewById(R.id.back_btn).setOnClickListener(this);
		// 赋初始值
		setCommentState(mIsCanComment);
		setPariseState(isCanParise);
	}

	@Override
	public void onResume() {
		super.onResume();
		mBaseApp.setContext(this, TAG);
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);
	}

	/**
	 * 去服务器获取配置信息
	 * 
	 * @author jyf
	 */
	private void getConfigFromServer() {
		boolean isSucess = mBaseApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_GetPushCfg, "");
		if (!isSucess) {
			GolukUtils.showToast(this, "获取失败");
			return;
		}
		LiveDialogManager.getManagerInstance().showCommProgressDialog(this,
				LiveDialogManager.DIALOG_TYPE_GET_PUSH_CONFIGE, "", "正在请求配置...", true);
	}

	private void saveConfigToServer() {
		String json = JsonUtil.getPushSetJson(mIsCanComment, isCanParise);
		mBaseApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_SetPushCfg,
				json);
	}

	private void setCommentState(boolean isOpen) {
		mIsCanComment = isOpen;
		if (isOpen) {
			mCanCommentBtn.setBackgroundResource(R.drawable.set_open_btn);
		} else {
			mCanCommentBtn.setBackgroundResource(R.drawable.set_close_btn);
		}
	}

	private void setPariseState(boolean isOpen) {
		isCanParise = isOpen;
		if (isOpen) {
			mCanPariseBtn.setBackgroundResource(R.drawable.set_open_btn);
		} else {
			mCanPariseBtn.setBackgroundResource(R.drawable.set_close_btn);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_btn:
			exit();
			break;
		case R.id.notify_setting_comment_btn:
			setCommentState(!mIsCanComment);
			break;
		case R.id.notify_setting_prise_btn:
			setPariseState(!isCanParise);
			break;
		}
	}

	private void exit() {
		// 把当前的设置通知上报服务器
		saveConfigToServer();
		this.finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void deal_getPush_CallBack(int success, Object param1, Object param2) {
		LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
		if (1 != success) {
			GolukUtils.showToast(this, "获取配置失败");
			return;
		}
		SettingBean bean = JsonUtil.parsePushSettingJson((String) param2);
		if (null == bean || !bean.isSucess || !"0".equals(bean.result)) {
			GolukUtils.showToast(this, "获取配置失败");
			return;
		}
		setCommentState(bean.isComment.equals("1") ? true : false);
		setPariseState(bean.isPraise.equals("1") ? true : false);
	}

	private void deal_setPush_CallBack(int success, Object param1, Object param2) {

	}

	public void page_CallBack(int event, int success, Object param1, Object param2) {
		if (IPageNotifyFn.PageType_GetPushCfg == event) {
			deal_getPush_CallBack(success, param1, param2);
		} else if (IPageNotifyFn.PageType_SetPushCfg == event) {
			deal_setPush_CallBack(success, param1, param2);
		}
	}

	@Override
	public void dialogManagerCallBack(int dialogType, int function, String data) {
		if (LiveDialogManager.DIALOG_TYPE_GET_PUSH_CONFIGE == dialogType) {
			// 取消
			LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
			mBaseApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_GetPushCfg,
					JsonUtil.getCancelJson());
		}

	}

}
