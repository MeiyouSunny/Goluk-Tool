package cn.com.mobnote.golukmobile.carrecorder.util;

import java.io.File;
import android.graphics.Bitmap;
import android.os.Environment;
import cn.com.mobnote.application.GolukApplication;
import com.lidroid.xutils.BitmapUtils;

public class BitmapManager {
	public BitmapUtils mBitmapUtils;
	private volatile static BitmapManager instance=null;
	
	public static BitmapManager getInstance() { 
		if (null == instance){
			synchronized (SettingUtils.class) { 
				if (null == instance){
					instance = new BitmapManager();
				}
			}
		}
		return instance;  
	}
	
	public BitmapManager(){
		String diskCachePath = Environment.getExternalStorageDirectory()
				+ File.separator + "goluk" + File.separator + "image_cache";
		mBitmapUtils = new BitmapUtils(GolukApplication.getInstance(), diskCachePath, 0.5f);
		mBitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
		mBitmapUtils.configMemoryCacheEnabled(true);
		mBitmapUtils.configThreadPoolSize(3);
		int screenwidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		int screenheight = SoundUtils.getInstance().getDisplayMetrics().heightPixels;
		mBitmapUtils.configDefaultBitmapMaxSize(screenwidth, screenheight);
	}
	
}
