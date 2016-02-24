 package cn.com.mobnote.golukmobile.photoalbum;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.entity.DoubleVideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageAsyncTask;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageAsyncTask.ICallBack;
import cn.com.mobnote.util.GlideUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

public class CloudWonderfulVideoAdapter extends BaseAdapter implements StickyListHeadersAdapter {
	private PhotoAlbumActivity mActivity = null;
	private LayoutInflater inflater = null;
	private StickyListHeadersListView mListView = null;
	private List<DoubleVideoInfo> mDataList = null;
	private List<String> mGroupNameList = null;
	private int count = 0;
	private float density = 1;
	private int screenWidth = 0;
	/** 滚动中锁标识 */
//	private boolean lock = false;

	public CloudWonderfulVideoAdapter(Context c, StickyListHeadersListView listview) {
		this.mActivity = (PhotoAlbumActivity) c;
		this.mListView = listview;
		this.inflater = LayoutInflater.from(c);
		this.density = SoundUtils.getInstance().getDisplayMetrics().density;
		this.screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		this.mDataList = new ArrayList<DoubleVideoInfo>();
		this.mGroupNameList = new ArrayList<String>();

	}

	public void setData(List<String> groupname, List<DoubleVideoInfo> data) {
		mDataList.clear();
		mGroupNameList.clear();
		mDataList.addAll(data);
		mGroupNameList.addAll(groupname);
		count = mDataList.size();
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public Object getItem(int arg0) {
		return mDataList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		int width = (int) (screenWidth - 95 * density) / 2;
		int height = (int) ((float) width / 1.77f);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.video_list_item, parent, false);
			holder.mVideoLayout1 = (RelativeLayout) convertView.findViewById(R.id.mVideoLayout1);
			holder.mVideoLayout2 = (RelativeLayout) convertView.findViewById(R.id.mVideoLayout2);
			holder.mTMLayout1 = (RelativeLayout) convertView.findViewById(R.id.mTMLayout1);
			holder.mTMLayout2 = (RelativeLayout) convertView.findViewById(R.id.mTMLayout2);
			holder.image1 = (ImageView) convertView.findViewById(R.id.video_first_needle1);
			holder.image2 = (ImageView) convertView.findViewById(R.id.video_first_needle2);
			holder.mVideoCountTime1 = (TextView) convertView.findViewById(R.id.video_countTime1);
			holder.mVideoCountTime2 = (TextView) convertView.findViewById(R.id.video_countTime2);
			holder.mVideoQuality1 = (ImageView) convertView.findViewById(R.id.video_quality1);
			holder.mVideoQuality2 = (ImageView) convertView.findViewById(R.id.video_quality2);
			holder.mVideoCreateTime1 = (TextView) convertView.findViewById(R.id.video_createtime1);
			holder.mVideoCreateTime2 = (TextView) convertView.findViewById(R.id.video_createtime2);
			holder.mVideoSize1 = (TextView) convertView.findViewById(R.id.video_size1);
			holder.mVideoSize2 = (TextView) convertView.findViewById(R.id.video_size2);
			holder.line = convertView.findViewById(R.id.line);
			holder.mNewIcon1 = (ImageView) convertView.findViewById(R.id.mNewIcon1);
			holder.mNewIcon2 = (ImageView) convertView.findViewById(R.id.mNewIcon2);
			holder.mAsycnedFlag1 = (TextView) convertView.findViewById(R.id.textview_listview_item1_asysc_flag);
			holder.mAsycnedFlag2 = (TextView) convertView.findViewById(R.id.textview_listview_item2_asysc_flag);
			RelativeLayout.LayoutParams lineParams = new RelativeLayout.LayoutParams((int) (2 * density),
					(int) (height + 4 * density));
			lineParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			lineParams.setMargins((int) (29 * density), 0, (int) (12 * density), 0);
			holder.line.setLayoutParams(lineParams);

			int marginTop = 0;
			// if(0 != position) {
			marginTop = (int) (4 * density);
			// }

			RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(width, height);
			layoutParams1.addRule(RelativeLayout.RIGHT_OF, R.id.line);
			layoutParams1.setMargins((int) (4 * density), marginTop, (int) (4 * density), 0);
			holder.mVideoLayout1.setLayoutParams(layoutParams1);

			RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(width, height);
			layoutParams2.setMargins(0, marginTop, 0, 0);
			layoutParams2.addRule(RelativeLayout.RIGHT_OF, R.id.mVideoLayout1);
			holder.mVideoLayout2.setLayoutParams(layoutParams2);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.image1.setImageResource(R.drawable.tacitly_pic);
		holder.image2.setImageResource(R.drawable.tacitly_pic);

		holder.mVideoLayout2.setVisibility(View.GONE);
		VideoInfo mVideoInfo1 = mDataList.get(position).getVideoInfo1();
		VideoInfo mVideoInfo2 = mDataList.get(position).getVideoInfo2();
		holder.mTMLayout1.setTag(mVideoInfo1.videoPath);
		holder.mTMLayout2.setTag("");
		holder.mVideoCountTime1.setText(mVideoInfo1.countTime);
		holder.mVideoCreateTime1.setText(mVideoInfo1.videoCreateDate.substring(11));
		holder.mVideoSize1.setText(mVideoInfo1.videoSize);
//		holder.image1.setTag("image:" + mVideoInfo1.filename);
		displayVideoQuality(mVideoInfo1.videoHP, holder.mVideoQuality1);
		loadImage(mVideoInfo1.filename, holder.image1);
		// if(mVideoInfo1.isNew) {
		// holder.mNewIcon1.setVisibility(View.VISIBLE);
		// }else {
		holder.mNewIcon1.setVisibility(View.GONE);
		// }
		if (mVideoInfo1.isAsync) {
			holder.mAsycnedFlag1.setVisibility(View.VISIBLE);
		} else {
			holder.mAsycnedFlag1.setVisibility(View.GONE);
		}
		if (null != mVideoInfo2) {
			holder.mTMLayout2.setTag(mVideoInfo2.videoPath);
			holder.mVideoLayout2.setVisibility(View.VISIBLE);
			holder.mVideoCountTime2.setText(mVideoInfo2.countTime);
			holder.mVideoCreateTime2.setText(mVideoInfo2.videoCreateDate.substring(11));
			holder.mVideoSize2.setText(mVideoInfo2.videoSize);
//			holder.image2.setTag("image:" + mVideoInfo2.filename);
			displayVideoQuality(mVideoInfo2.videoHP, holder.mVideoQuality2);
			loadImage(mVideoInfo2.filename, holder.image2);

			// if(mVideoInfo2.isNew) {
			// holder.mNewIcon2.setVisibility(View.VISIBLE);
			// }else {
			holder.mNewIcon2.setVisibility(View.GONE);
			// }

			if (mVideoInfo2.isAsync) {
				holder.mAsycnedFlag2.setVisibility(View.VISIBLE);
			} else {
				holder.mAsycnedFlag2.setVisibility(View.GONE);
			}
		}

		updateEditState(mDataList.get(position), holder.mTMLayout1, holder.mTMLayout2);

		return convertView;
	}

