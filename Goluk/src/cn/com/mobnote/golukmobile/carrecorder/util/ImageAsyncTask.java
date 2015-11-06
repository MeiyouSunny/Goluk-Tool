package cn.com.mobnote.golukmobile.carrecorder.util;

import cn.com.mobnote.golukmobile.carrecorder.util.ImageAsyncTask.ICallBack;
import cn.com.tiros.debug.GolukDebugUtils;
import android.graphics.Bitmap;
import android.os.AsyncTask;

public class ImageAsyncTask {
	
	public static void getBitmapForCache(String path, ICallBack mICallBack) {
		DownloadAsyncTask task = new DownloadAsyncTask(mICallBack);
		task.execute(path);
	}
	
	public interface ICallBack {
		public void SuccessCallback(String url, Bitmap mBitmap);
	}
	
}

class DownloadAsyncTask extends AsyncTask<String, String, Bitmap>{
	private String path = null;
	private ICallBack mICallBack = null;
	
	public DownloadAsyncTask(ICallBack _mICallBack) {
		this.mICallBack = _mICallBack;
	}

	@Override
	protected Bitmap doInBackground(String... arg0) {
		path = arg0[0];
		return ImageManager.getBitmapFromCacheEx(path, 194, 109);
	}
	
	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		if (null != mICallBack) {
			mICallBack.SuccessCallback(path, result);
		}
	}
}
