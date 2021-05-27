package com.mobnote.permission;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;

import com.mobnote.golukmain.R;

import java.util.List;

import androidx.annotation.StringRes;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 权限管理工具，使用的EasyPermission
 * Created by liumin on 2018/3/20.
 */

public class GolukPermissionUtils {

    public static final int CODE_REQUEST_PERMISSION = 8000;

    public static final int CODE_REQUEST_CAMERA_PERMISSION = 8001;
    public static final int CODE_REQUEST_SETTING_CAMERA_PERMISSION = 8002;


    public static boolean hasIndispensablePermission(Context context){
        return GolukPermissionUtils.hasExternalStoragePermission(context);
//                && GolukPermissionUtils.hasLocationPermission(context)
//                && GolukPermissionUtils.hasReadPhoneStatePermission(context);
    }

    public static boolean hasExternalStoragePermission(Context context) {
        return EasyPermissions.hasPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean hasLocationPermission(Context context) {
        return EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                && EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION);
    }


    public static boolean hasCameraPermission(Context context) {
        return EasyPermissions.hasPermissions(context, Manifest.permission.CAMERA);
    }

    public static boolean hasReadPhoneStatePermission(Context context) {
        return EasyPermissions.hasPermissions(context, Manifest.permission.READ_PHONE_STATE);
    }

    @SuppressLint("RestrictedApi")
    public static void requestPermissions(Object target, String[] permissions) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
//        if (target instanceof Activity) {
//            PermissionHelper
//                    .newInstance((Activity) target)
//                    .directRequestPermissions(CODE_REQUEST_PERMISSION,
//                            permissions);
//        } else if (target instanceof Fragment) {
//            PermissionHelper
//                    .newInstance((Fragment) target)
//                    .directRequestPermissions(CODE_REQUEST_PERMISSION,
//                            permissions);
//        } else if (target instanceof android.app.Fragment) {
//            PermissionHelper
//                    .newInstance((android.app.Fragment) target)
//                    .directRequestPermissions(CODE_REQUEST_PERMISSION,
//                            permissions);
//        }
    }

    @SuppressLint("RestrictedApi")
    public static void requestCameraPermission(Object target) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
//        if (target instanceof Activity) {
//            PermissionHelper
//                    .newInstance((Activity) target)
//                    .directRequestPermissions(CODE_REQUEST_CAMERA_PERMISSION, Manifest.permission.CAMERA);
//        } else if (target instanceof Fragment) {
//            PermissionHelper
//                    .newInstance((Fragment) target)
//                    .directRequestPermissions(CODE_REQUEST_CAMERA_PERMISSION, Manifest.permission.CAMERA);
//        } else if (target instanceof android.app.Fragment) {
//            PermissionHelper
//                    .newInstance((android.app.Fragment) target)
//                    .directRequestPermissions(CODE_REQUEST_CAMERA_PERMISSION, Manifest.permission.CAMERA);
//        }
    }

    public static void handlePermissionPermanentlyDenied(Object host, List<String> perms) {
        handlePermissionPermanentlyDenied(host, perms, R.string.permission_hint_msg);
    }

    /**
     * Android系统权限提示，第一次拒绝后，第二次提示权限才会出现“不再提示”的选项。
     * 勾选不再提示，host的shouldShowRequestPermissionRationale返回false，由此判断用户是否勾选“不再提示选项”；
     *
     * @param host
     * @param perms
     */
    public static void handlePermissionPermanentlyDenied(Object host, List<String> perms,@StringRes int msgRes) {
//        boolean isPermissionPermanentlyDenied = false;
//        GolukSettingDialog.Builder builder = null;
//        if (host instanceof Activity) {
//            isPermissionPermanentlyDenied = EasyPermissions.somePermissionDenied((Activity) host, perms.toArray(new String[]{}))
//                    || EasyPermissions.somePermissionPermanentlyDenied((Activity) host, perms);
//            builder = new GolukSettingDialog.Builder((Activity) host);
//        } else if (host instanceof Fragment) {
//            isPermissionPermanentlyDenied = EasyPermissions.somePermissionDenied((Fragment) host, perms.toArray(new String[]{}))
//                    || EasyPermissions.somePermissionPermanentlyDenied((Fragment) host, perms);
//            builder = new GolukSettingDialog.Builder((Fragment) host);
//        } else if (host instanceof android.app.Fragment) {
//            isPermissionPermanentlyDenied = EasyPermissions.somePermissionDenied((android.app.Fragment) host, perms.toArray(new String[]{}))
//                    || EasyPermissions.somePermissionPermanentlyDenied((android.app.Fragment) host, perms);
//            builder = new GolukSettingDialog.Builder((android.app.Fragment) host);
//        } else {
////            throw new IllegalArgumentException("host must be a sub Class of Activity or Fragment");
//            return;
//        }
//        if (isPermissionPermanentlyDenied) {
//            builder.setRequestCode(CODE_REQUEST_PERMISSION)
//                    .setTitle(" ")
//                    .setRationale(msgRes)
//                    .setNegativeButton(R.string.cancel)
//                    .setPositiveButton(R.string.setting)
//                    .build()
//                    .show();
//        }
    }

}
