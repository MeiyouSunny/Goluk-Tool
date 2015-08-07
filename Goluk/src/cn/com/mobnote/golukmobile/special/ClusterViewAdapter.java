package cn.com.mobnote.golukmobile.special;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.BitmapManager;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.MD5Utils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.newest.ClickCategoryListener;
import cn.com.mobnote.golukmobile.newest.ClickCommentListener;
import cn.com.mobnote.golukmobile.newest.ClickNewestListener;
import cn.com.mobnote.golukmobile.newest.ClickPraiseListener;
import cn.com.mobnote.golukmobile.newest.CommentDataInfo;
import cn.com.mobnote.golukmobile.newest.NewestAdapter.ViewHolder;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;

import com.bokecc.sdk.mobile.play.DWMediaPlayer;
import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;

@SuppressLint("InflateParams")
public class ClusterViewAdapter extends BaseAdapter implements VideoSuqareManagerFn, OnTouchListener {
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

		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener("videosharehotlist", this);
	}

	public void setData(List<ClusterInfo> data, SpecialInfo head) {
		clusterListData.clear();
		clusterListData.addAll(data);
		count = clusterListData.size();
		
		if (head == null) {
			VIEW_TYPE = 1;
		} else {
			count++;
			headdata = head;
		}
		
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
		return VIEW_TYPE;
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

				BitmapManager.getInstance().mBitmapUtils.display(image, headdata.imagepath);

			}
			break;
		case TYPE_2:
			if (convertView == null) {
				int index = position;
				if (headdata != null) {
					index--;
				}
				ClusterInfo clusterInfo = clusterListData.get(index);
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.newest_list_item, null);
				holder.imageLayout = (RelativeLayout) convertView.findViewById(R.id.imageLayout);
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

				holder.nikename.setText(clusterInfo.author);
				holder.time.setText(clusterInfo.sharingtime);
				holder.zText.setText(clusterInfo.praisenumber);
				holder.weiguan.setText(clusterInfo.clicknumber + "次围观");
				holder.detail.setText(clusterInfo.author + "  " + clusterInfo.describe);
				holder.totalcomments.setText("查看所有" + clusterInfo.comments + "条评论");

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
				
				int height = (int) ((float) width / 1.77f);
				RelativeLayout.LayoutParams mPlayerLayoutParams = new RelativeLayout.LayoutParams(width, height);
				mPlayerLayoutParams.addRule(RelativeLayout.BELOW, R.id.headlayout);
				holder.imageLayout.setLayoutParams(mPlayerLayoutParams);
				
				loadImage(holder.imageLayout, clusterInfo.imagepath);
				
				initListener(index--);
				
				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			break;
		default:
			break;
		}

		return convertView;
	}
	
	private void initListener(int index) {
		ClusterInfo clusterInfo = clusterListData.get(index);
		
		holder.commentLayout.setOnClickListener(new ClusterCommentListener(mContext, clusterInfo ,false));
		holder.totalcomments.setOnClickListener(new ClusterCommentListener(mContext, clusterInfo ,false));
	}

	public int getUserHead(String head) {
		return 0;
	}

	public void onBackPressed() {

	}

	public void onResume() {
		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener("videosharehotlist", this);
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
	
	private void loadImage(RelativeLayout layout, String url) {
		layout.removeAllViews();
        SimpleDraweeView view = new SimpleDraweeView(mContext);
        GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(mContext.getResources());
        GenericDraweeHierarchy hierarchy = builder
                    .setFadeDuration(300)
                    .setPlaceholderImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
                    .setFailureImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
                    .setActualImageScaleType(ScaleType.FIT_XY)
                    .build();
        view.setHierarchy(hierarchy);

        if (!lock) {
        	view.setImageURI(Uri.parse(url));
        }
                
        int height = (int) ((float) width / 1.77f);
        RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(width, height);
        layout.addView(view, mPreLoadingParams);
        
	}
	
	/**
	 * 锁住后滚动时禁止下载图片
	 * @author xuhw
	 * @date 2015年6月8日
	 */
	public void lock() {
		lock = true;
	}
	
	/**
	 * 解锁后恢复下载图片功能
	 * @author xuhw
	 * @date 2015年6月8日
	 */
	public void unlock() {
		lock = false;
		this.notifyDataSetChanged();
	}

	public static class ViewHolder {
		RelativeLayout imageLayout;
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

	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {

	}

	/*
	 * VideoSquareOnClickListener mVideoSquareOnClickListener = null;
	 * 
	 * public void setOnClick(VideoSquareOnClickListener
	 * _mVideoSquareOnClickListener) { mVideoSquareOnClickListener =
	 * _mVideoSquareOnClickListener; }
	 * 
	 * @Override public void VideoSuqare_CallBack(int event, int msg, int
	 * param1, Object param2) { if (event == SquareCmd_Req_GetShareUrl) { if
	 * (RESULE_SUCESS == msg) {
	 * 
	 * if (null == mVideoSquareOnClickListener) { return; }
	 * mVideoSquareOnClickListener.closeRqsDialog(mContext); try { JSONObject
	 * result = new JSONObject((String) param2); if
	 * (result.getBoolean("success")) { JSONObject data =
	 * result.getJSONObject("data"); String shareurl =
	 * data.getString("shorturl"); String coverurl = data.getString("coverurl");
	 * String describe = data.optString("describe");
	 * 
	 * String realDesc = "极路客精彩视频(使用#极路客Goluk#拍摄)";
	 * 
	 * if (TextUtils.isEmpty(describe)) { if
	 * ("1".equals(mVideoSquareOnClickListener
	 * .mVideoSquareInfo.mVideoEntity.type)) { describe = "#极路客直播#"; } else {
	 * describe = "#极路客精彩视频#"; } } String ttl = "极路客精彩视频分享"; if
	 * ("1".equals(mVideoSquareOnClickListener
	 * .mVideoSquareInfo.mVideoEntity.type)) {// 直播 ttl =
	 * mVideoSquareOnClickListener.mVideoSquareInfo.mUserEntity.nickname +
	 * "的直播视频分享"; realDesc = ttl + "(使用#极路客Goluk#拍摄)"; }
	 * 
	 * Bitmap bitmap =
	 * getThumbBitmap(mVideoSquareOnClickListener.mVideoSquareInfo
	 * .mVideoEntity.picture);
	 * 
	 * if (mContext instanceof VideoSquarePlayActivity) {
	 * VideoSquarePlayActivity vspa = (VideoSquarePlayActivity) mContext; if
	 * (vspa != null && !vspa.isFinishing()) {
	 * vspa.mCustomProgressDialog.close(); CustomShareBoard shareBoard = new
	 * CustomShareBoard(vspa, sharePlatform, shareurl, coverurl, describe, ttl,
	 * bitmap, realDesc);
	 * shareBoard.showAtLocation(vspa.getWindow().getDecorView(),
	 * Gravity.BOTTOM, 0, 0); }
	 * 
	 * } else if (mContext instanceof MainActivity) { MainActivity vsa =
	 * (MainActivity) mContext; if (vsa == null || vsa.isFinishing()) { return;
	 * } else { if (vsa.mCustomProgressDialog != null) {
	 * vsa.mCustomProgressDialog.close(); CustomShareBoard shareBoard = new
	 * CustomShareBoard(vsa, sharePlatform, shareurl, coverurl, describe, ttl,
	 * bitmap, realDesc);
	 * shareBoard.showAtLocation(vsa.getWindow().getDecorView(), Gravity.BOTTOM,
	 * 0, 0); }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * } else { GolukUtils.showToast(mContext, "网络异常，请检查网络"); } } catch
	 * (JSONException e) { e.printStackTrace(); } } else {
	 * mVideoSquareOnClickListener.closeRqsDialog(mContext);
	 * GolukUtils.showToast(mContext, "网络异常，请检查网络"); } }
	 * 
	 * }
	 */

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

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		switch (v.getId()) {

		case R.id.share_btn:
			Button sharebtn = (Button) v;
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				Drawable more_down = mContext.getResources().getDrawable(R.drawable.share_btn_press);
				sharebtn.setCompoundDrawablesWithIntrinsicBounds(more_down, null, null, null);
				sharebtn.setTextColor(Color.rgb(59, 151, 245));
				break;
			case MotionEvent.ACTION_UP:
				Drawable more_up = mContext.getResources().getDrawable(R.drawable.share_btn);
				sharebtn.setCompoundDrawablesWithIntrinsicBounds(more_up, null, null, null);
				sharebtn.setTextColor(Color.rgb(136, 136, 136));
				break;
			}
			break;
		}
		return false;
	}

}
