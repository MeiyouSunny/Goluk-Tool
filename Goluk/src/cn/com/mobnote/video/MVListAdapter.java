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
import cn.com.mobnote.golukmobile.VideoEditActivity;
import cn.com.mobnote.video.MVManage.MVEditData;

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
public class MVListAdapter extends BaseAdapter {
	private Context mContext = null;
	private ArrayList<MVEditData> mDataList = null;
	private LayoutInflater mLayoutInflater = null;
	/** 记录滤镜上一次点击的id */
	private int resIndex = 0;
	/** 记录是否改变了滤镜 默认true */
	private boolean mResChange = true;

	public MVListAdapter(Context context, ArrayList<MVEditData> data) {
		mContext = context;
		mDataList = data;
		mLayoutInflater = LayoutInflater.from(context);
	}

	public int getCurrentResIndex() {
		return resIndex;
	}

	/**
	 * 返回当前是否改变了滤镜
	 * 
	 * @return
	 */
	public boolean getResChange() {
		return mResChange;
	}

	/**
	 * 改变滤镜标识
	 * 
	 * @param b
	 */
	public void setResChange(boolean b) {
		mResChange = b;
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
			convertView = mLayoutInflater.inflate(R.layout.video_edit_mv_item, null);
			holder.img = (ImageView) convertView.findViewById(R.id.mv_img);
			holder.name = (TextView) convertView.findViewById(R.id.mv_name);
			holder.display = (ImageView) convertView.findViewById(R.id.display_img);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		MVEditData data = (MVEditData) mDataList.get(position);
		if (data.display) {
			holder.display.setVisibility(View.VISIBLE);
		} else {
			holder.display.setVisibility(View.GONE);
		}
		holder.img.setBackgroundResource(data.src);
		holder.name.setText(data.name);
		convertView.setOnClickListener(new onclick(position));
		return convertView;
	}

	class ViewHolder {
		ImageView img = null;
		TextView name = null;
		ImageView display = null;
	}

	class onclick implements OnClickListener {
		private int index;
		/** 滤镜对应值 */
		private int[] mFilter = { 0, 7, 1, 6, 2, 4, 5, 3 };

		public onclick(int index) {
			this.index = index;
		}

		/**
		 * 滤镜列表类别点击事件
		 */
		@Override
		public void onClick(View v) {
			MVEditData data = (MVEditData) getItem(index);
			if (index != resIndex) {
				MVEditData resData = (MVEditData) getItem(resIndex);
				resData.display = false;
				resIndex = index;
				mResChange = true;
				((VideoEditActivity) mContext).mVVPlayVideo.switchFilterId(mFilter[index]);
			}
			data.display = true;
			((VideoEditActivity) mContext).mMVListAdapter.notifyDataSetChanged();
		}
	}
}
