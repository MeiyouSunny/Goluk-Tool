package com.mobnote.golukmain.newest;

import java.util.ArrayList;
import java.util.List;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnLeftClickListener;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.player.MovieActivity;
import com.mobnote.golukmain.videosuqare.VideoCategoryActivity;
import com.mobnote.golukmain.videosuqare.VideoEntity;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;
import com.mobnote.util.GolukUtils;
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

public class ClickNewestListener implements OnClickListener {
	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;
	private NewestListView mNewestListView = null;
	private int mSource = 0;

	public ClickNewestListener(Context context, VideoSquareInfo info, NewestListView listView, int source) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
		mNewestListView = listView;
		this.mSource = source;
	}

	@Override
	public void onClick(View arg0) {
		if (null != mContext && mContext instanceof BaseActivity) {
			if(!isNetworkConnected()) {
				dialog(mContext.getString(R.string.user_net_unavailable));
				return;
			}
		}
		// 防止重复点击
		if (null != mContext && mContext instanceof BaseActivity) {
			if (!((BaseActivity) mContext).isAllowedClicked()) {
				return;
			}
			((BaseActivity) mContext).setJumpToNext();
		}

		if (isLive()) {
			if (null != mNewestListView) {
				toLiveList();
			} else {
				startLookLive();
			}

		} else {

			//观看视频统计
			playVideoStatistics();

			Intent intent = new Intent(mContext, MovieActivity.class);
			intent.putExtra("from", "suqare");
			intent.putExtra("image", mVideoSquareInfo.mVideoEntity.picture);
			intent.putExtra("playUrl", mVideoSquareInfo.mVideoEntity.ondemandwebaddress);
			uploadPlayer(mVideoSquareInfo.mVideoEntity.videoid, "1", "1");// 上报播放次数
			mContext.startActivity(intent);
		}
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

	/**
	 * 去直播列表
	 * 
	 * @author jyf
	 * @date 2015年8月9日
	 */
	private void toLiveList() {
		Intent intent = new Intent(mContext, VideoCategoryActivity.class);
		intent.putExtra(VideoCategoryActivity.KEY_VIDEO_CATEGORY_TYPE, VideoCategoryActivity.CATEGORY_TYPE_LIVE);
		// 此处attribute一定要写 0 ,　否则直播查不出来
		intent.putExtra(VideoCategoryActivity.KEY_VIDEO_CATEGORY_ATTRIBUTE, VideoCategoryActivity.LIVE_ATTRIBUTE_VALUE);
		intent.putExtra(VideoCategoryActivity.KEY_VIDEO_CATEGORY_TITLE, mContext.getString(R.string.video_square_text));
		mContext.startActivity(intent);
	}

	/**
	 * 查看别人直播
	 * 
	 * @author jyf
	 * @date 2015年8月9日
	 */
	private void startLookLive() {
		UserInfo user = new UserInfo();
		user.active = mVideoSquareInfo.mVideoEntity.livevideodata.active;
		user.aid = mVideoSquareInfo.mVideoEntity.livevideodata.aid;
		user.lat = mVideoSquareInfo.mVideoEntity.livevideodata.lat;
		if (mVideoSquareInfo.mVideoEntity.livevideodata.restime != null
				&& !"".equals(mVideoSquareInfo.mVideoEntity.livevideodata.restime)) {
			user.liveDuration = Integer.parseInt(mVideoSquareInfo.mVideoEntity.livevideodata.restime);
		} else {
			user.liveDuration = 0;
		}

		user.lon = mVideoSquareInfo.mVideoEntity.livevideodata.lon;
		user.nickname = mVideoSquareInfo.mUserEntity.nickname;
		user.persons = mVideoSquareInfo.mVideoEntity.clicknumber;
		user.picurl = mVideoSquareInfo.mVideoEntity.picture;
		user.sex = mVideoSquareInfo.mUserEntity.sex;
		user.speed = mVideoSquareInfo.mVideoEntity.livevideodata.speed;
		user.tag = mVideoSquareInfo.mVideoEntity.livevideodata.tag;
		user.uid = mVideoSquareInfo.mUserEntity.uid;
		user.zanCount = mVideoSquareInfo.mVideoEntity.praisenumber;
		user.head = mVideoSquareInfo.mUserEntity.headportrait;
		user.customavatar = mVideoSquareInfo.mUserEntity.mCustomAvatar;
		user.mUserLabel = mVideoSquareInfo.mUserEntity.label;
		user.link = mVideoSquareInfo.mUserEntity.link;

		//直播页面
		ZhugeUtils.eventLive(mContext, mContext.getString(R.string.str_zhuge_newest_event));
		GolukUtils.startPublishOrWatchLiveActivity(mContext,false,false,null,user);
	}

	/**
	 * 判断当前是否是直播数据
	 * 
	 * @return true/false 是/否
	 * @author jyf
	 * @date 2015年8月9日
	 */
	private boolean isLive() {
		return "1".equals(mVideoSquareInfo.mVideoEntity.type);
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
	 * 观看视频统计
	 */
	private void playVideoStatistics() {
		if (null != mVideoSquareInfo && null != mVideoSquareInfo.mVideoEntity) {
			String actionName = "";
			if (null != mVideoSquareInfo.mVideoEntity.videoExtra && null != mVideoSquareInfo.mVideoEntity.videoExtra.topicname) {
				actionName = mVideoSquareInfo.mVideoEntity.videoExtra.topicname;
			}
			JSONObject json = ZhugeUtils.eventPlayVideo(mContext, mVideoSquareInfo.mVideoEntity.videoid,
					mVideoSquareInfo.mVideoEntity.describe, actionName, mVideoSquareInfo.mVideoEntity.category, mSource);
			ZhugeSDK.getInstance().track(mContext, mContext.getString(R.string.str_zhuge_play_video_event), json);
		}
	}

}
