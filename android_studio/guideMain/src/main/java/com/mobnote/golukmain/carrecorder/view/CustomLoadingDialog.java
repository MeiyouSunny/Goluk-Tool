package com.mobnote.golukmain.carrecorder.view;

import com.mobnote.golukmain.R;

import cn.com.tiros.debug.GolukDebugUtils;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnShowListener;
import android.graphics.drawable.AnimationDrawable;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomLoadingDialog {

	AlertDialog customDialog;
	String textTitle;
	AnimationDrawable ad;
	ForbidBack forbidInterface;
    ImageView mLoadingImageIV;
    TextView mLoadingTextTV;

	public CustomLoadingDialog(Context context, String txt) {
		customDialog = new AlertDialog.Builder(context, R.style.CustomDialog).create();
		if (txt != null) {
			textTitle = txt;
		}

	}
	
	public void setCancel(boolean isCan) {
		if (null != customDialog) {
			customDialog.setCancelable(isCan);
		}
	}

	public void setListener(ForbidBack forbidInterface) {
		if (null != forbidInterface) {
			this.forbidInterface = forbidInterface;
		}
	}

	private void setData(int key) {
		if (null != forbidInterface) {
			forbidInterface.forbidBackKey(key);
		}
	}

    public void setTextTitle(String title) {
        mLoadingTextTV.setText(title);
    }

	public void show() {
		customDialog.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface arg0) {
                mLoadingImageIV = (ImageView) customDialog.getWindow().findViewById(R.id.loading_img);
                mLoadingTextTV = (TextView) customDialog.getWindow().findViewById(R.id.loading_text);
				if (textTitle != null && !"".equals(textTitle)) {
                    mLoadingTextTV.setText(textTitle);
				}
				ad = (AnimationDrawable) mLoadingImageIV.getBackground();

				if (ad != null) {
					ad.start();
				}
			}
		});

		customDialog.setCanceledOnTouchOutside(false);
		customDialog.show();
		customDialog.getWindow().setContentView(R.layout.video_square_loading);

		customDialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {

				if (arg2.getAction() == KeyEvent.ACTION_UP) {
					GolukDebugUtils.e("", "------------------customDialog-------------back");
					setData(1);
				}
				return false;
			}
		});

	}

	public void close() {
		if (customDialog != null) {
			if (customDialog.isShowing()) {
				if (ad != null) {
					ad.stop();
				}
				customDialog.dismiss();
			}
		}
	}

	public boolean isShowing() {
		if (null != customDialog) {
			return customDialog.isShowing();
		}

		return false;
	}

	public interface ForbidBack {
		public static final int BACK_OK = 1;

		void forbidBackKey(int backKey);
	}

	// customDialog.setOnShowListener(new AlertDialog.OnShowListener() {
	//
	// public void onShow(android.content.DialogInterface dialog) {
	//
	// ImageView image = (ImageView)
	// customDialog.findViewById(R.id.loading_img);
	//
	// AnimationDrawable ad = (AnimationDrawable) image.getBackground();
	//
	// if (ad != null) {
	//
	// ad.start();
	//
	// }
	//
	// }
	//
	// });

}
