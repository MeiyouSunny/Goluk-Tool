package com.mobnote.golukmain.special;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.util.ImageManager;
import com.mobnote.golukmain.carrecorder.util.MD5Utils;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.util.GlideUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;

@SuppressLint("InflateParams")
public class ClusterViewAdapter extends BaseAdapter{
	private Context mContext = null;
	private List<ClusterInfo> clusterListData = null;
	private int count = 0;
	private int form = 1;

	private SharePlatformUtil sharePlatform;

	private int width = 0;

	int VIEW_TYPE = 2;

	final int TYPE_1 = 0;
	final int TYPE_2 = 1;

	/** 滚动中锁标识 */
	private boolean lock = false;

	private SpecialInfo headdata;

	public ClusterViewAdapter(Context context, int plform, SharePlatformUtil spf) {
		mContext = context;
		clusterListData = new ArrayList<ClusterInfo>();
		sharePlatform = spf;
		width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
	}

	public void setData(List<ClusterInfo> data, SpecialInfo head) {
		clusterListData.clear();
		clusterListData.addAll(data);
		count = clusterListData.size();

		if (head != null && !"".equals(head)) {
			count++;
		}
		headdata = head;

		this.notifyDataSetChanged();
	}

	// 每个convert view都会调用此方法，获得当前所需要的view样式
	@Override
	public int getItemViewType(int position) {
		int p = position;
		if (headdata == null) {
			return TYPE_2;
		} else {
			if (p == 0)
				return TYPE_1;
			else
				return TYPE_2;
		}

	}

	@Override
	public int getViewTypeCount() {
		if (headdata == null) {
			return 1;
		} else {
			if (clusterListData == null || clusterListData.size() == 0) {
				return 1;
			} else {
				return 2;
			}
		}
	}

	@Override
	public int getCount() {
		return count;
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
	public View getView(int position, View convertView, ViewGroup parent) {

		int type = getItemViewType(position);

		switch (type) {
		case TYPE_1:
			if (headdata != null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.cluster_list_item, null);

				ImageView image = (ImageView) convertView.findViewById(R.id.mPreLoading);
				TextView txt = (TextView) convertView.findViewById(R.id.video_title);
				TextView link = (TextView) convertView.findViewById(R.id.link);

				txt.setText(headdata.describe);
				if (headdata.outurlname != null) {
					link.setText(headdata.outurlname);
				}

				int width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
				int height = (int) ((float) width / 1.77f);

				RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(width, height);
				image.setLayoutParams(mPreLoadingParams);

				GlideUtils.loadNetHead(mContext, image, headdata.imagepath, R.drawable.tacitly_pic);

				if ("1".equals(headdata.videotype)) {
					convertView.findViewById(R.id.mPlayBigBtn).setVisibility(View.GONE);
				}

				image.setOnClickListener(new SpecialCommentListener(mContext, this, headdata.imagepath,
						headdata.videopath, "suqare", headdata.videotype, headdata.videoid));
			}
			break;
		case TYPE_2:
			int index = position;
			if (headdata != null) {
				index--;
			}
			ClusterInfo clusterInfo = clusterListData.get(index);
			if (convertView == null) {

				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.newest_list_item, null);
				holder.imageLayout = (ImageView) convertView.findViewById(R.id.imageLayout);
				holder.headimg = (ImageView) convertView.findViewById(R.id.headimg);
				holder.nikename = (TextView) convertView.findViewById(R.id.nikename);
				holder.time = (TextView) convertView.findViewById(R.id.time);
				holder.function = (ImageView) convertView.findViewById(R.id.function);

				holder.praiseLayout = (LinearLayout) convertView.findViewById(R.id.praiseLayout);
				holder.zanIcon = (ImageView) convertView.findViewById(R.id.zanIcon);
				holder.zanText = (TextView) convertView.findViewById(R.id.zanText);

				holder.commentLayout = (LinearLayout) convertView.findViewById(R.id.commentLayout);
				holder.commentIcon = (ImageView) convertView.findViewById(R.id.commentIcon);
				holder.commentText = (TextView) convertView.findViewById(R.id.commentText);

				holder.shareLayout = (LinearLayout) convertView.findViewById(R.id.shareLayout);
				holder.shareIcon = (ImageView) convertView.findViewById(R.id.shareIcon);
				holder.shareText = (TextView) convertView.findViewById(R.id.shareText);

				holder.zText = (TextView) convertView.findViewById(R.id.zText);
				holder.weiguan = (TextView) convertView.findViewById(R.id.weiguan);
				holder.weiguan = (TextView) convertView.findViewById(R.id.weiguan);
				holder.totalcomments = (TextView) convertView.findViewById(R.id.totalcomments);

				holder.detail = (TextView) convertView.findViewById(R.id.detail);
				holder.comment1 = (TextView) convertView.findViewById(R.id.comment1);
				holder.comment2 = (TextView) convertView.findViewById(R.id.comment2);
				holder.comment3 = (TextView) convertView.findViewById(R.id.comment3);

				int height = (int) ((float) width / 1.77f);
				RelativeLayout.LayoutParams mPlayerLayoutParams = new RelativeLayout.LayoutParams(width, height);
				mPlayerLayoutParams.addRule(RelativeLayout.BELOW, R.id.headlayout);
				holder.imageLayout.setLayoutParams(mPlayerLayoutParams);

				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.nikename.setText(clusterInfo.author);
			holder.time.setText(clusterInfo.sharingtime);
			holder.zText.setText(clusterInfo.praisenumber);
			holder.weiguan.setText(clusterInfo.clicknumber + " " + mContext.getString(R.string.cluster_weiguan));
			holder.detail.setText(clusterInfo.author + "  " + clusterInfo.describe);
			holder.totalcomments.setText(mContext.getString(R.string.str_see_comments, clusterInfo.comments));
			holder.zText.setText(clusterInfo.praisenumber + " " + mContext.getString(R.string.str_usercenter_praise));
			loadImage(holder.imageLayout, clusterInfo.imagepath);
			initListener(index);
			// 没点过
			if ("0".equals(clusterInfo.ispraise)) {
				holder.zanIcon.setBackgroundResource(R.drawable.videodetail_like);
			} else {// 点赞过
				holder.zanIcon.setBackgroundResource(R.drawable.videodetail_like_press);
			}
			if (clusterInfo.ci1 != null) {
				holder.comment1.setText(clusterInfo.ci1.name + "  " + clusterInfo.ci1.text);
			} else {
				holder.comment1.setVisibility(View.GONE);
			}

			if (clusterInfo.ci2 != null) {
				holder.comment2.setText(clusterInfo.ci2.name + "  " + clusterInfo.ci2.text);
			} else {
				holder.comment2.setVisibility(View.GONE);
			}

			if (clusterInfo.ci3 != null) {
				holder.comment3.setText(clusterInfo.ci3.name + "  " + clusterInfo.ci3.text);
			} else {
				holder.comment3.setVisibility(View.GONE);
			}
			break;
		default:
			break;
		}

		return convertView;
	}

