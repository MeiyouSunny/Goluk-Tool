package com.rd.veuisdk.utils.apng;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.facebook.common.logging.FLog;
import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.rd.veuisdk.model.ApngInfo;
import com.rd.veuisdk.utils.ApngExtractFrames;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.apng.assist.ApngListener;
import com.rd.veuisdk.utils.apng.assist.AssistUtil;
import com.rd.veuisdk.utils.apng.assist.PngImageLoader;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ar.com.hjg.pngj.chunks.PngChunkFCTL;

/**
 * Reference: http://www.vogella.com/code/com.vogella.android.drawables.animation/src/com/vogella/android/drawables/animation/ColorAnimationDrawable.html
 */
public class ApngDrawable extends Drawable implements Animatable, Runnable {

	private static final float DELAY_FACTOR = 1000F;
	private final Uri sourceUri;

	private ArrayList<PngChunkFCTL> fctlArrayList = new ArrayList<>();
	private ArrayList<String> frameList = new ArrayList<>();
	private PngImageLoader imageLoader;
	private Paint paint;
	private String workingPath;

	private boolean isPrepared = false;
	private boolean isRunning = false;

	private int currentFrame;
	private int currentLoop;
	private int numFrames;
	private int numPlays;
	private boolean showLastFrameOnStop;
	private Bitmap baseBitmap;
	private float mScaling;
	private File baseFile;

	private ApngListener apngListener;

	public ApngDrawable(Context context, Bitmap bitmap, Uri uri) {
		super();
		currentFrame = -1;
		currentLoop = 0;
		mScaling = 0F;
		paint = new Paint();
		paint.setAntiAlias(true);
		baseBitmap = bitmap;

		File workingDir = AssistUtil.getWorkingDir(context);
		workingPath = workingDir.getPath();
		sourceUri = uri;
		imageLoader = PngImageLoader.getInstance();
	}

	public static ApngDrawable getFromView(View view) {
		if (view == null || !(view instanceof ImageView)) return null;
		Drawable drawable = ((ImageView) view).getDrawable();
		if (drawable == null || !(drawable instanceof ApngDrawable)) return null;
		return (ApngDrawable) drawable;
	}

	/**
	 * Specify an event listener for this APNG
	 * @param apngListener new listener instance
	 */
	public void setApngListener(ApngListener apngListener) {
		this.apngListener = apngListener;
	}

	/**
	 * Specify number of repeating. Note that this will override the value described in APNG file
	 * @param numPlays Number of repeating
	 */
	public void setNumPlays(int numPlays) {
		this.numPlays = numPlays;
	}

	/**
	 * Specify if, on animation end, will be showing the last frame instead of the first
	 * @param showLastFrameOnStop true if you want to show the last frame on stop
	 */
	public void setShowLastFrameOnStop(boolean showLastFrameOnStop) {
		this.showLastFrameOnStop = showLastFrameOnStop;
	}

	@Override
	public void start() {
		if (!isRunning()) {
			isRunning = true;
			currentFrame = 0;
			if (!isPrepared) {
				prepare();
			}
			if (isPrepared) {
				run();
				if (apngListener != null)
				    apngListener.onAnimationStart(this);
			} else {
				stop();
			}
		}
	}

