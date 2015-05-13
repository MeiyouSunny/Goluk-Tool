package cn.com.mobnote.golukmobile;


import java.util.ArrayList;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.console;
import cn.com.mobnote.video.MVListAdapter;
import cn.com.mobnote.video.MVManage;
import cn.com.mobnote.video.MVManage.MVEditData;
import cn.com.mobnote.video.VideCommentManage.VideoCommentData;
import cn.com.mobnote.video.VideCommentListAdapter;
import cn.com.mobnote.video.VideCommentManage;
import cn.com.mobnote.view.MyGridView;
import cn.com.mobnote.view.PullToRefreshView;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.LinearLayout.LayoutParams;
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
 * @ 功能描述:Goluk在线视频播放页面
 * 
 * @author 陈宣宇
 * 
 */
public class OnLineVideoPlayActivity extends BaseActivity implements SurfaceHolder.Callback, OnClickListener {
	/** 上下文 */
	private Context mContext = null;
	//private LayoutInflater mLayoutInflater = null;
	/** 返回按钮 */
	private Button mBackBtn = null;
	/** 下一步按钮 */
	private Button mNextBtn = null;
	/** 视频路径 */
	private String mFilePath = "";
	/** 视频播放器 */
	private MediaPlayer mMedioPlayer = new MediaPlayer();;
	private SurfaceView mSurfaceView = null;
	private SurfaceHolder mSurfaceHolder = null;
	private int position;
	/** 播放按钮 */
	private RelativeLayout mPlayLayout = null;
	/** 播放状态图片 */
	private ImageView mPlayStatusImage = null;
	/** mv列表layout */
	private LinearLayout mMVListLayout = null;
	
	
	/** 评论列表 */
	private ListView mListView = null;
	/** 评论列表适配器 */
	private VideCommentManage mVideCommentManage = null;
	/** 评论列表适配器 */
	private VideCommentListAdapter mVideCommentListAdapter = null;
	private ArrayList<VideoCommentData> mVideoCommentData = null;
	
	/** 上拉拉刷新控件 */
	private PullToRefreshView mPullToRefreshView = null;
	/** 数据列表页码,每页固定30条 */
	private int mPageSize = 1;
	private int mPageCount = 30;
	
	/** 用来接收消息,更新UI*/
	public static Handler mOnLineVideoPlayHandler = null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_play);
		mContext = this;
		//获取视频路径
		Intent intent = getIntent();
		mFilePath = intent.getStringExtra("cn.com.mobnote.video.path");
		
		mMVListLayout = (LinearLayout) findViewById(R.id.mvlistlayout);
		
		//视频初始化
		videoInit();
		//页面初始化
		init();
		//
