package cn.com.mobnote.video;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Message;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.DisplayMetrics;
import cn.com.mobnote.golukmobile.LocalVideoListActivity;
import cn.com.mobnote.golukmobile.MainActivity;
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
 * @ 功能描述:本地视频管理
 * 
 * @author 陈宣宇
 * 
 */

@SuppressLint({ "DefaultLocale", "SimpleDateFormat" })
public class LocalVideoManage {
	
	private Context mContext = null;
	/** 视频存放外卡文件路径 */
	private static final String APP_FOLDER = android.os.Environment.getExternalStorageDirectory().getPath();
	private String newImagePath = APP_FOLDER + "/" + "goluk/videoLog/videostatus";
	private String mFilePath = APP_FOLDER + "/" + "goluk/video/";
	/** 本地视频数据 */
	private ArrayList<LocalVideoData> mLocalVideoList = new ArrayList<LocalVideoData>();
	/** 本地视频列表页面数据 */
	private ArrayList<LocalVideoData> mLocalVideoListData = new ArrayList<LocalVideoData>();
	/** 来源标示,用来获取视频第一针发送消息 */
	private String mPageSource = "";
	/** 获取固定本地视频个数 */
	private int mListCount = 6;
	/** 本地视频传输文件列表 */
	private ArrayList<String> mFileNameList = new ArrayList<String>();
	private int mFileListSize = 0;
	/** 当前已经传输到第几个视频 */
	private int mVideoCurrent = 0;
	/** 主页activity */
	private MainActivity mMainActivity = null;
	/** 已经读取到的视频文件 */
	private Map<String,String> mLoadFileList = new HashMap<String,String>();
	/** 传输超过6个,保存刚要删除的视频,以便断网删除后,重新显示 */
	private LocalVideoData mLastLocalVideoData = null;
	
	public LocalVideoManage(Context context,String source){
		mContext = context;
		mPageSource = source;
		
		//保存MainActivity,用来解决列表页面视频同步更新主页
		if(source == "Main"){
			mMainActivity = ((MainActivity)mContext);
		}
	}
	
	/**
	 * 获取本地视频列表
	 * hasRead 是否开启线程读本地视屏
	 * count 获取视频条数
	 * @return
	 */
	public ArrayList<LocalVideoData> getLocalVideoList(boolean hasRead,int count){
		if(hasRead){
			//保存列表数量
			mListCount = count;
			//先返回文件个数给页面显示
			readLocalVideoFile(count);
			
			//开一个新线程读取本地视频文件
			ReadFileThread readFileThread = new ReadFileThread(false,null);
			Thread thread = new Thread(readFileThread);
			thread.start();
		}
		return mLocalVideoList;
	}
	
	/**
	 * 获取本地视频列表
	 * hasRead 是否开启线程读本地视屏
	 * count 获取视频条数
	 * @return
	 */
	public ArrayList<LocalVideoData> getLocalVideoList(boolean hasRead){
		if(hasRead){
			//先返回文件个数给页面显示
			readLocalVideoFile();
			
			//开一个新线程读取本地视频文件
			ReadListFileThread readFileThread = new ReadListFileThread(false,null);
			Thread thread = new Thread(readFileThread);
			thread.start();
		}
		return mLocalVideoListData;
	}
	