	/**
	 * 更新编辑状态
	 * 
	 * @param mDoubleVideoInfo
	 *            视频数据信息
	 * @param mTMLayout1
	 *            列表左侧编辑布局
	 * @param mTMLayout2
	 *            列表右侧编辑布局
	 * @author xuhw
	 * @date 2015年6月8日
	 */
	private void updateEditState(DoubleVideoInfo mDoubleVideoInfo, RelativeLayout mTMLayout1, RelativeLayout mTMLayout2) {
		VideoInfo mVideoInfo1 = mDoubleVideoInfo.getVideoInfo1();
		VideoInfo mVideoInfo2 = mDoubleVideoInfo.getVideoInfo2();
		List<String> selectedData = mActivity.getSelectedList();
		if (mActivity.getEditState()) {
			if (selectedData.contains(mVideoInfo1.videoPath)) {
				mTMLayout1.setVisibility(View.VISIBLE);
			} else {
				mTMLayout1.setVisibility(View.GONE);
			}

			if (null == mVideoInfo2) {
				return;
			}

			if (selectedData.contains(mVideoInfo2.videoPath)) {
				mTMLayout2.setVisibility(View.VISIBLE);
			} else {
				mTMLayout2.setVisibility(View.GONE);
			}
		} else {
			mTMLayout1.setVisibility(View.GONE);
			mTMLayout2.setVisibility(View.GONE);
		}

	}

