package cn.com.mobnote.golukmobile.followed;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import cn.com.mobnote.golukmobile.cluster.ClusterActivity;
import cn.com.mobnote.golukmobile.followed.bean.FollowVideoObjectBean;

public class VideoItemClickableSpan extends ClickableSpan {
	private Context mContext;
	private FollowVideoObjectBean mVideInfo;

	public VideoItemClickableSpan(Context context, String str, FollowVideoObjectBean videoInfo) {
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