//		initVideoEditList();
		/*
		pause=(Button)findViewById(R.id.button2);
		pause.setOnClickListener(new OnClickListener(){
		@Override
		public void onClick(View v) {
			player.pause();
		}});
		stop=(Button)findViewById(R.id.button3);
		stop.setOnClickListener(new OnClickListener(){
		@Override
		public void onClick(View v) {
			player.stop();
		}});
		*/
		console.log("onCreate");
	}
	
	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init(){
		//获取页面元素
		mBackBtn = (Button)findViewById(R.id.back_btn);
//		mNextBtn = (Button)findViewById(R.id.next_btn);
		mPlayLayout = (RelativeLayout)findViewById(R.id.play_layout);
		mPlayStatusImage = (ImageView)findViewById(R.id.play_image);
		
		mListView = (ListView)findViewById(R.id.video_comment_listview);
		//上拉刷新view
		//mPullToRefreshView = (PullToRefreshView)findViewById(R.id.search_channel_pull_refresh_view);
		
		mVideCommentManage = new VideCommentManage(mContext);
		mVideoCommentData = mVideCommentManage.getLocalVideoList(true,99);
		mVideCommentListAdapter = new VideCommentListAdapter(mContext,mVideoCommentData);
		mListView.setAdapter(mVideCommentListAdapter);
//		listView.setOnItemClickListener(this);
		
		//注册返回按钮事件
//		mRadioBackBtn.setOnClickListener(this);
		//注册收藏频道按钮事件
//		mFmRadioFavImageButton.setOnClickListener(this);
		
		//注册上拉刷新事件
//		mPullToRefreshView.setOnFooterRefreshListener(this);
//		mPlayBtn.setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				mMedioPlayer.start();
//			}
//		});
		
		//注册事件
		mBackBtn.setOnClickListener(this);
//		mNextBtn.setOnClickListener(this);
		mPlayLayout.setOnClickListener(this);
		
		//更新UI handler
		mOnLineVideoPlayHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch(what){
					case 1:
						console.log("mOnLineVideoPlayHandler---1");
						addVideoData();
					break;
				}
			}
		};
	}
	
	/**
	 * 初始化本地视频列表
	 */
	private void initVideoEditList(){
		MyGridView gridView = createMVGridView();
		MVManage mvManage = new MVManage(mContext);
		ArrayList<MVEditData> list = mvManage.getLocalVideoList();
		MVListAdapter adapter = new MVListAdapter(mContext,list);
		gridView.setAdapter(adapter);
		mMVListLayout.addView(gridView);
	}
	
	/**
	 * 创建本地视频列表
	 * @return
	 */
	private MyGridView createMVGridView() {
		MyGridView gridLayout = new MyGridView(mContext,null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		gridLayout.setLayoutParams(lp);
		gridLayout.setBackgroundColor(Color.rgb(19,19,19));
		//gridLayout.setBackgroundColor(Color.rgb(204,102,153));
		gridLayout.setNumColumns(4);
		gridLayout.setPadding(16,30,16,30);
		gridLayout.setVerticalSpacing(30);
		gridLayout.setHorizontalSpacing(16);
		//设置grid item点击效果为透明
		//gridLayout.setSelector(new ColorDrawable(Color.TRANSPARENT));
		return gridLayout;
	}
	
	/**
	 * 视频播放初始化
	 */
	private void videoInit(){
		mSurfaceView = (SurfaceView)findViewById(R.id.video_surface);
		//SurfaceHolder是SurfaceView的控制接口
		mSurfaceHolder = mSurfaceView.getHolder();
		//因为这个类实现了SurfaceHolder.Callback接口，所以回调参数直接this
		mSurfaceHolder.addCallback(this);
		//Surface类型
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		//mSurfaceHolder.setFixedSize(320, 220);//显示的分辨率,不设置为视频默认
	}
	
	/**
	 * 视频初始化完成,加载数据
	 */
	private void addVideoData(){
		//必须在surface创建后才能初始化MediaPlayer,否则不会显示图像
		mMedioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		//设置显示视频显示在SurfaceView上
		mMedioPlayer.setDisplay(mSurfaceHolder);
		//注册播放完成事件
		mMedioPlayer.setOnCompletionListener(new OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mp) {
				//显示图片
				mPlayStatusImage.setVisibility(View.VISIBLE);
			}
		});
		try{
			mMedioPlayer.reset();
			//隐藏图片
			mPlayStatusImage.setVisibility(View.GONE);
			mMedioPlayer.setDataSource(mFilePath);
			mMedioPlayer.prepare();
			//mMedioPlayer.setOnPreparedListener(new PrepareListener(position));
			mMedioPlayer.start();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 在surface的大小发生改变时触发
	 */
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Log.e("","chxy_____surfaceChanged" + arg1);
	}
	
	/**
	 * 在创建时触发，一般在这里调用画图的线程
	 */
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		console.log("chxy_____surfaceCreated");
		mOnLineVideoPlayHandler.sendEmptyMessageDelayed(1,50);
	}
	
	/**
	 * 销毁时触发，一般在这里将画图的线程停止、释放
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		console.log("chxy_____surfaceDestroyed" );
		if(mMedioPlayer.isPlaying()){
			//position = mMedioPlayer.getCurrentPosition();
			mMedioPlayer.stop();
		}
	}

	@Override
	protected void onDestroy() {
		console.log("chxy_____onDestroy" );
		super.onDestroy();
		//Activity销毁时停止播放，释放资源。不做这个操作，即使退出还是能听到视频播放的声音
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onStart(){
		console.log("chxy_____onStart" );
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		console.log("chxy_____onStop" );
		super.onStop();
	}
	
	/*
	private final class PrepareListener implements OnPreparedListener{
		private int position;
		public PrepareListener(int position) {
			this.position = position;
		}
		
		public void onPrepared(MediaPlayer mp) {
			mMedioPlayer.start();
			if(position>0){
				mMedioPlayer.seekTo(position);
			}
		}
	}
	*/
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch(id){
			case R.id.back_btn:
				//返回
				OnLineVideoPlayActivity.this.finish();
			break;
			case R.id.next_btn:
				//下一步,跳转到视频分享页面
				Intent videoShare = new Intent(mContext,VideoShareActivity.class);
				startActivity(videoShare);
			break;
			case R.id.play_layout:
				//暂停/播放
				if(mMedioPlayer.isPlaying()){
					mMedioPlayer.pause();
					//显示图片
					mPlayStatusImage.setVisibility(View.VISIBLE);
				}
				else{
					mMedioPlayer.start();
					//隐藏图片
					mPlayStatusImage.setVisibility(View.GONE);
				}
			break;
		}
	}

}
