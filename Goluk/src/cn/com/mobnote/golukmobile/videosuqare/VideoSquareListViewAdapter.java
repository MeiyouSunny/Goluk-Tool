package cn.com.mobnote.golukmobile.videosuqare;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.bokecc.sdk.mobile.play.DWMediaPlayer;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.SharePlatformUtil;
import cn.com.mobnote.golukmobile.carrecorder.util.BitmapManager;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.umeng.widget.CustomShareBoard;
import cn.com.mobnote.util.GolukUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class VideoSquareListViewAdapter extends BaseAdapter implements VideoSuqareManagerFn {
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
			holder.userhead = (ImageView) convertView.findViewById(R.id.user_head);
			holder.likenumber = (Button) convertView.findViewById(R.id.like_btn);
			holder.videotitle = (TextView) convertView.findViewById(R.id.video_title);
			holder.sharetime = (TextView) convertView.findViewById(R.id.time);
			holder.mPlayerLayout = (RelativeLayout) convertView.findViewById(R.id.mPlayerLayout);
			// holder.mSurfaceView = (SurfaceView) convertView
			// .findViewById(R.id.mSurfaceView);
			// holder.mSurfaceView.setZOrderMediaOverlay(true);
			holder.reporticon = (ImageButton) convertView.findViewById(R.id.report_icon);
			holder.liveicon = (ImageView) convertView.findViewById(R.id.live_icon);
			holder.mPreLoading = (ImageView) convertView.findViewById(R.id.mPreLoading);
			holder.likebtn = (Button) convertView.findViewById(R.id.like_btn);
			holder.sharebtn = (ImageButton) convertView.findViewById(R.id.share_btn);

			holder.mRingView = (RingView) convertView.findViewById(R.id.mRingView);
			// holder.mTextureView =
			// (TextureView)convertView.findViewById(R.id.mTextureView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if ("1".equals(mVideoSquareInfo.mVideoEntity.type)) {// 直播
			holder.reporticon.setVisibility(View.GONE);
			holder.liveicon.setVisibility(View.VISIBLE);
			// holder.mSurfaceView.setVisibility(View.GONE);
		} else {// 点播
			holder.reporticon.setVisibility(View.VISIBLE);
			holder.liveicon.setVisibility(View.GONE);
			// holder.mSurfaceView.setVisibility(View.VISIBLE);
		}
		holder.reporticon.setOnClickListener(new VideoSquareOnClickListener(mContext, mVideoSquareListData,
				mVideoSquareInfo, form, sharePlatform, this));
		if ("1".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {// 点赞过
			holder.likebtn.setBackgroundResource(R.drawable.livestreaming_heart_btn_down);// 设置点赞背景
		} else {
			holder.likebtn.setBackgroundResource(R.drawable.livestreaming_heart_btn);// 设置默认点赞背景
		}

		if ("1".equals(mVideoSquareInfo.mUserEntity.headportrait)) {
			holder.userhead.setBackgroundResource(R.drawable.individual_center_head_boy_one);
		} else if ("2".equals(mVideoSquareInfo.mUserEntity.headportrait)) {
			holder.userhead.setBackgroundResource(R.drawable.individual_center_head_boy_two);
		} else if ("3".equals(mVideoSquareInfo.mUserEntity.headportrait)) {
			holder.userhead.setBackgroundResource(R.drawable.individual_center_head_boy_three);
		} else if ("4".equals(mVideoSquareInfo.mUserEntity.headportrait)) {
			holder.userhead.setBackgroundResource(R.drawable.individual_center_head_girl_one);
		} else if ("5".equals(mVideoSquareInfo.mUserEntity.headportrait)) {
			holder.userhead.setBackgroundResource(R.drawable.individual_center_head_girl_two);
		} else if ("6".equals(mVideoSquareInfo.mUserEntity.headportrait)) {
			holder.userhead.setBackgroundResource(R.drawable.individual_center_head_girl_three);
		} else if ("7".equals(mVideoSquareInfo.mUserEntity.headportrait)) {
			holder.userhead.setBackgroundResource(R.drawable.individual_center_head_moren);
		} else {
			holder.userhead.setBackgroundResource(R.drawable.individual_center_head_moren);
		}

		holder.likebtn.setOnClickListener(new VideoSquareOnClickListener(mContext, mVideoSquareListData,
				mVideoSquareInfo, form, sharePlatform, this));
		holder.sharebtn.setOnClickListener(new VideoSquareOnClickListener(mContext, mVideoSquareListData,
				mVideoSquareInfo, form, sharePlatform, this));
		holder.username.setText(mVideoSquareInfo.mUserEntity.nickname);
		holder.looknumber.setText(mVideoSquareInfo.mVideoEntity.clicknumber);
		holder.likenumber.setText(mVideoSquareInfo.mVideoEntity.praisenumber);
		holder.videotitle.setText(mVideoSquareInfo.mVideoEntity.describe);
		holder.sharetime.setText(this.formatTime(mVideoSquareInfo.mVideoEntity.sharingtime));

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return time;
	}

	public static class ViewHolder {
		TextView username;
		TextView looknumber;
		ImageView userhead;
		Button likenumber;
		TextView videotitle;
		TextView sharetime;
		RelativeLayout mPlayerLayout;
		ImageView liveicon;
		ImageButton reporticon;
		ImageView mPreLoading;
		ImageButton sharebtn;
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
						if (TextUtils.isEmpty(describe)) {
							if("1".equals(mVideoSquareOnClickListener.mVideoSquareInfo.mVideoEntity.type)){
								describe = "#极路客直播#";
							}else{
								describe = "#极路客精彩视频#";
							}
						}

						if ("".equals(coverurl)) {

						}
						String ttl = "极路客精彩视频分享";
						if ("1".equals(mVideoSquareOnClickListener.mVideoSquareInfo.mVideoEntity.type)) {// 直播
							ttl = mVideoSquareOnClickListener.mVideoSquareInfo.mUserEntity.nickname + "的直播视频分享";
							
						}
						if (mContext instanceof VideoSquarePlayActivity) {
							VideoSquarePlayActivity vspa = (VideoSquarePlayActivity) mContext;
							if (vspa != null && !vspa.isFinishing()) {
								vspa.mCustomProgressDialog.close();
								CustomShareBoard shareBoard = new CustomShareBoard(vspa, sharePlatform, shareurl,
										coverurl, describe, ttl);
								shareBoard.showAtLocation(vspa.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
							}

						} else if (mContext instanceof MainActivity) {
							MainActivity vsa = (MainActivity) mContext;
							if (vsa == null || vsa.isFinishing()) {
								return;
							} else {
								if (vsa.mCustomProgressDialog != null) {
									vsa.mCustomProgressDialog.close();
									CustomShareBoard shareBoard = new CustomShareBoard(vsa, sharePlatform, shareurl,
											coverurl, describe, ttl);
									shareBoard.showAtLocation(vsa.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
								}

							}

						}

					} else {
						GolukUtils.showToast(mContext, "网络异常，请检查网络");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// mVideoSquareOnClickListener.closeRqsDialog(mContext);
				GolukUtils.showToast(mContext, "网络异常，请检查网络");
			}
		}

	}

}
