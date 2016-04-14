package com.mobnote.golukmain.followed;

import com.mobnote.golukmain.cluster.ClusterActivity;
import com.mobnote.golukmain.followed.bean.FollowedVideoObjectBean;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class VideoItemClickableSpan extends ClickableSpan {
	private Context mContext;
	private FollowedVideoObjectBean mVideInfo;

	public VideoItemClickableSpan(Context context, String str, FollowedVideoObjectBean videoInfo) {
		super();
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
			if(null != mVideInfo.video && null != mVideInfo.video.gen) {
				Intent intent = new Intent(mContext, ClusterActivity.class);
				intent.putExtra(ClusterActivity.CLUSTER_KEY_ACTIVITYID, mVideInfo.video.gen.topicid);
				intent.putExtra(ClusterActivity.CLUSTER_KEY_TITLE, mVideInfo.video.gen.topicname);
				mContext.startActivity(intent);
			}
		} catch (Exception e) {

		}

	}

}
