package com.rd.veuisdk.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rd.veuisdk.R;

public class ExtAdvancedProgressDialog extends Dialog {

    // 进度比例
    private TextView mProcessNum;
    // 剩余时间
    private TextView mMessage;
    // 进度条
    private ProgressBar mProgressBar;
    // 进度条最大值
    private int mMax = 100;
    // 进度条当前进度
    private int nProgress = 0;

    private boolean mIndeterminate;

    public ExtAdvancedProgressDialog(Context context) {
        super(context, R.style.dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogLayout = inflater.inflate(R.layout.rdveuisdk_dialog_advanced_progress,
                null);
        setContentView(dialogLayout);

        mProcessNum = (TextView) findViewById(R.id.tv_progressNum);
        mMessage = (TextView) findViewById(R.id.tv_message);
        mProgressBar = (ProgressBar) findViewById(R.id.import_progressBar);

        setIndeterminate(mIndeterminate);
        if (!mIndeterminate) {
            mProgressBar.setProgress(nProgress);
        }
        super.onCreate(savedInstanceState);

        LayoutParams lp = getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        this.onWindowAttributesChanged(lp);
    }

    public void setMessage(String message) {
        if (mMessage != null) {
            mMessage.setText(message);
        }
    }

    /**
     * @return the mMax
     */
    public int getMax() {
        return mMax;
    }

    /**
     * @param mMax the mMax to set
     */
    public void setMax(int mMax) {
        this.mMax = mMax;
    }

    public void setProgress(int progress) {

        // 如果0<=progress<=mMax
        progress = Math.min(mMax, progress);
        progress = Math.max(0, progress);
        nProgress = progress;
        if (mProgressBar != null) {
            mProgressBar.setMax(mMax);
            mProgressBar.setProgress(nProgress);

            mProcessNum.setText(nProgress + "%");
        }
    }

    public void setIndeterminate(boolean bIndeterminate) {

        mIndeterminate = bIndeterminate;
        if (mProgressBar != null) {
            mProgressBar.setIndeterminate(bIndeterminate);
        }
    }
}
