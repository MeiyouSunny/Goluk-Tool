package cn.com.mobnote.golukmobile;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.SmsShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.SmsHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.video.MVListAdapter;
import cn.com.mobnote.video.MVManage;
import cn.com.mobnote.video.MVManage.MVEditData;
import cn.com.mobnote.view.MyGridView;
import cn.com.mobonote.golukmobile.comm.GolukMobile;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.RelativeLayout;
import android.widget.Toast;
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
 * @ 功能描述:Goluk视频分享页面
 * 
 * @author 陈宣宇
 * 
 */

@SuppressLint("HandlerLeak")
public class VideoShareActivity extends Activity implements SurfaceHolder.Callback, OnClickListener {
	private static final String DESCRIPTOR = "com.umeng.share";
	private final UMSocialService mController = UMServiceFactory.getUMSocialService(DESCRIPTOR);
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
	/** 视频播放器 */
	private MediaPlayer mMedioPlayer = null;
	private SurfaceView mSurfaceView = null;
	private SurfaceHolder mSurfaceHolder = null;
	/** 播放按钮 */
	private RelativeLayout mPlayLayout = null;
	/** 播放状态图片 */
	private ImageView mPlayStatusImage = null;
	/** mv列表layout */
	private LinearLayout mMVListLayout = null;
	
	/** 分享layout */
	private RelativeLayout mShareLayout = null;
	
	/** 系统loading */
	private ProgressDialog mPdsave = null;
	