	/**
	 * 加载并显示预览图片
	 * 
	 * @param filename
	 *            图片名称
	 * @param image
	 *            显示控件
	 * @author xuhw
	 * @date 2015年6月8日
	 */
	private void loadImage(String filename, ImageView image) {
		filename = filename.replace(".mp4", ".jpg");
		String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
		GlideUtils.loadImage(mActivity, image, filePath + File.separator + filename, R.drawable.album_default_img);
//		Bitmap mBitmap = mActivity.getBitmap(filename);
//		if (null != mBitmap) {
//			image.setImageBitmap(mBitmap);
//		} else {
//			image.setImageResource(R.drawable.album_default_img);
//			// if (lock) {
//			// return;
//			// }
//
//			String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
//
//			ImageAsyncTask.getBitmapForCache(filePath + File.separator + filename, new ICallBack() {
//				@Override
//				public void SuccessCallback(String url, Bitmap mBitmap) {
//					String filename = url.substring(url.lastIndexOf("/") + 1);
//					if (null == mBitmap) {
//						return;
//					}
//
//					Bitmap b = mActivity.getBitmap(filename);
//					if (null == b) {
//						b = mBitmap;
//						mActivity.putBitmap(filename, mBitmap);
//					} else {
//						if (null != mBitmap) {
//							if (!mBitmap.isRecycled()) {
//								mBitmap.recycle();
//								mBitmap = null;
//							}
//						}
//					}
//
//					String imagefilename = filename.replace(".jpg", ".mp4");
//					ImageView image = (ImageView) mListView.findViewWithTag("image:" + imagefilename);
//					if (null != image) {
//						image.setImageBitmap(b);
//					}
//				}
//			});
//		}
	}

	/**
	 * 显示视频质量
	 * 
	 * @param videoName
	 *            视频名称
	 * @param videoHP
	 *            视频分辨率
	 * @param image
	 *            显示控件
	 * @author xuhw
	 * @date 2015年6月8日
	 */
	private void displayVideoQuality(String videoHP, ImageView image) {
		image.setVisibility(View.GONE);
		GolukDebugUtils.e("", "TTTTTTTTTTTT===@@@@@===videoHP=" + videoHP);

		if ("1080P".equalsIgnoreCase(videoHP)) {
			image.setVisibility(View.VISIBLE);
			image.setBackgroundResource(R.drawable.carrecorder_liveindex_icon_1080);
		}
	}

