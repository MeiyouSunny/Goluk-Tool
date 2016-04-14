package com.mobnote.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

public class ClipImageView extends ImageView implements View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener {

	public static final float DEFAULT_MAX_SCALE = 4.0f;
	public static final float DEFAULT_MID_SCALE = 2.0f;
	public static final float DEFAULT_MIN_SCALE = 1.0f;

	private float minScale = DEFAULT_MIN_SCALE;
	private float midScale = DEFAULT_MID_SCALE;
	private float maxScale = DEFAULT_MAX_SCALE;

	private MultiGestureDetector multiGestureDetector;

	private int borderlength;

	private boolean isJusted;
	
	public Context mContext = null;

	private final Matrix baseMatrix = new Matrix();
	private final Matrix drawMatrix = new Matrix();
	private final Matrix suppMatrix = new Matrix();
	private final RectF displayRect = new RectF();
	private final float[] matrixValues = new float[9];

	public ClipImageView(Context context) {
		this(context, null);
	}

	public ClipImageView(Context context, AttributeSet attr) {
		this(context, attr, 0);
	}

	public ClipImageView(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);

		super.setScaleType(ScaleType.MATRIX);

		setOnTouchListener(this);

		mContext = context;

		multiGestureDetector = new MultiGestureDetector(context);

	}

	private void configPosition() {
		super.setScaleType(ScaleType.MATRIX);
		Drawable d = getDrawable();
		if (d == null) {
			return;
		}
		
		DisplayMetrics metric = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels; // 屏幕宽度（像素）
		int height = metric.heightPixels; // 屏幕高度（像素）
		float mDensity = metric.density; // 屏幕密度（0.75 / 1.0 / 1.5）
		int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
		
		if (width > height)
		{
			int d1 = height;
			height = width;
			width = d1;
		}
		
		final float viewWidth = width;//getWidth();
		final float viewHeight = height - (180* mDensity);//getHeight();
		final int drawableWidth = d.getIntrinsicWidth();
		final int drawableHeight = d.getIntrinsicHeight();
		
//		borderlength = (int) (viewWidth - BORDERDISTANCE * 10);
		borderlength = ClipView.dip2px(mContext, 100) * 2;
		float scale = 1.0f;
		if (drawableWidth <= drawableHeight) {
			if (drawableWidth < borderlength) {
				baseMatrix.reset();
				scale = (float) borderlength / drawableWidth;
				baseMatrix.postScale(scale, scale);
			}
		} else {
			if (drawableHeight < borderlength) {
				baseMatrix.reset();
				scale = (float) borderlength / drawableHeight;
				baseMatrix.postScale(scale, scale);
			}
		}
		baseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2, (viewHeight - drawableHeight * scale) / 2);

		resetMatrix();
		isJusted = true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return multiGestureDetector.onTouchEvent(event);
	}

	private class MultiGestureDetector extends GestureDetector.SimpleOnGestureListener implements
			OnScaleGestureListener {

		private final ScaleGestureDetector scaleGestureDetector;
		private final GestureDetector gestureDetector;
		private final float scaledTouchSlop;

		private VelocityTracker velocityTracker;
		private boolean isDragging;

		private float lastTouchX;
		private float lastTouchY;
		private float lastPointerCount;

		public MultiGestureDetector(Context context) {
			scaleGestureDetector = new ScaleGestureDetector(context, this);

			gestureDetector = new GestureDetector(context, this);
			gestureDetector.setOnDoubleTapListener(this);

			final ViewConfiguration configuration = ViewConfiguration.get(context);
			scaledTouchSlop = configuration.getScaledTouchSlop();
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float scale = getScale();
			float scaleFactor = detector.getScaleFactor();
			if (getDrawable() != null
					&& ((scale < maxScale && scaleFactor > 1.0f) || (scale > minScale && scaleFactor < 1.0f))) {
				if (scaleFactor * scale < minScale) {
					scaleFactor = minScale / scale;
				}
				if (scaleFactor * scale > maxScale) {
					scaleFactor = maxScale / scale;
				}
				suppMatrix.postScale(scaleFactor, scaleFactor, getWidth() / 2, getHeight() / 2);
				checkAndDisplayMatrix();
			}
			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
		}

		public boolean onTouchEvent(MotionEvent event) {
			if (gestureDetector.onTouchEvent(event)) {
				return true;
			}

			scaleGestureDetector.onTouchEvent(event);

			/*
			 * Get the center x, y of all the pointers
			 */
			float x = 0, y = 0;
			final int pointerCount = event.getPointerCount();
			for (int i = 0; i < pointerCount; i++) {
				x += event.getX(i);
				y += event.getY(i);
			}
			x = x / pointerCount;
			y = y / pointerCount;

			/*
			 * If the pointer count has changed cancel the drag
			 */
			if (pointerCount != lastPointerCount) {
				isDragging = false;
				if (velocityTracker != null) {
					velocityTracker.clear();
				}
				lastTouchX = x;
				lastTouchY = y;
			}
			lastPointerCount = pointerCount;

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (velocityTracker == null) {
					velocityTracker = VelocityTracker.obtain();
				} else {
					velocityTracker.clear();
				}
				velocityTracker.addMovement(event);

				lastTouchX = x;
				lastTouchY = y;
				isDragging = false;
				break;

			case MotionEvent.ACTION_MOVE: {
				final float dx = x - lastTouchX, dy = y - lastTouchY;

				if (isDragging == false) {
					// Use Pythagoras to see if drag length is larger than
					// touch slop
					isDragging = Math.sqrt((dx * dx) + (dy * dy)) >= scaledTouchSlop;
				}

				if (isDragging) {
					if (getDrawable() != null) {
						suppMatrix.postTranslate(dx, dy);
						checkAndDisplayMatrix();
					}

					lastTouchX = x;
					lastTouchY = y;

					if (velocityTracker != null) {
						velocityTracker.addMovement(event);
					}
				}
				break;
			}
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				lastPointerCount = 0;
				if (velocityTracker != null) {
					velocityTracker.recycle();
					velocityTracker = null;
				}
				break;
			}

			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent event) {
			try {
				float scale = getScale();
				float x = getWidth() / 2;
				float y = getHeight() / 2;

				if (scale < midScale) {
					post(new AnimatedZoomRunnable(scale, midScale, x, y));
				} else if ((scale >= midScale) && (scale < maxScale)) {
					post(new AnimatedZoomRunnable(scale, maxScale, x, y));
				} else {
					post(new AnimatedZoomRunnable(scale, minScale, x, y));
				}
			} catch (Exception e) {
				// Can sometimes happen when getX() and getY() is called
			}

			return true;
		}
	}

	private class AnimatedZoomRunnable implements Runnable {
		// These are 'postScale' values, means they're compounded each iteration
		static final float ANIMATION_SCALE_PER_ITERATION_IN = 1.07f;
		static final float ANIMATION_SCALE_PER_ITERATION_OUT = 0.93f;

		private final float focalX, focalY;
		private final float targetZoom;
		private final float deltaScale;

		public AnimatedZoomRunnable(final float currentZoom, final float targetZoom, final float focalX,
				final float focalY) {
			this.targetZoom = targetZoom;
			this.focalX = focalX;
			this.focalY = focalY;

			if (currentZoom < targetZoom) {
				deltaScale = ANIMATION_SCALE_PER_ITERATION_IN;
			} else {
				deltaScale = ANIMATION_SCALE_PER_ITERATION_OUT;
			}
		}

		public void run() {
			suppMatrix.postScale(deltaScale, deltaScale, focalX, focalY);
			checkAndDisplayMatrix();

			final float currentScale = getScale();

			if (((deltaScale > 1f) && (currentScale < targetZoom))
					|| ((deltaScale < 1f) && (targetZoom < currentScale))) {
				// We haven't hit our target scale yet, so post ourselves
				// again
				// postOnAnimation(ClipImageView.this, this);

			} else {
				// We've scaled past our target zoom, so calculate the
				// necessary scale so we're back at target zoom
				final float delta = targetZoom / currentScale;
				suppMatrix.postScale(delta, delta, focalX, focalY);
				checkAndDisplayMatrix();
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void postOnAnimation(View view, Runnable runnable) {
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
			view.postOnAnimation(runnable);
		} else {
			view.postDelayed(runnable, 16);
		}
	}

	/**
	 * Returns the current scale value
	 * 
	 * @return float - current scale value
	 */
	public final float getScale() {
		suppMatrix.getValues(matrixValues);
		return matrixValues[Matrix.MSCALE_X];
	}

	@Override
	public void onGlobalLayout() {
		if (isJusted) {
			return;
		}
		configPosition();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

	/**
	 * Helper method that simply checks the Matrix, and then displays the result
	 */
	private void checkAndDisplayMatrix() {
		checkMatrixBounds();
		setImageMatrix(getDisplayMatrix());
	}

	private void checkMatrixBounds() {
		final RectF rect = getDisplayRect(getDisplayMatrix());
		if (null == rect) {
			return;
		}

		float deltaX = 0, deltaY = 0;
		final float viewWidth = getWidth();
		final float viewHeight = getHeight();
		if (rect.top > (viewHeight - borderlength) / 2) {
			deltaY = (viewHeight - borderlength) / 2 - rect.top;
		}
		if (rect.bottom < (viewHeight + borderlength) / 2) {
			deltaY = (viewHeight + borderlength) / 2 - rect.bottom;
		}
		if (rect.left > (viewWidth - borderlength) / 2) {
			deltaX = (viewWidth - borderlength) / 2 - rect.left;
		}
		if (rect.right < (viewWidth + borderlength) / 2) {
			deltaX = (viewWidth + borderlength) / 2 - rect.right;
		}
		// Finally actually translate the matrix
		suppMatrix.postTranslate(deltaX, deltaY);
	}

	/**
	 * Helper method that maps the supplied Matrix to the current Drawable
	 * 
	 * @param matrix
	 *            - Matrix to map Drawable against
	 * @return RectF - Displayed Rectangle
	 */
	private RectF getDisplayRect(Matrix matrix) {
		Drawable d = getDrawable();
		if (null != d) {
			displayRect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			matrix.mapRect(displayRect);
			return displayRect;
		}

		return null;
	}

	/**
	 * Resets the Matrix back to FIT_CENTER, and then displays it.s
	 */
	private void resetMatrix() {
		if (suppMatrix == null) {
			return;
		}
		suppMatrix.reset();
		setImageMatrix(getDisplayMatrix());
	}

	protected Matrix getDisplayMatrix() {
		drawMatrix.set(baseMatrix);
		drawMatrix.postConcat(suppMatrix);
		return drawMatrix;
	}

	// 截图
	public Bitmap clip() {

		int width = this.getWidth();
		int height = this.getHeight();

		if (width <= 0 || height <= 0 || borderlength <= 0) {
			return null;
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		draw(canvas);
		return Bitmap.createBitmap(bitmap, (getWidth() - borderlength) / 2, (getHeight() - borderlength) / 2,
				borderlength, borderlength);

	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);

		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();

		// 保证是方形，并且从中心画

		int width = bitmap.getWidth();

		int height = bitmap.getHeight();

		int w;

		int deltaX = 0;

		int deltaY = 0;

		if (width <= height) {
			w = width;
			deltaY = height - w;
		} else {
			w = height;
			deltaX = width - w;

		}

		final Rect rect = new Rect(deltaX, deltaY, w, w);

		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		canvas.drawARGB(0, 0, 0, 0);
		// canvas.drawColor(Color.WHITE);

		// 圆形，所有只用一个

		int radius = (int) (Math.sqrt(w * w * 2.0d) / 2);

		canvas.drawRoundRect(rectF, radius, radius, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;

	}

}