	private GolukMobile mGoluk = null;
	/** 视频ID */
	private String mVideoVid = "";
	/** 视频分享页面handler用来接收消息,更新UI*/
	public static Handler mVideoShareHandler = null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_share);
		mContext = this;
		//获取视频路径
		//Intent intent = getIntent();
		//mFilePath = intent.getStringExtra("cn.com.mobnote.video.path");
		
		//mMVListLayout = (LinearLayout) findViewById(R.id.mvlistlayout);
		
		//获取视频Id
		Intent intent = getIntent();
		mVideoVid = intent.getStringExtra("cn.com.mobnote.golukmobile.videovid");
		
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(this,"VideoShare");
		
		// 配置需要分享的相关平台
		configPlatforms();
		
		//视频初始化
		//videoInit();
		//页面初始化
		init();
		//
		//initVideoEditList();
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
	}
	
	/**
	 * 页面初始化
	 */
	private void init(){
		//获取页面元素
		mBackBtn = (Button)findViewById(R.id.back_btn);
		
		mShareLayout = (RelativeLayout)findViewById(R.id.share_layout);

//		mNextBtn = (Button)findViewById(R.id.next_btn);
//		mPlayLayout = (RelativeLayout)findViewById(R.id.play_layout);
//		mPlayStatusImage = (ImageView)findViewById(R.id.play_image);
//		mPlayBtn.setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				mMedioPlayer.start();
//			}
//		});
		//注册事件
		mBackBtn.setOnClickListener(this);
		mShareLayout.setOnClickListener(this);
//		mNextBtn.setOnClickListener(this);
//		mPlayLayout.setOnClickListener(this);
		
		//更新UI handler
		mVideoShareHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
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
	@SuppressWarnings("deprecation")
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
	 * 配置分享平台参数</br>
	 */
	private void configPlatforms() {
		// 添加新浪SSO授权
		mController.getConfig().setSsoHandler(new SinaSsoHandler());
		// 添加微信、微信朋友圈平台
		addWXPlatform();
		//添加短信
		addSMS();
	}
	/**
	 * @功能描述 : 添加微信平台分享
	 * @return
	 */
	private void addWXPlatform(){
		// 注意：在微信授权的时候，必须传递appSecret
		// wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
		String appId = "wx967daebe835fbeac";
		String appSecret = "5bb696d9ccd75a38c8a0bfe0675559b3";
		// 添加微信平台
		UMWXHandler wxHandler = new UMWXHandler(mContext, appId, appSecret);
		wxHandler.addToSocialSDK();
		
		// 支持微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(mContext, appId, appSecret);
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();
	}
	
	/**
	 * 添加短信平台</br>
	 */
	private void addSMS() {
		// 添加短信
		SmsHandler smsHandler = new SmsHandler();
		smsHandler.addToSocialSDK();
	}
	
	/**
	 * 根据不同的平台设置不同的分享内容</br>
	 */
	private void setShareContent(String vurl) {

		// 配置SSO
		mController.getConfig().setSsoHandler(new SinaSsoHandler());
		//mController.getConfig().setSsoHandler(new TencentWBSsoHandler());
		
		//UMImage urlImage = new UMImage(mContext,"http://www.umeng.com/images/pic/social/integrated_3.png");
		WeiXinShareContent weixinContent = new WeiXinShareContent();
		weixinContent.setShareContent(vurl);
		//weixinContent.setTitle("友盟社会化分享组件-微信");
		//weixinContent.setTargetUrl("http://www.umeng.com");
		//weixinContent.setShareMedia(urlImage);
		mController.setShareMedia(weixinContent);

		// 设置朋友圈分享的内容
		CircleShareContent circleMedia = new CircleShareContent();
		circleMedia.setShareContent(vurl);
		//circleMedia.setTitle("友盟社会化分享组件-朋友圈");
		//circleMedia.setShareImage(urlImage);
		// circleMedia.setShareMedia(uMusic);
		// circleMedia.setShareMedia(video);
		//circleMedia.setTargetUrl("http://www.umeng.com");
		mController.setShareMedia(circleMedia);

		// 设置短信分享内容
		SmsShareContent sms = new SmsShareContent();
		sms.setShareContent(vurl);
		//sms.setShareImage(urlImage);
		mController.setShareMedia(sms);

		SinaShareContent sinaContent = new SinaShareContent();
		sinaContent.setShareContent(vurl);
		mController.setShareMedia(sinaContent);
	}
	
	/**
	 * 本地视频分享回调
	 * @param json,分享数据
	 */
	public void videoShareCallBack(int success,String json){
		mPdsave.dismiss();
		if(1 == success){
			JSONObject obj;
			try {
				obj = new JSONObject(json);
				int code = Integer.valueOf(obj.getString("code"));
				if(200 == code){
					//请求成功
					String vurl = obj.getString("vurl");
					//设置分享内容
					setShareContent(vurl);
					
					mController.getConfig().setPlatforms(SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.SINA,SHARE_MEDIA.SMS);
					mController.openShare(this, false);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else{
			Toast.makeText(VideoShareActivity.this,"获取视频分享地址失败", Toast.LENGTH_SHORT).show();
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
		//必须在surface创建后才能初始化MediaPlayer,否则不会显示图像
		mMedioPlayer = new MediaPlayer();
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
			mMedioPlayer.setDataSource(mFilePath);
			mMedioPlayer.prepare();
			//创建完成立即播放
			mMedioPlayer.start();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 销毁时触发，一般在这里将画图的线程停止、释放
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//Activity销毁时停止播放，释放资源。不做这个操作，即使退出还是能听到视频播放的声音
	}
	
	@Override
	protected void onResume(){
		mApp.setContext(this,"VideoShare");
		super.onResume();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch(id){
			case R.id.back_btn:
				//返回
				VideoShareActivity.this.finish();
			break;
			
			case R.id.share_layout:
				//分享
				JSONObject obj = new JSONObject();
//				obj.put("vid",mVideoVid);
//				obj.put("btype","视频分类");
//				obj.put("desc","视频描述");
//				obj.put("share","1");
//				obj.put("imgpath","fs1:/Cache/test11.png");
//				String json = obj.toJSONString();
				mPdsave = ProgressDialog.show(VideoShareActivity.this, "", "请求分享链接...");
				//mPdsave.setMessage(String.format("请求分享链接..."));
				String videoType = "";
				String videoDes = "";
				try {
					videoType = URLEncoder.encode("视频分类","UTF-8");
					videoDes = URLEncoder.encode("视频描述","UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String json = "{\"vid\":\"" + mVideoVid + "\",\"btype\":\"" + videoType + "\",\"desc\":\"" + videoDes + "\",\"share\":\"1\",\"imgpath\":\"fs1:/Cache/test11.png\"}";
				boolean b = mApp.mGoluk.GoLuk_CommonGetPage(mApp.mGoluk.PageType_Share,json);
				
				Log.e("","chxy__b__share11" + b);
				//mController.getConfig().setPlatforms(SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.SINA,SHARE_MEDIA.SMS);
				//mController.openShare(this, false);
				Log.e("","chxy____share11" + json);
			break;
			
			case R.id.next_btn:
				//下一步
				
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
	
	
	
	/*
	@Override
	public void netTransNotifyCallBack(int event, int msg, Object param1,
			Object param2) {
		// TODO Auto-generated method stub
		Log.e("","chxy send3:" + param1 + "," + param2);
	}

	@Override
	public void pageNotifyCallBack(int type, int success, Object param1,Object param2) {
		// TODO Auto-generated method stub
		//send4:{"code":"200","vurl":"http://cdn3.lbs8.com/files/cdcvideo/e2360c72-da82-4379-aec2-871f806e85d820141113201707.mp4","vid":"e2360c72-da82-4379-aec2-871f806e85d820141113201707"},0
		String json = String.valueOf(param1);
		JSONObject obj;
		try {
			obj = new JSONObject(json);
			int code = Integer.valueOf(obj.getString("code"));
			if(200 == code){
				//请求成功
				String vurl = obj.getString("vurl");
				Log.e("","chxy send4 vurl:" + vurl);
				//设置分享内容
				setShareContent(vurl);
				
				mController.getConfig().setPlatforms(SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.SINA,SHARE_MEDIA.SMS);
				mController.openShare(this, false);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.e("","chxy send4:" + param1 + "," + param2);
	}
	
	*/
}
