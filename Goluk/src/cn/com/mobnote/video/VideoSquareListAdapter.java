package cn.com.mobnote.video;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.VideoEditActivity;
import cn.com.mobnote.video.LocalVideoManage.LocalVideoData;
import cn.com.mobnote.video.VideoSquareListManage.VideoSquareListData;
import cn.com.tiros.debug.GolukDebugUtils;
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
 * @ 功能描述:直播列表数据适配器
 * 
 * @author 陈宣宇
 * 
 */

@SuppressLint("InflateParams")
public class VideoSquareListAdapter extends BaseAdapter{
	private Context mContext = null;
	private ArrayList<VideoSquareListData> mDataList = null;
	private LayoutInflater mLayoutInflater = null;
	/** 显示默认图片view map */
	//private HashMap<String,View> mDefaultImageView = new HashMap<String,View>();
	
	public VideoSquareListAdapter(Context context, ArrayList<VideoSquareListData> data) {
		mContext = context;
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = mLayoutInflater.inflate(R.layout.video_square_item,null);
			holder.videoImg = (ImageView) convertView.findViewById(R.id.video_img);
			holder.userNameText = (TextView) convertView.findViewById(R.id.username_text);
			holder.likeBtn = (Button) convertView.findViewById(R.id.like_btn);
			holder.speedText = (TextView) convertView.findViewById(R.id.speed_text);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		VideoSquareListData data = (VideoSquareListData)mDataList.get(position);
		//holder.userPhoto.setBackgroundResource(data.headPath);
		holder.userNameText.setText(data.userName);
		holder.likeBtn.setText(data.likeNum);
		holder.speedText.setText(data.speed);
		//注册播放按钮事件
		//holder.playBtn.setOnClickListener(new onclick(position));
		return convertView;
	}

	class ViewHolder {
		ImageView videoImg = null;
		TextView userNameText = null;
		Button likeBtn = null;
		TextView speedText = null;
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
			GolukDebugUtils.i("","chxy_____path" + data.filePath);
			
			//跳转到视频编辑页面
			Intent videoEdit = new Intent(mContext,VideoEditActivity.class);
			videoEdit.putExtra("cn.com.mobnote.video.path", data.filePath);
			mContext.startActivity(videoEdit);
		}
	}
}

