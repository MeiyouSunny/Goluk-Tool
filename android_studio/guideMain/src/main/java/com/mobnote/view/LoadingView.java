package com.mobnote.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import cn.com.tiros.debug.GolukDebugUtils;
/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:本地视频上传loading自定义View
 * 
 * @author 陈宣宇
 * 
 */
public class LoadingView extends View {
	private final Paint paint;
	private final Context context;
	/** 下载进度 */
	private int mProgress = 0;
	public LoadingView(Context context) {
		this(context, null);
	}
	
	public LoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		this.paint = new Paint();
		//setWillNotDraw(false);
	}
	
	/**
	 * 更新视频上传进度
	 * @param p
	 */
	public void setCurrentProgress(int p) {
		if(p >= 0){
			GolukDebugUtils.e("","onDraw-------0------" + p);
			mProgress = p;
			this.invalidate();
			//this.postInvalidate();
			//在部分机型上面会出现,不自动执行onDraw事件,需要调用一下两个函数
			this.forceLayout();
			this.requestLayout();
		}
	}
	
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		//设置画笔为无锯齿
		paint.setAntiAlias(true);
		//设置画笔颜色
		paint.setColor(Color.rgb(179,179,179));
		//线宽
		paint.setStrokeWidth((float)2.0);
		//空心效果
		paint.setStyle(Style.STROKE);
		//设置内圆半径
		int innerCircle = dip2px(context,20);
		//设置圆环宽度
		int ringWidth = innerCircle + 2;
		//绘制圆形外边线
		canvas.drawCircle(ringWidth,ringWidth,innerCircle,paint);
		//绘制原型填充色
		paint.setColor(Color.rgb(95,95,95));
		paint.setAlpha(165);
		paint.setStyle(Style.FILL);
		canvas.drawCircle(ringWidth,ringWidth,innerCircle - 1,paint);
		paint.setStyle(Style.FILL);
		paint.setColor(Color.rgb(235,235,235));
		paint.setAlpha(65);
		int arcDiameter = innerCircle * 2 + 2;
		RectF localRectF = new RectF(2,2,arcDiameter,arcDiameter);
		canvas.drawArc(localRectF,270,(float) (mProgress * 3.6),true,paint);
		paint.setTextSize(22);
		paint.setColor(Color.rgb(255,255,255));
		//不同数字x坐标不一样
		int px = 15;
		if(mProgress > 9){
			px = 12;
		}
		if(mProgress > 99){
			px = 9;
			mProgress = 100;
		}
		px = dip2px(context,px);
		int py = dip2px(context,25);
		canvas.drawText(mProgress + "%",px,py,paint);
		super.onDraw(canvas);
	}

	/** 
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}
