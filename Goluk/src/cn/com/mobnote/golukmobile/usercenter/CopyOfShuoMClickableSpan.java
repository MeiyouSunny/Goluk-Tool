package cn.com.mobnote.golukmobile.usercenter;

import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class CopyOfShuoMClickableSpan extends ClickableSpan {

	private String string;
	private Context context;
	private VideoSquareInfo mVideInfo;

	public CopyOfShuoMClickableSpan(Context context, String str, VideoSquareInfo videoInfo) {
		super();
		this.string = str;
		this.context = context;
		this.mVideInfo = videoInfo;
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setColor(Color.rgb(255, 138, 0));
	}

	@Override
	public void onClick(View widget) {
		// TODO 启动活动聚合页
	}

}