	/**
	 * 读取本地视频文件
	 * @return
	 */
	public void readLocalVideoFile(){
		//清除缓存数据
		mLocalVideoListData.clear();
		mLoadFileList.clear();
		
		File file = new File(mFilePath);
		//必须是文件夹
		if(file.isDirectory()){
			//得到所有FILE
			File[] files = file.listFiles();
			//对所有的文件排序
			files = this.sortFile(files);
			//需要获取文件数量
			//int addCount = 0;
			//拿到已经点击过的视频文件名字
			Map<String,String> videoNames = this.getVideoLog();
			
			for(File f : files){
				if(f.exists()){
					if(f.isDirectory()){
						//是文件夹递归查找,自己保存的目录,只读这个目录的文件,走下面
					}else{
						String fileName = f.getName();
						//得到后缀
						int lastIndex = fileName.lastIndexOf(".");
						if(-1 != lastIndex){
							String fileSuffix = fileName.substring(lastIndex);
							if(fileSuffix.toLowerCase().equals(".mp4")){
								//视频全路径
								String path = f.getPath();
								//保存文件,用来上传完成的时候做验证,保存1标识是本地的视频
								mLoadFileList.put(fileName,"1");
								
								//获取文件大小
								FileInputStream fis;
								String size = "";
								try {
									fis = new FileInputStream(f);
									int fileLen = fis.available();
									size = String.format("%.1f", fileLen / 1024.f / 1024.f) + "M";
								}
								catch(Exception e){
								}
								
								//判断视频类别,WND1_,URG1_文件已这种格式开头为 8s/紧急
								String[] names = fileName.split("_");
								GolukDebugUtils.e("","fileName---" + fileName);
								String vt = names[0];
								
								long date = countFileDate(fileName);
								String time = this.getTime(date);
								
								//保存数据
								LocalVideoData data = new LocalVideoData();
								data.filePath = path;
								data.img = null;
								data.changeTime = time;
								data.fileSize = size;
								data.fileName = fileName;
								if(videoNames != null){
									if(videoNames.get(fileName) != null){
										data.isNew = false;
									}else{
										data.isNew = true;
									}
								}else{
									data.isNew = true;
								}
								data.videoType = vt;
								mLocalVideoListData.add(data);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 读取本地视频文件
	 * @return
	 */
	public void readLocalVideoFile(int count){
		//清除缓存数据
		mLocalVideoList.clear();
		//mLoadFileList.clear();
		
		File file = new File(mFilePath);
		//必须是文件夹
		if(file.isDirectory()){
			//得到所有FILE
			File[] files = file.listFiles();
			//对所有的文件排序
			files = this.sortFile(files);
			//需要获取文件数量
			int addCount = 0;
			//拿到已经点击过的视频文件名字
			Map<String,String> videoNames = this.getVideoLog();
			
			for(File f : files){
				if(f.exists()){
					if(f.isDirectory()){
						//是文件夹递归查找,自己保存的目录,只读这个目录的文件,走下面
					}else{
						String fileName = f.getName();
						//得到后缀
						int lastIndex = fileName.lastIndexOf(".");
						if(-1 != lastIndex){
							String fileSuffix = fileName.substring(lastIndex);
							if(fileSuffix.toLowerCase().equals(".mp4")){
								//视频全路径
								String path = f.getPath();
								//保存文件,用来上传完成的时候做验证,保存1标识是本地的视频
								//mLoadFileList.put(fileName,"1");
								
								//获取文件大小
								FileInputStream fis;
								String size = "";
								try {
									fis = new FileInputStream(f);
									int fileLen = fis.available();
									size = String.format("%.1f", fileLen / 1024.f / 1024.f) + "M";
								}
								catch(Exception e){
								}
								
								//判断视频类别,WND1_,URG1_文件已这种格式开头为 8s/紧急
								String[] names = fileName.split("_");
								GolukDebugUtils.e("","fileName---" + fileName);
								String vt = names[0];
								
//								Date date = new Date(f.lastModified());
//								//获取时间控件该显示的文字内容
//								String time = this.getTime(date);
								//获取文件最后修改时间
								//URG1_150128133420_0016.mp4,
								long date = countFileDate(fileName);
								String time = this.getTime(date);
								
								//保存数据
								LocalVideoData data = new LocalVideoData();
								data.filePath = path;
								data.img = null;
								data.changeTime = time;
								data.fileSize = size;
								data.fileName = fileName;
								if(videoNames != null){
									if(videoNames.get(fileName) != null){
										data.isNew = false;
									}else{
										data.isNew = true;
									}
								}else{
									data.isNew = true;
								}
								data.videoType = vt;
								mLocalVideoList.add(data);
								
								addCount++;
								//如果大于count,退出,首页只显示6个视频
								if(addCount >= count){
									break;
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 根据文件的最后修改时间进行排序
	 * @param files
	 * @return
	 */
	public File[] sortFile(File[] files){
		if(files != null && files.length > 0){
			File file = null;
			for(int i = 0; i < files.length; i++) {
				for(int j = i+1; j <= files.length-1; j++) {
					String fileI = files[i].getName();
					String fileJ =  files[j].getName();
					long timeI = countFileDate(fileI);
					long timeJ = countFileDate(fileJ);
					if(timeI < timeJ){
						file = files[i];
						files[i] = files[j];
						files[j] = file;
					}
				}
			}
		}
		return files;
	}
	
	public long countFileDate(String name){
		long time = new Date().getTime();
		//判断视频类别,WND1_,URG1_文件已这种格式开头为 8s/紧急
		String[] names = name.split("_");
		//获取文件最后修改时间
		//URG1_150128133420_0016.mp4,
		String date = "";
		if(names.length > 0){
			date = names[1];
			String year = "20" + date.substring(0,2);
			String mouth = date.substring(2,4);
			String day = date.substring(4,6);
			String hour = date.substring(6,8);
			String minute = date.substring(8,10);
			String second = date.substring(10, 12);
			String dateString = year + "-" + mouth + "-" + day + " " + hour + ":" + minute + ":" +second;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date currentTime = null;
			try {
				currentTime = sdf.parse(dateString);
				time = currentTime.getTime();
			}
			catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return time;
	}
	
	/**
	 * 根据最后的修改时间计算出改显示什么文字
	 * @param time
	 * @return
	 */
	public String getTime(long time){
		long now = System.currentTimeMillis();
		//long changetime = time.getTime();
		long changetime = time;
		long difference = now - changetime;
		if(difference < 86400000){
			return "今天";
		}else if(difference >= 86400000 && difference < 172800000){
			return "昨天";
		}else if(difference >= 172800000 && difference <= 604800000){
			Calendar calendar = Calendar.getInstance();
			Date date = new Date(time);
			calendar.setTime(date);
			int number = calendar.get(Calendar.DAY_OF_WEEK)-1;
			String[] str = {"星期日","星期一","星期二","星期三","星期四","星期五","星期六",};
			return str[number];
		}else{
			//获取文件最后修改时间
			SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd");
			return df.format(time);
		}
	}
	
	/**
	 * 获取视频第一针图片
	 */
	public void getAllLocalVideo(){
		int len = this.mLocalVideoList.size();
		for(int i = 0; i < len; i++){
			LocalVideoData data = this.mLocalVideoList.get(i);
			getVideoFirstImage(data);
		}
	}
	
	/**
	 * 获取视频第一针图片
	 */
	public void getAllLocalListVideo(){
		int len = this.mLocalVideoListData.size();
		for(int i = 0; i < len; i++){
			LocalVideoData data = this.mLocalVideoListData.get(i);
			getVideoFirstImage(data);
		}
	}
	
	public Bitmap getBitmapsFromVideo(String dataPath,int seconds) {
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource(dataPath);
		// 取得视频的长度(单位为毫秒) 
		//String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION); 
		// 取得视频的长度(单位为秒) 
		Bitmap bitmap = retriever.getFrameAtTime(seconds*1000*1000,MediaMetadataRetriever.OPTION_CLOSEST_SYNC); 
		return bitmap;
		//这个函数会报错
		/*
		03-11 19:04:44.503: E/AndroidRuntime(2767): java.lang.IllegalArgumentException
		03-11 19:04:44.503: E/AndroidRuntime(2767): 	at android.media.MediaMetadataRetriever.setDataSource(MediaMetadataRetriever.java:68)
		03-11 19:04:44.503: E/AndroidRuntime(2767): 	at cn.com.mobnote.video.LocalVideoManage.getBitmapsFromVideo(LocalVideoManage.java:418)
		03-11 19:04:44.503: E/AndroidRuntime(2767): 	at cn.com.mobnote.video.LocalVideoManage.getVideoFirstImage(LocalVideoManage.java:432)
		03-11 19:04:44.503: E/AndroidRuntime(2767): 	at cn.com.mobnote.video.LocalVideoManage.access$0(LocalVideoManage.java:428)
		03-11 19:04:44.503: E/AndroidRuntime(2767): 	at cn.com.mobnote.video.LocalVideoManage$ReadFileThread.run(LocalVideoManage.java:747)
		03-11 19:04:44.503: E/AndroidRuntime(2767): 	at java.lang.Thread.run(Thread.java:856)
		*/
	} 
	/**
	 * 获取视频第一针图片
	 */
	private void getVideoFirstImage(LocalVideoData data){
		String path = data.filePath;
		//获取视频第一针
		Bitmap bitMap = ThumbnailUtils.createVideoThumbnail(path, Thumbnails.MINI_KIND);
		//Bitmap bitMap = getBitmapsFromVideo(path,1);
		if(null != bitMap){
			//将第一针转成固定大小图片
			//计算屏幕宽度
			DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
			int width = dm.widthPixels - 22;
			float density = dm.density;
			Bitmap img = ThumbnailUtils.extractThumbnail(bitMap,width / 2,(int)(95 * density));
			GolukDebugUtils.e("","localvideo---width---" + width + "---density---" + density + "---iheight---" + (int)(95 * density));
			//bitmap转换成drawable给页面显示
			Drawable drawable = new BitmapDrawable(mContext.getResources(),img);
			//缓存数据
			data.img = drawable;
			
			//发消息给主线程
			Message msg = new Message();
			msg.what = 1;
			if(mPageSource == "Main"){
				if(null != MainActivity.mMainHandler){
					MainActivity.mMainHandler.sendMessage(msg);
				}
			}
			else if(mPageSource == "LocalVideoList"){
				if(null != LocalVideoListActivity.mVideoListHandler){
					LocalVideoListActivity.mVideoListHandler.sendMessage(msg);
				}
			}
		}
	}
	
	/**
	 * 读取视频日志
	 */
	private Map<String,String> getVideoLog(){
		String[] data = null;
		File file=new File(newImagePath);
		if(file.exists()){
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
				String str = br.readLine();
				data = str.split(";");
				br.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map<String,String> map=new HashMap<String,String>();
			for (String fileName : data) {
				map.put(fileName, "false");
			}
			return map;
		}else{
			return null;
		}
		
	}
	
	/**
	 * 点击视频播放的时候向日志文件中添加点击的文件名字
	 * @param str
	 */
	public void addVideoLog(String str){
		FileOutputStream outputStream = null;
		try{
			File file = new File(newImagePath);
			if(!file.exists()){
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}
			outputStream = new FileOutputStream(file,true);
			outputStream.write(str.getBytes());
			outputStream.flush();
			outputStream.close();
		}catch(IOException e){
			e.printStackTrace();
			try{
				outputStream.flush();
				outputStream.close();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * 解析本地传输视频目录
	 */
	public void analyzeVideoFile(String data){
		try {
			JSONObject obj = new JSONObject(data);
			JSONArray array = obj.getJSONArray("filepath");
			mFileListSize = array.length();
			//清除上传数据,不然第二次上传会出错
			mVideoCurrent = 0;
			mFileNameList.clear();
			//保存上传视频文件名
			for(int i = 0,len = array.length(); i < len; i++){
				mFileNameList.add(array.getString(i));
			}
			
			//添加视频item
			addUploadVideo();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 将要上传的视频数据添加到数据集
	 */
	public void addUploadVideo(){
		if(mFileListSize > mVideoCurrent){
			LocalVideoData data = new LocalVideoData();
			data.isUpload = true;
			//如果本地视频超过6个,需要删除最后一个(主页最多显示6个)
			if(mLocalVideoList.size() >= mListCount){
				//保存将要删除的视频,当传输中断时,恢复
				mLastLocalVideoData = mLocalVideoList.get(mListCount - 1);
				mLocalVideoList.remove(mListCount - 1);
			}
			mLocalVideoList.add(0,data);
			
			if(null != mMainActivity){
				//刷新首页视频数据
				mMainActivity.videoDataUpdate();
			}
			//if(mPageSource == "LocalVideoList"){
			//	((LocalVideoListActivity)mContext).videoDataUpdate();
			//}
		}
	}
	
	/**
	 * 本地视频上传完成
	 */
	public void videoUploadCallBack(){
		if(mFileNameList.size() >= mVideoCurrent && mFileNameList.size() != 0 && mLocalVideoList.size() > 0){
			String fileName = mFileNameList.get(mVideoCurrent);
			File f = new File(mFilePath + fileName);
			//String fileName = f.getName();
			//得到后缀
			String fileSuffix = fileName.substring(fileName.lastIndexOf("."));
			if(fileSuffix.toLowerCase().equals(".mp4")){
				//视频全路径
				String path = f.getPath();
				
				//保存文件,用来上传完成的时候做验证,保存1标识是本地的视频
				//mLoadFileList.put(fileName,"1");
				
				//获取文件最后修改时间
				//Date date = new Date(f.lastModified());
				long date = countFileDate(fileName);
				//获取时间控件该显示的文字内容
				String time = this.getTime(date);
				
				//获取文件大小
				FileInputStream fis;
				String size = "";
				try {
					fis = new FileInputStream(f);
					int fileLen = fis.available();
					size = String.format("%.1f", fileLen / 1024.f / 1024.f) + "M";
				}
				catch(Exception e){
				}
				
				//判断视频类别,WND1_,URG1_文件已这种格式开头为 8s/紧急
				String[] names = fileName.split("_");
				String vt = names[0];
				
				LocalVideoData data = (LocalVideoData)mLocalVideoList.get(0);
				//保存数据
				data.filePath = path;
				data.img = null;
				data.changeTime = time;
				data.fileSize = size;
				data.fileName = fileName;
				data.isNew = true;
				data.isUpload = false;
				data.videoType = vt;
				
				//改变上传目录标识,然后加载下一个视频
				mVideoCurrent++;
				
				//开一个新线程读取本地视频文件
				ReadFileThread readFileThread = new ReadFileThread(true,data);
				Thread thread = new Thread(readFileThread);
				thread.start();
				
				//加载下一个视频
				addUploadVideo();
			}
		}
	}
	
	/**
	 * 本地视频上传完成,更新本地视频列表数据
	 */
	public void videoUploadComplete(){
		File file = new File(mFilePath);
		//必须是文件夹
		if(file.isDirectory()){
			//得到所有FILE
			File[] files = file.listFiles();
			//对所有的文件排序
			files = this.sortFile(files);
			
			for(File f : files){
				if(f.exists()){
					if(f.isDirectory()){
						//是文件夹递归查找,自己保存的目录,只读这个目录的文件,走下面
					}else{
						String fileName = f.getName();
						//得到后缀
						String fileSuffix = fileName.substring(fileName.lastIndexOf("."));
						if(fileSuffix.toLowerCase().equals(".mp4")){
							//判断这个视频是否已经加载过了
							String isLoad = mLoadFileList.get(fileName);
							if(!"1".equals(isLoad)){
								//未加载
								//视频全路径
								String path = f.getPath();
								
								//保存文件,用来上传完成的时候做验证,保存1标识是本地的视频
								mLoadFileList.put(fileName,"1");
								
								//获取文件最后修改时间
								//Date date = new Date(f.lastModified());
								long date = countFileDate(fileName);
								//获取时间控件该显示的文字内容
								String time = this.getTime(date);
								
								//获取文件大小
								FileInputStream fis;
								String size = "";
								try {
									fis = new FileInputStream(f);
									int fileLen = fis.available();
									size = String.format("%.1f", fileLen / 1024.f / 1024.f) + "M";
								}
								catch(Exception e){
								}
								
								//判断视频类别,WND1_,URG1_文件已这种格式开头为 8s/紧急
								String[] names = fileName.split("_");
								String vt = names[0];
								
								//保存数据
								LocalVideoData data = new LocalVideoData();
								data.filePath = path;
								data.img = null;
								data.changeTime = time;
								data.fileSize = size;
								data.fileName = fileName;
								data.isNew = true;
								data.videoType = vt;
								mLocalVideoListData.add(0,data);
								
								//开一个新线程读取本地视频文件
								ReadListFileThread readFileThread = new ReadListFileThread(true,data);
								Thread thread = new Thread(readFileThread);
								thread.start();
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 链接中断删除正在上传视频
	 */
	public boolean removeVideoListByLinkOff(){
		boolean b = false;
		if(mLocalVideoList.size() > 0){
			LocalVideoData data = mLocalVideoList.get(0);
			//判断是否正在上传,如果上传完成,filePath,fileName是有值的;
			if(null == data.filePath){
				//正在上传,删除这条数据
				mLocalVideoList.remove(0);
				b = true;
				
				GolukDebugUtils.e("","wifi---传输中断删除数据---数据恢复---下标---" + mLocalVideoList.size());
				//恢复数据,少于6个,mLastLocalVideoData = null
				if(null != mLastLocalVideoData){
					mLocalVideoList.add(mLocalVideoList.size(),mLastLocalVideoData);
				}
			}
		}
		return b;
	}
	
	public class ReadFileThread implements Runnable{
		/** 标识是否获取全部数据的视频第一针 true/false 不获取/获取*/
		private boolean mHasOne = false;
		/** 视频数据 */
		private LocalVideoData mData = null;
		public ReadFileThread(boolean b,LocalVideoData data){
			mHasOne = b;
			mData = data;
		}
		
		@Override
		public void run(){
			try{
				Thread.sleep(1);
				if(!mHasOne){
					getAllLocalVideo();
				}
				else{
					getVideoFirstImage(mData);
				}
			}
			catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	
	public class ReadListFileThread implements Runnable{
		/** 标识是否获取全部数据的视频第一针 true/false 不获取/获取*/
		private boolean mHasOne = false;
		/** 视频数据 */
		private LocalVideoData mData = null;
		public ReadListFileThread(boolean b,LocalVideoData data){
			mHasOne = b;
			mData = data;
		}
		
		@Override
		public void run(){
			try{
				Thread.sleep(1);
				if(!mHasOne){
					getAllLocalListVideo();
				}
				else{
					getVideoFirstImage(mData);
				}
			}
			catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	
	public class LocalVideoData{
		//文件路径
		public String filePath = null;
		//视频第一针缩略图
		public Drawable img = null;
		//最后修改时间
		public String changeTime = null;
		//文件大小
		public String fileSize = null;
		//是否要显示new图标
		public boolean isNew = false;
		//文件名
		public String fileName = null;
		//传输中
		public boolean isUpload = false;
		//视频类别位置/8秒/紧急 0/1/2
		public String videoType = "0";
	}
}










