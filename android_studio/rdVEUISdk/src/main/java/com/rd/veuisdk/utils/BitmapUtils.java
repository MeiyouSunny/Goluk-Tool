package com.rd.veuisdk.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

public class BitmapUtils {

	private BitmapUtils() {

	}

	/**
	 * 获取一个指定大小的bitmap
	 * 
	 * @param bit
	 * @param w
	 *            指定宽
	 * @param h
	 *            指定高
	 * @return
	 */
	public static Bitmap getbitmap(Bitmap bit, int w, int h) {
		return Bitmap.createScaledBitmap(bit, w, h, true);
	}

	/**
	 * 将R.drawable.*转换成Bitmap
	 * 
	 * @param resId
	 * @return
	 */
	public static Bitmap decodeBitmap(Context c, int resId) {
		return BitmapFactory.decodeResource(c.getResources(), resId);
	}

	/**
	 * 截取图片的中间的dstWidth*dstHeight的区域
	 * 
	 * @param bm
	 * @return
	 */
	public static Bitmap cropCenter(Bitmap bm, int dstWidth, int dstHeight) {
		int startWidth = (bm.getWidth() - dstWidth) / 2;
		int startHeight = ((bm.getHeight() - dstHeight) / 2);
		Rect src = new Rect(startWidth, startHeight, startWidth + dstWidth,
				startHeight + dstHeight);
		return dividePart(bm, src);
	}

	/**
	 * 获取空图片的大小
	 * 
	 * @param path
	 * @return
	 */
	public static int[] getBitmapSize(String path) {

		Options opts = new Options();
		opts.inJustDecodeBounds = true; // 只解析图片大小
		BitmapFactory.decodeFile(path, opts);
		int[] wh = new int[2];
		wh[0] = opts.outWidth;
		wh[1] = opts.outHeight;
		return wh;

	}

	/**
	 * 剪切图片
	 * 
	 * @param bmp
	 *            被剪切的图片
	 * @param src
	 *            剪切的位置
	 * @return 剪切后的图片
	 */
	public static Bitmap dividePart(Bitmap bmp, Rect src) {
		int width = src.width();
		int height = src.height();
		Rect des = new Rect(0, 0, width, height);
		Bitmap croppedImage = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(croppedImage);
		canvas.drawBitmap(bmp, src, des, null);
		bmp.recycle();
		bmp = null;
		return croppedImage;
	}

	/**
	 * 加载大图片常用方式 按比例缩小，无法直接拉伸
	 * 
	 * @param path
	 *            图片路径
	 * @param maxx
	 *            图片最大宽
	 * @param maxy
	 *            图片最大高
	 * @return
	 */

	public static Bitmap getbigImage(String path, int maxx, int maxy) {
		Options opts = new Options();
		opts.inJustDecodeBounds = true; // 只解析图片大小
		BitmapFactory.decodeFile(path, opts);
		double mw = opts.outWidth;
		double mh = opts.outHeight;
		int a = 1;
		if (maxx > mw && maxy > mh)
			a = 1;
		else {
			int aw = (int) (mw / maxx);
			int ah = (int) mh / maxy;
			a = aw > ah ? ah : aw;
		}
		opts.inJustDecodeBounds = false;
		opts.inSampleSize = a;
		return BitmapFactory.decodeFile(path, opts);
	}

	/**
	 * 获取指定大小的缩略图
	 * 
	 * @param bitmap
	 * @param w
	 * @param h
	 * @return
	 */

	public static Bitmap getThumbnail(Bitmap bitmap, int w, int h) {
		// 获取缩略图
		return ThumbnailUtils.extractThumbnail(bitmap, w, h);
	}

	/**
	 * 把两张图合并成一张图
	 * 
	 * @param background
	 *            底图
	 * @param foreground
	 *            上层的图
	 * @return
	 */

