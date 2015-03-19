package cn.com.mobnote.golukmobile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.rd.car.editor.Constants;
import com.rd.car.editor.EditorParam;
import com.rd.car.editor.FilterPlaybackView;
import com.rd.car.editor.FilterVideoEditorException;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.entity.MixAudioInfo;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.AssetsFileUtils;
import cn.com.mobnote.util.console;
import cn.com.mobnote.video.MVListAdapter;
import cn.com.mobnote.video.MVManage;
import cn.com.mobnote.video.MVManage.MVEditData;
import cn.com.mobnote.view.MyGridView;
import cn.com.tiros.api.FileUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.Toast;

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
 * @ 功能描述:Goluk视频编辑页面
 * 
 * @author 陈宣宇
 * 
 */
@SuppressLint("HandlerLeak")
public class VideoEditActivity extends Activity implements  OnClickListener {
	/** 视频编辑页面handler用来接收消息,更新UI*/
	public static Handler mVideoEditHandler = null;
	/** mv滤镜appter */
	public MVListAdapter mMVListAdapter =null;
	/** 自定义播放器支持特效 */
	public FilterPlaybackView mVVPlayVideo = null;
	
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	//private LayoutInflater mLayoutInflater = null;
	/** 返回按钮 */
	private Button mBackBtn = null;
	/** 下一步按钮 */
	private Button mNextBtn = null;
	/** 视频路径 */
	private String mFilePath = "";
	
	/** 播放按钮 */
	private RelativeLayout mPlayLayout = null;
	/** 播放状态图片 */
	private ImageView mPlayStatusImage = null;
	/** loading布局 */
	private RelativeLayout mVideoLoadingLayout = null;
	/** loading图片 */
	private ImageView mLoadingImage = null;
	/** 音乐按钮 */
	private ImageButton mMusicBtn = null;
	/** 进度条 */
	private ProgressBar mVideoProgressBar = null;
	/** mv列表layout */
	private LinearLayout mMVListLayout = null;
	/** loading动画 */
	private AnimationDrawable mLoadingAnimation = null;
	/** 滤镜保存视频路径 */
	private String mVideoSavePath = null;
	/** 视频存放外卡文件路径 */
	private static final String APP_FOLDER = android.os.Environment.getExternalStorageDirectory().getPath();
	private String mNewVideoFilePath = APP_FOLDER + "/" + "tiros-com-cn-ext/";
	/** 上传视频时间记录 */
	private long uploadVideoTime = 0;
	
	/** 当前选择的配乐文件路径 */
	private String mStrMusicFilePath = "";
	
