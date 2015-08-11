package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.golukmobile.UserOpenUrlActivity;
import cn.com.mobnote.golukmobile.VideoSquareDeatilActivity;
import cn.com.mobnote.golukmobile.special.ClusterListActivity;
import cn.com.mobnote.golukmobile.special.SpecialListActivity;
import cn.com.tiros.debug.GolukDebugUtils;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickWonderfulSelectedListener implements OnClickListener{
	private JXListItemDataInfo mJXListItemDataInfo; 
	private Context mContext;
	
	public ClickWonderfulSelectedListener(Context context, JXListItemDataInfo info ) {
		this.mJXListItemDataInfo = info;
		this.mContext = context;
	}

	@Override
	public void onClick(View arg0) {
		GolukDebugUtils.e("", "TTTTTTTTTTT===========mJXListItemDataInfo.ztype=="+mJXListItemDataInfo.ztype);
		Intent intent = null;
		if ("1".equals(mJXListItemDataInfo.ztype)) {//专题
			intent = new Intent(mContext, SpecialListActivity.class);
			intent.putExtra("ztid", mJXListItemDataInfo.ztid);
			intent.putExtra("title", mJXListItemDataInfo.ztitle);
		}else if ("2".equals(mJXListItemDataInfo.ztype)) {//tag
			intent = new Intent(mContext, ClusterListActivity.class);
			intent.putExtra("ztid", mJXListItemDataInfo.ztid);
			intent.putExtra("title", mJXListItemDataInfo.ztitle);
		}else if ("3".equals(mJXListItemDataInfo.ztype)) {//单视频
			intent = new Intent(mContext, VideoSquareDeatilActivity.class);
			intent.putExtra("ztid", mJXListItemDataInfo.ztid);
			intent.putExtra("imageurl", mJXListItemDataInfo.jximg);
			intent.putExtra("title", mJXListItemDataInfo.ztitle);
		}else if ("4".equals(mJXListItemDataInfo.ztype)) {//url
			String url = mJXListItemDataInfo.adverturl;
			intent = new Intent(mContext, UserOpenUrlActivity.class);
			intent.putExtra("url", url);
		}
		mContext.startActivity(intent);
	}
	
}
