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
		long max = Runtime.getRuntime().maxMemory();
		int memoryCacheSize = (int)(max/8);
		int diskCacheSize = 50*1024*1024;
		String diskCachePath = Environment.getExternalStorageDirectory()
				+ File.separator + "tiros-com-cn-ext" + File.separator + "image_cache";
		mBitmapUtils = new BitmapUtils(GolukApplication.getInstance(), diskCachePath, memoryCacheSize, diskCacheSize);
		mBitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
		mBitmapUtils.configMemoryCacheEnabled(true);
		mBitmapUtils.configThreadPoolSize(3);
		int screenwidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		int screenheight = SoundUtils.getInstance().getDisplayMetrics().heightPixels;
		mBitmapUtils.configDefaultBitmapMaxSize(screenwidth, screenheight);
	}
	
}
