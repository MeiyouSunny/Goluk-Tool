package cn.com.mobnote.golukmobile.videosuqare;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class VideoSquareListViewAdapter extends BaseAdapter{
	private Context mContext=null;
	private List<VideoSquareInfo> mVideoSquareListData=null;
	private int count=0;
		
	public VideoSquareListViewAdapter(Context context){
		mContext=context;
		mVideoSquareListData= new ArrayList<VideoSquareInfo>();
	}
	
	public void setData(List<VideoSquareInfo> data){
		mVideoSquareListData.addAll(data);
		count = mVideoSquareListData.size();
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup parent) {
		VideoSquareInfo mVideoSquareInfo = mVideoSquareListData.get(arg0);
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.video_square_list_item, null); 
		}
		
		TextView username = (TextView)convertView.findViewById(R.id.username);
		username.setText(mVideoSquareInfo.mUserEntity.nickname);
		
		SurfaceView mSurfaceView = (SurfaceView)convertView.findViewById(R.id.mSurfaceView);
		SurfaceHolder mSurfaceHolder = mSurfaceView.getHolder();
		
		
		int width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		int height = (int)((float)width/1.77f);
		
		RelativeLayout mPlayerLayout = (RelativeLayout)convertView.findViewById(R.id.mPlayerLayout);
		LinearLayout.LayoutParams mPlayerLayoutParams = new LinearLayout.LayoutParams(width, height);
		mPlayerLayout.setLayoutParams(mPlayerLayoutParams);
		
		
		mSurfaceHolder.addCallback(new Callback() {
			@Override
			public void surfaceDestroyed(SurfaceHolder arg0) {
				LogUtils.d("SSS============surfaceDestroyed==========");
			}
			
			@Override
			public void surfaceCreated(SurfaceHolder arg0) {
				LogUtils.d("SSS============surfaceCreated==========");
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
				
			}
		});
	
		
		
		return convertView;
	}
	
	public void onDestroy(){
		
	}

}
