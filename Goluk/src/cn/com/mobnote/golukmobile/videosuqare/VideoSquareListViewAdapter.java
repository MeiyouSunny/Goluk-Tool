package cn.com.mobnote.golukmobile.videosuqare;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.bokecc.sdk.mobile.play.DWMediaPlayer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class VideoSquareListViewAdapter extends BaseAdapter {
	private Context mContext = null;
	private List<VideoSquareInfo> mVideoSquareListData = null;
	private HashMap<String, DWMediaPlayer> mDWMediaPlayerList = null;
	private int count = 0;
	private final String USERID = "77D36B9636FF19CF";
	private final String API_KEY = "O8g0bf8kqiWroHuJaRmihZfEmj7VWImF";
	private DisplayImageOptions options;
	private ImageLoader imageLoader = ImageLoader.getInstance();

	public VideoSquareListViewAdapter(Context context) {
		mContext = context;
		mVideoSquareListData = new ArrayList<VideoSquareInfo>();
		mDWMediaPlayerList = new HashMap<String, DWMediaPlayer>();

		options = new DisplayImageOptions.Builder()
				// .showImageOnLoading(R.drawable.ic_stub)
				// .showImageForEmptyUri(R.drawable.ic_empty)
				// .showImageOnFail(R.drawable.ic_error)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.cacheInMemory(true).cacheOnDisc(true).considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				// .displayer(new RoundedBitmapDisplayer(20))
				.build();
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
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.video_square_list_item, null);
			holder = new ViewHolder();
			holder.username = (TextView) convertView
					.findViewById(R.id.username);
			holder.looknumber = (TextView) convertView
					.findViewById(R.id.looknumber_text);
			holder.userhead = (ImageView) convertView
					.findViewById(R.id.user_head);
			holder.likenumber = (Button) convertView
					.findViewById(R.id.like_btn);
			holder.videotitle = (TextView) convertView
					.findViewById(R.id.video_title);
			holder.sharetime = (TextView) convertView.findViewById(R.id.time);
			holder.mPlayerLayout = (RelativeLayout) convertView
					.findViewById(R.id.mPlayerLayout);
			holder.mSurfaceView = (SurfaceView) convertView
					.findViewById(R.id.mSurfaceView);
			holder.reporticon = (ImageView) convertView
					.findViewById(R.id.report_icon);
			holder.liveicon = (ImageView) convertView
					.findViewById(R.id.live_icon);
			holder.mPreLoading = (ImageView) convertView
					.findViewById(R.id.mPreLoading);
			holder.likebtn = (Button) convertView
					.findViewById(R.id.like_btn);
			holder.sharebtn = (Button) convertView.findViewById(R.id.share_btn);
			
			holder.mRingView = (RingView) convertView
					.findViewById(R.id.mRingView);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if ("1".equals(mVideoSquareInfo.mVideoEntity.type)) {// 直播
			holder.reporticon.setVisibility(View.GONE);
			holder.liveicon.setVisibility(View.VISIBLE);
			holder.mSurfaceView.setVisibility(View.GONE);
		} else {// 点播
			holder.reporticon.setVisibility(View.VISIBLE);
			holder.liveicon.setVisibility(View.GONE);
			holder.mSurfaceView.setVisibility(View.VISIBLE);
		}
		
		if("1".equals(mVideoSquareInfo.mVideoEntity.ispraise)){// 点赞过
			holder.likebtn.setBackgroundResource(R.drawable.livestreaming_heart_btn_down);//设置点赞背景
		}else{
			holder.likebtn.setBackgroundResource(R.drawable.livestreaming_heart_btn);//设置默认点赞背景
		}
		
		holder.likebtn.setOnClickListener(new VideoSquareOnClickListener(mContext,mVideoSquareListData,mVideoSquareInfo));
		holder.sharebtn.setOnClickListener(new VideoSquareOnClickListener(mContext,mVideoSquareListData,mVideoSquareInfo));
		holder.username.setText(mVideoSquareInfo.mUserEntity.nickname);
		holder.looknumber.setText(mVideoSquareInfo.mVideoEntity.clicknumber);
		holder.likenumber.setText(mVideoSquareInfo.mVideoEntity.praisenumber);
		holder.videotitle.setText(mVideoSquareInfo.mVideoEntity.describe);
		holder.sharetime.setText(this
				.formatTime(mVideoSquareInfo.mVideoEntity.sharingtime));

		holder.mPlayerLayout.setOnClickListener(new VideoOnClickListener(
				holder, mDWMediaPlayerList, mVideoSquareInfo));

		String videoid = mVideoSquareInfo.mVideoEntity.videoid;
		if ("2".equals(mVideoSquareInfo.mVideoEntity.type)) {
			if (!TextUtils.isEmpty(videoid)) {
				if (!mDWMediaPlayerList.containsKey(videoid)) {
					holder.mPreLoading.setVisibility(View.VISIBLE);
					DWMediaPlayer mDWMediaPlayer = new DWMediaPlayer();
					mDWMediaPlayer.setVideoPlayInfo(videoid, USERID, API_KEY,
							mContext);
					mDWMediaPlayer.setOnErrorListener(new VideoOnErrorListener(
							mVideoSquareInfo));
					mDWMediaPlayer
							.setOnPreparedListener(new VideoOnPreparedListener(
									mDWMediaPlayerList, mVideoSquareInfo));
					mDWMediaPlayer
							.setOnBufferingUpdateListener(new VideoOnBufferingUpdateListener(
									mDWMediaPlayerList, holder,
									mVideoSquareInfo));
					mDWMediaPlayerList.put(videoid, mDWMediaPlayer);
				} else {
					DWMediaPlayer mDWMediaPlayer = mDWMediaPlayerList
							.get(videoid);
					if (null != mDWMediaPlayer) {
						LogUtils.d("SSS==========1111=====videoid="
								+ mVideoSquareInfo.mVideoEntity.videoid);
						if (mDWMediaPlayer.isPlaying()) {
							LogUtils.d("SSS=======222===GONE======");
							holder.mPreLoading.setVisibility(View.GONE);
						} else {

						}
					}
				}
			}
		}

		int width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		int height = (int) ((float) width / 1.77f);

		SurfaceHolder mSurfaceHolder = holder.mSurfaceView.getHolder();
		LinearLayout.LayoutParams mPlayerLayoutParams = new LinearLayout.LayoutParams(
				width, height);
		holder.mPlayerLayout.setLayoutParams(mPlayerLayoutParams);
		mSurfaceHolder.addCallback(new SurfaceViewCallback(mDWMediaPlayerList,
				mVideoSquareInfo));

//		imageLoader.displayImage(mVideoSquareInfo.mUserEntity.headportrait,
//				holder.userhead, options, null);
		imageLoader.displayImage(mVideoSquareInfo.mVideoEntity.picture,
				holder.mPreLoading, options, null);

		return convertView;
	}

	public int getUserHead(String head) {
		return 0;
	}

	public void onBackPressed() {
		if (null != imageLoader) {
			imageLoader.stop();
		}
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
		if (null != imageLoader) {
//			imageLoader.clearMemoryCache();
			// imageLoader.clearDiscCache();
		}
		if (null != mDWMediaPlayerList) {
			Iterator<String> iter = mDWMediaPlayerList.keySet().iterator();
			while (iter.hasNext()) {
				Object key = iter.next();
				if (null != key) {
					DWMediaPlayer player = mDWMediaPlayerList.get(key);
					if (null != player) {
						player.release();
					}
				}
			}
		}
	}

	@SuppressLint("SimpleDateFormat")
	public String formatTime(String date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(date, pos);

		formatter = new SimpleDateFormat("MM月dd日 HH时mm分");
		return formatter.format(strtodate);

	}
	

	public static class ViewHolder {
		TextView username;
		TextView looknumber;
		ImageView userhead;
		Button likenumber;
		TextView videotitle;	
		TextView sharetime;
		RelativeLayout mPlayerLayout;
		SurfaceView mSurfaceView;
		ImageView liveicon;
		ImageView reporticon;
		ImageView mPreLoading;
		Button sharebtn;
		Button likebtn;
		RingView mRingView;
	}

}
