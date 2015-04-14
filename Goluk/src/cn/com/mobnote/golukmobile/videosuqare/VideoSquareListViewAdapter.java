package cn.com.mobnote.golukmobile.videosuqare;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.golukmobile.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

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
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.video_square_list_item, null); 
		}
		
		return convertView;
	}
	
	public void onDestroy(){
		
	}

}
