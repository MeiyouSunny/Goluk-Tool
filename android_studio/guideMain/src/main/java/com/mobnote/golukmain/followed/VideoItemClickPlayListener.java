package com.mobnote.golukmain.followed;

import java.util.ArrayList;
import java.util.List;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnLeftClickListener;
import com.mobnote.golukmain.followed.bean.FollowedVideoObjectBean;
import com.mobnote.golukmain.player.MovieActivity;
import com.mobnote.golukmain.videosuqare.VideoEntity;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;
import com.mobnote.golukmain.videosuqare.ZhugeParameterFn;
import com.mobnote.util.ZhugeUtils;
import com.zhuge.analysis.stat.ZhugeSDK;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;

import org.json.JSONObject;

public class VideoItemClickPlayListener implements OnClickListener, ZhugeParameterFn {
	private FollowedVideoObjectBean mVideoSquareInfo;
	private Context mContext;

	public VideoItemClickPlayListener(Context context, FollowedVideoObjectBean info) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
	}

	@Override
	public void onClick(View arg0) {
		if (null != mContext && mContext instanceof BaseActivity) {
			if(!isNetworkConnected()) {
				dialog(mContext.getString(R.string.user_net_unavailable));
				return;
			}
		}

		//观看视频统计
		playVideoStatistics();

		Intent intent = new Intent(mContext, MovieActivity.class);
		intent.putExtra("from", "suqare");
		intent.putExtra("image", mVideoSquareInfo.video.picture);
		intent.putExtra("playUrl", mVideoSquareInfo.video.ondemandwebaddress);
		uploadPlayer(mVideoSquareInfo.video.videoid, "1", "1");// 上报播放次数
		mContext.startActivity(intent);
	}

	private void dialog(String msg) {
		final CustomDialog customDialog = new CustomDialog(mContext);
		customDialog.setCancelable(true);
		customDialog.setMessage(msg, Gravity.CENTER);
		customDialog.setLeftButton(mContext.getString(R.string.str_button_ok), new OnLeftClickListener() {
			@Override
			public void onClickListener() {
				customDialog.dismiss();;
			}
		});
		customDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				customDialog.dismiss();
			}
		});
		customDialog.show();
	}

	public boolean isNetworkConnected() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	}

	public static void uploadPlayer(String videoid, String channel, String clicknumber) {
		VideoSquareInfo vsi = new VideoSquareInfo();
		VideoEntity ve = new VideoEntity();
		ve.videoid = videoid;
		ve.clicknumber = clicknumber;
		vsi.mVideoEntity = ve;
		List<VideoSquareInfo> list = new ArrayList<VideoSquareInfo>();
		list.add(vsi);
		GolukApplication.getInstance().getVideoSquareManager().clickNumberUpload(channel, list);
	}

	/**
	 * 我的关注内容列表观看视频统计
	 */
	private void playVideoStatistics() {
		if (null != mVideoSquareInfo && null != mVideoSquareInfo.video) {
			String actionName = "";
			if (null != mVideoSquareInfo.video.gen && null != mVideoSquareInfo.video.gen.topicname) {
				actionName = mVideoSquareInfo.video.gen.topicname;
			}
			JSONObject json = ZhugeUtils.eventPlayVideo(mContext, mVideoSquareInfo.video.videoid,
					mVideoSquareInfo.video.describe, actionName, mVideoSquareInfo.video.category, ZHUGE_PLAY_VIDEO_PAGE_FOLLOWED);
			ZhugeSDK.getInstance().track(mContext, mContext.getString(R.string.str_zhuge_play_video_event), json);
		}
	}

}
