package com.mobnote.golukmain.videosuqare;

import android.content.Context;  
import android.graphics.Canvas;  
import android.graphics.Color;
import android.graphics.Paint;  
import android.graphics.RectF;
import android.util.AttributeSet;  
import android.view.View;  
  
public class RingView extends View {  
    private final  Paint paint;  
    private int mProcess=0;
      
    public RingView(Context context) {  
        this(context, null);  
    }  
  
    public RingView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        this.paint = new Paint();  
        this.paint.setAntiAlias(true); //消除锯齿  
    }  
  
    @Override  
    protected void onDraw(Canvas canvas) {  
        int center = getWidth()/2;  
        int width = getWidth();
        //绘制外圆  
        this.paint.setColor(Color.argb(0x7f, 0x00, 0x00, 0x00));
        canvas.drawCircle(center,center, center, this.paint);  
        
        //绘制扇形
        this.paint.setColor(Color.argb(0x7f, 0xff, 0xff, 0xff));
        RectF oval2 = new RectF(0, 0, width, width);
        canvas.drawArc(oval2, 270, mProcess, true, paint);  
        
        super.onDraw(canvas);  
    }  
    
    /**
     * 更新下载进度
     * @param process　0~100
     * @author xuhw
     * @date 2015年4月16日
     */
    public void setProcess(int process){
    	if(process < 0){
    		process = 0;
    	}else if(process >= 100){
    		process = 100;
    	}
    	mProcess = (int)(process*3.6f);
    	this.invalidate();
    }
}  