	public static Bitmap toConformBitmap(Bitmap background, Bitmap foreground) {
		if (background == null) {
			return null;
		}

		int bgWidth = background.getWidth();
		int bgHeight = background.getHeight();
		// create the new blank bitmap 创建一个新的和SRC长度宽度一样的位图
		Bitmap newbmp = Bitmap
				.createBitmap(bgWidth, bgHeight, Config.ARGB_8888);
		Canvas cv = new Canvas(newbmp);
		// draw bg into
		cv.drawBitmap(background, 0, 0, null);// 在 0，0坐标开始画入bg
		// draw fg into
		cv.drawBitmap(foreground, 0, 0, null);// 在 0，0坐标开始画入fg ，可以从任意位置画入
		// save all clip
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存
		// store
		cv.restore();// 存储
		return newbmp;
	}

	/**
	 * 根据传入的路径返回一个指定大小的图片
	 * 
	 * @param path
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap getBitmap(String path, int width, int height) {

		int[] bsize = getBitmapSize(path);
		Bitmap b;
		if (bsize[0] > width && bsize[1] > height) { // 防止图片太大。直接decodefile，内存不足
			b = getbigImage(path, width, height);
		} else {
			b = BitmapFactory.decodeFile(path);
		}
		b = getScaleBitmap(b, width, height);
		return getBitmap(b, width, height);
	}

	/**
	 * 讲一个bitmap等比缩放到指定高度
	 * 
	 * @param b
	 * @param width
	 * @param height
	 * @return
	 */

	public static Bitmap getBitmap(Bitmap b, int width, int height) {

		if (b.getWidth() != width) {
			return getZoomBitmap(b, width, height);// 缩放图片到等比大小
		} else {
			return b;
		}
	}

