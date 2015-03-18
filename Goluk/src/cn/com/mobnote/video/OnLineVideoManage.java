package cn.com.mobnote.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.OnLineVideoPlayActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.LoadImageManager;
import cn.com.mobnote.util.console;
import cn.com.tiros.api.FileUtils;



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
 * @ 功能描述:在线视频管理
 * 
 * @author 陈宣宇
 * 
 */

public class OnLineVideoManage {
	
	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	/** 在线视频布局 */
	private LinearLayout mOnlineVideoLayout = null;
	
	
	/** 在线视频内容 */
	private ViewPager mPager = null;
	/** Tab页面列表 */
	private ArrayList<View> mListViews;
	/** 在线视频数据适配器 */
	private OnLineVideoListAdapter mOnLineVideoAdapter = null;
	/** 在线视频圆点图标view */
	private LinearLayout mOnLineVideoCursorLayout = null;
	/** 视频圆点img */
	private List<ImageView> mCursorList = new ArrayList<ImageView>();
	/** 当前显示视频下标 */
	private int mCurrentItem = 0;
	/** 视频图片view*/
	private HashMap<String,ImageView> mVideoImage = new HashMap<String, ImageView>();
	/** 视频跳转路径保存 */
	private ArrayList<String> mVideoPlayUrl = new ArrayList<String>();
	
	/** 视频存放外卡文件路径 */
	private static final String APP_FOLDER = "capture.jpg";
	private String mFilePath = android.os.Environment.getExternalStorageDirectory().getPath() + "/" + APP_FOLDER;
	
	public OnLineVideoManage(Context context){
		mContext = context;
		
		if(null == mOnlineVideoLayout){
			this.mLayoutInflater = LayoutInflater.from(mContext);
			mOnlineVideoLayout = (LinearLayout) ((MainActivity)mContext).findViewById(R.id.onlinevideo_layout);
		}
		/*
		if(this.mPager == null){
			this.mLayoutInflater = LayoutInflater.from(mContext);
			//视频轮播布局
			this.mPager = (ViewPager)((MainActivity)mContext).findViewById(R.id.vPager);
			//视频轮播圆点布局
			this.mOnLineVideoCursorLayout = (LinearLayout)((MainActivity)mContext).findViewById(R.id.video_cursor_layout);
		}
		*/
	}
	
	/**
	 * 在线视频初始化
	 */
	public void initOnLineVideo(){
		if(null != mOnlineVideoLayout){
			
		}
		/*
		if(this.mPager != null){
			//获取视频数据
			this.getLocalOnLineVideoList();
			this.mOnLineVideoAdapter = new OnLineVideoListAdapter(this.mListViews);
			this.mPager.setAdapter(this.mOnLineVideoAdapter);
			this.mPager.setOnPageChangeListener(new OnLineVideoChangeListener());
		}
		*/
	}
	
	/**
	 * 获取本地缓存在线视频列表
	 * @return
	 */
	public void getLocalOnLineVideoList(){
		//先返回一个空数组,以后需要补充获取本地缓存数据
		this.mListViews = new ArrayList<View>();
	}
	
