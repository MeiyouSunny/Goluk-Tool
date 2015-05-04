package cn.com.mobnote.golukmobile.carrecorder;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.entity.DoubleVideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class IPCFileAdapter extends BaseAdapter implements StickyListHeadersAdapter {
	private Context mContext=null;
	private LayoutInflater inflater;
	private List<DoubleVideoInfo> mDataList;
	private List<String> mGroupNameList;
	private int count=0;
	private int screenWidth=0;
	private float density;
	
	public IPCFileAdapter(Context c){
		mContext=c;
		inflater = LayoutInflater.from(c);
		mDataList=new ArrayList<DoubleVideoInfo>();
		mGroupNameList = new ArrayList<String>();
		screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		density = SoundUtils.getInstance().getDisplayMetrics().density;
	}
	
	public void setData(List<String> groupname, List<DoubleVideoInfo> data){
		mDataList.clear();
		mDataList.addAll(data);
		mGroupNameList.addAll(groupname);
		count = mDataList.size();
		this.notifyDataSetChanged();
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.carrecorder_videolist_item, parent, false);
			holder.mVideoLayout1 = (RelativeLayout)convertView.findViewById(R.id.mVideoLayout1);
			holder.mVideoLayout2 = (RelativeLayout)convertView.findViewById(R.id.mVideoLayout2);
			holder.mTMLayout1 = (RelativeLayout)convertView.findViewById(R.id.mTMLayout1);
			holder.mTMLayout2 = (RelativeLayout)convertView.findViewById(R.id.mTMLayout2);
			holder.image1 = (ImageView)convertView.findViewById(R.id.video_first_needle1);
			holder.image2 = (ImageView)convertView.findViewById(R.id.video_first_needle2);
			holder.mVideoCountTime1 = (TextView)convertView.findViewById(R.id.video_countTime1);
			holder.mVideoCountTime2 = (TextView)convertView.findViewById(R.id.video_countTime2);
			holder.mVideoQuality1 = (ImageView)convertView.findViewById(R.id.video_quality1);
			holder.mVideoQuality2 = (ImageView)convertView.findViewById(R.id.video_quality2);
			holder.mVideoCreateTime1 = (TextView)convertView.findViewById(R.id.video_createtime1);
			holder.mVideoCreateTime2 = (TextView)convertView.findViewById(R.id.video_createtime2);
			holder.mVideoSize1 = (TextView)convertView.findViewById(R.id.video_size1);
			holder.mVideoSize2 = (TextView)convertView.findViewById(R.id.video_size2);
			
			int width = (int)(screenWidth-24*density)/2;
			int height = (int)((float)width/1.77f);
			RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(width, height);
			RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(width, height);
			layoutParams1.setMargins((int)(8*density), (int)(8*density), (int)(8*density), 0);
			layoutParams2.setMargins(0, (int)(8*density), 0, 0);
			layoutParams2.addRule(RelativeLayout.RIGHT_OF, R.id.mVideoLayout1);
			holder.mVideoLayout1.setLayoutParams(layoutParams1);
			holder.mVideoLayout2.setLayoutParams(layoutParams2);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		IPCFileManagerActivity a = (IPCFileManagerActivity)mContext;
		List<String> selectedData = a.getSelectedListData();
		if(!a.getIsEditState()){
			holder.mTMLayout1.setVisibility(View.GONE);
			holder.mTMLayout2.setVisibility(View.GONE);
		}else{
			
			
		}
		
		holder.mVideoLayout2.setVisibility(View.GONE);
		VideoInfo mVideoInfo1 = mDataList.get(position).getVideoInfo1();
		VideoInfo mVideoInfo2 = mDataList.get(position).getVideoInfo2();
		holder.mTMLayout1.setTag(mVideoInfo1.videoPath);
		holder.mTMLayout2.setTag("");
		if(selectedData.contains(mVideoInfo1.videoPath)){
			holder.mTMLayout1.setVisibility(View.VISIBLE);
		}else{
			holder.mTMLayout1.setVisibility(View.GONE);
		}
		if(mVideoInfo1.videoHP.contains("1080")){
			holder.mVideoQuality1.setBackgroundResource(R.drawable.carrecorder_icon_1080);
		}else if(mVideoInfo1.videoHP.contains("720")){
			holder.mVideoQuality1.setBackgroundResource(R.drawable.carrecorder_icon_720);
		}else{
			holder.mVideoQuality1.setBackgroundResource(R.drawable.carrecorder_icon_480);
		}
		holder.mVideoCountTime1.setText(mVideoInfo1.countTime);
		holder.mVideoCreateTime1.setText(mVideoInfo1.videoCreateDate);
		holder.mVideoSize1.setText(mVideoInfo1.videoSize);
		
		holder.image1.setBackgroundResource(R.drawable.carrecorder_xcjlybj);
		Bitmap videoBitmap1 = mDataList.get(position).getVideoInfo1().videoBitmap;
		if (null != videoBitmap1) {
			BitmapDrawable bd = new BitmapDrawable(videoBitmap1);
			holder.image1.setBackgroundDrawable(bd);
		}
		
		if(null != mVideoInfo2){
			if(selectedData.contains(mVideoInfo2.videoPath)){
				holder.mTMLayout2.setVisibility(View.VISIBLE);
			}else{
				holder.mTMLayout2.setVisibility(View.GONE);
			}
			holder.mTMLayout2.setTag(mVideoInfo2.videoPath);
			holder.mVideoLayout2.setVisibility(View.VISIBLE);
			if(mVideoInfo2.videoHP.contains("1080")){
				holder.mVideoQuality1.setBackgroundResource(R.drawable.carrecorder_icon_1080);
			}else if(mVideoInfo2.videoHP.contains("720")){
				holder.mVideoQuality1.setBackgroundResource(R.drawable.carrecorder_icon_720);
			}else{
				holder.mVideoQuality1.setBackgroundResource(R.drawable.carrecorder_icon_480);
			}
			
			holder.mVideoCountTime2.setText(mVideoInfo2.countTime);
			holder.mVideoCreateTime2.setText(mVideoInfo2.videoCreateDate);
			holder.mVideoSize2.setText(mVideoInfo2.videoSize);
		
			holder.image2.setBackgroundResource(R.drawable.carrecorder_xcjlybj);
			Bitmap videoBitmap2 = mDataList.get(position).getVideoInfo2().videoBitmap;
			if (null != videoBitmap2) {
				BitmapDrawable bd = new BitmapDrawable(videoBitmap2);
				holder.image2.setBackgroundDrawable(bd);
			}
		}
		


		return convertView;
	}

	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderViewHolder holder;
		if (convertView == null) {
			holder = new HeaderViewHolder();
			convertView = inflater.inflate(R.layout.carrecorder_datatitle, parent, false);
			holder.title = (TextView) convertView.findViewById(R.id.title);
			convertView.setTag(holder);
		} else {
			holder = (HeaderViewHolder) convertView.getTag();
		}
		
		String headerText ="";
		for(int i=0;i<mGroupNameList.size();i++){
			if(mDataList.get(position).getVideoInfo1().videoCreateDate.contains(mGroupNameList.get(i))){
				headerText=mGroupNameList.get(i);
				break;
			}
		}
		holder.title.setText(headerText);
		return convertView;
	}
	
	class HeaderViewHolder {
		TextView title;
	}
	
	class ViewHolder {
		RelativeLayout mVideoLayout1;
		RelativeLayout mVideoLayout2;
		RelativeLayout mTMLayout1;
		RelativeLayout mTMLayout2;
		ImageView image1;
		ImageView image2;
		TextView mVideoCountTime1;
		TextView mVideoCountTime2;
		ImageView mVideoQuality1;
		ImageView mVideoQuality2;
		TextView mVideoCreateTime1;
		TextView mVideoCreateTime2;
		TextView mVideoSize1;
		TextView mVideoSize2;
	}
	
	public int getCount() {
		return count;
	}
	
	public Object getItem(int position) {
		return mDataList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public long getHeaderId(int position) {
		long id=0;
		String groupname = "";
		for(int i=0;i<mGroupNameList.size();i++){
			groupname = mGroupNameList.get(i);
			String path1 = mDataList.get(position).getVideoInfo1().videoCreateDate;
			if(path1.contains(groupname)){
				id = i;
				break;
			}
		}
		
		return id;
	}
	
}
