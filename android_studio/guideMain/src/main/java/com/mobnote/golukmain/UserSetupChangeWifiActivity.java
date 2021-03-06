package com.mobnote.golukmain;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;

import org.json.JSONObject;

import cn.com.tiros.debug.GolukDebugUtils;
import goluk.com.t1s.api.ApiUtil;
import goluk.com.t1s.api.callback.CallbackCmd;
import likly.dollar.$;

/**
 * 功能：设置页中修改极路客WIFI密码
 * 
 * @author mobnote
 *
 */
public class UserSetupChangeWifiActivity extends BaseActivity implements OnClickListener {

	/** title部分 **/
	private ImageButton mBtnBack = null;// 返回
	private TextView mTextTitle = null;// title
	private Button mBtnSave = null;// 保存
	/** body **/
	private EditText mEditText = null;
	private EditText mEditText2 = null;

	private GolukApplication mApp = null;

	private String mGolukSSID = "";
	private String mGolukPWD = "";
	private String mApSSID = "";
	private String mApPWD = "";

	// 写死ip,网关
	private final String ip = "192.168.1.103";
	private final String way = "192.168.1.103";
	
	private ImageView mImageView1, mImageView2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		setContentView(R.layout.user_setup_changewifi_password);
		mApp = (GolukApplication) getApplication();
		Intent intent = getIntent();
		mApp.setContext(this, "changePassword");

		mGolukSSID = intent.getStringExtra("golukssid");
		mGolukPWD = intent.getStringExtra("golukpwd");
		mApSSID = intent.getStringExtra("apssid");
		mApPWD = intent.getStringExtra("appwd");

		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApp.setContext(this, "changePassword");
		
	}

	// 初始化
	public void initView() {
		mBtnBack = (ImageButton) findViewById(R.id.back_btn);
		mBtnSave = (Button) findViewById(R.id.user_title_right);
		mEditText = (EditText) findViewById(R.id.changewifi_password_editText);
		mEditText2 = (EditText) findViewById(R.id.changewifi_password_editText_2);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mImageView1 = (ImageView) findViewById(R.id.imageView1);
		mImageView2 = (ImageView) findViewById(R.id.imageView2);
		
		mTextTitle.setText(this.getResources().getString(R.string.str_wifi_pwd_title));

		/**
		 * 获取摄像头管理页面传来的WIFI密码
		 */
		Intent it = getIntent();
		String password = it.getStringExtra("wifiPwd").toString();
		GolukDebugUtils.i("lily", password + "------ChangeWiFiPassword----");
		if(mApp.isMainland()) {
			if (!"".equals(password)) {
				mEditText.setText(password);
				mEditText.setSelection(password.length());
			} else {
				mEditText.setText("");
			}
		}
		if (IPCControlManager.T1_SIGN.equals(mApp.mIPCControlManager.mProduceName)
				|| IPCControlManager.T1s_SIGN.equals(mApp.mIPCControlManager.mProduceName)
				|| IPCControlManager.T2_SIGN.equals(mApp.mIPCControlManager.mProduceName)
				|| IPCControlManager.T3_SIGN.equals(mApp.mIPCControlManager.mProduceName)
				|| IPCControlManager.T3U_SIGN.equals(mApp.mIPCControlManager.mProduceName)) {
			mImageView1.setImageResource(R.drawable.ipcbind_t_direct_gif_3);
			mImageView2.setVisibility(View.INVISIBLE);
		} else {
			mImageView1.setImageResource(R.drawable.ipcbind_g_direct_bg);
			mImageView2.setVisibility(View.VISIBLE);
		}

		// 监听
		mBtnBack.setOnClickListener(this);
		mBtnSave.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.back_btn) {
			UserUtils.hideSoftMethod(UserSetupChangeWifiActivity.this);
			this.finish();
		} else if (id == R.id.user_title_right) {
			// 点击保存按钮隐藏软件盘
			UserUtils.hideSoftMethod(UserSetupChangeWifiActivity.this);
			savePassword();
		} else {
		}
	}

	/**
	 * 设置IPC信息成功回调
	 */
	public void setIpcLinkWiFiCallBack(int state) {
		if (0 == state) {
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_wifi_change_success));
			this.setResult(10);
			this.finish();
		} else {
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_wifi_change_fail));
		}
	}

	private void savePassword() {
		final String newPwd = mEditText.getText().toString();
		if (newPwd.length() < 8 || newPwd.length() > 15) {
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_wifi_pwd_limit));
			mEditText.requestFocus();
			return;
		}

		final String newPwdConfirm = mEditText2.getText().toString();
		if (newPwdConfirm.length() < 8 || newPwdConfirm.length() > 15) {
			GolukUtils.showToast(this, this.getResources().getString(R.string.confirm_password));
			mEditText2.requestFocus();
			return;
		}

		if(!newPwd.equals(newPwdConfirm)){
			GolukUtils.showToast(this, this.getResources().getString(R.string.confirm_password_error));
			mEditText.requestFocus();
			return;
		}

		// T1SP
		if (mApp.getIPCControlManager().isT2S()) {
			updateWifiPwd(newPwd);
			return;
		}
		// Other
		String json = getSetIPCJson();
		mApp.stopDownloadList();
		boolean b = mApp.mIPCControlManager.setIpcLinkPhoneHot(json);
	}

	private String getSetIPCJson() {
		// 连接ipc热点wifi---调用ipc接口
		GolukDebugUtils.e("", "通知ipc连接手机热点--setIpcLinkPhoneHot---1");
		final String newPwd = mEditText.getText().toString();
		String json = getIPCJson(mGolukSSID, mGolukPWD, mApSSID, newPwd);

		return json;
	}

	private String getIPCJson(String golukSSID, String golukPWD, String apSSID, String apPWD) {
		String json = null;
		try {
			JSONObject obj = new JSONObject();
			obj.put("AP_SSID", apSSID);
			obj.put("AP_PWD", apPWD);
			// obj.put("GolukSSID", golukSSID);
			// obj.put("GolukPWD", golukPWD);
			// obj.put("GolukIP", ip);
			// obj.put("GolukGateway", way);

			json = obj.toString();
		} catch (Exception e) {

		}
		return json;
	}

	/**
	 * T2S修改WIFI密码
	 *
	 * @param pwd
	 */
	private void updateWifiPwd(String pwd) {
		if (TextUtils.isEmpty(pwd))
			return;
		ApiUtil.modifyWifiPassword(pwd, new CallbackCmd() {
			@Override
			public void onSuccess(int i) {
				// 必须重启网络才会生效
				resetT1SPNet();
			}

			@Override
			public void onFail(int i, int i1) {
				$.toast().text(R.string.str_wifi_change_fail).show();
			}
		});
	}

	/**
	 * 重启T1SP网络
	 */
	private void resetT1SPNet() {
		ApiUtil.reconnectWIFI(new CallbackCmd() {
			@Override
			public void onSuccess(int i) {
				$.toast().text(R.string.str_wifi_change_success).show();
				setResult(10);
				finish();
			}

			@Override
			public void onFail(int i, int i1) {
				$.toast().text(R.string.str_wifi_change_fail).show();
			}
		});
	}

}