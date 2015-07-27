package cn.com.mobnote.video;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.startshare.ShareFilterLayout;
import cn.com.mobnote.golukmobile.startshare.VideoEditActivity;
import cn.com.mobnote.video.MVManage.MVEditData;

@SuppressLint("InflateParams")
public class MVListAdapter extends BaseAdapter {
	private Context mContext = null;
	private ArrayList<MVEditData> mDataList = null;
	private LayoutInflater mLayoutInflater = null;
	/** 记录滤镜上一次点击的id */
	private int resIndex = 0;
	/** 记录是否改变了滤镜 默认true */
	private boolean mResChange = true;
	private ShareFilterLayout mFilterInstance = null;

	public MVListAdapter(Context context, ArrayList<MVEditData> data, ShareFilterLayout filterInstance) {
		mContext = context;
		mDataList = data;
		mFilterInstance = filterInstance;
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
			mFilterInstance.mMVListAdapter.notifyDataSetChanged();
		}
	}
}
