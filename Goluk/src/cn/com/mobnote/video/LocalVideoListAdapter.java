package cn.com.mobnote.video;

import java.util.ArrayList;
import java.util.List;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.LocalVideoListActivity;
import cn.com.mobnote.golukmobile.LocalVideoShareListActivity;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.VideoEditActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.video.LocalVideoListManage.DoubleVideoData;
import cn.com.mobnote.video.LocalVideoListManage.LocalVideoData;
import cn.com.mobnote.view.LoadingView;
import cn.com.mobnote.view.MyGridView;
/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:本地视频列表数据适配器
 * 
 * @author 陈宣宇
 * 
 */

public class LocalVideoListAdapter extends BaseAdapter implements StickyListHeadersAdapter {
//	private Context mContext = null;
//	private ArrayList<LocalVideoData> mDataList = null;
//	private LayoutInflater mLayoutInflater = null;
//	/** 来源标示,用来处理点击事件 */
//	private String mPageSource = "";
	
//	public LocalVideoListAdapter(Context context, ArrayList<LocalVideoData> data,String source) {
//		mContext = context;
//		mPageSource = source;
//		mDataList = data;
//		mLayoutInflater = LayoutInflater.from(context);
//	}
	private Context mContext = null;
	private String mPageSource = "";
	private LayoutInflater inflater;
	private List<DoubleVideoData> mDataList;
	private List<String> mGroupNameList;
	private int count = 0;
	private int screenWidth = 0;
	private float density;
	
	public LocalVideoListAdapter(Context c,String source){
		mContext = c;
		mPageSource = source;
		inflater = LayoutInflater.from(c);
		mDataList=new ArrayList<DoubleVideoData>();
		mGroupNameList = new ArrayList<String>();
		screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		density = SoundUtils.getInstance().getDisplayMetrics().density;
	}
	
	public void setData(List<String> groupname, List<DoubleVideoData> data){
		mDataList.clear();
		mDataList.addAll(data);
		mGroupNameList.addAll(groupname);
		count = mDataList.size();
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			if(mPageSource.equals("LocalVideoList")){
				convertView = inflater.inflate(R.layout.local_video_list_item, parent, false);
			}
			else{
				convertView = inflater.inflate(R.layout.local_video_share_list_item, parent, false);
			}
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
			holder.mNew1 = (ImageView)convertView.findViewById(R.id.mNew1);
			holder.mNew2 = (ImageView)convertView.findViewById(R.id.mNew2);
			
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
		
		LocalVideoData mVideoInfo1 = mDataList.get(position).getVideoInfo1();
		LocalVideoData mVideoInfo2 = mDataList.get(position).getVideoInfo2();
		List<String> selectedData = null;
		if(mPageSource.equals("LocalVideoList")) {
			LocalVideoListActivity a = (LocalVideoListActivity)mContext;
			selectedData = a.getSelectedListData();
			if(a.getIsEditState()){
				holder.mTMLayout1.setVisibility(View.GONE);
				holder.mTMLayout2.setVisibility(View.GONE);
			}
			
			if(mVideoInfo1.isNew){
				holder.mNew1.setVisibility(View.VISIBLE);
			}else{
				holder.mNew1.setVisibility(View.GONE);
			}
			
			if(null != mVideoInfo2){
				if(mVideoInfo2.isNew){
					holder.mNew2.setVisibility(View.VISIBLE);
				}else{
					holder.mNew2.setVisibility(View.GONE);
				}
			}
			
		}
		else{
			LocalVideoShareListActivity a = (LocalVideoShareListActivity)mContext;
			selectedData = a.getSelectedListData();
		}
		
		
		holder.mVideoLayout2.setVisibility(View.GONE);
		holder.mTMLayout1.setTag(mVideoInfo1.videoPath);
		holder.mTMLayout2.setTag("");
		if(selectedData.contains(mVideoInfo1.videoPath)){
			holder.mTMLayout1.setVisibility(View.VISIBLE);
		}else{
			holder.mTMLayout1.setVisibility(View.GONE);
		}
		if(1 == mVideoInfo1.videoHP){
			holder.mVideoQuality1.setBackgroundResource(R.drawable.carrecorder_icon_1080);
		}else if(2 == mVideoInfo1.videoHP){
			holder.mVideoQuality1.setBackgroundResource(R.drawable.carrecorder_icon_720);
		}else if(3 == mVideoInfo1.videoHP){
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
			if(1 == mVideoInfo2.videoHP){
				holder.mVideoQuality2.setBackgroundResource(R.drawable.carrecorder_icon_1080);
			}else if(2 == mVideoInfo2.videoHP){
				holder.mVideoQuality2.setBackgroundResource(R.drawable.carrecorder_icon_720);
			}else if(3 == mVideoInfo2.videoHP){
				holder.mVideoQuality2.setBackgroundResource(R.drawable.carrecorder_icon_480);
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
		ImageView mNew1;
		ImageView mNew2;
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

