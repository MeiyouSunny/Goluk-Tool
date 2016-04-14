package com.mobnote.golukmain.special;

import java.util.ArrayList;
import java.util.List;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.player.MovieActivity;
import com.mobnote.golukmain.player.VideoPlayerActivity;
import com.mobnote.golukmain.videosuqare.VideoEntity;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;
import com.mobnote.util.GolukUtils;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class SpecialCommentListener implements OnClickListener{
	private Context mContext;
	
	private ClusterViewAdapter mClusterViewAdapter = null;
	
	String imagepath;
	String videopath;
	String from;
	String type;
	String vid ;
	
	public SpecialCommentListener(Context context, ClusterViewAdapter viewAdapter, String ipath,String vpath,String f,String t,String videoid) {
		this.mContext = context;
		this.mClusterViewAdapter = viewAdapter;
		imagepath = ipath;
		videopath = vpath;
		from = f;
		type = t;
		vid = videoid;
	}
	
	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.mPreLoading
				|| id == R.id.imageLayout) {
			if("2".equals(type)){
				Intent intent = new Intent(mContext, MovieActivity.class);
				intent.putExtra("from", from);
				intent.putExtra("image", imagepath);
				intent.putExtra("playUrl", videopath);
				if(vid == null || "".equals(vid)){
					vid = "232";
				}
				uploadPlayer(vid, "1", "1");// 上报播放次数
				mContext.startActivity(intent);
			}
		} else if (id == R.id.shareLayout) {
			ClusterListActivity vsa = (ClusterListActivity) mContext;
			vsa.mCustomProgressDialog = new CustomLoadingDialog(vsa, null);
			vsa.mCustomProgressDialog.show();
			boolean result = GolukApplication.getInstance()
					.getVideoSquareManager().getShareUrl(vid, "2");
			if (!result) {
				vsa.mCustomProgressDialog.close();
				GolukUtils.showToast(mContext, mContext.getString(R.string.network_error));
			} else {
				// 保存将要分享的视频id
				if (null != mClusterViewAdapter) {
					mClusterViewAdapter.setWillShareVideoId(vid);
				}
				
			}
		} else {
		}
		
		
	}
	
	private void uploadPlayer(String videoid, String channel, String clicknumber) {
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
