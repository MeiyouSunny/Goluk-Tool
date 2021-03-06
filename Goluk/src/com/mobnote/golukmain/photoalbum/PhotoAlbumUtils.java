package com.mobnote.golukmain.photoalbum;

import java.util.List;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PhotoAlbumUtils {
	public static PhotoAlbumUtils photoAlbumUtile;
	
	public static PhotoAlbumUtils getInstall(){
		if(photoAlbumUtile == null){
			photoAlbumUtile = new PhotoAlbumUtils();
		}
		return photoAlbumUtile;
	}
	
	/**
	 * 获取listView的EmptyView
	 * @param context
	 * @param type
	 * @return
	 */
	public View getEmptyView(Context context,int type){
		
		TextView iv = new TextView(context);
		
		Drawable topDrawable = null;
		
		if(type == 0){//本地
			topDrawable = context.getResources().getDrawable(R.drawable.album_img_novideo); 
			iv.setText(context.getResources().getString(R.string.photoalbum_no_video_text));
		}else {
			if(GolukApplication.getInstance().isIpcLoginSuccess){
				if(type == 1){//精彩
					topDrawable = context.getResources().getDrawable(R.drawable.album_img_novideo); 
				}else if(type == 2){//紧急
					topDrawable = context.getResources().getDrawable(R.drawable.album_img_novideo); 
				}else if(type == 3){//循环
					topDrawable = context.getResources().getDrawable(R.drawable.album_img_novideo); 
				}
				iv.setText(context.getResources().getString(R.string.photoalbum_no_video_text));
			}else{
				topDrawable = context.getResources().getDrawable(R.drawable.finish_t1_pic_1); 
				iv.setText(context.getResources().getString(R.string.str_album_no_connect));
			}
		}
		
		topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(), topDrawable.getMinimumHeight());  
		iv.setCompoundDrawables(null, topDrawable, null, null);
		
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		iv.setGravity(Gravity.CENTER);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		iv.setLayoutParams(lp);
		
		return iv;
	}
	
	/**
	 * 查询文件录制起始时间
	 * @param filename
	 * @param list
	 * @return
	 */
	public static long findtime(String filename,List<VideoInfo> list) {
		long time = 0;
		if (null != list) {
			for (int i = 0; i < list.size(); i++) {
				if (filename.equals(list.get(i).filename)) {
					return list.get(i).time;
				}
			}
		}
		return time;
	}

}
