package cn.com.mobnote.golukmobile.special;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.player.MovieActivity;
import cn.com.mobnote.golukmobile.player.VideoPlayerActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoEntity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.util.GolukUtils;
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
		switch (view.getId()) {
		case R.id.mPreLoading:
		case R.id.imageLayout:
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
			break;
		case R.id.shareLayout:
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
			break;
		default:
			break;
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
