package com.mobnote.golukmain.videodetail;

import com.mobnote.golukmain.cluster.ClusterActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class TopicClickableSpan extends ClickableSpan {

	private Context mContext;
	private String mStr ;
	private VideoDetailRetBean mVideoDetailRetBean;
	
	public TopicClickableSpan(Context context,String str,VideoDetailRetBean videoDetailRetBean) {
		super();
		this.mContext = context;
		this.mStr = str;
		this.mVideoDetailRetBean = videoDetailRetBean;
	}

	@Override
	public void onClick(View view) {
		if(null == mVideoDetailRetBean || null == mVideoDetailRetBean.data || null == mVideoDetailRetBean.data.avideo) {
			return;
		}
		// 启动活动聚合页
		if (mVideoDetailRetBean.data.avideo.video == null || mVideoDetailRetBean.data.avideo.video.recom == null) {
			return;
		}
		Intent intent = new Intent(mContext, ClusterActivity.class);
		intent.putExtra(ClusterActivity.CLUSTER_KEY_ACTIVITYID, mVideoDetailRetBean.data.avideo.video.recom.topicid);
		intent.putExtra(ClusterActivity.CLUSTER_KEY_TITLE, mVideoDetailRetBean.data.avideo.video.recom.topicname);
		mContext.startActivity(intent);
	}
	
	@Override
	public void updateDrawState(TextPaint ds) {
		super.updateDrawState(ds);
		ds.setColor(Color.rgb(0, 128, 255));
		ds.setUnderlineText(false);
	}
	
}
