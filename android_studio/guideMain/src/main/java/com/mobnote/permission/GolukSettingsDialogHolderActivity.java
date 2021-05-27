package com.mobnote.permission;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by liumin on 2018/3/19.
 */

public class GolukSettingsDialogHolderActivity extends AppCompatActivity implements DialogInterface.OnClickListener {
    private static final int APP_SETTINGS_RC = 7534;

    private AlertDialog mDialog;
    private GolukSettingDialog mGolukSettingDialog;

    public static Intent createShowDialogIntent(Context context, GolukSettingDialog dialog) {
        return new Intent(context, GolukSettingsDialogHolderActivity.class)
                .putExtra(GolukSettingDialog.EXTRA_APP_SETTINGS, dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGolukSettingDialog = GolukSettingDialog.fromIntent(getIntent(), this);
        mDialog = mGolukSettingDialog.showDialog(this, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    private void startPermissionSettingActivity() {
        if (mGolukSettingDialog != null) {
            if (mGolukSettingDialog.getRequestCode() == GolukPermissionUtils.CODE_REQUEST_PERMISSION) {
                startActivityForResult(
                        new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS),
                        APP_SETTINGS_RC);
            }
        }
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == Dialog.BUTTON_POSITIVE) {
            startPermissionSettingActivity();
        } else if (which == Dialog.BUTTON_NEGATIVE) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        } else {
            throw new IllegalStateException("Unknown button type: " + which);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        setResult(resultCode, data);
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
