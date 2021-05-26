package com.mobnote.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;

public class SettingImageView {
	
	public  int PHOTO_REQUEST_CODE = 5000;
	public int CAMERA_QUQUEST_CODE = 6000;
	public  int CANCELED_CODE = 4000;
	
	
	public   Uri mCameraUri;
	
	public  Context mContext = null;
	
	public SettingImageView(Context context){
		mContext = context;
	}
	
	public boolean getCamera() {
		PackageManager packageManager = mContext.getPackageManager();
		if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return false;
		}
		try {
			String name = "headimage" + System.currentTimeMillis();
			ContentValues cv = new ContentValues();
			cv.put(MediaStore.Images.Media.TITLE, name);
			cv.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".jpeg");
			cv.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
			mCameraUri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

			Intent it = new Intent();
			it.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			it.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public  void deleteUri() {
		if (mCameraUri != null)
		mContext.getContentResolver().delete(mCameraUri, null, null);
	}
	
	
	/**
	 * 转换图片成圆形
	 * 
	 * @param bitmap
	 *            传入Bitmap对象
	 * @return
	 */
	public static Bitmap toRoundBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;

			left = 0;
			top = 0;
			right = width;
			bottom = width;

			height = width;

			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;

			float clip = (width - height) / 2;

			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;

			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}

		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);

		paint.setAntiAlias(true);// 设置画笔无锯齿

		canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas

		// 以下有两种方法画圆,drawRounRect和drawCircle
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);// 画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。
		// canvas.drawCircle(roundPx, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));// 设置两张图片相交时的模式,参考http://trylovecatch.iteye.com/blog/1189452
		canvas.drawBitmap(bitmap, src, dst, paint); // 以Mode.SRC_IN模式合并bitmap和已经draw了的Circle

		return output;
	}
}
