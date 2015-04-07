package cn.com.mobnote.golukmobile;


import java.util.ArrayList;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.map.BaiduMapManage;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.console;
import cn.com.mobnote.video.VideCommentListAdapter;
import cn.com.mobnote.video.VideCommentManage;
import cn.com.mobnote.video.VideCommentManage.VideoCommentData;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.rd.car.player.RtmpPlayerView;

/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:小车本视频直播
 * 
 * @author 陈宣宇
 * 
 */
public class LiveVideoPlayActivity extends Activity implements OnClickListener {
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	//private LayoutInflater mLayoutInflater = null;
	/** 返回按钮 */
	private Button mBackBtn = null;
	/** 刷新按钮 */
	private Button mRefirshBtn = null;
	/** 页面滚动条 */
	private ScrollView mVideoLiveScrollView = null;
	/** 视频loading */
	private RelativeLayout mVideoLoading = null;
	/** 播放布局 */
	private RelativeLayout mPlayLayout = null;
	/** 直播超时提示文字 */
	private TextView mTimeOutText = null;
	/** 地图layout */
	//private RelativeLayout mMapLayout = null;
	/** 百度地图 */
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	private BaiduMapManage mBaiduMapManage = null;
	
	/** 自定义播放器支持特效 */
	public RtmpPlayerView mRPVPalyVideo = null;
	/** 播放按钮 */
	//private RelativeLayout mPlayLayout = null;
	/** 播放状态图片 */
	//private ImageView mPlayStatusImage = null;
	
	/** 评论列表 */
	private ListView mListView = null;
	/** 评论列表适配器 */
	private VideCommentManage mVideCommentManage = null;
	/** 评论列表适配器 */
	private VideCommentListAdapter mVideCommentListAdapter = null;
	private ArrayList<VideoCommentData> mVideoCommentData = null;
	/** 上拉拉刷新控件 */
	//private PullToRefreshView mPullToRefreshView = null;
	/** 数据列表页码,每页固定30条 */
	//private int mPageSize = 1;
	//private int mPageCount = 30;
	
	/** 用户aid */
	private String mAid = "";
	/** 用户uid */
	private String mUid = "";
	/** 视频地址 */
	private String mFilePath = "";
	/** 气泡图片 */
	//private String mImageUrl = "";
	
	/** 首页handler用来接收消息,更新UI*/
	public static Handler mLiveVideoHandler = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_live_play);
		mContext = this;
		
		//获取视频路径
		Intent intent = getIntent();
		mAid = intent.getStringExtra("cn.com.mobnote.map.aid");
		mUid = intent.getStringExtra("cn.com.mobnote.map.uid");
		//mImageUrl = intent.getStringExtra("cn.com.mobnote.map.imageurl");
		
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(this,"LiveVideo");
		
		//页面初始化
		init();
		//地图初始化
		initMap();
		//评论初始化
		initComment();
		
		//请求视频直播数据
		getVideoLiveData();
	}
	
	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init(){
		//获取页面元素
		mVideoLiveScrollView = (ScrollView)findViewById(R.id.video_live_scroll);
		mBackBtn = (Button)findViewById(R.id.back_btn);
		mRefirshBtn = (Button) findViewById(R.id.refirsh_btn);
		mTimeOutText = (TextView) findViewById(R.id.time_out_text);
		mVideoLoading = (RelativeLayout) findViewById(R.id.video_loading);
		mPlayLayout = (RelativeLayout)findViewById(R.id.play_layout);
		
		//mNextBtn = (Button)findViewById(R.id.next_btn);
		//视频图片
		//mVideoDefaultImage = (ImageView)findViewById(R.id.video_default_image);
		//mPlayStatusImage = (ImageView)findViewById(R.id.play_image);
		
		mRPVPalyVideo = (RtmpPlayerView)this.findViewById(R.id.vRtmpPlayVideo);
		//先显示气泡上的默认图片
//		if(null != mImageUrl && !"".equals(mImageUrl)){
//			Drawable img = LoadImageManager.getLoacalBitmap(mImageUrl,mContext);
//			//mVideoImage.setBackgroundDrawable(img);
//			mRPVPalyVideo.setBackgroundDrawable(img);
//		}
//		else{
//			mVideoDefaultImage.setVisibility(View.VISIBLE);
//		}
		//改变滚动条位置
		mVideoLiveScrollView.smoothScrollTo(0,0);
		
		
		//注册事件
		mBackBtn.setOnClickListener(this);
		mRefirshBtn.setOnClickListener(this);
		mPlayLayout.setOnClickListener(this);
		
		
		//更新UI handler
		mLiveVideoHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch(what){
					case 1:
						//测试获取视频详情
						Object obj = new Object();
						LiveVideoDataCallBack(1,obj);
					break;
					case 2:
						//5秒超时显示,提示文字
						mTimeOutText.setVisibility(View.VISIBLE);
					break;
				}
			}
		};
		
