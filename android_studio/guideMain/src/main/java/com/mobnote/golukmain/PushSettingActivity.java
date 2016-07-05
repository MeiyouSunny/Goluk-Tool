package com.mobnote.golukmain;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.bean.GetPushSettingRequest;
import com.mobnote.golukmain.bean.PushMsgSettingBean;
import com.mobnote.golukmain.bean.SetPushMsgSettingBean;
import com.mobnote.golukmain.bean.SetPushSettingRequest;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.live.LiveDialogManager;
import com.mobnote.golukmain.live.LiveDialogManager.ILiveDialogManagerFn;
import com.mobnote.golukmain.xdpush.SettingBean;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;

public class PushSettingActivity extends BaseActivity implements OnClickListener, ILiveDialogManagerFn,IRequestResultListener {

	public static final String TAG = "PushSettingActivity";
	private Button mCanCommentBtn = null;
	private Button mCanPariseBtn = null;
	private Button mCanFollowBtn = null;

	/** 是否允许评论 */
	private boolean mIsCanComment = true;
	/** 是否允许点赞 */
	private boolean isCanParise = true;
	/** 有人关注我 */
	private boolean isCanFollow = true;
	
	private GetPushSettingRequest mGetPushSettingRequest = null;
	private SetPushSettingRequest mSetPushSettingRequest = null;

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
		mCanFollowBtn = (Button) findViewById(R.id.notify_setting_follow_btn);
		mCanCommentBtn.setOnClickListener(this);
		mCanPariseBtn.setOnClickListener(this);
		mCanFollowBtn.setOnClickListener(this);
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
		mGetPushSettingRequest = new GetPushSettingRequest(IPageNotifyFn.PageType_GetPushCfg, this);
		mGetPushSettingRequest.get(mBaseApp.getMyInfo().uid);
		
		LiveDialogManager.getManagerInstance().showCommProgressDialog(this,
				LiveDialogManager.DIALOG_TYPE_GET_PUSH_CONFIGE, "",
				this.getResources().getString(R.string.str_request_config_ongoing), true);
	}

	private void saveConfigToServer() {
		mSetPushSettingRequest = new SetPushSettingRequest(IPageNotifyFn.PageType_SetPushCfg, this);
		mSetPushSettingRequest.get(mBaseApp.getMyInfo().uid, mIsCanComment == true ? "1":"0", isCanParise == true ? "1":"0", isCanFollow == true ? "1":"0");
//		String json = JsonUtil.getPushSetJson(mIsCanComment, isCanParise,isCanFollow);
//		mBaseApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_SetPushCfg,
//				json);
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
	
	private void setFollowState(boolean isOpen) {
		isCanFollow = isOpen;
		if (isOpen) {
			mCanFollowBtn.setBackgroundResource(R.drawable.set_open_btn);
		} else {
			mCanFollowBtn.setBackgroundResource(R.drawable.set_close_btn);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.back_btn) {
			exit();
		} else if (id == R.id.notify_setting_comment_btn) {
			if(!GolukApplication.getInstance().isUserLoginToServerSuccess()){
				GolukApplication.getInstance().isUserLoginSucess = false;
				GolukApplication.getInstance().loginStatus = 2;
				GolukApplication.getInstance().autoLoginStatus = 3;
				GolukUtils.startUserLogin(this);
				return;
			}
			setCommentState(!mIsCanComment);
		} else if (id == R.id.notify_setting_prise_btn) {
			if(!GolukApplication.getInstance().isUserLoginToServerSuccess() ){
				GolukApplication.getInstance().isUserLoginSucess = false;
				GolukApplication.getInstance().loginStatus = 2;
				GolukApplication.getInstance().autoLoginStatus = 3;
				GolukUtils.startUserLogin(this);
				return;
			}
			setPariseState(!isCanParise);
		} else if (id == R.id.notify_setting_follow_btn) {
			if(!GolukApplication.getInstance().isUserLoginToServerSuccess() ){
				GolukApplication.getInstance().isUserLoginSucess = false;
				GolukApplication.getInstance().loginStatus = 2;
				GolukApplication.getInstance().autoLoginStatus = 3;
				GolukUtils.startUserLogin(this);
				return;
			}
			setFollowState(!isCanFollow);
		}
	}

	private void exit() {
		// 把当前的设置通知上报服务器
		saveConfigToServer();
//		this.finish();
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
			GolukUtils.showToast(this, getResources().getString(R.string.network_error));
			return;
		}
		SettingBean bean = JsonUtil.parsePushSettingJson((String) param2);
		if (null == bean || !bean.isSucess || !"0".equals(bean.result)) {
			GolukUtils.showToast(this, getResources().getString(R.string.network_error));
			return;
		}
		setFollowState(bean.isFollow.equals("1") ? true : false);
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


	@Override
	public void onLoadComplete(int requestType, Object result) {
        LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();

		if(requestType == IPageNotifyFn.PageType_GetPushCfg) {
			PushMsgSettingBean psb = (PushMsgSettingBean) result;
			if(psb != null){
//				SettingBean bean = JsonUtil.parsePushSettingJson((String) param2);
				if (psb != null && psb.data != null ) {
					if(!GolukUtils.isTokenValid(psb.data.result)){
						GolukApplication.getInstance().isUserLoginSucess = false;
						GolukApplication.getInstance().loginStatus = 2;
						GolukApplication.getInstance().autoLoginStatus = 3;
						GolukUtils.startUserLogin(this);
						this.finish();
						return;
					}
				}
				if (null == psb || !psb.success || !"0".equals(psb.data.result)) {
					GolukUtils.showToast(this, this.getResources().getString(R.string.str_getwificfg_fail));
					return;
				}
				setFollowState(psb.data.isfollow.equals("1") ? true : false);
				setCommentState(psb.data.iscomment.equals("1") ? true : false);
				setPariseState(psb.data.ispraise.equals("1") ? true : false);
			}else{
				GolukUtils.showToast(this, getResources().getString(R.string.network_error));
				return;
			}
		} else if(requestType == IPageNotifyFn.PageType_SetPushCfg) {
            SetPushMsgSettingBean retBean = (SetPushMsgSettingBean)result;
            if(null == retBean || null == retBean.data) {
                Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if(!GolukUtils.isTokenValid(retBean.data.result)) {
                Toast.makeText(this, getString(R.string.invalid_token), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if(!"0".equals(retBean.data.result)) {
                Toast.makeText(this, getString(R.string.str_push_setting_save_fail), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            finish();
        }
	}

}
