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
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.SharePlatformUtil;
import cn.com.mobnote.golukmobile.carrecorder.util.BitmapManager;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.umeng.widget.CustomShareBoard;
import cn.com.tiros.utils.LogUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
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
import android.widget.Toast;

@SuppressLint("InflateParams")
public class VideoSquareListViewAdapter extends BaseAdapter implements VideoSuqareManagerFn{
	private Context mContext = null;
	private List<VideoSquareInfo> mVideoSquareListData = null;
	private HashMap<String, DWMediaPlayer> mDWMediaPlayerList = null;
	private int count = 0;
	private final String USERID = "77D36B9636FF19CF";
	private final String API_KEY = "O8g0bf8kqiWroHuJaRmihZfEmj7VWImF";
	private int form = 1;
	private DWMediaPlayer mDWMediaPlayer=null;
	private HashMap<String, SurfaceHolder> mHolderList=null;
	private String curVideoid="";
	private int index;
	private ListView mRTPullListView=null;
	private SharePlatformUtil sharePlatform;

	public VideoSquareListViewAdapter(ListView _mRTPullListView, Context context,int plform,SharePlatformUtil spf) {
		mContext = context;
		this.mRTPullListView=_mRTPullListView;
		mVideoSquareListData = new ArrayList<VideoSquareInfo>();
		mDWMediaPlayerList = new HashMap<String, DWMediaPlayer>();
		mHolderList = new HashMap<String, SurfaceHolder>();
		form = plform;//1:热门页面 2:广场页
		sharePlatform = spf;
		
		GolukApplication.getInstance().getVideoSquareManager()
		.addVideoSquareManagerListener("videosharehotlist", this);
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
//			holder.mSurfaceView = (SurfaceView) convertView
//					.findViewById(R.id.mSurfaceView);
//			holder.mSurfaceView.setZOrderMediaOverlay(true);
			holder.reporticon = (ImageButton) convertView
					.findViewById(R.id.report_icon);
			holder.liveicon = (ImageView) convertView
					.findViewById(R.id.live_icon);
			holder.mPreLoading = (ImageView) convertView
					.findViewById(R.id.mPreLoading);
			holder.likebtn = (Button) convertView.findViewById(R.id.like_btn);
			holder.sharebtn = (ImageButton) convertView.findViewById(R.id.share_btn);

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
//			holder.mSurfaceView.setVisibility(View.GONE);
		} else {// 点播
			holder.reporticon.setVisibility(View.VISIBLE);
			holder.liveicon.setVisibility(View.GONE);
//			holder.mSurfaceView.setVisibility(View.VISIBLE);
		}
		holder.reporticon.setOnClickListener(new VideoSquareOnClickListener(mContext,mVideoSquareListData,mVideoSquareInfo,form,sharePlatform, this));
		if("1".equals(mVideoSquareInfo.mVideoEntity.ispraise)){// 点赞过
			holder.likebtn.setBackgroundResource(R.drawable.livestreaming_heart_btn_down);//设置点赞背景
		}else{
			holder.likebtn.setBackgroundResource(R.drawable.livestreaming_heart_btn);//设置默认点赞背景
		}
		
		if("1".equals(mVideoSquareInfo.mUserEntity.headportrait)){
			holder.userhead.setBackgroundResource(R.drawable.individual_center_head_boy_one);
		}else if("2".equals(mVideoSquareInfo.mUserEntity.headportrait)){
			holder.userhead.setBackgroundResource(R.drawable.individual_center_head_boy_two);
		}else if("3".equals(mVideoSquareInfo.mUserEntity.headportrait)){
			holder.userhead.setBackgroundResource(R.drawable.individual_center_head_boy_three);
		}else if("4".equals(mVideoSquareInfo.mUserEntity.headportrait)){
			holder.userhead.setBackgroundResource(R.drawable.individual_center_head_girl_one);
		}else if("5".equals(mVideoSquareInfo.mUserEntity.headportrait)){
			holder.userhead.setBackgroundResource(R.drawable.individual_center_head_girl_two);
		}else if("6".equals(mVideoSquareInfo.mUserEntity.headportrait)){
			holder.userhead.setBackgroundResource(R.drawable.individual_center_head_girl_three);
		}else if("7".equals(mVideoSquareInfo.mUserEntity.headportrait)){
			holder.userhead.setBackgroundResource(R.drawable.individual_center_head_moren);
		}else {
			holder.userhead.setBackgroundResource(R.drawable.individual_center_head_moren);
		}
		
		holder.likebtn.setOnClickListener(new VideoSquareOnClickListener(mContext,mVideoSquareListData,mVideoSquareInfo,form,sharePlatform,this));
		holder.sharebtn.setOnClickListener(new VideoSquareOnClickListener(mContext,mVideoSquareListData,mVideoSquareInfo,form,sharePlatform,this));
		holder.username.setText(mVideoSquareInfo.mUserEntity.nickname);
		holder.looknumber.setText(mVideoSquareInfo.mVideoEntity.clicknumber);
		holder.likenumber.setText(mVideoSquareInfo.mVideoEntity.praisenumber);
		holder.videotitle.setText(mVideoSquareInfo.mVideoEntity.describe);
		holder.sharetime.setText(this.formatTime(mVideoSquareInfo.mVideoEntity.sharingtime));
		
		holder.mPlayerLayout.setOnClickListener(new VideoOnClickListener(
				mVideoSquareListData, holder, mDWMediaPlayerList,
				mVideoSquareInfo,mContext,form));

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
		LinearLayout.LayoutParams mPlayerLayoutParams = new LinearLayout.LayoutParams(
				width, height);
		holder.mPlayerLayout.setLayoutParams(mPlayerLayoutParams);
		RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(
				width, height);
		holder.mPreLoading.setLayoutParams(mPreLoadingParams);
//		mSurfaceHolder.addCallback(new SurfaceViewCallback(this, arg0, mHolderList,
//				mVideoSquareListData, mDWMediaPlayerList, mVideoSquareInfo));

