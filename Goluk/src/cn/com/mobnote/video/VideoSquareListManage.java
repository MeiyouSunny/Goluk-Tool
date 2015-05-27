package cn.com.mobnote.video;

import android.content.Context;
import java.util.ArrayList;
import cn.com.mobnote.golukmobile.R;



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
 * @ 功能描述:直播列表管理
 * 
 * @author 陈宣宇
 * 
 */

public class VideoSquareListManage {
	

	private Context mContext = null;
	/** 视频存放外卡文件路径 */
	//private static final String APP_FOLDER = "goluk/video/";
	//private String mFilePath = android.os.Environment.getExternalStorageDirectory().getPath() + "/" + APP_FOLDER;
	/** 本地视频数据 */
	private ArrayList<VideoSquareListData> mVideoSquareListData = new ArrayList<VideoSquareListData>();
	
	public VideoSquareListManage(Context context){
		mContext = context;
	}
	
	/**
	 * 获取视频评论列表
	 * count 获取视频条数
	 * @return
	 */
	public ArrayList<VideoSquareListData> getVideoSquareList(int count){
		VideoSquareListData data1 = new VideoSquareListData();
		data1.headPath = R.drawable.editor_boy_two;
		data1.userName = "评论标题";
		data1.likeNum = "111";
		data1.speed = "60公里/分钟前";
		mVideoSquareListData.add(data1);
		mVideoSquareListData.add(data1);
		mVideoSquareListData.add(data1);
		mVideoSquareListData.add(data1);
		mVideoSquareListData.add(data1);
		mVideoSquareListData.add(data1);
		mVideoSquareListData.add(data1);
		VideoSquareListData data2 = new VideoSquareListData();
		data2.headPath = R.drawable.editor_girl_three;
		data2.userName = "评论标题11";
		data2.likeNum = "44";
		data2.speed = "3公里/钟前";
		mVideoSquareListData.add(data2);
		return mVideoSquareListData;
	}
	
	public class VideoSquareListData{
		//头像
		public int headPath;
		//昵称
		public String userName;
		//点赞
		public String likeNum;
		//速度
		public String speed;
		//视频图片
		public String videoImg;
	}
	
}










