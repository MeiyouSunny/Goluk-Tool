package cn.com.mobnote.golukmobile.wifimanage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import cn.com.mobnote.golukmobile.wifimanage.SocketServer;

public class FileManage {
	private String filePath = Environment.getExternalStorageDirectory()
			.getPath() + "/golukvedio/";
	private Handler hander = null;
	
	private Context mContext = null;

	public FileManage(Context context,Handler _hander) {
		mContext = context;
		hander = _hander;
	}
	
	

	/**
	 * 测试文件接入 ====================测试入口
	 * 
	 * @return
	 */
	private String[] testGetAllFiles() {
		File dirlist = new File(filePath);
		List<String> list = null;
		String[] filePath = null;
		File[] fileList = dirlist.listFiles();
		// 如果指定文件夹下文件不为null
		if (fileList != null && fileList.length > 0) {
			list = new ArrayList<String>();
			for (int i = 0; i < fileList.length; i++) {
				// 不处理非文件夹
				if (!fileList[i].isDirectory()) {
					list.add(fileList[i].getPath());
				}
			}
		}
		// 得到文件夹下文件
		if (list != null) {
			filePath = list.toArray(new String[0]);
			list = null;
		}
		return filePath;
	}
	
	
	/**添加同步指定的文件这里需要玉峰接入
	 * @param pathName  文件路径	
	 * @param vtype   1：8秒视频   2：紧急视频
	 */
	public void addSynchroFile(int vtype,String... pathNames) {
		//pathNames=changeFileNames(pathNames,vtype);
		for(String tempPath:pathNames){
			//将视频传入服务器
			SocketServer.getInstance(mContext,hander).clients.addSocketFile(tempPath);
		}
		
	 
	}
	
	
	
	
	/**
	 * 获取指定文件的文件路径 ??这里调用玉峰的方法
	 * 
	 * @return
	 */
//	private String[] getAllFiles() {
//
//		List<IVideoInfo> list = VideoUtils.getAllTypeVideos(mContext,1);
//		if (null == list || list.size() <= 0) {
//			return null;
//		}
//		int count = list.size();
//
//		String[] pathData = new String[count];
//		for (int i = count - 1; i >= 0; i--) {
//			pathData[i] = list.get(i).getVideoPath();
//		}
//
//		return pathData;
//	}
///////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 获取8秒视频需要同步的路径
	 * 
	 * @param filePathC
	 * @return
	 */
	public String[] getFiles(String[] filePathC) {
		String[] rs = null;
//		String[] comPS = null;
//		// 合包的方法
//		// String[] filePathS = this.getAllFiles();
//		// 测试的方法
//		String[] filePathS = this.getAllFiles();   //////////////////需要玉峰配置这里
//		
//		// 如果本地文件不为null才能进行比较和排序
//		if (filePathS != null) {
//			//将文件 添加8秒视频的标志
//			filePathS=changeFileNames(filePathS,1);
//			// 客户端有文件才进行比较
//			if (filePathC != null) {
//				comPS = this.comParatorFile(filePathS, filePathC);
//			} else {
//				comPS = filePathS;
//			}
//			// 不再进行排序
//			// rs=this.sortFile(comPS);
//			// 直接将差异赋值
//			rs = comPS;
//		}

		return rs;
	}

	/**
	 * 比较文件差异
	 * 
	 * @param filePathS
	 * @param filePathC
	 * @returng
	 */
	private String[] comParatorFile(String[] filePathS, String[] filePathC) {
		Map<String, String> map = new HashMap<String, String>();
		List<String> fileList = new ArrayList<String>();
		// 不为null进行比较
		if (filePathC != null) {
			for (String temPath : filePathC) {

				map.put(temPath, temPath);
			}
			for (String tempS : filePathS) {
				// 去掉路径比较
				if (!map.containsKey(filterPath(tempS)[1])) {
					fileList.add(tempS);
				}
			}
		}

		if (fileList.size() > 0) {
			map = null;
			return (String[]) fileList.toArray(new String[0]);
		}
		return null;
	}

	/**
	 * 文件排序
	 * 
	 * @param filePath
	 * @return
	 */
	/**
	 * private String[] sortFile(String[] filePath) { String[] rs = null;
	 * List<String> list = null; if (filePath != null) { list =
	 * Arrays.asList(filePath); Collections.sort(list, new Comparator<String>()
	 * {
	 * 
	 * public int compare(String s1, String s2) { File file1 = new File(dirlist
	 * + "/" + s1); File file2 = new File(dirlist + "/" + s2);
	 * 
	 * if (file1.lastModified() > file2.lastModified()) { return -1; } else {
	 * return 1; }
	 * 
	 * } }); rs = (String[]) list.toArray(new String[0]);
	 * 
	 * } list = null; return rs; }
	 **/
	/**
	 * 获取文件名
	 * 
	 * @param path
	 * @return
	 */
	private static String[] filterPath(String path) {
		String[] sz=new String[2];
		int count = path.lastIndexOf("/");
		sz[0]=path.substring(0,count);
		sz[1]=path.substring(count + 1);
		return sz;
	}
	
	/** 将文件 按照视频类型重命名
	 * @param localfiles  文件数组
	 * @param vtype 1:8秒   2:紧急
	 * @return
	 */
	public static String[] changeFileNames(String[] localfiles, int vtype) {
		String [] rs=null;
		  if(localfiles==null){
			  return rs;
		  }else{
			  List<String> list =new ArrayList<String>();
			  String[] sz=null;
			  for(String temp:localfiles){
				  sz=filterPath(temp);
				  list.add(sz[0]+"/"+vtype+"_"+sz[1]);
			  }
			  rs=(String[])list.toArray(new String[0]);
			  list=null;
		  }
		return rs;
	}
}