	@Override
	public void stop() {
		if (isRunning()) {
			currentLoop = 0;
			unscheduleSelf(this);
			isRunning = false;
			if (apngListener != null) apngListener.onAnimationEnd(this);
		}
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public void run() {
		if (showLastFrameOnStop && numPlays > 0 && currentLoop >= numPlays) {
			stop();
			return;
		}
		if (fctlArrayList.size() > 0) {
			if (currentFrame < 0) {
				currentFrame = 0;
			} else if (currentFrame >= fctlArrayList.size()) {
				currentFrame = 0;
			}
			PngChunkFCTL pngChunk = fctlArrayList.get(currentFrame);
			int delayNum = pngChunk.getDelayNum();
			int delayDen = pngChunk.getDelayDen();
			int delay = Math.round(delayNum * DELAY_FACTOR / delayDen);
			scheduleSelf(this, SystemClock.uptimeMillis() + delay);
		}
		invalidateSelf();
	}

	@Override
	public void draw(Canvas canvas) {
		if (fctlArrayList.size() <= 0) {
			drawBaseBitmap(canvas);
		} else {
			if (currentFrame < fctlArrayList.size() && currentFrame >= 0) {
				drawAnimateBitmap(canvas, currentFrame);
			}
			if (!showLastFrameOnStop && numPlays > 0 && currentLoop >= numPlays) {
				stop();
			}
			if (numPlays > 0 && currentFrame == numFrames - 1) {
				currentLoop++;
				if (apngListener != null) apngListener.onAnimationRepeat(this);
			}
			currentFrame++;
		}
	}

	@Override
	public void setAlpha(int alpha) {
		paint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		paint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	//绘制
	private void drawAnimateBitmap(Canvas canvas, int frameIndex) {
		Bitmap bitmap = getCacheBitmap(frameIndex);
		if (bitmap == null) {
			bitmap = BitmapFactory.decodeFile(frameList.get(frameIndex));
			cacheBitmap(frameIndex, bitmap);
		}
		if (bitmap == null) return;
		if (mScaling == 0F) {
			float scalingByWidth = ((float) canvas.getWidth())/ bitmap.getWidth();
			float scalingByHeight = ((float) canvas.getHeight())/ bitmap.getHeight();
			mScaling = scalingByWidth <= scalingByHeight ? scalingByWidth : scalingByHeight;
		}
		RectF dst = new RectF(
				0, 0,
				mScaling * bitmap.getWidth(),
				mScaling * bitmap.getHeight());
		canvas.drawBitmap(bitmap, null, dst, paint);
	}

	//解析失败 绘制base
	private void drawBaseBitmap(Canvas canvas) {
		RectF dst = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
		canvas.drawBitmap(baseBitmap, null, dst, paint);
		cacheBitmap(0, baseBitmap);
	}

	private void prepare() {
		String imagePath = getImagePathFromUri();
		if (imagePath == null)
		    return;
		baseFile = new File(imagePath);
		if (!baseFile.exists()) {
			return;
		}
		frameList.clear();
		fctlArrayList.clear();
		if (ApngExtractFrames.process(baseFile) > 0) {
			//成功
			String name = baseFile.getName();
			ApngInfo apng = ApngInfo.createApng(baseFile, name.substring(0,  name.lastIndexOf(".")));
			fctlArrayList.addAll(apng.getFctlArrayList());
			frameList.addAll(apng.getFrameList());
			numFrames = frameList.size();
		}
		isPrepared = true;
	}

	private String getImagePathFromUri() {
		if (sourceUri == null) return null;
		String imagePath = null;
		try {
			String filename = sourceUri.getLastPathSegment();
			File file = new File(workingPath, filename);
			if (!file.exists()) {
				FileUtils.copyFile(new File(sourceUri.getPath()), file);
			}
			imagePath = file.getPath();
		} catch (Exception e) {
			FLog.e("Error: %s", e.toString());
		}
		return imagePath;
	}

	private String getCacheKey(int frameIndex) {
		return String.format("%s_%s", sourceUri.toString(), frameIndex);
	}

	private void cacheBitmap(int frameIndex, Bitmap bitmap) {
		if (bitmap == null) return;
		MemoryCache memoryCache = imageLoader == null ? null : imageLoader.getMemoryCache();
		if (memoryCache == null) return;
		memoryCache.put(getCacheKey(frameIndex), bitmap);
	}

	private Bitmap getCacheBitmap(int frameIndex) {
		MemoryCache memoryCache = imageLoader == null ? null : imageLoader.getMemoryCache();
		if (memoryCache == null) return null;
		return memoryCache.get(getCacheKey(frameIndex));
	}

}