	@Override
	public long getHeaderId(int position) {
		long id = 0;
		String groupname = "";
		for (int i = 0; i < mGroupNameList.size(); i++) {
			groupname = mGroupNameList.get(i);
			String path1 = mDataList.get(position).getVideoInfo1().videoCreateDate;
			if (path1.contains(groupname)) {
				id = i;
				break;
			}
		}

		return id;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderViewHolder holder;
		 if (convertView == null) {
		holder = new HeaderViewHolder();
		convertView = inflater.inflate(R.layout.video_list_groupname, parent, false);
		holder.date = (TextView) convertView.findViewById(R.id.date);
		holder.mTopLine = (ImageView) convertView.findViewById(R.id.mTopLine);
		convertView.setTag(holder);
		 } else {
		 holder = (HeaderViewHolder) convertView.getTag();
		 }

		if (0 == position) {
			holder.mTopLine.setVisibility(View.GONE);
		} else {
			holder.mTopLine.setVisibility(View.VISIBLE);
		}

		String headerText = "";
		for (int i = 0; i < mGroupNameList.size(); i++) {
			if (mDataList.get(position).getVideoInfo1().videoCreateDate.contains(mGroupNameList.get(i))) {
				headerText = mGroupNameList.get(i);
				break;
			}
		}

		String time[] = headerText.split("-");
		if (3 == time.length) {
			Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH) + 1;
			int day = c.get(Calendar.DAY_OF_MONTH);
			int o_year = Integer.parseInt(time[0]);
			if (year == o_year) {
				int o_month = Integer.parseInt(time[1]);
				int o_day = Integer.parseInt(time[2]);

				if (month == o_month) {
					if (day == o_day) {
						holder.date.setText(mActivity.getResources().getString(R.string.str_today));
					} else if (day == (o_day + 1)) {
						holder.date.setText(mActivity.getResources().getString(R.string.str_yestoday));
					} else {
						holder.date.setText(time[1] + "/" + time[2]);
					}
				} else {
					holder.date.setText(time[1] + "/" + time[2]);
				}

			} else {
				String t_str = time[0] + "\n" + time[1] + "/" + time[2];

				SpannableStringBuilder style = new SpannableStringBuilder(t_str);
				style.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)), 0, 4,
						Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				style.setSpan(new AbsoluteSizeSpan(16, true), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				holder.date.setText(style);
			}
		} else {
			holder.date.setText(headerText);
		}

		return convertView;
	}

	static class HeaderViewHolder {
		TextView year;
		TextView date;
		ImageView mTopLine;
	}

	static class ViewHolder {
		RelativeLayout mVideoLayout1;
		RelativeLayout mVideoLayout2;
		RelativeLayout mTMLayout1;
		RelativeLayout mTMLayout2;
		ImageView image1;
		ImageView image2;
		TextView mVideoCountTime1;
		TextView mVideoCountTime2;
		ImageView mVideoQuality1;
		ImageView mVideoQuality2;
		TextView mVideoCreateTime1;
		TextView mVideoCreateTime2;
		TextView mVideoSize1;
		TextView mVideoSize2;
		View line;
		ImageView mNewIcon1;
		ImageView mNewIcon2;
		TextView mAsycnedFlag1;
		TextView mAsycnedFlag2;
	}

	/**
	 * 锁住后滚动时禁止下载图片
	 * 
	 * @author xuhw
	 * @date 2015年6月8日
	 */
//	public void lock() {
//		lock = true;
//	}

	/**
	 * 解锁后恢复下载图片功能
	 * 
	 * @author xuhw
	 * @date 2015年6月8日
	 */
//	public void unlock() {
//		lock = false;
//		// this.notifyDataSetChanged();
//	}

	public void updateImage(String filename) {
		filename = filename.replace(".jpg", ".mp4");
		ImageView image = (ImageView) mListView.findViewWithTag("image:" + filename);
		if (null != image) {
			loadImage(filename, image);
		}
	}

	public void updateAsyncFlag(String filename, boolean flag) {
		if (mDataList != null) {
			for (DoubleVideoInfo item: mDataList) {
				VideoInfo videoInfo1 = item.getVideoInfo1();
				if (filename.equals(videoInfo1.filename)) {
					videoInfo1.isAsync = flag;
					notifyDataSetChanged();
					break;
				}
				VideoInfo videoInfo2 = item.getVideoInfo2();

				if (videoInfo2 != null && filename.equals(videoInfo2.filename)) {
					videoInfo2.isAsync = flag;
					notifyDataSetChanged();
					break;
				}
			}
		}
	}
}
