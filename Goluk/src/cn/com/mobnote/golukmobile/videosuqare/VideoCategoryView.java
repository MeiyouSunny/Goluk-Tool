package cn.com.mobnote.golukmobile.videosuqare;


import cn.com.mobnote.golukmobile.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

@SuppressLint("InflateParams")
public class VideoCategoryView {
	private Context mContext=null;
	private RelativeLayout mRootLayout=null;
	
	public VideoCategoryView(Context context){
		mContext=context;
		mRootLayout = (RelativeLayout)LayoutInflater.from(mContext).inflate(R.layout.video_category, null); 
	}
	
	public View getView(){
		return mRootLayout;
	}

}
