package com.mobnote.golukmain.carrecorder.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class CustomVideoView extends VideoView {
	
	private int videoWidth;
	private int videoHeight;

	public CustomVideoView(Context context) {
        super(context);
	}
	
    public CustomVideoView(Context context, AttributeSet attrs) {
            super(context, attrs);
    }
    
    public CustomVideoView(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    }
    
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//    	int width = getDefaultSize(0, widthMeasureSpec);
//    	int height = getDefaultSize(0, heightMeasureSpec);
//    	setMeasuredDimension(width, height);
//    }
//    
//    @Override
//    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//    		setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(),widthMeasureSpec),
//    				getDefaultSize(getSuggestedMinimumHeight(),heightMeasureSpec));
////    		getHolder().setFixedSize(getMeasuredWidth(), getMeasuredHeight());
//    }
//    
//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom);
//        if(null != getHolder()) 
//        	getHolder().setSizeFromLayout();
//    }

    @Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = getDefaultSize(videoWidth, widthMeasureSpec);
		int height = getDefaultSize(videoHeight, heightMeasureSpec);
		if (videoWidth > 0 && videoHeight > 0) {
			if (videoWidth * height > width * videoHeight) {
				height = width * videoHeight / videoWidth;
			} else if (videoWidth * height < width * videoHeight) {
				width = height * videoWidth / videoHeight;
			}
		}
		setMeasuredDimension(width, height);
	}

	public int getVideoWidth() {
		return videoWidth;
	}

	public void setVideoWidth(int videoWidth) {
		this.videoWidth = videoWidth;
	}

	public int getVideoHeight() {
		return videoHeight;
	}

	public void setVideoHeight(int videoHeight) {
		this.videoHeight = videoHeight;
	}
    
}