	/** 当前重叠配音路径列表 */
	private ArrayList<MixAudioInfo> audioInfos = new ArrayList<MixAudioInfo>();
	/** 当前选择的配音文件路径 */
	private String m_strRecorderingFilePath;
	/** 控制视频是配乐操作 */
	private boolean isSoundTrack;
	/** 当前选择的配乐文件路径 */
	private String m_strMusicFilePath;
	/** 配音录制时记录开始位置和结束位置 的范围 */
	private int startTime, endTime;
	/** 内置音乐路径列表 */
	private List<String> assetsMusicPaths = new ArrayList<String>() ;
	/** 进度条线程 */
	private Thread mProgressThread = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_edit);
		
		mContext = this;
		//获取视频路径
		Intent intent = getIntent();
		mFilePath = intent.getStringExtra("cn.com.mobnote.video.path");
		
		mMVListLayout = (LinearLayout)findViewById(R.id.mvlistlayout);
		
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(this,"VideoEdit");
		
		//页面初始化
		init();
		//视频初始化
		videoInit();
		//编辑选项表格
		initVideoEditList();
	}
	
	/**
	 * 页面初始化
	 */
	private void init(){
		//获取页面元素
		mBackBtn = (Button)findViewById(R.id.back_btn);
		mNextBtn = (Button)findViewById(R.id.next_btn);
		mPlayLayout = (RelativeLayout)findViewById(R.id.play_layout);
		mPlayStatusImage = (ImageView)findViewById(R.id.play_image);
		mVideoLoadingLayout = (RelativeLayout)findViewById(R.id.video_loading_layout);
		mLoadingImage = (ImageView)findViewById(R.id.loading_img);
		mVideoProgressBar = (ProgressBar) findViewById(R.id.video_progress_bar);
		mLoadingAnimation = (AnimationDrawable)mLoadingImage.getBackground();
		mMusicBtn = (ImageButton) findViewById(R.id.music_btn);
		
		//注册事件
		mBackBtn.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);
		mPlayLayout.setOnClickListener(this);
		mMusicBtn.setOnClickListener(this);
		
		//更新UI handler
		mVideoEditHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch(what){
					case 1:
						String path = (String) msg.obj;
						console.log("select music---" + path);
						addMusicToVideo(path);
					break;
					case 2:
						//mVVPlayVideo.stop();
						setMuteVideo(false);
						setMixAudioFilePath("1.mp3", true);
					break;
				}
			}
		};
	}
	
	/**
	 * 视频播放初始化
	 */
	private void videoInit(){
		/*
		mSurfaceView = (SurfaceView)findViewById(R.id.video_surface);
		//SurfaceHolder是SurfaceView的控制接口
		mSurfaceHolder = mSurfaceView.getHolder();
		//因为这个类实现了SurfaceHolder.Callback接口，所以回调参数直接this
		mSurfaceHolder.addCallback(this);
		//Surface类型
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		//显示的分辨率,不设置为视频默认
		//mSurfaceHolder.setFixedSize(320, 220);
		*/
		
//		assetsMusicPaths.add("");
//		String assetsMusic = addAssets("想念", "1.mp3");
//		console.log("music---" + assetsMusic);
//		assetsMusicPaths.add(assetsMusic);
//		assetsMusic = addAssets("漂亮男孩", "2.mp3");
//		console.log("music---" + assetsMusic);
//		assetsMusicPaths.add(assetsMusic);
		
		mVVPlayVideo = (FilterPlaybackView) this.findViewById(R.id.vvPlayVideo);
		//内置滤镜最大id为Constants.FILTER_ID_WARM
		int nFilterId = Constants.FILTER_ID_WARM + 1;
		//添加内置滤镜名称，与Constants.*定义对应
		//添加扩展的滤镜
		mVVPlayVideo.addFilter(nFilterId++, R.raw.gutongse);
		mVVPlayVideo.addFilter(nFilterId++, R.raw.lanseshike);
		mVVPlayVideo.addFilter(nFilterId++, R.raw.youge);
		
		//播放器准备过程的回调接口
		mVVPlayVideo.setPlaybackListener(new FilterPlaybackView.FilterPlaybackViewListener() {
			@Override
			public void onPrepared(MediaPlayerControl mpc) {
				//视频播放已就绪
				console.log("edit video 加载完成");
				updateVideoProgress();
				//删除背景图片
				//mVVPlayVideo.setBackgroundDrawable(null);
				//Toast.makeText(VideoEditActivity.this, "视频播放已就绪！",Toast.LENGTH_SHORT).show();
			}

			@Override
			public boolean onError(MediaPlayerControl mpc,int nErrorNo, String strErrInfo) {
				//视频播放出错
				Toast.makeText(VideoEditActivity.this,"视频播放出错,errorNo: " + nErrorNo + ",info: "+ strErrInfo, Toast.LENGTH_SHORT).show();
				return false;
			}

			@Override
			public void onCompletion(MediaPlayerControl mpc) {
				//视频播放完成
				//Toast.makeText(VideoEditActivity.this, "视频播放完毕！",Toast.LENGTH_SHORT).show();
				mVideoProgressBar.setProgress(mVVPlayVideo.getDuration());
				//String strDuration = updateTime(mVVPlayVideo.getDuration());
			}
		});
		
		try {
			//设置视频源
			mVVPlayVideo.setVideoPath(mFilePath);
			mVVPlayVideo.switchFilterId(0);
			mVVPlayVideo.start();
			
			//setMuteVideo(false);
			//mVideoEditHandler.sendEmptyMessageDelayed(2,100);
			//setMixAudioFilePath("1.mp3", true);
			
		} catch (FilterVideoEditorException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * 初始化滤镜布局
	 */
	private void initVideoEditList(){
		MyGridView gridView = createMVGridView();
		MVManage mvManage = new MVManage(mContext);
		ArrayList<MVEditData> list = mvManage.getLocalVideoList();
		mMVListAdapter = new MVListAdapter(mContext,list);
		gridView.setAdapter(mMVListAdapter);
		mMVListLayout.addView(gridView);
	}
	
	/**
	 * 创建本地滤镜列表布局
	 * @return
	 */
	private MyGridView createMVGridView() {
		MyGridView gridLayout = new MyGridView(mContext,null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		gridLayout.setLayoutParams(lp);
		gridLayout.setBackgroundColor(Color.rgb(237,237,237));
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
	 * 上传本地视频
	 */
	@SuppressWarnings("static-access")
	private void videoUpload(String path){
		//将本地视频地址,转成logic可读路径fs1://
		String localPath = FileUtils.javaToLibPath(path);
		uploadVideoTime = SystemClock.uptimeMillis();
		boolean b = mApp.mGoluk.GoLuk_CommonGetPage(mApp.mGoluk.PageType_UploadVideo,localPath);
		if(b){
			//隐藏播放图片
			mPlayStatusImage.setVisibility(View.GONE);
			//显示loading布局
			mVideoLoadingLayout.setVisibility(View.VISIBLE);
			//启动loading动画
			mLoadingAnimation.start();
			
			//重置滤镜标识
			mMVListAdapter.setResChange(false);
		}
		else{
			Toast.makeText(mContext,"调用视频上传接口失败",Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * 重置视频播放状态
	 */
	private boolean changeVideoPlayState(){
		if(mVVPlayVideo.isPlaying()){
			//显示播放图片
			mPlayStatusImage.setVisibility(View.VISIBLE);
			//如果正在播放视频，则暂停播放
			mVVPlayVideo.pause();
		}
		//判断当前视频改变了滤镜效果,如果没有,可以直接上传分享
		boolean b = mMVListAdapter.getResChange();
		return b;
	}
	
	 /**
	 * 保存视频
	 */
	protected void onSaveVideo() {
		boolean b = changeVideoPlayState();
		if(!b){
			//直接上传本地视频
			videoUpload(mVideoSavePath);
		}
		else{
			try {
				// 创建保存视频参数，默认参数为 输出size为480*480,码率为512k，帧率为21的视频
				EditorParam editorParam = new EditorParam();
				//高清
//				editorParam.nVideoWidth = 854;
//				editorParam.nVideoHeight = 480;
				//标清
				editorParam.nVideoWidth = 640;
				editorParam.nVideoHeight = 360;
				
				editorParam.nVideoBitrate = 512 * 1024;
				editorParam.nFps = 21;
				
				mVideoSavePath = mNewVideoFilePath + "newvideo.mp4";
				mVVPlayVideo.saveVideo(mVideoSavePath, editorParam,
					new FilterPlaybackView.FilterVideoEditorListener() {
						ProgressDialog m_pdSave;
						long m_lUseTimeChecker;
						
						@Override
						public void onFilterVideoSaveStart() {
							m_pdSave = ProgressDialog.show(VideoEditActivity.this, "", "开始保存编辑。。。");
							m_pdSave.setCanceledOnTouchOutside(false);
							m_pdSave.setCancelable(true);
							m_pdSave.setOnCancelListener(new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel(DialogInterface dialog) {
									mVVPlayVideo.cancelSave();
								}
							});
							m_lUseTimeChecker = SystemClock.uptimeMillis();
						}
	
						@Override
						public boolean onFilterVideoSaving(int nProgress,int nMax) {
							m_pdSave.setMessage(String.format("保存编辑中%d%%...",nProgress));
							// 返回false代表取消保存。。。
							return true;
						}
	
						@Override
						public void onFilterVideoEnd(boolean bSuccess,boolean bCancel) {
							m_pdSave.dismiss();
							//String strInfo = "";
							if (bCancel) {
								//strInfo = "已取消视频保存！";
							}
							else if (bSuccess) {
								//strInfo = "保存视频成功！";
								//item.setVideoPath(AssetsFileUtils.getCreateTempFileDir(VideoEditActivity.this) + "/测试保存编辑和上传后.mp4");
								//uploadVideo(item);
								//保存成功,上传视频
								videoUpload(mVideoSavePath);
							}
							
							//Toast.makeText(VideoEditActivity.this, strInfo,Toast.LENGTH_SHORT).show();
							if (null != mVVPlayVideo && mVVPlayVideo.needReload()) {
								try {
									mVVPlayVideo.reload();
								}
								catch (FilterVideoEditorException e) {
									Toast.makeText(VideoEditActivity.this,"重加载视频失败，" + e.getMessage(),Toast.LENGTH_SHORT).show();
								}
							}
							/**/
							Toast.makeText(VideoEditActivity.this,"视频编辑保存使用时间：" + (SystemClock.uptimeMillis() - m_lUseTimeChecker) + "ms", Toast.LENGTH_SHORT).show();
						}
			
						@Override
						public void onFilterVideoSaveError(int nErrorType,int nErrorNo, String strErrorInfo) {
							Toast.makeText(VideoEditActivity.this,"保存视频失败，" + strErrorInfo,Toast.LENGTH_SHORT).show();
						}
					});
			}
			catch (FilterVideoEditorException e) {
				Toast.makeText(this, "保存视频失败，" + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	
	/**
	 * 选择视频配乐静音
	 */
	private void setMuteVideo(boolean isMuteVideo) {
		if (mVVPlayVideo.isPlaying() || mVVPlayVideo.isPausing()) {
			mVVPlayVideo.stop();
		}
		try {
			mVVPlayVideo.clearAllMixAudio();
			//设置是否静音
			mVVPlayVideo.muteMainVideo(isMuteVideo);
			if(isMuteVideo){
				// 如果是静音情况就需要重置当前配乐路径为空
				mStrMusicFilePath = null;
			}
			
			// 判断配音列表是否存在配音资源，如果存在就要将配音文件重新添加进去在播放
			if (audioInfos.size() > 0) {
				setMixAudioFilePath(m_strRecorderingFilePath, false);
			}
			else {
				// 不存在，则直接播放
				mVVPlayVideo.start();
			}
		}
		catch (FilterVideoEditorException e) {
			console.toast("设置静音失败！", mContext);
			console.log("FilterVideoEditor muteMainVideo exception:" + e.getMessage());
		}
	}
	
	/**
	 * 设置配制音频路径
	 * 
	 * @param mixAudioPath
	 *            配乐或配音路径
	 * @param isSoundTrack
	 *            是否配乐
	 */
	private void setMixAudioFilePath(String path, boolean isSoundTrack) {
		///mnt/sdcard/Android/data/com.rd.car.demo/files/assets/1.mp3
		console.log("music---setMixAudioFilePath---1---" + path);
		String mixAudioPath = addAssets(path);
		console.log("music---setMixAudioFilePath---2---" + mixAudioPath);
		if (TextUtils.isEmpty(mixAudioPath)) {
			console.toast("不支持该" + (isSoundTrack ? "音乐！" : "录音！"),mContext);
			return;
		}
		setSoundTrack(isSoundTrack);
		if (isSoundTrack) {
			m_strMusicFilePath = mixAudioPath;
		}
		else {
			// 添加一组配音信息对象当配音信息集合中去
			m_strRecorderingFilePath = mixAudioPath;
			MixAudioInfo audioInfo = new MixAudioInfo(0, 0, startTime, endTime,0, m_strRecorderingFilePath,1.0f);
			// 屏蔽当前添加的配音路径是否与最近添加的路径相同，不相同则添加
			if (audioInfos.size() == 0 || !audioInfos.get(audioInfos.size() - 1).getRecordFiePath().equals(m_strRecorderingFilePath)) {
				audioInfos.add(audioInfo);
			}
		}
		console.log("选择播放" + (isSoundTrack ? "配乐：" : "配音：") + mixAudioPath);
		console.log("music---444---" + mixAudioPath);
		if (mVVPlayVideo.isPausing() || mVVPlayVideo.isPlaying()) {
			//mVVPlayVideo.stop();
		}
		console.log("music---555---" + mixAudioPath);
		try {
			// 清理所有音频列表后，重新添加不是配乐或配音情况下的配乐或配音文件
			mVVPlayVideo.clearAllMixAudio();
			if(!TextUtils.isEmpty(mixAudioPath)) {
				// from to 设置为0代表配乐在视频全时间线循环
				mVVPlayVideo.addMixAudio(mixAudioPath, 0, 0,1.0f);
			}
			console.log("music---666---" + mixAudioPath);
			for (MixAudioInfo info : audioInfos) {
				// 设置添加配音在一个指定时间段播放
				mVVPlayVideo.addMixAudio(info.getRecordFiePath(),info.getRecordStart(), info.getRecordEnd(),info.getFactor());
			}
			//videoPlaystate();
		}
		catch (Exception e) {
			console.toast("添加" + (isSoundTrack ? "音乐！" : "配音！") + "失败，" + e.getMessage(),mContext);
		}
	}
	
	/**
	 * 重置视频播放状态
	 */
	private void videoPlaystate() {
		//m_ibVideoPlayOrPause.setVisibility(View.VISIBLE);
		if (mVVPlayVideo.isPlaying()) {
			// 如果正在播放视频，则暂停播放
			mVVPlayVideo.pause();
		}
		else {
			// 如果有视频播放但被暂停，则继续播放
			mVVPlayVideo.start();
//			m_ibVideoPlayOrPause.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					m_ibVideoPlayOrPause.setVisibility(View.GONE);
//				}
//			}, 2000);
		}
		// 根据视频的播放状态改变按钮的背景图片
		//m_ibVideoPlayOrPause.setBackgroundResource(m_vvPlayVideo.isPlaying() ? R.drawable.video_pause_bg : R.drawable.video_player_bg);
	}
	
	/**
	 * 设置是否配乐
	 * 
	 * @param isSoundTrack
	 */
	public void setSoundTrack(boolean isSoundTrack) {
		this.isSoundTrack = isSoundTrack;
	}
	
	/**
	 * 添加资源到配置目录
	 * 
	 * @param strAssetFile
	 * @return 导出内置音乐文件信息后返回一个数据对象
	 */
	private String addAssets(String StrFileName, String strAssetFile) {
		String path = AssetsFileUtils.getAssetFileNameForSdcard(this,strAssetFile);
		File file = new File(path);
		if (!file.exists()) {
			AssetsFileUtils.CopyAssets(this.getResources().getAssets(),strAssetFile, file.getAbsolutePath());
		}
		return file.getAbsolutePath();
//		return new String[] {
//			TextUtils.isEmpty(StrFileName) ? file.getName() : StrFileName,
//			file.getAbsolutePath()
//		};
	}
	
	private String addAssets(String strAssetFile) {
		String path = AssetsFileUtils.getAssetFileNameForSdcard(this,strAssetFile);
		File file = new File(path);
		if (!file.exists()) {
			AssetsFileUtils.CopyAssets(this.getResources().getAssets(),strAssetFile, file.getAbsolutePath());
		}
		return file.getAbsolutePath();
	}
	
	/**
	 * 添加音频到视频
	 * @param path
	 */
	private void addMusicToVideo(String path){
		mVVPlayVideo.stop();
		
		setMuteVideo(false);
		//选择音频试听,循环播放,视频重新播放
		setMixAudioFilePath(path,true);
		
		//启动进度条线程
		updateVideoProgress();
		mVVPlayVideo.start();
		//隐藏图片
		mPlayStatusImage.setVisibility(View.GONE);
	}
	
	/**
	 * 本地视频上传回调
	 * @param vid,视频ID
	 */
	public void videoUploadCallBack(int success,String vid){
		//视频上传成功,回调,跳转到视频分享页面
		//隐藏loading
		mLoadingAnimation.stop();
		//显示播放图片
		mPlayStatusImage.setVisibility(View.VISIBLE);
		//隐藏loading布局
		mVideoLoadingLayout.setVisibility(View.GONE);
		if(1 == success){
			Toast.makeText(VideoEditActivity.this,"视频上传使用时间：" + (SystemClock.uptimeMillis() - uploadVideoTime) + "ms", Toast.LENGTH_SHORT).show();
			//跳转视频分享页面
			Intent videoShare = new Intent(mContext,VideoShareActivity.class);
			videoShare.putExtra("cn.com.mobnote.golukmobile.videovid",vid);
			startActivity(videoShare);
		}
		else{
			Toast.makeText(VideoEditActivity.this,"视频上传失败", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * 停止进度条线程
	 */
	private void stopProgressThread() {
		Thread tmpThread = mProgressThread;
		mProgressThread = null;
		if (tmpThread != null) {
			tmpThread.interrupt();
		}
	}
	
	/**
	 * 更新播放进度
	 * 
	 */
	private void updateVideoProgress() {
		// 启动一个新线程用于更新进度条
		mProgressThread = new Thread() {
			public void run() {
				//视频的总长度
				int maxDuration = mVVPlayVideo.getDuration();
				while (null != mProgressThread && null != mVVPlayVideo) {
					// 如果有视频播放才更新进度条
					//totalTime = updateTime(mVVPlayVideo.getDuration());
					// 设置进度条的长度为视频的总长度
					mVideoProgressBar.setMax(maxDuration);
					//console.log("video---progress---max---" + mVVPlayVideo.getDuration());
					// 如果视频正在播放而且进度条没有被拖动
					if (mVVPlayVideo.isPlaying()) {
						// 设置进度条的当前进度为视频已经播放的长度
						int position = mVVPlayVideo.getCurrentPosition();
						//console.log("video---progress---" + position);
						mVideoProgressBar.setProgress(position);
						//Message msg = new Message();
						//msg.obj = updateTime(mVVPlayVideo.getCurrentPosition()) + "/" + totalTime;
						//handler.sendMessage(msg);
					}
					try {
						// 休眠50毫秒
						Thread.sleep(50);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		};
		mProgressThread.start();
	}
	
	/**
	 * 获取视频当前播放时间格式化后的字符串
	public String updateTime(int t) {
		int s = t / 1000 % 60;
		int m = t / 1000 / 60;
		return (m > 9 ? m : "0" + m) + ":" + (s > 9 ? s : "0" + s);
	}
	 */
	
	@Override
	protected void onPause() {
		if (mVVPlayVideo != null) {
			if (mVVPlayVideo.isPlaying()) {
				console.log("video---edit---pause");
				mVVPlayVideo.stop();
				//显示图片
				mPlayStatusImage.setVisibility(View.VISIBLE);
			}
			mVVPlayVideo.onPause();
		}
		//mMediaPlayer.stop();
		//停止进度条线程
		stopProgressThread();
		super.onPause();
	}

	@Override
	protected void onResume(){
		if (mVVPlayVideo != null){
			mVVPlayVideo.onResume();
		}
		mApp.setContext(this,"VideoEdit");
		
		//addMusicToVideo();
		
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		mVVPlayVideo.cleanUp();
		mVVPlayVideo = null;
		//mMediaPlayer.reset();
		//mMediaPlayer.release();
		//mMediaPlayer = null;
		
		super.onDestroy();
		/*
		if(mMedioPlayer.isPlaying()){
			mMedioPlayer.stop();
		}
		mMedioPlayer.release();
		//Activity销毁时停止播放，释放资源。不做这个操作，即使退出还是能听到视频播放的声音
		*/
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch(id){
			case R.id.back_btn:
				//返回
				VideoEditActivity.this.finish();
			break;
			case R.id.next_btn:
				//下一步,跳转到视频分享页面
				Log.e("","chxy send video share");
				//停止进度条线程
				stopProgressThread();
				
				//保存编辑视频到本地
				onSaveVideo();
			break;
			case R.id.play_layout:
				//暂停/播放
				if(mVVPlayVideo.isPlaying()){
					//停止进度条线程
					stopProgressThread();
					mVVPlayVideo.pause();
					//显示图片
					mPlayStatusImage.setVisibility(View.VISIBLE);
				}
				else{
					//启动进度条线程
					updateVideoProgress();
					mVVPlayVideo.start();
					//隐藏图片
					mPlayStatusImage.setVisibility(View.GONE);
				}
			break;
			case R.id.music_btn:
				Intent music = new Intent(mContext,VideoEditMusicActivity.class);
				music.putExtra("cn.com.mobnote.golukmobile.musicfilepath",mStrMusicFilePath);
				startActivity(music);
			break;
		}
	}
}











