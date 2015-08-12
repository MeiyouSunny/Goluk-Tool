package cn.com.mobnote.golukmobile;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.live.LiveDialogManager;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 功能：设置页中修改极路客WIFI密码
 * 
 * @author mobnote
 *
 */
public class UserSetupChangeWifiActivity extends BaseActivity implements OnClickListener, OnTouchListener {

	/** title部分 **/
	private ImageButton mBtnBack = null;// 返回
	private TextView mTextTitle = null;// title
	private Button mBtnSave = null;// 保存
	/** body **/
	private EditText mEditText = null;

	private GolukApplication mApp = null;

	private String mGolukSSID = "";
	private String mGolukPWD = "";
	private String mApSSID = "";
	private String mApPWD = "";

	// 写死ip,网关
	private final String ip = "192.168.1.103";
	private final String way = "192.168.1.103";

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
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		
		mTextTitle.setText("极路客WiFi密码");

		/**
		 * 获取摄像头管理页面传来的WIFI密码
		 */
		Intent it = getIntent();
		String password = it.getStringExtra("wifiPwd").toString();
		GolukDebugUtils.i("lily", password + "------ChangeWiFiPassword----");
		if (!"".equals(password)) {
			mEditText.setText(password);
			mEditText.setSelection(password.length());
		} else {
			mEditText.setText("");
		}

		// 监听
		mBtnBack.setOnClickListener(this);
		mBtnSave.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		// 返回
		case R.id.back_btn:
			UserUtils.hideSoftMethod(UserSetupChangeWifiActivity.this);
			this.finish();
			break;
		// 保存
		case R.id.user_title_right:
			// 点击保存按钮隐藏软件盘
			UserUtils.hideSoftMethod(UserSetupChangeWifiActivity.this);
			savePassword();
			break;
		default:
			break;
		}
	}

	/**
	 * 设置IPC信息成功回调
	 */
	public void setIpcLinkWiFiCallBack(int state) {
		LiveDialogManager.getManagerInstance().dissmissCustomDialog();
		if (0 == state) {
			GolukUtils.showToast(this, "修改密码成功");
			this.setResult(10);
			this.finish();
		} else {
			GolukUtils.showToast(this, "修改密码失败");
		}
	}

	private void savePassword() {
		final String newPwd = mEditText.getText().toString();
		if (newPwd.length() < 8 || newPwd.length() > 15) {
			GolukUtils.showToast(this, "极路客密码必须为8-15个字符");
			return;
		}

		String json = getSetIPCJson();
		boolean b = mApp.mIPCControlManager.setIpcLinkPhoneHot(json);
		if (!b) {
			GolukUtils.showToast(this, "调用设置IPC连接热点失败");
		} else {
			LiveDialogManager.getManagerInstance().showCustomDialog(this, "请稍候...");
		}
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

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		switch (view.getId()) {
		case R.id.user_title_right:
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				mBtnSave.setTextColor(Color.rgb(0, 197, 176));
				break;
			case MotionEvent.ACTION_UP:
				mBtnSave.setTextColor(Color.WHITE);
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		return false;
	}
}
