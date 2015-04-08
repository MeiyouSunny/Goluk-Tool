package cn.com.mobnote.video;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cn.com.mobnote.util.console;


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
 * @ 功能描述:视频编辑页面选择音乐管理类
 * 
 * @author 陈宣宇
 * 
 */

@SuppressLint("SimpleDateFormat")
public class MusicManage {
	private Context mContext = null;
	/** 音频存放文件路径 */
	private String mFilePath = "music";
	/** 音频文件对应名称 */
	private String[] mMusicName = {"1","2","3","4","5","6","7","8","9"};
	
	public MusicManage(Context context){
		mContext = context;
	}
	
	/**
	 * 获取本地滤镜主题列表
	 * @return
	 */
	public ArrayList<MusicData> getSystemMusicList(){
		ArrayList<MusicData> list = new ArrayList<MusicData>();
		list = readLocalVideoFile(list);
		return list;
	}
	
	/**
	 * 读取本地音频文件
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public ArrayList<MusicData> readLocalVideoFile(ArrayList<MusicData> list){
		try {
			String[] str = mContext.getAssets().list(mFilePath);
			if (str.length > 0) {
				//如果是目录
				File file = new File("/data/" + mFilePath);
				file.mkdirs();
				for (String fileName : str) {
					//得到后缀
					int lastIndex = fileName.lastIndexOf(".");
					if(-1 != lastIndex){
						String fileSuffix = fileName.substring(lastIndex);
						if(fileSuffix.toLowerCase().equals(".mp3")){
							//音频全路径
							String path = mFilePath + "/" + fileName;
							console.log("music---" + path);
							//保存数据
							MusicData data = new MusicData();
							data.filePath = path;
							int index = Integer.valueOf(fileName.substring(0,1)) - 1;
							data.fileName = mMusicName[index];
							data.status = "愉悦";
							list.add(data);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public class MusicData{
		//路径
		public String filePath;
		//名称
		public String fileName;
		//标识
		public String status;
		//是否选中
		public boolean isCheck = false;
	}
}










