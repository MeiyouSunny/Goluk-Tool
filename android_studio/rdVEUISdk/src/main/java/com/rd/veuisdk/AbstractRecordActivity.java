package com.rd.veuisdk;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.rd.gallery.ImageManager;
import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 录制基础类，主要包含 检查权限
 *
 * @create 2019/8/1
 */
public abstract class AbstractRecordActivity extends BaseActivity {
    protected Dialog mDlgCameraFailed;
    protected boolean permissionGranted = false;
    private final int REQUEST_CODE_PERMISSIONS = 100;
    protected boolean bRecordPrepared = false;
    protected boolean bCameraPrepared = false;

    @Override
    protected void onStart() {
        super.onStart();
        checkSdDialog();
        if (permissionGranted) {
            bRecordPrepared = false;
            onCameraPermissionGranted();
        }
    }


    /**
     * 检查授权
     */
    public void checkPermission() {
        permissionGranted = false;
        //先确保相机录音权限,再初始化摄像头
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.RECORD_AUDIO);
            }
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.CAMERA);
            }
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE_PERMISSIONS);
            } else {
                permissionGranted = true;
            }
        } else {
            permissionGranted = true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSIONS: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        if (permissions[i] == Manifest.permission.CAMERA) {
                            onToast(R.string.permission_camera_error);
                        } else if (permissions[i] == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                            onToast(R.string.permission_external_storage_error);
                        } else {
                            onToast(R.string.permission_audio_error);
                        }
                        finish();
                        return;
                    }
                }
                permissionGranted = true;
                onCameraPermissionGranted();
            }
            break;
            default:
                break;
        }
    }

    /**
     * 授权成功
     */
    public abstract void onCameraPermissionGranted();

    /**
     * 准备打开摄像头时，核心返回权限失败
     */
    protected void onCameraPermissionFailed() {
        if (null != mDlgCameraFailed) {
            mDlgCameraFailed.dismiss();
            mDlgCameraFailed = null;
        }
        mDlgCameraFailed = SysAlertDialog.showAlertDialog(
                this,
                getString(R.string.dialog_tips),
                getString(R.string.permission_camera_error_p_allow),
                getString(R.string.exit),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        AbstractRecordActivity.this.finish();
                    }
                }, getString(R.string.setting),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        Utils.gotoAppInfo(AbstractRecordActivity.this,
                                AbstractRecordActivity.this
                                        .getPackageName());
                    }
                });
        mDlgCameraFailed.setCancelable(false);
        mDlgCameraFailed.setCanceledOnTouchOutside(false);
    }


    /**
     * 录制核心检测target>23 ,未授权
     */
    protected void onCoreCheckPermissionDenied(String strResultInfo) {
        Dialog permissionDialog = SysAlertDialog.showAlertDialog(
                this,
                getString(R.string.dialog_tips),
                strResultInfo,
                getString(R.string.exit),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        AbstractRecordActivity.this.finish();
                    }
                }, getString(R.string.setting),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        CoreUtils.gotoAppInfo(AbstractRecordActivity.this,
                                AbstractRecordActivity.this
                                        .getPackageName());
                    }
                });
        permissionDialog.setCancelable(false);
        permissionDialog.setCanceledOnTouchOutside(false);
    }


    /**
     * 关闭dialog
     */
    protected void closeCameraFailedDialog() {
        if (null != mDlgCameraFailed) {
            mDlgCameraFailed.dismiss();
            mDlgCameraFailed = null;
        }
    }

    /**
     * 检查sd是否存在
     */
    protected void checkSdDialog() {
        if (!ImageManager.hasStorage()) {
            Dialog dlg = SysAlertDialog.showAlertDialog(this,
                    R.string.app_name, R.string.record_no_external_storage,
                    android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }, -1, null);
            dlg.setCancelable(false);
            dlg.setCanceledOnTouchOutside(false);
        }
    }

    /**
     * 切换摄像头
     */
    public abstract void onSwitchCameraButtonClick();


    /**
     * 闪光灯
     */
    public abstract void onFlashModeClick();

    /**
     * 检测闪光灯和摄像头方向
     */
    public abstract void checkFlashMode();

    private Runnable mCheckFlashModeRunnable = new Runnable() {

        @Override
        public void run() {
            checkFlashMode();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        CoreUtils.hideVirtualBar(this);
        mHandler.postDelayed(mCheckFlashModeRunnable, 300);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mCheckFlashModeRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mCheckFlashModeRunnable);
    }

    protected Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                default: {
                }
                break;
            }
        }
    };


}
