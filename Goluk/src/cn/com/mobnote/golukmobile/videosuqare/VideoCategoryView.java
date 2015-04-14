package cn.com.mobnote.golukmobile.videosuqare;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

@SuppressLint("InflateParams")
public class VideoCategoryView implements VideoSuqareManagerFn{
	private Context mContext=null;
	private RelativeLayout mRootLayout=null;
	
	public VideoCategoryView(Context context){
		mContext=context;
		mRootLayout = (RelativeLayout)LayoutInflater.from(mContext).inflate(R.layout.video_category, null); 
		
		
		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener("videocategory", this);
		boolean a = GolukApplication.getInstance().getVideoSquareManager().getSquareList();
		System.out.println("YYYY==22222==getSquareList======a="+a);
	}
	
	public View getView(){
		return mRootLayout;
	}
	
	public void onDestroy(){
		GolukApplication.getInstance().getVideoSquareManager().removeVideoSquareManagerListener("videocategory");
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,Object param2) {
		if(event == SquareCmd_Req_SquareList){
			if(RESULE_SUCESS == msg){
				System.out.println("YYY====getSquareList===33333=======msg="+msg+"===param2="+param2);
				
				
				
				
			}
		}
	}

}
