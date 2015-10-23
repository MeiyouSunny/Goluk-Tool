package cn.com.mobnote.golukmobile.newest;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.live.LiveActivity;
import cn.com.mobnote.golukmobile.live.UserInfo;
import cn.com.mobnote.golukmobile.player.VideoPlayerActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoCategoryActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoEntity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;

public class ClickNewestListener implements OnClickListener {
	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;
	private NewestListView mNewestListView = null;

	public ClickNewestListener(Context context, VideoSquareInfo info, NewestListView listView) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
		mNewestListView = listView;
	}

	@Override
	public void onClick(View arg0) {
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
			Intent intent = new Intent(mContext, VideoPlayerActivity.class);
			intent.putExtra("from", "suqare");
			intent.putExtra("image", mVideoSquareInfo.mVideoEntity.picture);
			intent.putExtra("playUrl", mVideoSquareInfo.mVideoEntity.ondemandwebaddress);
			uploadPlayer(mVideoSquareInfo.mVideoEntity.videoid, "1", "1");// 上报播放次数
			mContext.startActivity(intent);
		}

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
		intent.putExtra(VideoCategoryActivity.KEY_VIDEO_CATEGORY_TITLE, "直播列表");
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
		user.nickName = mVideoSquareInfo.mUserEntity.nickname;
		user.persons = mVideoSquareInfo.mVideoEntity.clicknumber;
		user.picurl = mVideoSquareInfo.mVideoEntity.picture;
		user.sex = mVideoSquareInfo.mUserEntity.sex;
		user.speed = mVideoSquareInfo.mVideoEntity.livevideodata.speed;
		user.tag = mVideoSquareInfo.mVideoEntity.livevideodata.tag;
		user.uid = mVideoSquareInfo.mUserEntity.uid;
		user.zanCount = mVideoSquareInfo.mVideoEntity.praisenumber;
		user.head = mVideoSquareInfo.mUserEntity.headportrait;
		user.customavatar = mVideoSquareInfo.mUserEntity.mCustomAvatar;
		Intent intent = new Intent(mContext, LiveActivity.class);
		intent.putExtra(LiveActivity.KEY_IS_LIVE, false);
		intent.putExtra(LiveActivity.KEY_GROUPID, "");
		intent.putExtra(LiveActivity.KEY_PLAY_URL, "");
		intent.putExtra(LiveActivity.KEY_JOIN_GROUP, "");
		intent.putExtra(LiveActivity.KEY_USERINFO, user);

		// uploadPlayer(mVideoSquareInfo.mVideoEntity.videoid, "1", "1");//
		// 上报播放次数
		mContext.startActivity(intent);
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

}
