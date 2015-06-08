package cn.com.mobnote.golukmobile.carrecorder.view;

import cn.com.mobnote.golukmobile.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomLoadingDialog {

	AlertDialog customDialog;
	String textTitle;
	AnimationDrawable ad;

	public CustomLoadingDialog(Context context, String txt) {
		customDialog = new AlertDialog.Builder(context, R.style.CustomDialog).create();
		if (txt != null) {
			textTitle = txt;
		}

	}

	public void show() {
		customDialog.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface arg0) {
				ImageView image = (ImageView) customDialog.getWindow().findViewById(R.id.loading_img);
				TextView txt = (TextView) customDialog.getWindow().findViewById(R.id.loading_text);
				if (textTitle != null && !"".equals(textTitle)) {
					txt.setText(textTitle);
				}
				ad = (AnimationDrawable) image.getBackground();

				if (ad != null) {

					ad.start();

				}
			}
		});

		customDialog.show();
		customDialog.getWindow().setContentView(R.layout.video_square_loading);

	}

	public void close() {
		if (customDialog != null) {
			if (customDialog.isShowing()) {
				if (ad != null) {
					ad.stop();
					customDialog.dismiss();
				}
			}
		}
	}

	public boolean isShowing() {
		if (null != customDialog) {
			return customDialog.isShowing();
		}

		return false;
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
