package cn.com.mobnote.video;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.LiveVideoPlayActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.console;
import cn.com.mobnote.video.LiveVideoListManage.LiveVideoListData;

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
public class LiveVideoListAdapter extends BaseAdapter{
	private Context mContext = null;
	private ArrayList<LiveVideoListData> mDataList = null;
	private LayoutInflater mLayoutInflater = null;
	/** 头像数据标识集合 */
	private int[] mHeadImg = {0,R.drawable.editor_boy_one,R.drawable.editor_boy_two,R.drawable.editor_boy_three,
			R.drawable.editor_girl_one,R.drawable.editor_girl_two,R.drawable.editor_girl_three,
			R.drawable.head_unknown
	};
	
	public LiveVideoListAdapter(Context context, ArrayList<LiveVideoListData> data) {
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
			convertView = mLayoutInflater.inflate(R.layout.video_live_list_item,null);
			holder.videoImg = (ImageView) convertView.findViewById(R.id.video_img);
			holder.userNameText = (TextView) convertView.findViewById(R.id.username_text);
			holder.likeBtn = (Button) convertView.findViewById(R.id.like_btn);
			holder.speedText = (TextView) convertView.findViewById(R.id.speed_text);
			holder.playBtn = (ImageButton) convertView.findViewById(R.id.play_img);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		LiveVideoListData data = (LiveVideoListData)mDataList.get(position);
		
		//头像
		Resources res = mContext.getResources();
		Drawable head = res.getDrawable(mHeadImg[Integer.valueOf(data.headPath)]);
		//调用setCompoundDrawables时，必须调用Drawable.setBounds()方法,否则图片不显示
		head.setBounds(0, 0, head.getMinimumWidth(), head.getMinimumHeight());
		holder.userNameText.setCompoundDrawables(head, null, null, null);
		holder.userNameText.setText(data.userName);
		
		holder.likeBtn.setText(data.likeNum);
		holder.speedText.setText(data.speed);
		if(null != data.videoImg){
			holder.videoImg.setBackgroundDrawable(data.videoImg);
		}
		
		//注册播放按钮事件
		holder.playBtn.setOnClickListener(new onclick(position));
		return convertView;
	}

	class ViewHolder {
		ImageView videoImg = null;
		TextView userNameText = null;
		Button likeBtn = null;
		TextView speedText = null;
		ImageButton playBtn = null;
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
			LiveVideoListData data = (LiveVideoListData)getItem(index);
			console.log("点击直播列表item---aid---" + data.aid);
			
			//首页地图气泡跳转到直播详情页
			Intent live = new Intent(mContext,LiveVideoPlayActivity.class);
			live.putExtra("cn.com.mobnote.map.aid",  data.aid);
			live.putExtra("cn.com.mobnote.map.uid", "1");
			mContext.startActivity(live);
		}
	}
}

