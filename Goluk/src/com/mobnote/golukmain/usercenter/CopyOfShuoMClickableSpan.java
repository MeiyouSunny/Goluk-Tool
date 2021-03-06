package com.mobnote.golukmain.usercenter;

import com.mobnote.golukmain.cluster.ClusterActivity;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

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
		ds.setColor(Color.rgb(0, 128, 255));
	}

	@Override
	public void onClick(View widget) {
		try {
			// 启动活动聚合页
			if(null != mVideInfo.mVideoEntity && null != mVideInfo.mVideoEntity.videoExtra) {
				Intent intent = new Intent(mContext, ClusterActivity.class);
				intent.putExtra(ClusterActivity.CLUSTER_KEY_ACTIVITYID, mVideInfo.mVideoEntity.videoExtra.topicid);
				intent.putExtra(ClusterActivity.CLUSTER_KEY_TITLE, mVideInfo.mVideoEntity.videoExtra.topicname);
				mContext.startActivity(intent);
			}
		} catch (Exception e) {

		}

	}

}
