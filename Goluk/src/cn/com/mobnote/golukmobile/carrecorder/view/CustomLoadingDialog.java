package cn.com.mobnote.golukmobile.carrecorder.view;

import cn.com.mobnote.golukmobile.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;

public class CustomLoadingDialog{
	
	AlertDialog customDialog;
	
	AnimationDrawable ad ;
	
	public CustomLoadingDialog(Context context) {
		customDialog = new AlertDialog.Builder(context).create();
	}
	
	public void show(){
		customDialog.setOnShowListener(new OnShowListener() {
			
			@Override
			public void onShow(DialogInterface arg0) {
				// TODO Auto-generated method stub
				ImageView image = (ImageView) customDialog.getWindow().findViewById(R.id.loading_img);

				ad = (AnimationDrawable) image.getBackground();

				if (ad != null) {

					ad.start();

				}
			}
		});
		
		
		customDialog.show();
		customDialog.getWindow().setContentView(R.layout.video_square_loading);
		
	}
	
	public void close(){
		if(customDialog != null){
			if(customDialog.isShowing()){
				if(ad != null){
					ad.stop();
				}
				customDialog.dismiss();
			}
		}
	}
	
//	customDialog.setOnShowListener(new AlertDialog.OnShowListener() {
//
//		public void onShow(android.content.DialogInterface dialog) {
//
//			ImageView image = (ImageView) customDialog.findViewById(R.id.loading_img);
//
//			AnimationDrawable ad = (AnimationDrawable) image.getBackground();
//
//			if (ad != null) {
//
//				ad.start();
//
//			}
//
//		}
//
//	});

}
