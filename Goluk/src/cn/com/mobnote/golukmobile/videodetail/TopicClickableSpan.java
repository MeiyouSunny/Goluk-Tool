package cn.com.mobnote.golukmobile.videodetail;

import cn.com.mobnote.util.GolukUtils;
import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class TopicClickableSpan extends ClickableSpan {

	private Context mContext;
	private String mStr ;
	
	public TopicClickableSpan(Context context,String str) {
		super();
		this.mContext = context;
		this.mStr = str;
	}

	@Override
	public void onClick(View view) {
		GolukUtils.showToast(mContext, "跳聚合条聚合条聚合");
	}
	
	@Override
	public void updateDrawState(TextPaint ds) {
		super.updateDrawState(ds);
		ds.setColor(Color.rgb(255, 138, 0));
		ds.setUnderlineText(false);
	}
	
}
