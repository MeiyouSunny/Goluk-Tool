package com.mobnote.golukmain.carrecorder.settings;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.base.CarRecordBaseActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 *
 * 碰撞感应灵敏度控制
 *
 * 2015年4月6日
 *
 * @author xuhw
 */
@SuppressLint("InflateParams")
public class ImpactSensitivityActivity extends CarRecordBaseActivity implements OnClickListener, IPCManagerFn {
	private TextView mCloseText = null;
	private TextView mLowText = null;
	private TextView mMiddleText = null;
	private TextView mHighText = null;

	private ImageButton mCloseIcon = null;
	private ImageButton mLowIcon = null;
	private ImageButton mMiddleIcon = null;
	private ImageButton mHighIcon = null;
	private int policy = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_impact_sensitivity, null));
		setTitle(this.getResources().getString(R.string.pzgy_title));

		initView();
		GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("sensitivity", this);
		boolean flag = GolukApplication.getInstance().getIPCControlManager().getGSensorControlCfg();
		GolukDebugUtils.e("xuhw", "YYYYY===getIPCControlManager============getGSensorControlCfg======flag=" + flag);
	}

	/**
	 * 初始化控件
	 * 
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void initView() {
		findViewById(R.id.close).setOnClickListener(this);
		findViewById(R.id.low).setOnClickListener(this);
		findViewById(R.id.middle).setOnClickListener(this);
		findViewById(R.id.high).setOnClickListener(this);

		mCloseText = (TextView) findViewById(R.id.closeText);
		mLowText = (TextView) findViewById(R.id.lowText);
		mMiddleText = (TextView) findViewById(R.id.middleText);
		mHighText = (TextView) findViewById(R.id.highText);
		mCloseIcon = (ImageButton) findViewById(R.id.cRight);
		mLowIcon = (ImageButton) findViewById(R.id.dRight);
		mMiddleIcon = (ImageButton) findViewById(R.id.zRight);
		mHighIcon = (ImageButton) findViewById(R.id.gRight);

	}

	/**
	 * 切换碰撞灵敏度
	 * 
	 * @param type
	 *            灵敏度类型
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void updateSensitivity(int _policy) {
		policy = _policy;
		mCloseText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		mLowText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		mMiddleText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		mHighText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		mCloseIcon.setVisibility(View.GONE);
		mLowIcon.setVisibility(View.GONE);
		mMiddleIcon.setVisibility(View.GONE);
		mHighIcon.setVisibility(View.GONE);

		if (0 == policy) {
			mCloseIcon.setVisibility(View.VISIBLE);
			mCloseText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		} else if (1 == policy) {
			mLowIcon.setVisibility(View.VISIBLE);
			mLowText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		} else if (2 == policy) {
			mMiddleIcon.setVisibility(View.VISIBLE);
			mMiddleText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		} else {
			mHighIcon.setVisibility(View.VISIBLE);
			mHighText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		int id = v.getId();
		if (id == R.id.back_btn) {
			exit();
		} else if (id == R.id.close) {
			updateSensitivity(0);
		} else if (id == R.id.low) {
			updateSensitivity(1);
		} else if (id == R.id.middle) {
			updateSensitivity(2);
		} else if (id == R.id.high) {
			updateSensitivity(3);
		} else {
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "impactsensitivity");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
			if (msg == IPC_VDCP_Msg_GetGSensorControlCfg) {
				GolukDebugUtils.e("xuhw", "YYYYY===GSensor====event=" + event + "==msg=" + msg + "===param1=" + param1
						+ "==param2=" + param2);
				if (param1 == RESULE_SUCESS) {
					try {
						JSONObject json = new JSONObject((String) param2);
						policy = json.optInt("policy");
						updateSensitivity(policy);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void exit() {
		boolean flag = GolukApplication.getInstance().getIPCControlManager().setGSensorControlCfg(policy);
		GolukDebugUtils.e("xuhw", "YYYYY====setGSensorControlCfg===policy=" + policy + "==flag=" + flag);
		GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("sensitivity");
		finish();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

}
