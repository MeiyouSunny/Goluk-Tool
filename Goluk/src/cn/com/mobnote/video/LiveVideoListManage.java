package cn.com.mobnote.video;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Message;
import cn.com.mobnote.golukmobile.LiveVideoListActivity;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.LoadImageManager;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.debug.GolukDebugUtils;



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

public class LiveVideoListManage {
	
	private Context mContext = null;
	/** 视频存放外卡文件路径 */
	//private static final String APP_FOLDER = "goluk/video/";
	//private String mFilePath = android.os.Environment.getExternalStorageDirectory().getPath() + "/" + APP_FOLDER;
	/** 本地视频数据 */
	private ArrayList<LiveVideoListData> mLiveVideoListData = new ArrayList<LiveVideoListData>();
	
	public LiveVideoListManage(Context context){
		mContext = context;
	}
	
	/**
	 * 获取视频评论列表
	 * count 获取视频条数
	 * @return
	 */
	public ArrayList<LiveVideoListData> getVideoSquareList(){
		/*
		LiveVideoListData data1 = new LiveVideoListData();
		data1.headPath = R.drawable.editor_boy_two;
		data1.userName = "评论标题";
		data1.likeNum = "111";
		data1.speed = "60公里/分钟前";
		mLiveVideoListData.add(data1);
		mLiveVideoListData.add(data1);
		mLiveVideoListData.add(data1);
		mLiveVideoListData.add(data1);
		mLiveVideoListData.add(data1);
		mLiveVideoListData.add(data1);
		mLiveVideoListData.add(data1);
		LiveVideoListData data2 = new LiveVideoListData();
		data2.headPath = R.drawable.editor_girl_three;
		data2.userName = "评论标题11";
		data2.likeNum = "44";
		data2.speed = "3公里/钟前";
		mLiveVideoListData.add(data2);
		*/
		return mLiveVideoListData;
	}
	
	/**
	 * 添加直播栏目
	 */
	@SuppressWarnings("static-access")
	public void addLiveVideoItem(JSONArray list){
		try{
			mLiveVideoListData.clear();
			
			for(int i = 0,len = list.length(); i < len; i++){
				LiveVideoListData data = new LiveVideoListData();
				JSONObject obj = list.getJSONObject(i);
				
				String aid = obj.getString("aid");
				String url = obj.getString("picurl");
				
				data.aid = aid;
				data.headPath = obj.getString("head");
				data.userName = obj.getString("nickname");
				data.likeNum = "44";
				data.speed = obj.getString("speed") + "公里/小时";
				mLiveVideoListData.add(data);
				
				//下载截图
				String json = "{\"purl\":\"" + url + "\",\"aid\":\"" + aid + "\",\"type\":\"1\"}";
				GolukDebugUtils.e("","下载直播列表item图片---addLiveVideoItem---json" + json);
				((LiveVideoListActivity)mContext).mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_GetPictureByURL,json);
			}
		}
		catch(Exception ex){
			
		}
	}
	
	public void updateHeadImg(JSONObject obj){
		try{
			String dAid = obj.getString("aid");
			for(int i = 0,len = mLiveVideoListData.size(); i < len; i++){
				LiveVideoListData data = mLiveVideoListData.get(i);
				String aid = data.aid;
				
				if(aid.equals(dAid)){
					String path = obj.getString("purl");
					String localPath = FileUtils.libToJavaPath(path);
					Drawable img = LoadImageManager.getLoacalBitmap(localPath,mContext);
					data.videoImg = img;
				}
			}
			//通知页面刷新
			//发消息给主线程
			Message msg = new Message();
			msg.what = 1;
			if(null != LiveVideoListActivity.mVideoLiveListHandler){
				LiveVideoListActivity.mVideoLiveListHandler.sendMessage(msg);
			}
		}
		catch(Exception ex){
			
		}
	}
	
	public class LiveVideoListData{
		//aid
		public String aid;
		//头像
		public String headPath;
		//昵称
		public String userName;
		//点赞
		public String likeNum;
		//速度
		public String speed;
		//视频图片
		public Drawable videoImg = null;
	}
	
}










