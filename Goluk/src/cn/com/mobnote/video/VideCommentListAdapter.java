package cn.com.mobnote.video;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.VideoEditActivity;
import cn.com.mobnote.video.LocalVideoManage.LocalVideoData;
import cn.com.mobnote.video.VideCommentManage.VideoCommentData;
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
 * @ 功能描述:在线视频评论列表数据适配器
 * 
 * @author 陈宣宇
 * 
 */

public class VideCommentListAdapter extends BaseAdapter{
	private Context mContext = null;
	private ArrayList<VideoCommentData> mDataList = null;
	private LayoutInflater mLayoutInflater = null;
	/** 显示默认图片view map */
	//private HashMap<String,View> mDefaultImageView = new HashMap<String,View>();
	
	public VideCommentListAdapter(Context context, ArrayList<VideoCommentData> data) {
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
			convertView = mLayoutInflater.inflate(R.layout.video_comment_item,null);
			holder.userPhoto = (ImageView) convertView.findViewById(R.id.user_photo);
			holder.commentTitle = (TextView) convertView.findViewById(R.id.comment_title);
			holder.commentDesc = (TextView) convertView.findViewById(R.id.comment_desc);
			holder.commentTime = (TextView) convertView.findViewById(R.id.comment_time);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		VideoCommentData data = (VideoCommentData)mDataList.get(position);
		
		if(position%2 == 0){
			//偶数行
			convertView.setBackgroundColor(Color.rgb(237,237,237));
		}
		else{
			//基数行
			convertView.setBackgroundColor(Color.rgb(255,255,255));
		}
		
		holder.userPhoto.setBackgroundResource(data.headPath);
		holder.commentTitle.setText(data.title);
		holder.commentDesc.setText(data.comment);
		holder.commentTime.setText(data.time);
		//注册播放按钮事件
		//holder.playBtn.setOnClickListener(new onclick(position));
		return convertView;
	}

	class ViewHolder {
		ImageView userPhoto = null;
		TextView commentTitle = null;
		TextView commentDesc = null;
		TextView commentTime = null;
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

