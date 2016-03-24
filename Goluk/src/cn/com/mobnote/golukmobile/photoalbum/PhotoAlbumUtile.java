package cn.com.mobnote.golukmobile.photoalbum;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;

public class PhotoAlbumUtile {
	public static PhotoAlbumUtile photoAlbumUtile;
	
	public static PhotoAlbumUtile getInstall(){
		if(photoAlbumUtile == null){
			photoAlbumUtile = new PhotoAlbumUtile();
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

}
