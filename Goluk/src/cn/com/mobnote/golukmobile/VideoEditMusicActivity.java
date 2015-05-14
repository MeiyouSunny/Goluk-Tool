package cn.com.mobnote.golukmobile;

import java.util.ArrayList;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.console;
import cn.com.mobnote.video.MusicListAdapter;
import cn.com.mobnote.video.MusicManage;
import cn.com.mobnote.video.MusicManage.MusicData;
import cn.com.mobnote.view.PullToRefreshView;
import cn.com.mobnote.view.PullToRefreshView.OnFooterRefreshListener;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

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
 * @ 功能描述:选择音乐
 * 
 * @author 陈宣宇
 * 
 */

public class VideoEditMusicActivity extends BaseActivity implements OnClickListener ,OnFooterRefreshListener{

	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	//private LayoutInflater mLayoutInflater = null;
	/** 返回按钮 */
	//private Button mBackBtn = null;
	
	private Button mCompleteBtn = null;
	private RelativeLayout mNoMusicLayout = null;
	private ImageView mNoMusicCheckImg = null;
	/** 选中的音频路径 */
	private String mCheckMusicPath = "";
	/** 音乐选择列表 */
	private ListView mVideoEditMusicList = null;
	/** 直播列表适配器 */
	private MusicManage mMusicManage = null;
	/** 直播列表适配器 */
	public MusicListAdapter mMusicListAdapter = null;
	private ArrayList<MusicData> mMusicListData = null;
	/** 播放音频 */
	private MediaPlayer mMediaPlayer = new MediaPlayer();
	
	/** 上拉拉刷新控件 */
	private PullToRefreshView mPullToRefreshView = null;
	
	/** 视频分享页面handler用来接收消息,更新UI*/
	public static Handler mVideoShareHandler = null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_edit_music_list);
		mContext = this;
	
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(mContext,"VideoEditMusic");
		
		//获取默认选择音频路径
		Intent intent = getIntent();
		mCheckMusicPath = intent.getStringExtra("cn.com.mobnote.golukmobile.musicfilepath");
		
		//页面初始化
		init();
		
		changeMusicDefaultSelected();
	}
	
	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init(){
		//获取页面元素
		//mBackBtn = (Button)findViewById(R.id.back_btn);
		mCompleteBtn = (Button)findViewById(R.id.complete_btn);
		mNoMusicCheckImg = (ImageView)findViewById(R.id.no_music_check_img);
		mNoMusicLayout = (RelativeLayout)findViewById(R.id.no_music_item);
		//注册事件
		//mBackBtn.setOnClickListener(this);
		
		mVideoEditMusicList = (ListView)findViewById(R.id.video_edit_music_listview);
		//上拉刷新view
//		mPullToRefreshView = (PullToRefreshView)findViewById(R.id.listview_pull_refresh_view);
		
		mMusicManage = new MusicManage(mContext);
		mMusicListData = mMusicManage.getSystemMusicList();
		mMusicListAdapter = new MusicListAdapter(mContext,mMusicListData);
		mVideoEditMusicList.setAdapter(mMusicListAdapter);
		
		//注册上拉刷新事件
//		mPullToRefreshView.setOnFooterRefreshListener(this);
		mCompleteBtn.setOnClickListener(this);
		mNoMusicLayout.setOnClickListener(this);
		
		//更新UI handler
		mVideoShareHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
			}
		};
	}
	
	/**
	 * 改变默认选中音频
	 */
	public void changeMusicDefaultSelected(){
		if(!"".equals(mCheckMusicPath)){
			mNoMusicCheckImg.setVisibility(View.INVISIBLE);
			for(int i = 0,len = mMusicListData.size(); i < len; i++){
				MusicData data = (MusicData)mMusicListData.get(i);
				if(data.filePath.equals(mCheckMusicPath)){
					data.isCheck = true;
					
					//修改默认音频下标
					mMusicListAdapter.setResIndex(i);
				}
				else{
					data.isCheck = false;
				}
			}
		}
		else{
			mNoMusicCheckImg.setVisibility(View.VISIBLE);
		}
		mMusicListAdapter.notifyDataSetChanged();
	}
	
	/**
	 * 隐藏无音乐选中图标
	 */
	public void changeNoMusicStatus(boolean b,String path){
		mCheckMusicPath = path;
		if(b){
			mNoMusicCheckImg.setVisibility(View.VISIBLE);
			for(int i = 0,len = mMusicListData.size(); i < len; i++){
				MusicData data = (MusicData)mMusicListData.get(i);
				data.isCheck = false;
			}
			mMusicListAdapter.notifyDataSetChanged();
			
			//停止音频播放
			mMediaPlayer.stop();
		}
		else{
			mNoMusicCheckImg.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * 播放选择的音频
	 * @param path
	 */
	public void playSelectMusicSound(String path) {
		console.log("music---playSelectMusicSound---" + path);
		try {
			if(!"".equals(path)){
				// 重置mediaPlayer实例，reset之后处于空闲状态
				mMediaPlayer.reset();
				// 设置需要播放的音乐文件的路径，只有设置了文件路径之后才能调用prepare
				AssetFileDescriptor fileDescriptor = mContext.getAssets().openFd(path);
				mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),fileDescriptor.getLength());
				// 准备播放，只有调用了prepare之后才能调用start
				mMediaPlayer.prepare();
				// 开始播放
				mMediaPlayer.start();
			}
		} catch (Exception ex) {
		}
	}
	
	@Override
	protected void onDestroy() {
		if(null != mMediaPlayer){
			mMediaPlayer.stop();
			mMediaPlayer = null;
		}
		super.onDestroy();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch(id){
			case R.id.back_btn:
				//返回
				this.finish();
			break;
			case R.id.complete_btn:
				//完成
				this.finish();
				if(null != VideoEditActivity.mVideoEditHandler){
					Message msg = new Message();
					msg.what = 1;
					msg.obj = mCheckMusicPath;
					VideoEditActivity.mVideoEditHandler.sendMessage(msg);
				}
			break;
			case R.id.no_music_item:
				changeNoMusicStatus(true,"");
			break;
//			case R.id.music_btn:
//				Intent music = new Intent(mContext,VideoShareActivity.class);
//				//videoShare.putExtra("cn.com.mobnote.golukmobile.videovid",vid);
//				startActivity(music);
//			break;
		}
	}

	@Override
	public void onFooterRefresh(PullToRefreshView view) {
		// TODO Auto-generated method stub
		mPullToRefreshView.onFooterRefreshComplete();
	}
}











