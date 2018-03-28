package com.mobnote.golukmain.carrecorder.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IpcDataParser;
import com.mobnote.golukmain.carrecorder.base.CarRecordBaseActivity;
import com.mobnote.golukmain.carrecorder.entity.RecordStorgeState;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnLeftClickListener;
import com.mobnote.golukmain.carrecorder.view.CustomFormatDialog;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 *
 * 容量查询
 *
 * 2015年4月7日
 *
 * @author xuhw
 */
@SuppressLint("InflateParams")
public class StorageCpacityQueryActivity extends CarRecordBaseActivity implements IPCManagerFn, OnClickListener {
	/** SD卡总容量 */
	private TextView mTotalSize = null;
	/** 已用容量 */
	private TextView mUsedSize = null;
	private CustomFormatDialog mCustomFormatDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("storage", this);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_storage_cpacity_query, null));
		setTitle(this.getResources().getString(R.string.rlcx_title));

		initView();
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			boolean flag = GolukApplication.getInstance().getIPCControlManager().queryRecordStorageStatus();
			GolukDebugUtils.e("xuhw", "YYY======queryRecordStorageStatus=====flag=" + flag);
			if (!flag) {

			}
		}
	}

	/**
	 * 初始化控件
	 * 
	 * @author xuhw
	 * @date 2015年4月7日
	 */
	private void initView() {
		mTotalSize = (TextView) findViewById(R.id.mTotalSize);
		mUsedSize = (TextView) findViewById(R.id.mUsedSize);

		mTotalSize.setText(this.getResources().getString(R.string.str_zero_gb_text));
		mUsedSize.setText(this.getResources().getString(R.string.str_zero_mb_text));

		findViewById(R.id.mFormatSDCard).setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
			if (msg == IPC_VDCP_Msg_RecPicUsage) {
				GolukDebugUtils.e("xuhw", "YYY===========11111111========param1=" + param1 + "====param2==" + param2);
				if (param1 == RESULE_SUCESS) {
					RecordStorgeState mRecordStorgeState = IpcDataParser.parseRecordStorageStatus((String) param2);
					if (null != mRecordStorgeState) {
						double usedsize = mRecordStorgeState.totalSdSize - mRecordStorgeState.leftSize;
						double cyclesize = mRecordStorgeState.normalRecQuota - mRecordStorgeState.normalRecSize;
						double wonderfulsize = mRecordStorgeState.wonderfulRecQuota
								- mRecordStorgeState.wonderfulRecSize;
						double emergencysize = mRecordStorgeState.urgentRecQuota - mRecordStorgeState.urgentRecSize;
						double picsize = mRecordStorgeState.picQuota - mRecordStorgeState.picSize;

						mTotalSize.setText(getSize(mRecordStorgeState.totalSdSize));
						mUsedSize.setText(getSize(usedsize));

						GolukDebugUtils.e("xuhw", "YYY===========２２２２２=========normalRecQuota="
								+ mRecordStorgeState.normalRecQuota + "=====normalRecSize="
								+ mRecordStorgeState.normalRecSize);
					}

				} else {
					GolukDebugUtils.e("xuhw", "YYY===========３３３３３===============");
				}
			} else if (msg == IPC_VDCP_Msg_FormatDisk) {
				if (null != mCustomFormatDialog && mCustomFormatDialog.isShowing()) {
					mCustomFormatDialog.dismiss();
				}
				GolukDebugUtils.e("xuhw", "YYYYYY====IPC_VDCP_Msg_FormatDisk====msg=" + msg + "===param1=" + param1
						+ "==param2=" + param2);
				String message = "";
				if (param1 == RESULE_SUCESS) {
					message = this.getResources().getString(R.string.str_carrecorder_storage_format_sdcard_success);
					GolukApplication.getInstance().getIPCControlManager().queryRecordStorageStatus();
				} else {
					message = this.getResources().getString(R.string.str_carrecorder_storage_format_sdcard_fail);
				}
				CustomDialog dialog = new CustomDialog(this);
				dialog.setMessage(message, Gravity.CENTER);
				dialog.setLeftButton(this.getResources().getString(R.string.user_repwd_ok), null);
				dialog.show();
			}

		}
	}

	/**
	 * 容量大小转字符串
	 * 
	 * @param size
	 *            容量大小
	 * @return
	 * @author xuhw
	 * @date 2015年4月11日
	 */
	private String getSize(double size) {
		String result = "";
		double totalsize = 0;

		java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
		if (size >= 1024) {
			totalsize = size / 1024;
			result = df.format(totalsize) + "GB";
		} else {
			totalsize = size;
			result = df.format(totalsize) + "MB";
		}

		return result;
	}

	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, "storagecpacityquery");
	}

	public void exit() {
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("storage");
		}
		if (null != mCustomFormatDialog && mCustomFormatDialog.isShowing()) {
			mCustomFormatDialog.dismiss();
		}
		mCustomFormatDialog = null;
		finish();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.back_btn) {
			exit();
		} else if (id == R.id.mFormatSDCard) {
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
									mCustomFormatDialog = new CustomFormatDialog(StorageCpacityQueryActivity.this);
									mCustomFormatDialog.setCancelable(false);
									mCustomFormatDialog.setMessage(StorageCpacityQueryActivity.this.getResources()
											.getString(R.string.str_carrecorder_storage_format_sdcard_formating));
									mCustomFormatDialog.show();
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

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

}
