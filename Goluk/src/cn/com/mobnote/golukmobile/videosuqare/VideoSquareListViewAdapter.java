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
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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
	private int form = 1;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DWMediaPlayer mDWMediaPlayer=null;
	private HashMap<String, SurfaceHolder> mHolderList=null;
	private String curVideoid="";
	private int index;
	private ListView mRTPullListView=null;

	public VideoSquareListViewAdapter(ListView _mRTPullListView, Context context,int plform) {
		mContext = context;
		this.mRTPullListView=_mRTPullListView;
		mVideoSquareListData = new ArrayList<VideoSquareInfo>();
		mDWMediaPlayerList = new HashMap<String, DWMediaPlayer>();
		mHolderList = new HashMap<String, SurfaceHolder>();
		form = plform;//1:热门页面 2:广场页
		options = new DisplayImageOptions.Builder()
				// .showImageOnLoading(R.drawable.ic_stub)
				// .showImageForEmptyUri(R.drawable.ic_empty)
				// .showImageOnFail(R.drawable.ic_error)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.cacheInMemory(true).cacheOnDisc(true).considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				// .displayer(new RoundedBitmapDisplayer(20))
				.build();
		
//		mDWMediaPlayer = new DWMediaPlayer();
//		mDWMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
//			@Override
//			public void onPrepared(MediaPlayer arg0) {
//				arg0.start();
//				System.out.println("SSSYYY========arg0.start();=======");
//			}
//		});
//		mDWMediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
//			@Override
//			public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
//				int first = mRTPullListView.getFirstVisiblePosition();
//				int position = index - first;
//				View view = mRTPullListView.getChildAt(position);
//				if(null != view){
//					RingView ring = (RingView)view.findViewById(R.id.mRingView);
//					ImageView image = (ImageView)view.findViewById(R.id.mPreLoading);
//					if(null != ring){
//						ring.setVisibility(View.VISIBLE);
//						ring.setProcess(arg1);
//						System.out.println("SSSYYY========onBufferingUpdate==VISIBLE====arg1="+arg1);
//						if(arg1 >= 100){
//							ring.setVisibility(View.GONE);
//						}
//					}
//					if(null != image){
//						if(arg1 >= 100){
//							image.setVisibility(View.GONE);
//						}
//					}
//				}
////				System.out.println("SSSYYY========onBufferingUpdate======arg1="+arg1);
//			}
//		});
//		mDWMediaPlayer.setOnErrorListener(new OnErrorListener() {
//			@Override
//			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
//				System.out.println("SSSYYY========onError======arg1="+arg1+"=arg2="+arg2);
//				return false;
//			}
//		});
//		mDWMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
//			@Override
//			public void onCompletion(MediaPlayer arg0) {
//				mDWMediaPlayer.setDisplay(null);
//				System.out.println("SSSYYY========onCompletion======");
//			}
//		});
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
			holder.mSurfaceView.setZOrderMediaOverlay(true);
			holder.reporticon = (ImageButton) convertView
					.findViewById(R.id.report_icon);
			holder.liveicon = (ImageView) convertView
					.findViewById(R.id.live_icon);
			holder.mPreLoading = (ImageView) convertView
					.findViewById(R.id.mPreLoading);
			holder.likebtn = (Button) convertView.findViewById(R.id.like_btn);
			holder.sharebtn = (Button) convertView.findViewById(R.id.share_btn);

			holder.mRingView = (RingView) convertView
					.findViewById(R.id.mRingView);
//			holder.mTextureView = (TextureView)convertView.findViewById(R.id.mTextureView);
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
			holder.reporticon.setOnClickListener(new VideoSquareOnClickListener(mContext,mVideoSquareListData,mVideoSquareInfo,form));
		}
		
		if("1".equals(mVideoSquareInfo.mVideoEntity.ispraise)){// 点赞过
			holder.likebtn.setBackgroundResource(R.drawable.livestreaming_heart_btn_down);//设置点赞背景
		}else{
			holder.likebtn.setBackgroundResource(R.drawable.livestreaming_heart_btn);//设置默认点赞背景
		}
		
		holder.likebtn.setOnClickListener(new VideoSquareOnClickListener(mContext,mVideoSquareListData,mVideoSquareInfo,form));
		holder.sharebtn.setOnClickListener(new VideoSquareOnClickListener(mContext,mVideoSquareListData,mVideoSquareInfo,form));
		holder.username.setText(mVideoSquareInfo.mUserEntity.nickname);
		holder.looknumber.setText(mVideoSquareInfo.mVideoEntity.clicknumber);
		holder.likenumber.setText(mVideoSquareInfo.mVideoEntity.praisenumber);
		holder.videotitle.setText(mVideoSquareInfo.mVideoEntity.describe);
		holder.sharetime.setText(this
				.formatTime(mVideoSquareInfo.mVideoEntity.sharingtime));

		holder.mPlayerLayout.setOnClickListener(new VideoOnClickListener(
				mVideoSquareListData, holder, mDWMediaPlayerList,
				mVideoSquareInfo));

