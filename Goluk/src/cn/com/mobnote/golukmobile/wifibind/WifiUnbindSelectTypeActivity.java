package cn.com.mobnote.golukmobile.wifibind;

import cn.com.mobnote.eventbus.EventFinishWifiActivity;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.IPCControlManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class WifiUnbindSelectTypeActivity extends BaseActivity implements OnClickListener {

	public static final String KEY_IPC_TYPE = "key_ipc_type";
	/** 关闭按钮 **/
	private ImageView mCloseBtn;

	private RelativeLayout mT1Layout;
	private RelativeLayout mG2Layout;
	private RelativeLayout mG1Layout;
	private RelativeLayout mG1sLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unbind_type_layout);

		initView();
		initLisenner();
	}

	/**
	 * 初始化view
	 */
	private void initView() {
		mCloseBtn = (ImageView) findViewById(R.id.close_btn);
		mT1Layout = (RelativeLayout) findViewById(R.id.goluk_t1_layout);
		mG2Layout = (RelativeLayout) findViewById(R.id.goluk_g2_layout);
		mG1Layout = (RelativeLayout) findViewById(R.id.goluk_g1_layout);
		mG1sLayout = (RelativeLayout) findViewById(R.id.goluk_g1s_layout);
	}

	/**
	 * 初始化view的监听
	 */
	private void initLisenner() {
		mCloseBtn.setOnClickListener(this);
		mT1Layout.setOnClickListener(this);
		mG2Layout.setOnClickListener(this);
		mG1Layout.setOnClickListener(this);
		mG1sLayout.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.close_btn:
			this.finish();
			break;
		case R.id.goluk_t1_layout:
			click_BindIpc(IPCControlManager.T1_SIGN);
			break;
		case R.id.goluk_g2_layout:
			click_BindIpc(IPCControlManager.G1_SIGN);
			break;
		case R.id.goluk_g1_layout:
			click_BindIpc(IPCControlManager.G1_SIGN);
			break;
		case R.id.goluk_g1s_layout:
			click_BindIpc(IPCControlManager.G1_SIGN);
			break;
		default:
			break;
		}
	}

	/**
	 * 跳转到绑定界面
	 * 
	 * @param ipcType
	 *            用户选择的类型, G1,G2,T1
	 * @author jyf
	 */
	private void click_BindIpc(String ipcType) {
		Intent intent = new Intent(this, WiFiLinkListActivity.class);
		intent.putExtra(KEY_IPC_TYPE, ipcType);
		startActivity(intent);
	}

	public void onEventMainThread(EventFinishWifiActivity event) {
		finish();
	}

}