	private String mVideoId = null;

	public void setWillShareVideoId(String vid) {
		mVideoId = vid;
	}

	public String getWillShareVideoId() {
		return mVideoId;
	}

	private void initListener(int index) {
		ClusterInfo clusterInfo = clusterListData.get(index);

		holder.commentLayout.setOnClickListener(new ClusterCommentListener(mContext, clusterInfo, false));
		holder.totalcomments.setOnClickListener(new ClusterCommentListener(mContext, clusterInfo, false));
		holder.praiseLayout.setOnClickListener(new ClusterPressListener(mContext, clusterInfo, this));
		holder.function.setOnClickListener(new ClusterPressListener(mContext, clusterInfo, this));
		holder.imageLayout.setOnClickListener(new SpecialCommentListener(mContext, this, clusterInfo.imagepath,
				clusterInfo.videopath, "suqare", clusterInfo.videotype, clusterInfo.videoid));
		holder.shareLayout.setOnClickListener(new SpecialCommentListener(mContext, this, clusterInfo.imagepath,
				clusterInfo.videopath, "suqare", clusterInfo.videotype, clusterInfo.videoid));
	}

	public int getUserHead(String head) {
		return 0;
	}

	public void onBackPressed() {

	}

	/**
	 * 点赞
	 * 
	 * @Title: setLikePress
	 * @Description: TODO
	 * @param clusterInfo
	 *            void
	 * @author 曾浩
	 * @throws
	 */
	public void setLikePress(ClusterInfo clusterInfo) {
		for (int i = 0; i < clusterListData.size(); i++) {
			ClusterInfo cl = clusterListData.get(i);
			if (cl.videoid.equals(clusterInfo.videoid)) {
				clusterListData.set(i, clusterInfo);
				break;
			}
		}

		this.notifyDataSetChanged();

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

	private void loadImage(ImageView layout, String url) {
		GlideUtils.loadImage(mContext, layout, url, R.drawable.tacitly_pic);
	}

	/**
	 * 锁住后滚动时禁止下载图片
	 * 
	 * @author xuhw
	 * @date 2015年6月8日
	 */
	public void lock() {
		lock = true;
	}

	/**
	 * 解锁后恢复下载图片功能
	 * 
	 * @author xuhw
	 * @date 2015年6月8日
	 */
	public void unlock() {
		lock = false;
		this.notifyDataSetChanged();
	}

	public static class ViewHolder {
		ImageView imageLayout;
		ImageView headimg;
		TextView nikename;
		TextView time;
		ImageView function;

		LinearLayout praiseLayout;
		ImageView zanIcon;
		TextView zanText;

		LinearLayout commentLayout;
		ImageView commentIcon;
		TextView commentText;

		LinearLayout shareLayout;
		ImageView shareIcon;
		TextView shareText;

		TextView zText;
		TextView weiguan;
		TextView detail;
		TextView totalcomments;

		TextView comment1;
		TextView comment2;
		TextView comment3;
	}

	public Bitmap getThumbBitmap(String netUrl) {
		String name = MD5Utils.hashKeyForDisk(netUrl) + ".0";
		String path = Environment.getExternalStorageDirectory() + File.separator + "goluk/image_cache";
		File file = new File(path + File.separator + name);
		Bitmap t_bitmap = null;
		if (null == file) {
			return null;
		}
		if (file.exists()) {
			t_bitmap = ImageManager.getBitmapFromCache(file.getAbsolutePath(), 100, 100);
		}
		return t_bitmap;
	}
}
