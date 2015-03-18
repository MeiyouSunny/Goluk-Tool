package cn.com.mobnote.video;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Message;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.golukmobile.LocalVideoListActivity;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.OnLineVideoPlayActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.VideoEditActivity;
import cn.com.mobnote.util.LoadImageManager;
import cn.com.mobnote.video.LocalVideoManage.LocalVideoData;
import cn.com.mobnote.video.LocalVideoManage.ReadFileThread;
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

public class VideCommentManage {
	

	private Context mContext = null;
	/** 视频存放外卡文件路径 */
	private static final String APP_FOLDER = "tiros-com-cn-ext/video/";
	private String mFilePath = android.os.Environment.getExternalStorageDirectory().getPath() + "/" + APP_FOLDER;
	/** 本地视频数据 */
	private ArrayList<VideoCommentData> mVideoCommentList = new ArrayList<VideoCommentData>();
	
	public VideCommentManage(Context context){
		mContext = context;
	}
	
	/**
	 * 获取视频评论列表
	 * count 获取视频条数
	 * @return
	 */
	public ArrayList<VideoCommentData> getLocalVideoList(boolean hasRead,int count){
		VideoCommentData data1 = new VideoCommentData();
		data1.headPath = R.drawable.portrait_bg;
		data1.title = "评论标题";
		data1.comment = "评论内容,dddddddd";
		data1.time = "1分钟前";
		mVideoCommentList.add(data1);
		mVideoCommentList.add(data1);
		mVideoCommentList.add(data1);
		mVideoCommentList.add(data1);
		mVideoCommentList.add(data1);
		mVideoCommentList.add(data1);
		mVideoCommentList.add(data1);
		VideoCommentData data2 = new VideoCommentData();
		data2.headPath = R.drawable.portrait_bg;
		data2.title = "评论标题11";
		data2.comment = "评论内容2222,dddddddd";
		data2.time = "33333分钟前";
		mVideoCommentList.add(data2);
		return mVideoCommentList;
	}
	
	public class VideoCommentData{
		//头像
		public int headPath;
		//标题
		public String title;
		//评论
		public String comment;
		//时间
		public String time;
	}
	
}










