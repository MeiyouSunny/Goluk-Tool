package com.mobnote.golukmain.carrecorder.settings;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.base.CarRecordBaseActivity;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomFormatDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnLeftClickListener;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 *
 * 格式化SD卡
 *
 * 2015年4月9日
 *
 * @author xuhw
 */
public class FormatSDCardActivity extends CarRecordBaseActivity implements OnClickListener, IPCManagerFn {
	private CustomFormatDialog mCustomFormatDialog = null;

	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_format_sd_card, null));
		setTitle(this.getResources().getString(R.string.str_carrecorder_storage_format_sdcard));

		findViewById(R.id.mFormat).setOnClickListener(this);
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("format", this);
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		int id = v.getId();
		if (id == R.id.back_btn) {
			exit();
		} else if (id == R.id.mFormat) {
			CustomDialog dialog = new CustomDialog(this);
			dialog.setMessage(
					this.getResources().getString(R.string.str_carrecorder_storage_format_sdcard_dialog_message),
					Gravity.CENTER);
			dialog.setLeftButton(
					this.getResources().getString(R.string.str_carrecorder_storage_format_sdcard_dialog_yes),
					new OnLeftClickListener() {
						@Override
						public void onClickListener() {
							if (GolukApplication.getInstance().getIpcIsLogin()) {
								boolean flag = GolukApplication.getInstance().getIPCControlManager().formatDisk();
								GolukDebugUtils.e("xuhw", "YYYYYY=====formatDisk===flag=" + flag);
								if (flag) {
									// if(null == mCustomFormatDialog){
									mCustomFormatDialog = new CustomFormatDialog(FormatSDCardActivity.this);
									mCustomFormatDialog.setCancelable(false);
									mCustomFormatDialog.setMessage(getResources().getString(
											R.string.str_carrecorder_storage_format_sdcard_formating));
									mCustomFormatDialog.show();
									// }
								}
							}
						}
					});
			dialog.setRightButton(
					this.getResources().getString(R.string.str_carrecorder_storage_format_sdcard_dialog_no), null);
			dialog.show();
		} else {
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "formatsdcard");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
			if (msg == IPC_VDCP_Msg_FormatDisk) {
				if (null != mCustomFormatDialog && mCustomFormatDialog.isShowing()) {
					mCustomFormatDialog.dismiss();
				}
				GolukDebugUtils.e("xuhw", "YYYYYY====IPC_VDCP_Msg_FormatDisk====msg=" + msg + "===param1=" + param1
						+ "==param2=" + param2);
				String message = "";
				if (param1 == RESULE_SUCESS) {
					message = this.getResources().getString(R.string.str_carrecorder_storage_format_sdcard_success);
				} else {
					message = this.getResources().getString(R.string.str_carrecorder_storage_format_sdcard_fail);
				}
				CustomDialog dialog = new CustomDialog(this);
				dialog.setMessage(message, Gravity.CENTER);
				dialog.setLeftButton(this.getResources().getString(R.string.str_button_ok), null);
				dialog.show();
			}
		}
	}

	public void exit() {
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("format");
		}
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
