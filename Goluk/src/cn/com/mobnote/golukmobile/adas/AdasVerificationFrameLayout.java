package cn.com.mobnote.golukmobile.adas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class AdasVerificationFrameLayout extends FrameLayout {
	private Paint mInnerPaint;
	private Rect mValidRect;
	private Point mPoint = new Point();
	private boolean mInTouchMode = false;
	/**左移十字中心*/
	public static final int LEFT = 0;
	/**右移十字中心*/
	public static final int RIGHT = 1;
	/**上移十字中心*/
	public static final int UP = 2;
	/**下移十字中心*/
	public static final int DOWN = 3;
	/**移动步进*/
	private static final int UNIT_STEP = 6;
	
	private int mWidth = 0;
	private int mHeight = 0;
	
	private int mRawX = 0;
	private int mRawY = 0;		
	public AdasVerificationFrameLayout(Context context) {
		super(context);
		init();
	}

	public AdasVerificationFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		// TODO Auto-generated constructor stub
	}

	public AdasVerificationFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		mValidRect = new Rect(23*w/64, h/4, 41*w/64, 5*h/8);
		if (mRawX == 0) {
			mPoint.set(w / 2, h / 2);
		} else {
			mPoint.set(mRawX*w/1920, mRawY*h/1080);
		}
		mWidth = w;
		mHeight = h;
	}

	private void init() {
		mInnerPaint = new Paint();
		mInnerPaint.setAntiAlias(true);
		mInnerPaint.setColor(Color.GREEN);
		mInnerPaint.setStyle(Paint.Style.STROKE);
		mInnerPaint.setStrokeWidth(4);
		mInnerPaint.setStrokeJoin(Paint.Join.ROUND);
		PathEffect effects = new DashPathEffect(new float[]{5,5,5,5},1);
		mInnerPaint.setPathEffect(effects);
		setWillNotDraw(false);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if (mInTouchMode && mInnerPaint != null) {
			Path path = new Path();
			path.moveTo(mPoint.x, 0);
			path.lineTo(mPoint.x, getHeight());
			path.moveTo(0, mPoint.y);
			path.lineTo(getWidth(), mPoint.y);
			canvas.drawPath(path, mInnerPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		float x = event.getX();
		float y = event.getY();
		switch(event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if (mInTouchMode && mValidRect.contains((int)x, (int)y)) {
				mPoint.set((int)x, (int)y);
				invalidate();
			}
			break;
		}
		return true;
	}

	public void setTouchMode(boolean inTouchMode) {
		mInTouchMode = inTouchMode;
	}

	public void setMoving(int direct) {
		int x = mPoint.x;
		int y = mPoint.y;
		switch (direct) {
		case LEFT:
			x = mPoint.x - UNIT_STEP;
			break;
		case RIGHT:
			x = mPoint.x + UNIT_STEP;
			break;
		case UP:
			y = mPoint.y - UNIT_STEP;
			break;
		case DOWN:
			y = mPoint.y + UNIT_STEP;
			break;
		}
		if (mValidRect.contains((int)x, (int)y)) {
			mPoint.set(x, y);
			invalidate();
		}
	}

	public Point getLocation() {
		if (mWidth == 0 || mHeight == 0) {
			return null;
		}
		int x = mPoint.x * 1920 / mWidth;
		int y = mPoint.y * 1080 / mHeight;
		Point point = new Point(x, y);
		return point;
	}

	public void setLocation(int x, int y) {
		mRawX = x;
		mRawY = y;
	}
}
