package cn.com.mobnote.golukmobile.photoalbum;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.util.EncodingUtils;

public class FileInfoManagerUtils {
	
	/**
	 * 根据文件名计算日期
	 * @param name
	 * @return
	 */
	public static String countFileDateToString(String name){
		//判断视频类别,WND1_,URG1_文件已这种格式开头为 8s/紧急
		String[] names = name.split("_");
		//获取文件最后修改时间
		//URG1_150128133420_0016.mp4,
		String date = "";
		String dateString = "";
		if(names.length > 1){
			try{
				date = names[1];
				String year = "20" + date.substring(0,2);
				String mouth = date.substring(2,4);
				String day = date.substring(4,6);
				String hour = date.substring(6,8);
				String minute = date.substring(8,10);
				//String second = date.substring(10, 12);
				dateString = year + "-" + mouth + "-" + day + " " + hour + ":" + minute;
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		return dateString;
	}
	
	/**
	 * 根据文件的最后修改时间进行排序
	 * @param files
	 * @return
	 */
	public static List<String> sortFile(List<String> files){
		String file = "";
		if(files != null && files.size() > 0){
			for(int i = 0; i < files.size(); i++) {
				for(int j = i + 1; j <= files.size() - 1; j++) {
					String fileI = files.get(i);
					String fileJ = files.get(j);
					long timeI = Long.valueOf(fileI.substring(5, 17));
					long timeJ = Long.valueOf(fileJ.substring(5, 17));
					if(timeI > timeJ){
						file = files.get(i);
						files.set(i, files.get(j));
						files.set(j, file);
					}
				}
			}
		}
		return files;
	}
	
	/**
	 * 读取本地视频配置文件
	 * @return
	 */
	public static List<String> getVideoConfigFile(String path) {
		List<String> data = new ArrayList<String>();
		
		File file=new File(path);
		if (file.exists()) {
				String str="";
				try {
					FileInputStream fin = new FileInputStream(path);
					int length = fin.available();
					byte[] buffer = new byte[length];
					fin.read(buffer);
					str = EncodingUtils.getString(buffer, "UTF-8");
					fin.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				String[] files = str.split(",");
				
				//去重
				for (String f : files) {
					if (!data.contains(f)) {
						data.add(f);
					}
				}
			return data;
		}else{
			return null;
		}
		
	}
	
	/**
	 * 获取文件大小
	 * @param f
	 * @return
	 */
	@SuppressWarnings("resource")
	public static String getFileSize(File f){
		//获取文件大小
		FileInputStream fis = null;
		String size = "";
		try {
			fis = new FileInputStream(f);
			int fileLen = fis.available();
			size = String.format("%.1f", fileLen / 1024.f / 1024.f) + "MB";
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return size;
	}


}
