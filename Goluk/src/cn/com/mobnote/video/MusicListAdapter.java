package cn.com.mobnote.video;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.VideoEditMusicActivity;
import cn.com.mobnote.video.MusicManage.MusicData;
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
 * @ 功能描述:视频编辑页面MV选项适配器
 * 
 * @author 陈宣宇
 * 
 */

@SuppressLint("InflateParams")
public class MusicListAdapter extends BaseAdapter{
	private Context mContext = null;
	private ArrayList<MusicData> mDataList = null;
	private LayoutInflater mLayoutInflater = null;
	/** 记录音频上一次点击的id */
	private int resIndex = 0;
	/** 记录是否改变了滤镜 默认true */
	//private boolean mResChange = true;
	
	public MusicListAdapter(Context context, ArrayList<MusicData> data) {
		mContext = context;
		mDataList = data;
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	/**
	 * 返回当前是否改变了滤镜
	 * @return
	
	public boolean getResChange(){
		return mResChange;
	}
	 */
	
	/**
	 * 修改默认选择音频下标
	 * @param i
	 */
	public void setResIndex(int i){
		resIndex = i;
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
			convertView = mLayoutInflater.inflate(R.layout.video_edit_music_item, null);
			holder.checkImg = (ImageView) convertView.findViewById(R.id.music_check_img);
			holder.name = (TextView) convertView.findViewById(R.id.music_name);
			holder.status = (TextView) convertView.findViewById(R.id.music_status);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		MusicData data = (MusicData)mDataList.get(position);
		
		if(data.isCheck){
			holder.checkImg.setVisibility(View.VISIBLE);
		}else{
			holder.checkImg.setVisibility(View.INVISIBLE);
		}
		holder.name.setText(data.fileName);
		holder.status.setText(data.status);
		
		convertView.setOnClickListener(new onclick(position));
		//convertView.setOnTouchListener(new itemTouch(position));
		return convertView;
	}

	class ViewHolder {
		ImageView checkImg = null;
		TextView name = null;
		TextView status = null;
	}
	
	/*
	class itemTouch implements OnTouchListener{
		private int index;
		
		public itemTouch(int index){
			this.index = index;
		}
		
		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			int action = event.getAction();
			switch (action) {
				case MotionEvent.ACTION_DOWN:
					arg0.setBackgroundColor(Color.rgb(204,204,204));
				break;
				case MotionEvent.ACTION_UP:
					arg0.setBackgroundColor(Color.rgb(255,255,255));
				break;
			}
			return false;
		}
	}
	*/
	
	class onclick implements OnClickListener{
		private int index;
		public onclick(int index){
			this.index = index;
		}
		
		/**
		 * 音频列表点击事件
		 */
		@Override
		public void onClick(View v) {
			MusicData data = (MusicData)getItem(index);
			if(index != resIndex){
				MusicData resData = (MusicData)getItem(resIndex);
				resData.isCheck = false;
				resIndex = index;
				//mResChange = true;
				//((VideoEditActivity)mContext).mVVPlayVideo.switchFilterId(mFilter[index]);
			}
			data.isCheck = true;
			((VideoEditMusicActivity)mContext).mMusicListAdapter.notifyDataSetChanged();
			((VideoEditMusicActivity)mContext).changeNoMusicStatus(false,data.filePath);
			//播放音频
			((VideoEditMusicActivity)mContext).playSelectMusicSound(data.filePath);
		}
	}
}

