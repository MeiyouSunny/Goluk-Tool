package cn.com.mobnote.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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
 * @ 功能描述:日志输出,习惯了js,凑合吧
 * 
 * @author 陈宣宇
 * 
 */
public class console {
	public static String TAG = "chxy";
	/** 日志文件路径 */
	private static final String APP_FOLDER = "goluk";
	private static final String fileName = "/chxy_";
	private static final String filePath = android.os.Environment.getExternalStorageDirectory().getPath() + "/" + APP_FOLDER;


	public static void toast(String msg,Context mContext){
		Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
	}
	
	@SuppressLint("SimpleDateFormat")
	public static void print(String name,String msg) {
		FileOutputStream outputStream = null;
		try{
			File file = new File(filePath + fileName + name + ".txt");
			if(!file.exists()){
				file.createNewFile();
			}
			outputStream = new FileOutputStream(file,true);
			
			String timeFormat = "yyyy-MM-dd HH:mm:ss";
			SimpleDateFormat df = new SimpleDateFormat(timeFormat);
			String time = df.format(new Date());
			String info = time + ", " + msg + "\n";
			outputStream.write(info.getBytes());
			outputStream.flush();
			outputStream.close();
		}
		catch(IOException e){
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
}
