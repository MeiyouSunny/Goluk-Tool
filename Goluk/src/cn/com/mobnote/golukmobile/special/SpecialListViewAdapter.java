package cn.com.mobnote.golukmobile.special;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.MD5Utils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.util.GlideUtils;

@SuppressLint("InflateParams")
public class SpecialListViewAdapter extends BaseAdapter {
	private Context mContext = null;
	private List<SpecialInfo> specialListData = null;

	public SpecialListViewAdapter(Context context, int plform) {
		mContext = context;
		specialListData = new ArrayList<SpecialInfo>();

	}

	public void setData(List<SpecialInfo> data) {
		specialListData.clear();
		specialListData.addAll(data);
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return specialListData.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	ViewHolder holder;

	@Override
	public View getView(int arg0, View convertView, ViewGroup parent) {
		SpecialInfo specialInfo = specialListData.get(arg0);
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.special_list_item, null);

			holder = new ViewHolder();
			holder.author = (TextView) convertView.findViewById(R.id.author);
			holder.mPreLoading = (ImageView) convertView.findViewById(R.id.mPreLoading);
			holder.videoTitle = (TextView) convertView.findViewById(R.id.video_title);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.author.setText(specialInfo.author);
		holder.videoTitle.setText(specialInfo.describe);
		holder.mPreLoading.setOnClickListener(new SpecialCommentListener(mContext, null, specialInfo.imagepath,
				specialInfo.videopath, "suqare", specialInfo.videotype, specialInfo.videoid));

		int width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		int height = (int) ((float) width / 1.77f);

		RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(width, height);
		holder.mPreLoading.setLayoutParams(mPreLoadingParams);
		GlideUtils.loadImage(mContext, holder.mPreLoading, specialInfo.imagepath, R.drawable.tacitly_pic);

		return convertView;
	}

	public int getUserHead(String head) {
		return 0;
	}

	public void onBackPressed() {

	}

	@SuppressLint("SimpleDateFormat")
	public String formatTime(String date) {
		String time = "";
		if (null != date) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");

			try {
				Date strtodate = formatter.parse(date);
				if (null != strtodate) {
					formatter = new SimpleDateFormat("MM月dd日 HH时mm分");
					if (null != formatter) {
						time = formatter.format(strtodate);
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return time;
	}

	public static class ViewHolder {
		ImageView mPreLoading;
		TextView videoTitle;
		TextView author;
	}

	public Bitmap getThumbBitmap(String netUrl) {
		String name = MD5Utils.hashKeyForDisk(netUrl) + ".0";
		String path = Environment.getExternalStorageDirectory() + File.separator + "goluk/image_cache";
		File file = new File(path + File.separator + name);
		Bitmap t_bitmap = null;
		if (file.exists()) {
			t_bitmap = ImageManager.getBitmapFromCache(file.getAbsolutePath(), 100, 100);
		}
		return t_bitmap;
	}

}
