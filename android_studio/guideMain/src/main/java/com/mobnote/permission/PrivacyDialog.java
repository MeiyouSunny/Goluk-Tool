package com.mobnote.permission;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserOpenUrlActivity;

import androidx.appcompat.app.AlertDialog;

public class PrivacyDialog {

    public void showClauseDialog(final Context context, final OnPrivacySelectListener listener) {
        final View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_privacy, null);
        final Dialog dialog = new AlertDialog
                .Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .show();
        final WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        params.width = Math.round(width * 4 / 5);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);

        dialogView.findViewById(R.id.btn_agree).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onAgreePrivacy();
                }
            }
        });
        dialogView.findViewById(R.id.btn_disagree).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Process.killProcess(Process.myPid());
            }
        });

        dialogView.findViewById(R.id.agreement).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mBugLayout = new Intent(context, UserOpenUrlActivity.class);
                mBugLayout.putExtra(UserOpenUrlActivity.FROM_TAG, "agreement");
                context.startActivity(mBugLayout);
            }
        });
        dialogView.findViewById(R.id.privacy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mBugLayout = new Intent(context, UserOpenUrlActivity.class);
                mBugLayout.putExtra(UserOpenUrlActivity.FROM_TAG, "privacy");
                context.startActivity(mBugLayout);
            }
        });

        dialog.show();
    }

    public interface OnPrivacySelectListener {
        void onAgreePrivacy();
    }

}
