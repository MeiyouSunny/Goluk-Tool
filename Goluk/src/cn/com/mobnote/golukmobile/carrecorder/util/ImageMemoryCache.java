package cn.com.mobnote.golukmobile.carrecorder.util;

import android.graphics.Bitmap;
import android.util.LruCache;

public class ImageMemoryCache {
	private volatile static ImageMemoryCache instance=null;
	private LruCache<String, Bitmap> mMemoryCache = null;
	
	public static ImageMemoryCache getInstance() { 
		if (null == instance){
			synchronized (ImageMemoryCache.class) { 
				if (null == instance){
					instance = new ImageMemoryCache();
				}
			}
		}
		return instance;  
	}
	
	public ImageMemoryCache(){
		int maxSize = (int)(Runtime.getRuntime().maxMemory()/10);
		mMemoryCache = new LruCache<String, Bitmap>(maxSize){  
		    @Override  
		    protected int sizeOf(String key, Bitmap bitmap) {  
		    	if (bitmap == null) {
		    		return 0;
		    	}
		    	return bitmap.getRowBytes() * bitmap.getHeight();
		    }  
		};  
	}
	
	public synchronized void addMemoryCache(String url) {
		Bitmap mBitmap = mMemoryCache.get(url);
		if (null == mBitmap) {
			mMemoryCache.put(url, mBitmap);
		}
	}
	
	public synchronized void removeMemoryCache(String url) {
		Bitmap mBitmap = mMemoryCache.remove(url);
		if (null != mBitmap) {
			if (!mBitmap.isRecycled()) {
				mBitmap.recycle();
				mBitmap = null;
			}
		}
	}
	
	public synchronized boolean checkKeyExits(String url) {
		Bitmap mBitmap = mMemoryCache.get(url);
		if (null != mBitmap) {
			return true;
		}
		return false;
	}
	
}
