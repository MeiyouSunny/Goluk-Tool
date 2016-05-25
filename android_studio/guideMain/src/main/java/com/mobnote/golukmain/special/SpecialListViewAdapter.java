package com.mobnote.golukmain.special;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.usercenter.NewUserCenterActivity;
import com.mobnote.golukmain.usercenter.UCUserInfo;
import com.mobnote.golukmain.videodetail.VideoDetailActivity;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
	public Object getItem(int index) {
		if(null != specialListData) {
			specialListData.get(index);
		}
		return null;
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup parent) {
		final SpecialInfo specialInfo = specialListData.get(arg0);
		ViewHolder holder = null;

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.special_list_item, null);

			holder = new ViewHolder();
			holder.author = (TextView) convertView.findViewById(R.id.tv_special_list_item_thank_author);
			holder.mPreLoading = (ImageView) convertView.findViewById(R.id.mPreLoading);
			holder.videoTitle = (TextView) convertView.findViewById(R.id.video_title);
			holder.tvLocation = (TextView)convertView.findViewById(R.id.tv_special_list_item_location);
			holder.rewardBtn = (Button) convertView.findViewById(R.id.btn_special_list_item_reward);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if(null != specialInfo) {
			holder.author.setText(mContext.getString(R.string.str_thank_to_author) + " " + specialInfo.author);
			if(null != specialInfo.gen) {
				if("1".equals(specialInfo.gen.sysflag)) {
//					SpannableString spanString = new SpannableString(" " + specialInfo.describe);
//					Drawable d = mContext.getResources().getDrawable(
//							R.drawable.special_list_item_reward);
//					d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
//					ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
//					spanString.setSpan(span, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					holder.rewardBtn.setVisibility(View.VISIBLE);
					holder.videoTitle.setText("  " + specialInfo.describe);
				} else {
					holder.rewardBtn.setVisibility(View.GONE);
					holder.videoTitle.setText(specialInfo.describe);
				}
			} else {
				holder.videoTitle.setText(specialInfo.describe);
			}
			holder.mPreLoading.setOnClickListener(new SpecialCommentListener(mContext, null, specialInfo.imagepath,
				specialInfo.videopath, "suqare", specialInfo.videotype, specialInfo.videoid));
			if(TextUtils.isEmpty(specialInfo.location)) {
				holder.tvLocation.setVisibility(View.GONE);
			} else {
				holder.tvLocation.setText(specialInfo.location);
				holder.tvLocation.setVisibility(View.VISIBLE);
			}
			holder.author.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startUserCenter(specialInfo);
				}
			});

			holder.videoTitle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startVideoDetailActivity(specialInfo);
				}
			});

			int width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
			int height = (int) ((float) width / 1.77f);

			RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(width, height);
			holder.mPreLoading.setLayoutParams(mPreLoadingParams);
			GlideUtils.loadImage(mContext, holder.mPreLoading, specialInfo.imagepath, R.drawable.tacitly_pic);
		}

		return convertView;
	}

	private void startUserCenter(SpecialInfo specialInfo) {
		// 跳转当前点赞人的个人中心
		GolukUtils.startUserCenterActivity(mContext,specialInfo.uid);
	}

	private void startVideoDetailActivity(SpecialInfo specialInfo) {
		Intent intent = new Intent(mContext, VideoDetailActivity.class);
		intent.putExtra("videoid", specialInfo.videoid);
		mContext.startActivity(intent);
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
					formatter = new SimpleDateFormat(mContext.getString(R.string.cluster_time_format));
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

	static class ViewHolder {
		ImageView mPreLoading;
		TextView videoTitle;
		TextView author;
		TextView tvLocation;
		Button rewardBtn;
	}

//	public Bitmap getThumbBitmap(String netUrl) {
//		String name = MD5Utils.hashKeyForDisk(netUrl) + ".0";
//		String path = Environment.getExternalStorageDirectory() + File.separator + "goluk/image_cache";
//		File file = new File(path + File.separator + name);
//		Bitmap t_bitmap = null;
//		if (file.exists()) {
//			t_bitmap = ImageManager.getBitmapFromCache(file.getAbsolutePath(), 100, 100);
//		}
//		return t_bitmap;
//	}

}
