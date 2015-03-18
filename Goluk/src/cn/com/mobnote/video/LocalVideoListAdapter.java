package cn.com.mobnote.video;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.VideoEditActivity;
import cn.com.mobnote.video.LocalVideoManage.LocalVideoData;
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

public class LocalVideoListAdapter extends BaseAdapter{
	private Context mContext = null;
	private ArrayList<LocalVideoData> mDataList = null;
	private LayoutInflater mLayoutInflater = null;
	/** 来源标示,用来处理点击事件 */
	private String mPageSource = "";
	
	public LocalVideoListAdapter(Context context, ArrayList<LocalVideoData> data,String source) {
		mContext = context;
		mPageSource = source;
		mDataList = data;
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return mDataList == null ? 0 : mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList == null ? null : mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = mLayoutInflater.inflate(R.layout.index_myvideo_item, null);
			//convertView.setLayoutParams(new RelativeLayout.LayoutParams(242,190));
			holder.img = (ImageView) convertView.findViewById(R.id.video_first_needle);
			holder.time = (TextView) convertView.findViewById(R.id.video_change_time);
			holder.size = (TextView) convertView.findViewById(R.id.video_size);
			holder.playBtn = (ImageButton) convertView.findViewById(R.id.video_play_btn);
			holder.newImage = (ImageView) convertView.findViewById(R.id.new_image);
			holder.videoTypeImage = (ImageView) convertView.findViewById(R.id.video_type_img);
			holder.timeLayout = (RelativeLayout) convertView.findViewById(R.id.video_time_layout);
			holder.loading = (LoadingView) convertView.findViewById(R.id.video_upload_loading);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		LocalVideoData data = (LocalVideoData)mDataList.get(position);
		
		if(null != data){
			//首先判断该视频正在上传
			if(data.isUpload){
				//显示默认图片,如果不这么写,由于android-item特性会显示被删除item的view
				holder.img.setBackgroundResource(R.drawable.screenshot3);
				DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
				int width = dm.widthPixels - 22;
				float density = dm.density;
				//RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(350, (int)(100 * density));
				convertView.setLayoutParams(new MyGridView.LayoutParams(width / 2, (int)(95 * density)));
				//convertView.setLayoutParams(layoutParams);
				
				//显示loading
				holder.loading.setCurrentProgress(0);
				holder.loading.setVisibility(View.VISIBLE);
				
				//隐藏其他控件
				holder.timeLayout.setVisibility(View.GONE);
				holder.newImage.setVisibility(View.GONE);
				holder.playBtn.setVisibility(View.GONE);
			}
			else{
				//隐藏loading
				holder.loading.setVisibility(View.GONE);
				
				//显示其他控件
				holder.timeLayout.setVisibility(View.VISIBLE);
				holder.newImage.setVisibility(View.VISIBLE);
				holder.playBtn.setVisibility(View.VISIBLE);
				holder.videoTypeImage.setVisibility(View.VISIBLE);
				
				//判断显示视频类别图标
				if("WND1".equals(data.videoType)){
					//8s
					holder.videoTypeImage.setBackgroundResource(R.drawable.time_12s);
				}
				else if("URG1".equals(data.videoType)){
					//紧急
					holder.videoTypeImage.setBackgroundResource(R.drawable.dangerous);
				}
				else{
					//未知
					holder.videoTypeImage.setVisibility(View.GONE);
				}
				
				//判断new图标是否显示
				if(data.isNew == false){
					holder.newImage.setVisibility(View.GONE);
				}else{
					holder.newImage.setVisibility(View.VISIBLE);
				}
				if(null != data.img){
					//视频文件异步读取中
					holder.img.setBackgroundDrawable(data.img);
				}
				else{
					//计算屏幕宽度,因为视频第一针的图片,宽度需要动态计算,所以默认图片也需要计算
					//DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
					//int width = dm.widthPixels -24;
					holder.img.setBackgroundResource(R.drawable.screenshot3);
				}
				holder.time.setText(data.changeTime);
				holder.size.setText(data.fileSize);
				//注册播放按钮事件
				holder.playBtn.setOnClickListener(new onclick(position));
			}
		}
		return convertView;
	}

	class ViewHolder {
		ImageView img = null;
		TextView time = null;
		TextView size = null;
		ImageButton playBtn = null;
		ImageView newImage = null;
		ImageView videoTypeImage = null;
		RelativeLayout timeLayout = null;
		LoadingView loading = null;
	}
	
	class onclick implements OnClickListener{
		private int index;
		
		public onclick(int index){
			this.index = index;
		}
		
		/**
		 * 播放按钮事件
		 */
		@Override
		public void onClick(View v) {
			LocalVideoData data = (LocalVideoData)getItem(index);
			//拿到文件名称封装成日志文件格式
			String filename = data.fileName + ";";
			//点击视频,不显示new标签
			data.isNew = false;
			//向日志文件中追加日志
			if(mPageSource == "Main"){
				((MainActivity)mContext).mLocalVideoManage.addVideoLog(filename);
				((MainActivity)mContext).mLocalVideoListAdapter.notifyDataSetChanged();
			}
			else if(mPageSource == "LocalVideoList"){
				((LocalVideoListActivity)mContext).mLocalVideoManage.addVideoLog(filename);
				((LocalVideoListActivity)mContext).mLocalVideoListAdapter.notifyDataSetChanged();
			}
			
			//跳转到视频编辑页面
			Intent videoEdit = new Intent(mContext,VideoEditActivity.class);
			videoEdit.putExtra("cn.com.mobnote.video.path", data.filePath);
			mContext.startActivity(videoEdit);
		}
	}
}

