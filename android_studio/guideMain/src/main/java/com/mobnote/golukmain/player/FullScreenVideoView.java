package com.mobnote.golukmain.player;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import com.mobnote.golukmain.player.factory.TextureVideoView;

/**
 * 自动全屏的VideoView
 */
public class FullScreenVideoView extends TextureVideoView {

	private int videoWidth;
	private int videoHeight;

	public FullScreenVideoView(Context context) {
		super(context);
	}

	public FullScreenVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FullScreenVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = getDefaultSize(videoWidth, widthMeasureSpec);
		int height = getDefaultSize(videoHeight, heightMeasureSpec);
		if (videoWidth > 0 && videoHeight > 0) {
			if (videoWidth * height > width * videoHeight) {
				height = width * videoHeight / videoWidth;
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

    // Fix the following bug
    // java.lang.UnsupportedOperationException:TextureView doesn't support displaying a background drawable
    @Override
    public void setBackgroundDrawable(Drawable background) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N && background != null) {
            setBackgroundDrawable(background);
        }
    }

}
