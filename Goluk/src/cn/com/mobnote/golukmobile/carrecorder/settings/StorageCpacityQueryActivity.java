package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.base.CarRecordBaseActivity;
import cn.com.mobnote.golukmobile.carrecorder.entity.RecordStorgeState;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomFormatDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
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
	/** 循环视频可用容量 */
	private TextView mCycleSize = null;
	/** 精彩视频可用容量 */
	private TextView mWonderfulSize = null;
	/** 紧急视频可用容量 */
	private TextView mEmergencySize = null;
	/** 其它可用容量 */
	private TextView mOtherSize = null;
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
		mCycleSize = (TextView) findViewById(R.id.mCycleSize);
		mWonderfulSize = (TextView) findViewById(R.id.mWonderfulSize);
		mEmergencySize = (TextView) findViewById(R.id.mEmergencySize);
		mOtherSize = (TextView) findViewById(R.id.mOtherSize);

		mTotalSize.setText("0GB");
		mUsedSize.setText("0MB");
		mCycleSize.setText("0GB");
		mWonderfulSize.setText("0MB");
		mEmergencySize.setText("0MB");
		mOtherSize.setText("0MB");

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
						mCycleSize.setText(getSize(cyclesize));
						mWonderfulSize.setText(getSize(wonderfulsize));
						mEmergencySize.setText(getSize(emergencysize));
						mOtherSize.setText(getSize(picsize));

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
		finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_btn:
			exit();
			break;
		case R.id.mFormatSDCard:
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
			break;

		default:
			break;
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