//		holder.mPreLoading.setVisibility(View.GONE);
//		String videoid = mVideoSquareInfo.mVideoEntity.videoid;
//		if ("2".equals(mVideoSquareInfo.mVideoEntity.type)) {
//			if (!TextUtils.isEmpty(videoid)) {
//				if (!mDWMediaPlayerList.containsKey(mVideoSquareInfo.id)) {
//					holder.mPreLoading.setVisibility(View.VISIBLE);
//					DWMediaPlayer mDWMediaPlayer = new DWMediaPlayer();
//					mDWMediaPlayer.setVideoPlayInfo(videoid, USERID, API_KEY,
//							mContext);
//					mDWMediaPlayer.setOnErrorListener(new VideoOnErrorListener(
//							mVideoSquareInfo));
//					mDWMediaPlayer
//							.setOnPreparedListener(new VideoOnPreparedListener(
//									mVideoSquareListData, mDWMediaPlayerList,
//									mVideoSquareInfo));
//					mDWMediaPlayer
//							.setOnBufferingUpdateListener(new VideoOnBufferingUpdateListener(
//									mVideoSquareListData, mDWMediaPlayerList,
//									holder, mVideoSquareInfo));
//					mDWMediaPlayerList.put(mVideoSquareInfo.id, mDWMediaPlayer);
//				} else {
//					DWMediaPlayer mDWMediaPlayer = mDWMediaPlayerList
//							.get(mVideoSquareInfo.id);
//					if (null != mDWMediaPlayer) {
//						if (mDWMediaPlayer.isPlaying()) {
//							LogUtils.d("SSS=======222===GONE======");
//							
//						} else {
//							
//						}
//					}
//				}
//			}
//		}else{
//			holder.mPreLoading.setVisibility(View.VISIBLE);
//		}

		int width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		int height = (int) ((float) width / 1.77f);

//		holder.mSurfaceView.setZOrderMediaOverlay(true);
//		SurfaceHolder mSurfaceHolder = holder.mSurfaceView.getHolder();
////		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//		LinearLayout.LayoutParams mPlayerLayoutParams = new LinearLayout.LayoutParams(
//				width, height);
//		holder.mPlayerLayout.setLayoutParams(mPlayerLayoutParams);
//		mSurfaceHolder.addCallback(new SurfaceViewCallback(this, arg0, mHolderList,
//				mVideoSquareListData, mDWMediaPlayerList, mVideoSquareInfo));

		// imageLoader.displayImage(mVideoSquareInfo.mUserEntity.headportrait,
		// holder.userhead, options, null);
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
			// imageLoader.clearMemoryCache();
			// imageLoader.clearDiscCache();
		}
//		if (null != mDWMediaPlayerList) {
//			Iterator<String> iter = mDWMediaPlayerList.keySet().iterator();
//			while (iter.hasNext()) {
//				Object key = iter.next();
//				if (null != key) {
//					DWMediaPlayer player = mDWMediaPlayerList.get(key);
//					if (null != player) {
//						player.release();
//					}
//				}
//			}
//		}
		
		if(null != mDWMediaPlayer){
			mDWMediaPlayer.release();
		}
	}

	@SuppressLint("SimpleDateFormat")
	public String formatTime(String date) {
		String time="";
		if(null != date){
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			ParsePosition pos = new ParsePosition(0);
			Date strtodate = formatter.parse(date, pos);
			
			if(null != strtodate){
				formatter = new SimpleDateFormat("MM月dd日 HH时mm分");
				if(null != formatter){
					time =  formatter.format(strtodate);
				}
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
		SurfaceView mSurfaceView;
		ImageView liveicon;
		ImageButton reporticon;
		ImageView mPreLoading;
		Button sharebtn;
		Button likebtn;
		RingView mRingView;
		TextureView mTextureView;
	}
	
	public void pausePlayer(){
//		if(mDWMediaPlayer.isPlaying()){
//			mDWMediaPlayer.pause();
//		}
	}
	
	public void startPlayer(){
//		if(!mDWMediaPlayer.isPlaying()){
//			mDWMediaPlayer.start();
//		}
	}
	
	public void updatePlayerState(int _index){
//		String vid = mVideoSquareListData.get(_index).mVideoEntity.videoid;
////		LogUtils.d("SSS===updatePlayerState==vid==="+vid);
//		if(!curVideoid.equals(vid)){
////			LogUtils.d("SSS===updatePlayerState==index==="+_index+"==vid="+vid);
////			LogUtils.d("SSS===updatePlayerState==mHolderList==="+mHolderList.size());
//			
//			View view = mRTPullListView.getChildAt(index);
//			if(null != view){
//				RingView ring = (RingView)view.findViewById(R.id.mRingView);
//				ImageView image = (ImageView)view.findViewById(R.id.mPreLoading);
//				if(null != ring){
//					ring.setProcess(0);
//					ring.setVisibility(View.GONE);
//				}
//				if(null != image){
//					image.setVisibility(View.VISIBLE);
//				}
//			}
//				
//			mDWMediaPlayer.reset();
//			int first = mRTPullListView.getFirstVisiblePosition();
//			int position = _index - first;
//			SurfaceHolder mSurfaceHolder=null;
//			if(position <= 0)
//				position = 0;
//			if(position >= mHolderList.size())
//				position = mHolderList.size() - 1;
//			mSurfaceHolder = mHolderList.get(""+_index);
//			
//			LogUtils.d("SSS===updatePlayerState==_index==="+_index+"===2222===mSurfaceHolder==="+mSurfaceHolder);
//			
//			index = _index;
//			curVideoid = vid;
//			String videoid = mVideoSquareListData.get(_index).mVideoEntity.videoid;
//			mDWMediaPlayer.setVideoPlayInfo(videoid, USERID, API_KEY, mContext);
//			mDWMediaPlayer.setDisplay(mSurfaceHolder);
//			mDWMediaPlayer.prepareAsync();
//		}
	}
	
	
}