	/**
	 * 根据传入的宽/高，从当前bitmap中截取出一个等比例的bitmap
	 * 
	 * @param b
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap getScaleBitmap(Bitmap b, int width, int height) {
		// 第一步 按比例裁剪
		double msize = (width + 0.0) / height;
		double currensize = (b.getWidth() + 0.0) / (b.getHeight() + 0.0);
		// 宽/高 >msize(16/9);
		if (currensize > msize) {
			// 裁剪宽，，高不变
			int mw = (int) (b.getHeight() * msize);
			b = cropCenter(b, mw, b.getHeight());
		} else if (currensize < msize) {
			// 宽不变，裁剪高
			int mh = (b.getWidth() * height) / width;
			b = cropCenter(b, b.getWidth(), mh);
		}
		// 第二步 缩放到目标大小
		return getBitmap(b, width, height);

	}

	public static void getThumbNail(int width, int height, int[] size) {
		double f = (width + 0.0) / height;
		double ftar = 16.0 / 9;
		int tarh = height;
		if (f < ftar) // ver 宽不变，裁剪高
		{
			tarh = (int) (width / ftar);
		} else {
			// 裁剪宽，，高不变
		}
		// scanl to ;
		double scale = (width + 0.0) / ThumbNailUtils.THUMB_WIDTH;
		// double th=
		size[0] = ThumbNailUtils.THUMB_WIDTH;
		size[1] = (int) (tarh / scale);
	}

	/**
	 * 缩放bitmap到指定的大小 ,原图宽高比例/与传入的width:height 比例可不同
	 * 
	 * @param bitmap
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap getZoomBitmap(Bitmap bitmap, int width, int height) {
		double sw = (width + 0.0) / bitmap.getWidth();
		double sh = (height + 0.0) / bitmap.getHeight();
		/* 设置宽高各自应放大缩小的比例 */
		float scaleWidth = (float) (1 * sw);
		float scaleHeight = (float) (1 * sh);
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap temp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		bitmap.recycle();
		bitmap = null;
		return temp;
	}

	/**
	 * 获取iamgeview的图片
	 * 
	 * @param mView
	 * @return
	 */
	public static Bitmap getbitmap(ImageView mView) {
		mView.setDrawingCacheEnabled(true);
		Bitmap mBitmap = mView.getDrawingCache();
		mView.setDrawingCacheEnabled(false);
		return mBitmap;
	}

	/**
	 * 保存控件内容
	 * 
	 * @param mView
	 * @param path
	 */
	public static void savebitmapbyview(View mView, String path) {
		mView.setDrawingCacheEnabled(true);
		Bitmap mBitmap = mView.getDrawingCache();
		File file = new File(path);
		if (file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileOutputStream out = new FileOutputStream(file);
			mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mView.setDrawingCacheEnabled(false);
	}

	/**
	 * 保存图片
	 * 
	 * @param b
	 *            头像bitmap
	 * @param path
	 *            文件目标路径
	 * @param isPng
	 *            是否保存为png
	 */
	public static void saveBitmapToFile(Bitmap b, String path, boolean isPng) {
		if (TextUtils.isEmpty(path)) {
			return;
		}
		File file = new File(path);
		if (file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (isPng) {
				b.compress(Bitmap.CompressFormat.PNG, 98, out);
			} else {
				b.compress(Bitmap.CompressFormat.JPEG, 90, out);
			}
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveBitmapToFile(Bitmap b, String path) {
		saveBitmapToFile(b, path, false);
	}

	/**
	 * 设置位图旋转角度
	 * 
	 * @param b
	 * @param rotate
	 * @return
	 */
	public static Bitmap setBitmapRotate(Bitmap b, int rotate) {
		if (rotate != 0 && b != null) {
			Matrix m = new Matrix();
			m.postRotate(rotate);
			try {
				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
						b.getHeight(), m, true);
				if (b != b2) {
					b.recycle();
					b = b2;
				}
			} catch (OutOfMemoryError ex) {
			}
		}
		return b;
	}

	public static Bitmap drawableToBitmap(Drawable drawable, int width,
			int height) {

		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888

		: Bitmap.Config.RGB_565);

		Canvas canvas = new Canvas(bitmap);

		drawable.setBounds(0, 0, width, height);

		drawable.draw(canvas);

		return bitmap;

	}

	/**
	 * 保存bitmap 到本地路径
	 * 
	 * @param bitmap
	 * @param path
	 * @return
	 */
	public static String onSaveImage(Bitmap bitmap, String path) {

		FileOutputStream os;
		try {
			os = new FileOutputStream(path);
			bitmap.compress(CompressFormat.JPEG, 80, os);
			try {
				os.close();
				bitmap.recycle();
				bitmap = null;
				return path;
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		bitmap.recycle();
		bitmap = null;
		return null;
	}

	/**
	 * 获取视频的截图
	 * 
	 * @param videopPath
	 * @param nTime
	 *            ms
	 * @return
	 */
	public static Bitmap createVideoShot(String videopPath, long nTime) {
		Bitmap bitmap = null;
		android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();

		try {// MODE_CAPTURE_FRAME_ONLY
			retriever.setDataSource(videopPath);
			Bitmap tempbitmap = retriever.getFrameAtTime(1000 * nTime,
					MediaMetadataRetriever.OPTION_PREVIOUS_SYNC);
			bitmap = BitmapUtils.getScaleBitmap(tempbitmap,
					ThumbNailUtils.THUMB_WIDTH, ThumbNailUtils.THUMB_HEIGHT);
			tempbitmap.recycle();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				retriever.release();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return bitmap;
	}

	public static Bitmap copyBmp(Bitmap src, int outwidth, int outHeight) {
		if (null != src && !src.isRecycled()) {
			Bitmap dst = Bitmap.createBitmap(outwidth, outHeight,
					Config.ARGB_8888);
			Canvas cv = new Canvas(dst);
			cv.drawBitmap(src, null, new Rect(0, 0, outwidth, outHeight),
					new Paint());

			// cv.drawBitmap(src, 0, 0, null);
			cv.save();

			return dst;
		}

		return null;

	}

}
