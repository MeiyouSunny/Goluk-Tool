package cn.com.mobnote.golukmobile.usercenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import cn.com.mobnote.golukmobile.cluster.ClusterActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;

public class CopyOfShuoMClickableSpan extends ClickableSpan {

	private String mString;
	private Context mContext;
	private VideoSquareInfo mVideInfo;

	public CopyOfShuoMClickableSpan(Context context, String str, VideoSquareInfo videoInfo) {
		super();
		this.mString = str;
		this.mContext = context;
		this.mVideInfo = videoInfo;
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setColor(Color.rgb(255, 138, 0));
	}

	@Override
	public void onClick(View widget) {
		// 启动活动聚合页
		Intent intent = new Intent(mContext, ClusterActivity.class);
		intent.putExtra(ClusterActivity.CLUSTER_KEY_ACTIVITYID, mVideInfo.mVideoEntity.videoExtra.topicid);
		intent.putExtra(ClusterActivity.CLUSTER_KEY_UID, "");
		mContext.startActivity(intent);
	}

}