//		Message msg = new Message();
//		msg.what = 1;
//		LiveVideoPlayActivity.mLiveVideoHandler.sendMessageDelayed(msg,2000);
	}
	
	private void initMap(){
		//mMapLayout = (RelativeLayout) findViewById(R.id.map_layout);
		//获取地图控件引用
		mMapView = (MapView) findViewById(R.id.bmapView);
		
		mMapView.showZoomControls(false);
		mMapView.showScaleControl(false);
		mBaiduMap = mMapView.getMap();
		mBaiduMapManage = new BaiduMapManage(this,mBaiduMap,"LiveVideo");
		
		//为了解决地图拖动事件冲突问题
		RelativeLayout mapBlankView = (RelativeLayout)findViewById(R.id.map_blankview);
		
		//注册touch拦截事件
		mapBlankView.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_UP){
					mVideoLiveScrollView.requestDisallowInterceptTouchEvent(false);
				}else{
					mVideoLiveScrollView.requestDisallowInterceptTouchEvent(true);
				}
				return false;
			}
		});
	}
	
	/**
	 * 评论初始化
	 */
	private void initComment(){
		mListView = (ListView)findViewById(R.id.video_comment_listview);
		//上拉刷新view
		//mPullToRefreshView = (PullToRefreshView)findViewById(R.id.search_channel_pull_refresh_view);
		
		mVideCommentManage = new VideCommentManage(mContext);
		mVideoCommentData = mVideCommentManage.getLocalVideoList(true,99);
		mVideCommentListAdapter = new VideCommentListAdapter(mContext,mVideoCommentData);
		mListView.setAdapter(mVideCommentListAdapter);
		//listView.setOnItemClickListener(this);
	}
	
	/**
	 * 视频播放初始化
	 */
	private void videoInit(){
		//视频事件回调注册
		mRPVPalyVideo.setPlayerListener(new RtmpPlayerView.RtmpPlayerViewLisener() {
			@Override
			public void onPlayerPrepared(final RtmpPlayerView rpv) {
				console.log("live---onPlayerPrepared");
				rpv.setHideSurfaceWhilePlaying(true);
			}
			
			@Override
			public boolean onPlayerError(RtmpPlayerView rpv, int arg1, int arg2,String arg3) {
				//视频播放出错
				console.log("live---onPlayerError" + arg2 + "," + arg3);
				console.toast("播放器出现错误...", mContext);
				rpv.removeCallbacks(retryRunnable);
				// FIXME:5秒后重连
				rpv.postDelayed(retryRunnable, 5000);
				mVideoLoading.setVisibility(View.GONE);
				mPlayLayout.setVisibility(View.VISIBLE);
				return false;
			}
			
			@Override
			public void onPlayerCompletion(RtmpPlayerView rpv) {
				//视频播放完成
				console.log("live---onPlayerCompletion");
				rpv.removeCallbacks(retryRunnable);
				rpv.postDelayed(retryRunnable, 5000);
				mPlayLayout.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onPlayerBegin(RtmpPlayerView arg0) {
				console.log("live---onPlayerBegin");
				mVideoLoading.setVisibility(View.GONE);
			}
			
			@Override
			public void onPlayBuffering(RtmpPlayerView arg0, boolean start) {
				console.log("live---onPlayBuffering---" + "arg1---" + start);
				if (start) {
					// 缓冲开始
				} else {
					// 缓冲结束
				}
			}
			
			@Override
			public void onGetCurrentPosition(RtmpPlayerView arg0, int arg1) {
				//console.log("onGetCurrentPosition");
			}
		});
		// 设置缓冲时间，缓冲时间越长，则超时时间也需对应加长
		mRPVPalyVideo.setBufferTime(1000);
		//设置视频源
		mRPVPalyVideo.setConnectionTimeout(30000);
		//mRPVPalyVideo.setDataSource("rtmp://124.238.236.92/live/test10");
		mRPVPalyVideo.setDataSource(mFilePath);
		mRPVPalyVideo.start();
	}
	
	/**
	 * 获取视频直播数据
	 */
	private void getVideoLiveData(){
		mPlayLayout.setVisibility(View.GONE);
		mVideoLoading.setVisibility(View.VISIBLE);
		mTimeOutText.setVisibility(View.GONE);
		
		String condi = "{\"uid\":\"" + mUid + "\",\"desAid\":\"" + mAid + "\"}";
		console.log("PageType_GetVideoDetail---获取直播详情---" + condi);
		boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_GetVideoDetail,condi);
		if(!b){
			console.log("PageType_GetVideoDetail---获取直播详情---失败" + b);
		}
		else{
			//5秒未开始直播,显示提示文字
			mLiveVideoHandler.sendEmptyMessageDelayed(2,5000);
		}
	}
	
	/**
	 * 设置视频直播地址
	 * @param data
	 */
	private void setVideoLiveUrl(JSONObject data){
		try{
			JSONObject json = data.getJSONObject("data");
			//请求成功
			//视频直播流地址
			String vurl = json.getString("vurl");
			if(!"".equals(vurl) && null != vurl){
				//把视频直播流给到播放器
				mFilePath = vurl;
				//视频初始化
				videoInit();
			}
			else{
				//获取图片数据
				String picUrl = json.getString("picurl");
				if(!"".equals(picUrl) && null != picUrl){
					//调用图片下载接口
				}
			}
			//获取经纬度数据
			String lon = json.getString("lon");
			String lat = json.getString("lat");
			String head = json.getString("head");
			if(!"".equals(lon) && null != lon && !"".equals(lat) && null != lat){
				//添加地图大头针
				mBaiduMapManage.AddMapPoint(lon,lat,head);
				mBaiduMapManage.SetMapCenter(Double.parseDouble(lon),Double.parseDouble(lat));
			}
		}
		catch(Exception e){
			
		}
	}
	
	/**
	 * 重连runnable
	*/
	private Runnable retryRunnable = new Runnable() {
		@Override
		public void run() {
			// start();
		}
	};

	/**
	 * 视频直播数据返回
	 * @param obj
	 */
	public void LiveVideoDataCallBack(int success,Object obj){
		//String str = "{\"code\":\"200\",\"state\":\"true\",\"vurl\":\"http://cdn3.lbs8.com/files/cdcvideo/test11.mp4\",\"cnt\":\"5\",\"lat\":\"39.93923\",\"lon\":\"116.357428\",\"picurl\":\"http://img.cool80.com/i/png/217/02.png\"}";
		if(1 == success){
			String str = (String)obj;
			console.log("视频直播数据返回--LiveVideoDataCallBack:" + str);
			try {
				JSONObject data = new JSONObject(str);
				int code = data.getInt("code");
				switch(code){
					case 200:
						//获取直播数据成功
						setVideoLiveUrl(data);
					break;
					case 400:
						//各种找不到数据
					break;
					case 405:
						//直播方一定时间内没有上传经纬度信息
					break;
					case 420:
						//直播方卡死,接受不到atk信息
					break;
					case 500:
						//服务器异常
					break;
				}
			}
			catch(Exception e){
				
			}
		}
		else{
			//console.log("请求直播详情服务错误");
			Builder dialog = new AlertDialog.Builder(mContext);
			dialog.setTitle("提示");
			dialog.setMessage("请求直播服务超时，请重试.");
			try{
				int type =(Integer) obj;
				if(type == 0){
					dialog.setMessage("直播方数据异常，请重试.");
				}
			}
			catch(Exception e){
			}
			dialog.setNegativeButton("确认",new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialoginterface, int i){
					//按钮事件,重试
					//showAutoLoginTip();
				}
			});
			dialog.show();
			
			mVideoLoading.setVisibility(View.GONE);
			mPlayLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onDestroy() {
		console.log("liveplay---onDestroy");
		if(null != mRPVPalyVideo){
			mRPVPalyVideo.removeCallbacks(retryRunnable);
			mRPVPalyVideo.cleanUp();
			mRPVPalyVideo = null;
		}
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		console.log("liveplay---onPause");
		//mPlayLayout.setVisibility(View.VISIBLE);
		super.onPause();
	};
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch(id){
			case R.id.back_btn:
				//返回
				LiveVideoPlayActivity.this.finish();
			break;
			case R.id.refirsh_btn:
				//刷新,请求视频直播数据
				getVideoLiveData();
			break;
			case R.id.play_layout:
				//请求视频直播数据
				getVideoLiveData();
			break;
		}
	}
}






