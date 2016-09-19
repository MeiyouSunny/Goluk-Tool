package com.mobnote.golukmain.wifibind;

import com.mobnote.eventbus.EventFinishWifiActivity;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;

import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class WifiUnbindSelectTypeActivity extends BaseActivity implements OnClickListener {

	// 设备类型
	public static final String KEY_IPC_TYPE = "key_ipc_type";
	/** 设备真正的类型 */
	public static final String KEY_IPC_REAL_TYPE = "key_ipc_real_type";
	/** 关闭按钮 **/
	private ImageView mCloseBtn;

	private RelativeLayout mT1Layout;
	private RelativeLayout mG2Layout;
	private RelativeLayout mG1Layout;
	private RelativeLayout mT1sLayout;
	private RelativeLayout mT2Layout;
	private RelativeLayout mT3Layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unbind_type_layout);
		EventBus.getDefault().register(this);
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
		mT1sLayout = (RelativeLayout) findViewById(R.id.goluk_t1s_layout);
		mT2Layout = (RelativeLayout) findViewById(R.id.goluk_t2_layout);
		mT3Layout = (RelativeLayout) findViewById(R.id.goluk_t3_layout);
	}

	/**
	 * 初始化view的监听
	 */
	private void initLisenner() {
		mCloseBtn.setOnClickListener(this);
		mT1Layout.setOnClickListener(this);
		mG2Layout.setOnClickListener(this);
		mG1Layout.setOnClickListener(this);
		mT1sLayout.setOnClickListener(this);
		mT2Layout.setOnClickListener(this);
		mT3Layout.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.close_btn) {
			this.finish();
		} else if (id == R.id.goluk_t1_layout) {
			click_BindIpc(IPCControlManager.MODEL_T, IPCControlManager.T1_SIGN);
		} else if (id == R.id.goluk_t1s_layout) {
			click_BindIpc(IPCControlManager.MODEL_T, IPCControlManager.T1s_SIGN);
		} else if (id == R.id.goluk_g2_layout) {
			click_BindIpc(IPCControlManager.MODEL_G, IPCControlManager.G2_SIGN);
		} else if (id == R.id.goluk_g1_layout) {
			click_BindIpc(IPCControlManager.MODEL_G, IPCControlManager.G1_SIGN);
		} else if (id == R.id.goluk_t2_layout) {
			click_BindIpc(IPCControlManager.MODEL_T, IPCControlManager.T2_SIGN);
		} else if (id == R.id.goluk_t3_layout) {
			click_BindIpc(IPCControlManager.MODEL_T, IPCControlManager.T3_SIGN);
		} else {
		}
	}

	/**
	 * 跳转到绑定界面
	 * 
	 * @param ipcType
	 *            用户选择的类型, G1,G2,T1
	 * @author jyf
	 */
	private void click_BindIpc(String ipcType, String realType) {
		Intent intent = new Intent(this, WiFiLinkListActivity.class);
		intent.putExtra(KEY_IPC_TYPE, ipcType);
		intent.putExtra(KEY_IPC_REAL_TYPE, realType);
		startActivity(intent);
	}

	public void onEventMainThread(EventFinishWifiActivity event) {
		GolukDebugUtils.e("", "completeSuccess-------------SelectType");
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

}
