package com.mobnote.golukmain.carrecorder.settings;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.IpcDataParser;
import com.mobnote.golukmain.carrecorder.base.CarRecordBaseActivity;
import com.mobnote.golukmain.carrecorder.entity.IPCIdentityState;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 *
 * 版本信息
 *
 * 2015年4月9日
 *
 * @author xuhw
 */
@SuppressLint("InflateParams")
public class VersionActivity extends CarRecordBaseActivity implements IPCManagerFn {
	/** Goluk设备编号 */
	private TextView mDeviceId = null;
	/** 固件版本号 */
	private TextView mVersion = null;
	/** ipc设备型号 **/
	private TextView mTextIpcModel = null;
	/****/
	private String mIpcModelName = "";
	private ImageView mIPCImage = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_version, null));
		setTitle(this.getResources().getString(R.string.my_version_title_text));

		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("carversion", this);
		}
		mIpcModelName = GolukApplication.getInstance().mIPCControlManager.mProduceName;
		mDeviceId = (TextView) findViewById(R.id.mDeviceId);
		mVersion = (TextView) findViewById(R.id.mVersion);
		mTextIpcModel = (TextView) findViewById(R.id.text_model);
		mIPCImage = (ImageView) findViewById(R.id.im_carrecorder_version_icon);

		mDeviceId.setText("");
		mVersion.setText("");
		mTextIpcModel.setText(this.getResources().getString(R.string.str_goluk) + mIpcModelName);
		if (IPCControlManager.T1_SIGN.equals(mIpcModelName) || IPCControlManager.T1s_SIGN.equals(mIpcModelName)
				|| IPCControlManager.T2_SIGN.equals(mIpcModelName) || IPCControlManager.T3_SIGN.equals(mIpcModelName) || IPCControlManager.T3U_SIGN.equals(mIpcModelName)) {
			mIPCImage.setImageResource(R.drawable.connect_t1_icon_1);
		} else {
			mIPCImage.setImageResource(R.drawable.ipc);
		}
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			boolean a = GolukApplication.getInstance().getIPCControlManager().getIPCIdentity();
			GolukDebugUtils.e("xuhw", "YYYYYY=======getIPCIdentity============a=" + a);

			boolean v = GolukApplication.getInstance().getIPCControlManager().getVersion();
			GolukDebugUtils.e("xuhw", "YYYYYY=======getVersion============v=" + v);
		}

		float density = SoundUtils.getInstance().getDisplayMetrics().density;
		int screedWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		LinearLayout idlayout = (LinearLayout) findViewById(R.id.idlayout);
		LinearLayout vsnlayout = (LinearLayout) findViewById(R.id.vsnlayout);
		int paddingLeft = (int) (screedWidth / 2 - 85 * density);
		idlayout.setPadding(paddingLeft, (int) (58 * density), 0, 0);
		vsnlayout.setPadding(paddingLeft, (int) (6 * density), 0, 0);

	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("xuhw", "YYYYYY====IPC_VDCP_Msg_GetIdentity====msg=" + msg + "===param1=" + param1
				+ "==param2=" + param2);

		if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
			if (msg == IPC_VDCP_Msg_GetIdentity) {
				if (param1 == RESULE_SUCESS) {
					final IPCIdentityState mVersionState = IpcDataParser.parseVersionState((String) param2);
					if (null != mVersionState) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mDeviceId.setText(mVersionState.name);
							}
						});
					}
				}
			} else if (IPC_VDCP_Msg_GetVersion == msg) {
				if (param1 == RESULE_SUCESS) {
					String str = (String) param2;
					if (TextUtils.isEmpty(str)) {
						return;
					}

					try {
						JSONObject json = new JSONObject(str);
						String version = json.optString("version");
						mVersion.setText(version);
					} catch (JSONException e) {
						e.printStackTrace();
					}

				}
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "carrecordversion");
	}

	public void exit() {
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("carversion");
		}
		finish();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.back_btn) {
			exit();
		} else {
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

}
