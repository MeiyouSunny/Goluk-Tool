package cn.com.mobnote.golukmobile.videodetail;

import cn.com.mobnote.golukmobile.cluster.ClusterActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class TopicClickableSpan extends ClickableSpan {

	private Context mContext;
	private String mStr ;
	private VideoJson mVideoJson;
	
	public TopicClickableSpan(Context context,String str,VideoJson videoJson) {
		super();
		this.mContext = context;
		this.mStr = str;
		this.mVideoJson = videoJson;
	}

	@Override
	public void onClick(View view) {
		// 启动活动聚合页
		Intent intent = new Intent(mContext, ClusterActivity.class);
		intent.putExtra(ClusterActivity.CLUSTER_KEY_ACTIVITYID, mVideoJson.data.avideo.video.recom.topicid);
		intent.putExtra(ClusterActivity.CLUSTER_KEY_UID, "");
		mContext.startActivity(intent);
	}
	
	@Override
	public void updateDrawState(TextPaint ds) {
		super.updateDrawState(ds);
		ds.setColor(Color.rgb(255, 138, 0));
		ds.setUnderlineText(false);
	}
	
}
