package cn.com.mobnote.golukmobile.videosuqare;

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
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;

import com.bokecc.sdk.mobile.play.DWMediaPlayer;

@SuppressLint("InflateParams")
public class VideoSquareListViewAdapter extends BaseAdapter implements VideoSuqareManagerFn, OnTouchListener {
	private Context mContext = null;
	private List<VideoSquareInfo> mVideoSquareListData = null;
	private HashMap<String, DWMediaPlayer> mDWMediaPlayerList = null;
	private int count = 0;
	private int form = 1;
	private DWMediaPlayer mDWMediaPlayer = null;
	private SharePlatformUtil sharePlatform;

	public VideoSquareListViewAdapter(Context context, int plform, SharePlatformUtil spf) {
		mContext = context;
		mVideoSquareListData = new ArrayList<VideoSquareInfo>();
		mDWMediaPlayerList = new HashMap<String, DWMediaPlayer>();
		form = plform;// 1:热门页面 2:广场页
		sharePlatform = spf;

		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener("videosharehotlist", this);
	}

	public void setData(List<VideoSquareInfo> data) {
		mVideoSquareListData.clear();
		mVideoSquareListData.addAll(data);
		count = mVideoSquareListData.size();
		this.notifyDataSetChanged();
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
	public View getView(int arg0, View convertView, ViewGroup parent) {
		VideoSquareInfo mVideoSquareInfo = mVideoSquareListData.get(arg0);
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.video_square_list_item, null);
			holder = new ViewHolder();
			holder.username = (TextView) convertView.findViewById(R.id.username);
			holder.looknumber = (TextView) convertView.findViewById(R.id.looknumber_text);
			holder.looknumberIcon = (TextView) convertView.findViewById(R.id.looknumber_icon);
			holder.userhead = (ImageView) convertView.findViewById(R.id.user_head);
			holder.videotitle = (TextView) convertView.findViewById(R.id.video_title);
			holder.sharetime = (TextView) convertView.findViewById(R.id.time);
			holder.mPlayerLayout = (RelativeLayout) convertView.findViewById(R.id.mPlayerLayout);
			holder.reporticon = (ImageButton) convertView.findViewById(R.id.report_icon);
			holder.liveicon = (ImageView) convertView.findViewById(R.id.live_icon);
			holder.mPreLoading = (ImageView) convertView.findViewById(R.id.mPreLoading);
			holder.likebtn = (Button) convertView.findViewById(R.id.like_btn);
			holder.sharebtn = (Button) convertView.findViewById(R.id.share_btn);

			holder.mRingView = (RingView) convertView.findViewById(R.id.mRingView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if ("1".equals(mVideoSquareInfo.mVideoEntity.type)) {// 直播
			holder.liveicon.setVisibility(View.VISIBLE);
		} else {// 点播
			holder.reporticon.setVisibility(View.VISIBLE);
			holder.liveicon.setVisibility(View.GONE);
		}
		holder.reporticon.setOnClickListener(new VideoSquareOnClickListener(mContext, mVideoSquareListData,
				mVideoSquareInfo, form, sharePlatform, this));
		if ("1".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {// 点赞过
			Drawable drawable = mContext.getResources().getDrawable(R.drawable.like_btn_press);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			holder.likebtn.setCompoundDrawables(drawable, null, null, null); // 设置点赞背景
		} else {

			Drawable drawable = mContext.getResources().getDrawable(R.drawable.like_btn);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			holder.likebtn.setCompoundDrawables(drawable, null, null, null);// 设置默认点赞背景
		}

		if ("1".equals(mVideoSquareInfo.mUserEntity.headportrait)) {
			holder.userhead.setBackgroundResource(R.drawable.editor_head_boy1);
		} else if ("2".equals(mVideoSquareInfo.mUserEntity.headportrait)) {
			holder.userhead.setBackgroundResource(R.drawable.editor_head_boy2);
		} else if ("3".equals(mVideoSquareInfo.mUserEntity.headportrait)) {
			holder.userhead.setBackgroundResource(R.drawable.editor_head_boy3);
		} else if ("4".equals(mVideoSquareInfo.mUserEntity.headportrait)) {
			holder.userhead.setBackgroundResource(R.drawable.editor_head_girl4);
		} else if ("5".equals(mVideoSquareInfo.mUserEntity.headportrait)) {
			holder.userhead.setBackgroundResource(R.drawable.editor_head_girl5);
		} else if ("6".equals(mVideoSquareInfo.mUserEntity.headportrait)) {
			holder.userhead.setBackgroundResource(R.drawable.editor_head_girl6);
		} else if ("7".equals(mVideoSquareInfo.mUserEntity.headportrait)) {
			holder.userhead.setBackgroundResource(R.drawable.editor_head_feault7);
		} else {
			holder.userhead.setBackgroundResource(R.drawable.editor_head_feault7);
		}

		holder.likebtn.setOnClickListener(new VideoSquareOnClickListener(mContext, mVideoSquareListData,
				mVideoSquareInfo, form, sharePlatform, this));
		holder.sharebtn.setOnClickListener(new VideoSquareOnClickListener(mContext, mVideoSquareListData,
				mVideoSquareInfo, form, sharePlatform, this));
		holder.sharebtn.setOnTouchListener(this);
		holder.username.setText(mVideoSquareInfo.mUserEntity.nickname);

		String fmtnumber = mVideoSquareInfo.mVideoEntity.clicknumber;

		DecimalFormat df = new DecimalFormat("#,###");
		int wg = Integer.parseInt(fmtnumber);

		if (wg < 100000) {
			holder.looknumber.setText(df.format(wg));
		} else {
			holder.looknumber.setText("100,000+");
		}

		holder.videotitle.setText(mVideoSquareInfo.mVideoEntity.describe);
		holder.sharetime.setText(this.formatTime(mVideoSquareInfo.mVideoEntity.sharingtime));
		holder.likebtn.setText(mVideoSquareInfo.mVideoEntity.praisenumber);
		holder.mPlayerLayout.setOnClickListener(new VideoOnClickListener(mVideoSquareListData, holder,
				mDWMediaPlayerList, mVideoSquareInfo, mContext, form));

		int width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		int height = (int) ((float) width / 1.77f);

		LinearLayout.LayoutParams mPlayerLayoutParams = new LinearLayout.LayoutParams(width, height);
		holder.mPlayerLayout.setLayoutParams(mPlayerLayoutParams);
		RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(width, height);
		holder.mPreLoading.setLayoutParams(mPreLoadingParams);
		BitmapManager.getInstance().mBitmapUtils.display(holder.mPreLoading, mVideoSquareInfo.mVideoEntity.picture);

		return convertView;
	}

	public int getUserHead(String head) {
		return 0;
	}

	public void onBackPressed() {

	}

	public void onResume() {
		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener("videosharehotlist", this);
	}

	public void onStop() {
		if (null != mDWMediaPlayerList) {
			Iterator<String> iter = mDWMediaPlayerList.keySet().iterator();
			while (iter.hasNext()) {
				Object key = iter.next();
				if (null != key) {
					DWMediaPlayer player = mDWMediaPlayerList.get(key);
					if (null != player) {
						if (player.isPlaying()) {
							player.pause();
						}
					}
				}
			}
		}
	}

	public void onDestroy() {
		if (null != mDWMediaPlayer) {
			mDWMediaPlayer.release();
		}
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
		TextView username;
		TextView looknumber;
		TextView looknumberIcon;
		ImageView userhead;
		TextView videotitle;
		TextView sharetime;
		RelativeLayout mPlayerLayout;
		ImageView liveicon;
		ImageButton reporticon;
		ImageView mPreLoading;
		Button sharebtn;
		Button likebtn;
		RingView mRingView;
	}

	VideoSquareOnClickListener mVideoSquareOnClickListener = null;

	public void setOnClick(VideoSquareOnClickListener _mVideoSquareOnClickListener) {
		mVideoSquareOnClickListener = _mVideoSquareOnClickListener;
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		if (event == SquareCmd_Req_GetShareUrl) {
			if (RESULE_SUCESS == msg) {

				if (null == mVideoSquareOnClickListener) {
					return;
				}
				mVideoSquareOnClickListener.closeRqsDialog(mContext);
				try {
					JSONObject result = new JSONObject((String) param2);
					if (result.getBoolean("success")) {
						JSONObject data = result.getJSONObject("data");
						String shareurl = data.getString("shorturl");
						String coverurl = data.getString("coverurl");
						String describe = data.optString("describe");

						String realDesc = "极路客精彩视频(使用#极路客Goluk#拍摄)";

						if (TextUtils.isEmpty(describe)) {
							if ("1".equals(mVideoSquareOnClickListener.mVideoSquareInfo.mVideoEntity.type)) {
								describe = "#极路客直播#";
							} else {
								describe = "#极路客精彩视频#";
							}
						}
						String ttl = "极路客精彩视频分享";
						if ("1".equals(mVideoSquareOnClickListener.mVideoSquareInfo.mVideoEntity.type)) {// 直播
							ttl = mVideoSquareOnClickListener.mVideoSquareInfo.mUserEntity.nickname + "的直播视频分享";
							realDesc = ttl + "(使用#极路客Goluk#拍摄)";
						}

						Bitmap bitmap = getThumbBitmap(mVideoSquareOnClickListener.mVideoSquareInfo.mVideoEntity.picture);

						// if (mContext instanceof VideoSquarePlayActivity) {
						// VideoSquarePlayActivity vspa =
						// (VideoSquarePlayActivity) mContext;
						// if (vspa != null && !vspa.isFinishing()) {
						// vspa.mCustomProgressDialog.close();
						// CustomShareBoard shareBoard = new
						// CustomShareBoard(vspa, sharePlatform, shareurl,
						// coverurl, describe, ttl, bitmap, realDesc);
						// shareBoard.showAtLocation(vspa.getWindow().getDecorView(),
						// Gravity.BOTTOM, 0, 0);
						// }
						//
						// } else

						if (mContext instanceof MainActivity) {
							MainActivity vsa = (MainActivity) mContext;
							if (vsa == null || vsa.isFinishing()) {
								return;
							} else {
								if (vsa.mCustomProgressDialog != null) {
									vsa.mCustomProgressDialog.close();
									CustomShareBoard shareBoard = new CustomShareBoard(vsa, sharePlatform, shareurl,
											coverurl, describe, ttl, bitmap, realDesc, "");
									shareBoard.showAtLocation(vsa.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
								}

							}

						}

					} else {
						GolukUtils.showToast(mContext, "网络异常，请检查网络");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				mVideoSquareOnClickListener.closeRqsDialog(mContext);
				GolukUtils.showToast(mContext, "网络异常，请检查网络");
			}
		}

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