	/**
	 * 在线视频文本数据请求回调
	 * @param data
	 */
	@SuppressLint("InflateParams")
	public void onLineVideoDataCallback(String json){
		if(this.mOnlineVideoLayout != null && json != null){
			try {
				JSONObject obj = new JSONObject(json);
				//请求成功
				JSONArray list = obj.getJSONArray("json");
				JSONObject data;
				
				//删除原来布局
				mOnlineVideoLayout.removeAllViews();
				
				for(int i = 0,len = list.length(); i < len; i++){
					RelativeLayout view = (RelativeLayout)mLayoutInflater.inflate(R.layout.index_onlinevideo_item,null);
					//设置元素margin
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
					params.setMargins(12,34,16,32);
					view.setLayoutParams(params);
					
					mOnlineVideoLayout.addView(view);
					
					TextView desc = (TextView)view.findViewById(R.id.video_desc);
					ImageView img = (ImageView)view.findViewById(R.id.video_img);
					//播放按钮view
					ImageButton btn = (ImageButton)view.findViewById(R.id.video_play_btn);
					//点赞
					Button likeBtn = (Button)view.findViewById(R.id.like_btn);
					//评论
					Button commentBtn = (Button)view.findViewById(R.id.comment_btn);
					
					data = list.getJSONObject(i);
					//视频描述
					String descStr = data.getString("desc");
					//视频ID
					String vid = data.getString("vid");
					//视频地址
					String vurl = data.getString("vurl");
					//评论数
					String comment = data.getString("comment");
					//点赞数
					String like = data.getString("ilike");
					
					desc.setText(descStr);
					likeBtn.setText(like);
					commentBtn.setText(comment);
					//保存图片view
					this.mVideoImage.put(vid,img);
					//保存视频播放url
					this.mVideoPlayUrl.add(vurl);
					
					//注册view事件
					btn.setOnClickListener(new OnLineVideoPlayBtnClickListener(vurl));
				}
				
				mOnlineVideoLayout.invalidate();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/*
		if(this.mPager != null && json != null){
			try {
				JSONObject obj = new JSONObject(json);
				int code = Integer.valueOf(obj.getString("code"));
				if(200 == code){
					//请求成功
					JSONArray list = obj.getJSONArray("json");
					JSONObject data;
					for(int i = 0,len = list.length(); i < len; i++){
						RelativeLayout view = (RelativeLayout)mLayoutInflater.inflate(R.layout.index_onlinevido_lay,null);
						this.mListViews.add(view);
						//添加圆点图标
						this.addVideoCursor();
						
						TextView desc = (TextView)view.findViewById(R.id.online_video_desc);
						ImageView img = (ImageView)view.findViewById(R.id.online_video_img);
						//播放按钮view
						ImageButton btn = (ImageButton)view.findViewById(R.id.online_video_play_btn);
						
						data = list.getJSONObject(i);
						//视频描述
						String descStr = data.getString("desc");
						//视频ID
						String vid = data.getString("vid");
						//视频地址
						String vurl = data.getString("vurl");
						//评论数
						
						//点赞数
						
						Log.e("","chxy______vurl" + vurl);
						desc.setText(descStr);
						//保存图片view
						this.mVideoImage.put(vid,img);
						//保存视频播放url
						this.mVideoPlayUrl.add(vurl);
						//注册view事件
						btn.setOnClickListener(new OnLineVideoPlayBtnClickListener());
					}
					this.mPager.setAdapter(this.mOnLineVideoAdapter);
				}
				else{
					//请求失败
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
	}
	
	/**
	 * 在线视频图片下载完成回调
	 * @param path
	 */
	public void onLineVideoImgCallback(String json){
		//{'vid':'test11','path':'fs1:/Cache/test11.png'}
		//String json = "{\"vid\":\"vid2\",\"path\":\"mmmm\"}";
		try {
			JSONObject obj = new JSONObject(json);
			String vid = obj.getString("vid");
			String path = obj.getString("path");
			String localPath = FileUtils.libToJavaPath(path);
			Drawable img = LoadImageManager.getLoacalBitmap(localPath,mContext);
			ImageView view = this.mVideoImage.get(vid);
			view.setBackgroundDrawable(img);
			console.log(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		try {
			JSONObject obj = new JSONObject(json);
			String vid = obj.getString("vid");
			String path = obj.getString("path");
			String localPath = FileUtils.libToJavaPath(path);
			Log.e("","chxy______img" + localPath);
			Drawable img = LoadImageManager.getLoacalBitmap(localPath,mContext);
			Log.e("","chxy______img" + img);
			ImageView view = this.mVideoImage.get(vid);
			view.setBackgroundDrawable(img);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
	
	/**
	 * 添加在线视频下标点图片
	 * @author cxy
	 * @date 2014-6-12
	 */
	private void addVideoCursor(){
		//获取图标view
		if(this.mOnLineVideoCursorLayout != null){
			//设置元素margin
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			params.setMargins(0,0,10,0);
			
			ImageView img = new ImageView(mContext);
			img.setBackgroundResource(R.drawable.video_cursor);
			img.setLayoutParams(params);
			
			//保存图标对象
			this.mCursorList.add(img);
			//添加图标
			this.mOnLineVideoCursorLayout.addView(img);
			//刷新view
			this.mOnLineVideoCursorLayout.invalidate();
			
			//如果当前下标为0,更新圆点高亮和类别图片
			if(this.mCurrentItem == 0){
				ImageView firstImg = this.mCursorList.get(0);
				firstImg.setBackgroundResource(R.drawable.video_cursor_curr);
			}
		}
	}
	
	/**
	 * 修改当前高亮圆点
	 * @param current
	 * @author cxy
	 * @date 2014-11-10
	 */
	public void changeCurrentCursor(int pre,int current){
		try{
			//获取当前正在显示的高亮图标
			ImageView preImg = this.mCursorList.get(pre);
			preImg.setBackgroundResource(R.drawable.video_cursor);
			//将要高亮显示的图标
			ImageView currentImg = this.mCursorList.get(current);
			currentImg.setBackgroundResource(R.drawable.video_cursor_curr);
		}
		catch(Exception ex){}
	}
	/**
	 * 视频切换事件监听
	 */
	protected class OnLineVideoChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0){
			//arg0是表示你当前选中的页面，这事件是在你页面跳转完毕的时候调用的。
			//修改高亮圆点
			changeCurrentCursor(mCurrentItem,arg0);
			//保存当前下标
			mCurrentItem = arg0;
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			//arg0 ==1的时候表示正在滑动，arg0==2的时候表示滑动完毕了，arg0==0的时候表示什么都没做，就是停在那。
			if(1 == arg0){
				//停止自动轮播
				//handler.removeMessages(1);
			}
			else if( 0 == arg0){
				//走完2之后,完全停下来之后就会走0,所以在最后重新启动轮播
				//重新启动自动轮播
				//startAutoChangeMessage();
			}
		}
	}
	
	/**
	 * 点击播放按钮click事件内部类
	 * @author cxy
	 */
	protected class OnLineVideoPlayBtnClickListener implements OnClickListener {
		private String mUrl = "";
		public OnLineVideoPlayBtnClickListener(String url){
			mUrl = url;
		}
		@Override
		public void onClick(View v){
			//String path = mVideoPlayUrl.get(mCurrentItem);
			console.log("chxy _____ path" + mUrl);
			//点击视频
			Intent videoEdit = new Intent(mContext,OnLineVideoPlayActivity.class);
			videoEdit.putExtra("cn.com.mobnote.video.path",mUrl);
			mContext.startActivity(videoEdit);
		}
	}
	
	protected class OnLineVideoViewTouchListener implements OnTouchListener{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			/*
			int action = event.getAction();
			switch (v.getId()) {
				case R.id.search_start:
					switch (action) {
						case MotionEvent.ACTION_DOWN:
							searchBtn.setTextColor(Color.rgb(110,2,2));
						break;
						case MotionEvent.ACTION_UP:
							searchBtn.setTextColor(Color.rgb(255,255,255));
						break;
					}
				break;
			}
			*/
			return false;
		}
		
	}
	
	public class OnLineVideoData{
		//视频文件路径
		public String filePath;
		//图片地址,可能是本地地址
		public String imgPath;
		//视频描述
		public String videoDesc;
	}
	
}










