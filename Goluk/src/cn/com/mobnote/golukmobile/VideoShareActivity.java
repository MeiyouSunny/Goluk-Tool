package cn.com.mobnote.golukmobile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.umeng.widget.CustomShareBoard;
import cn.com.mobnote.util.console;
import cn.com.tiros.api.FileUtils;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.SmsShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMVideo;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.SmsHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

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
public class VideoShareActivity extends Activity  implements OnClickListener {
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
	//private Button mNextBtn = null;
	
	/** 分享layout */
	private RelativeLayout mShareLayout = null;
	
	/** 系统loading */
	private ProgressDialog mPdsave = null;
	/** 视频ID */
	private String mVideoVid = "";
	/** 视频路径 */
	private String mVideoPath = "";
	/** 上传视频时间记录 */
	private long uploadVideoTime = 0;
	
	
	public Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1://延迟让server返回的文字内容显示
				JSONObject json = new JSONObject();
				try {
					json.put("code", 200);
					json.put("videourl", "http://cdn2.xiaocheben.com/files/cdcvideo/test1111.mp4");
					json.put("imageurl", "http://cdn2.xiaocheben.com/files/cdcpic/test1111.png");
					json.put("text", "骚年赶紧戳进来吧");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				videoShareCallBack(1, json.toString());
			default:
				break;
			}
		};
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_share);
		
		mContext = this;
		//获取视频Id
		Intent intent = getIntent();
		mVideoPath = intent.getStringExtra("cn.com.mobnote.golukmobile.videopath");
		
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(this,"VideoShare");
		
		//配置需要分享的相关平台
		configPlatforms();
		
		//初始化
		init();
		
		//上传已倒出的本地视频
		uploadShareVideo();
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/**使用SSO授权必须添加如下代码 */
		UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode);
		if(ssoHandler != null){
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}
	
	/**
	 * 页面初始化
	 */
	private void init(){
		//获取页面元素
		mBackBtn = (Button)findViewById(R.id.back_btn);
		
		mShareLayout = (RelativeLayout)findViewById(R.id.share_layout);
		//注册事件
		mBackBtn.setOnClickListener(this);
		mShareLayout.setOnClickListener(this);
	}
	
	/**
	 * 上传要分享的视频
	 */
	private void uploadShareVideo(){
		//将本地视频地址,转成logic可读路径fs1://
		if(!"".equals(mVideoPath) && null != mVideoPath){
			String localPath = FileUtils.javaToLibPath(mVideoPath);
			uploadVideoTime = SystemClock.uptimeMillis();
			boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_UploadVideo,localPath);
			if(!b){
				Toast.makeText(mContext,"调用视频上传接口失败",Toast.LENGTH_SHORT).show();
			}
			else{
				//显示全局上传进度条
				//重置滤镜标识
				//mMVListAdapter.setResChange(false);
			}
		}
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
		//添加腾讯QQ
		addQQQZonePlatform();
	}
	
	/**
	 * @功能描述 : 添加微信平台分享
	 * @return
	 */
	private void addWXPlatform(){
		// 注意：在微信授权的时候，必须传递appSecret
		// wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
		String appId = "wx493f46bf1a71416f";
		String appSecret = "b572ec9cbd3fac52e138e34eff0b4926";
		// 添加微信平台
		UMWXHandler wxHandler = new UMWXHandler(mContext, appId, appSecret);
		wxHandler.addToSocialSDK();
		
		// 支持微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(mContext, appId, appSecret);
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();
	}
	
	/**
	 * @功能描述 : 添加QQ平台支持 QQ分享的内容， 包含四种类型， 即单纯的文字、图片、音乐、视频. 参数说明 : title, summary,
	 *       image url中必须至少设置一个, targetUrl必须设置,网页地址必须以"http://"开头 . title :
	 *       要分享标题 summary : 要分享的文字概述 image url : 图片地址 [以上三个参数至少填写一个] targetUrl
	 *       : 用户点击该分享时跳转到的目标地址 [必填] ( 若不填写则默认设置为友盟主页 )
	 * @return
	*/
	private void addQQQZonePlatform() {
		String appId = "1104418156";
		String appKey = "G7OfQ0qbqe5OJlUP";
		// 添加QQ支持, 并且设置QQ分享内容的target url
		UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler((Activity) mContext,appId, appKey);
		qqSsoHandler.addToSocialSDK();
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
	private void setShareContent(String videourl,String imageurl,String text) {
		
		UMImage umimage = new UMImage(mContext,imageurl);
		UMVideo video = new UMVideo(videourl);
		video.setThumb(umimage);
		
		// 配置新浪SSO
		mController.getConfig().setSsoHandler(new SinaSsoHandler());
		
		//微信
		WeiXinShareContent weixinContent = new WeiXinShareContent();
		
		weixinContent.setShareContent("Goluk分享内容");
		weixinContent.setTitle(text);
		weixinContent.setTargetUrl(videourl);
		weixinContent.setShareMedia(video);
		mController.setShareMedia(weixinContent);
		
		// 设置朋友圈分享的内容
		CircleShareContent circleMedia = new CircleShareContent();
		circleMedia.setShareContent("Goluk分享内容");
		circleMedia.setTitle(text);
		circleMedia.setTargetUrl(videourl);
		circleMedia.setShareMedia(video);
		mController.setShareMedia(circleMedia);

		// 设置短信分享内容
		SmsShareContent sms = new SmsShareContent();
		sms.setShareContent(text+"。"+videourl);
		//sms.setShareImage(umimage);
		mController.setShareMedia(sms);
		
		//新浪微博分享
		SinaShareContent sinaContent = new SinaShareContent();
		sinaContent.setShareContent("Goluk分享内容");
		sinaContent.setTitle(text);
		sinaContent.setTargetUrl(videourl);
		sinaContent.setShareMedia(video);
		mController.setShareMedia(sinaContent);
		
		
		//qq分享
		QQShareContent  qqContent = new QQShareContent();
		qqContent.setShareContent("Goluk分享内容");
		qqContent.setTitle(text);
		qqContent.setTargetUrl(videourl);
		qqContent.setShareMedia(video);
		mController.setShareMedia(qqContent);
	}
	
	/**
	 * 本地视频上传回调
	 * @param vid,视频ID
	 */
	public void videoUploadCallBack(int success,String vid){
		//视频上传成功,回调,跳转到视频分享页面
		//隐藏loading
		//mLoadingAnimation.stop();
		//显示播放图片
		//mPlayStatusImage.setVisibility(View.VISIBLE);
		//隐藏loading布局
		//mVideoLoadingLayout.setVisibility(View.GONE);
		if(1 == success){
			console.toast("视频上传使用时间：" + (SystemClock.uptimeMillis() - uploadVideoTime) + "ms",mContext);
			//保存视频上传ID
			mVideoVid = vid;
			console.log("视频上传返回id---videoUploadCallBack---vid---" + vid);
		}
		else{
			console.toast("视频上传失败",mContext);
		}
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
				System.out.println("分享地址回调:"+json.toString());
				int code = Integer.valueOf(obj.getString("code"));
				if(200 == code){
					//请求成功
					String videourl = obj.getString("vurl");
					String imageurl = obj.getString("imageurl");
					String text = obj.getString("text");
					
					//设置分享内容
					setShareContent(videourl,imageurl,text);
					
					CustomShareBoard shareBoard = new CustomShareBoard(this);
					shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
				
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else{
			Toast.makeText(VideoShareActivity.this,"获取视频分享地址失败", Toast.LENGTH_SHORT).show();
		}
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
				//mHandler.sendEmptyMessageDelayed(1, 500);//5毫秒后去调用自己的回调函数
				
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
				boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_Share,json);
				
				Log.e("","chxy__b__share11" + b);
				//mController.getConfig().setPlatforms(SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.SINA,SHARE_MEDIA.SMS);
				//mController.openShare(this, false);
				Log.e("","chxy____share11" + json);
			break;
			
			case R.id.next_btn:
				//下一步
				
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