		// imageLoader.displayImage(mVideoSquareInfo.mUserEntity.headportrait,
		// holder.userhead, options, null);
//		imageLoader.displayImage(mVideoSquareInfo.mVideoEntity.picture,
//				holder.mPreLoading, options, null);
//		BitmapDisplayConfig config = new BitmapDisplayConfig();
//		Bitmap mBitmap = BitmapManager.getInstance().mBitmapUtils.getBitmapFromMemCache(mVideoSquareInfo.mVideoEntity.picture, config);
//		if(null == mBitmap){
			BitmapManager.getInstance().mBitmapUtils.display(holder.mPreLoading, mVideoSquareInfo.mVideoEntity.picture);
//		}else{
//			holder.mPreLoading.setImageBitmap(mBitmap);
//		}
		
		return convertView;
	}

	public int getUserHead(String head) {
		return 0;
	}

	public void onBackPressed() {
		
	}
	
	public void onResume(){
		GolukApplication.getInstance().getVideoSquareManager()
		.addVideoSquareManagerListener("videosharehotlist", this);
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
		if(null != mDWMediaPlayer){
			mDWMediaPlayer.release();
		}
	}

	@SuppressLint("SimpleDateFormat")
	public String formatTime(String date) {
		String time="";
		if(null != date){
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			
			try {
				Date strtodate = formatter.parse(date);
				if(null != strtodate){
					formatter = new SimpleDateFormat("MM月dd日 HH时mm分");
					if(null != formatter){
						time =  formatter.format(strtodate);
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
//		SurfaceView mSurfaceView;
		ImageView liveicon;
		ImageButton reporticon;
		ImageView mPreLoading;
		ImageButton sharebtn;
		Button likebtn;
		RingView mRingView;
//		TextureView mTextureView;
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
	
	VideoSquareOnClickListener mVideoSquareOnClickListener=null;
	public void setOnClick(VideoSquareOnClickListener _mVideoSquareOnClickListener){
		mVideoSquareOnClickListener=_mVideoSquareOnClickListener;
	}
	
	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,
			Object param2) {
		System.out.println("YYYY==888888==getSquareList8888====form===" + form
				+ "event=" + event + "===msg=" + msg + "==param2=" + param2);

		System.out.println("YYYY+RESULT-2-2-2-2-2-2-2");
		if (event == SquareCmd_Req_GetShareUrl) {
			System.out.println("YYYY+RESULT-3-3-3-3-3-3-3");
			if (RESULE_SUCESS == msg) {
				mVideoSquareOnClickListener.closeRqsDialog(mContext);
				
//				Activity activity = SysApplication.getInstance().getCurActivity();
//				if(activity instanceof VideoSquarePlayActivity || activity instanceof MainActivity){
//				}else{
//					return;
//				}
				
				if(null == mVideoSquareOnClickListener){
					return;
				}
				
				try {
					System.out.println("YYYY+RESULT-1-1-1-1-1-1-1");
					JSONObject result = new JSONObject((String) param2);
					System.out.println("YYYY+RESULT00000000");
					if (result.getBoolean("success")) {
						JSONObject data = result.getJSONObject("data");
						String shareurl = data.getString("shorturl");
						String coverurl = data.getString("coverurl");
						String describe =data.optString("describe");
						if (TextUtils.isEmpty(describe)) {
							describe = "#极路客精彩视频#";
						}
						
						if ("".equals(coverurl)) {

						}
						System.out.println("YYYY+RESULT11111111");
						// 设置分享内容
						//sharePlatform.setShareContent(shareurl, coverurl,describe);
						System.out.println("YYYY+RESULT22222222");
						String ttl = "极路客精彩视频分享";
						if ("1".equals(mVideoSquareOnClickListener.mVideoSquareInfo.mVideoEntity.type)) {// 直播
							ttl =mVideoSquareOnClickListener. mVideoSquareInfo.mUserEntity.nickname + "的直播视频分享";
						}
						LogUtil.e("xuhw", "BBBBBB==2222====nikename="+mVideoSquareOnClickListener. mVideoSquareInfo.mUserEntity.nickname);
						if (mContext instanceof VideoSquarePlayActivity) {
							System.out.println("YYYY+VideoSquarePlayActivity");
							VideoSquarePlayActivity vspa = (VideoSquarePlayActivity) mContext;
							if (vspa!=null && !vspa.isFinishing()) {
								vspa.mCustomProgressDialog.close();
								CustomShareBoard shareBoard = new CustomShareBoard(vspa,sharePlatform,shareurl, coverurl,describe,ttl);
								shareBoard.showAtLocation(vspa.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
							}
							
						} else if(mContext instanceof MainActivity){
							System.out.println("YYYY+VideoSquareActivity");
							MainActivity vsa = (MainActivity) mContext;
							if(vsa == null || vsa.isFinishing()){
								return ;
							}else{
								if(vsa.mCustomProgressDialog!=null){
									vsa.mCustomProgressDialog.close();
									CustomShareBoard shareBoard = new CustomShareBoard(vsa,sharePlatform,shareurl, coverurl,describe,ttl);
									shareBoard.showAtLocation(vsa.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
								}
								
							}

						}

					}else{
//						mVideoSquareOnClickListener.closeRqsDialog(mContext);
						Toast.makeText(mContext, "网络异常，请检查网络",Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
//				mVideoSquareOnClickListener.closeRqsDialog(mContext);
				Toast.makeText(mContext, "网络异常，请检查网络",Toast.LENGTH_SHORT).show();
			}
		}

	}
	
}
