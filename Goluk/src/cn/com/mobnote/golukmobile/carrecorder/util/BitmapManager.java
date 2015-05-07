package cn.com.mobnote.golukmobile.carrecorder.util;

import java.io.File;
import android.graphics.Bitmap;
import android.os.Environment;
import cn.com.mobnote.application.GolukApplication;
import com.lidroid.xutils.BitmapUtils;

public class BitmapManager {
	public BitmapUtils mBitmapUtils;
	private static BitmapManager instance=null;
	
	public static synchronized BitmapManager getInstance() { 
		if (instance==null)
			synchronized (SettingUtils.class) { 
				instance = new BitmapManager();
			}
			return instance;  
	}
	
	public BitmapManager(){
		String imagecache = Environment.getExternalStorageDirectory()
				+ File.separator + "tiros-com-cn-ext" + File.separator + "image_cache";
		mBitmapUtils = new BitmapUtils(GolukApplication.getInstance(), imagecache);
		mBitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
	}
	
	

}
