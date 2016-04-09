package com.mobnote.golukmain.special;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.newest.FunctionDialog;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class ClusterPressListener implements OnClickListener {
	private ClusterInfo clusterInfo;
	private ClusterViewAdapter clusterViewAdapter;
	private Context mContext;

	public ClusterPressListener(Context context, ClusterInfo info, ClusterViewAdapter cva) {
		this.clusterInfo = info;
		this.clusterViewAdapter = cva;
		this.mContext = context;
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.praiseLayout) {
			// 没有点赞过
			if ("0".equals(clusterInfo.ispraise)) {
				clusterInfo.praisenumber = (Integer.parseInt(clusterInfo.praisenumber) + 1) + "";
				clusterInfo.ispraise = "1";
			} else {
				clusterInfo.praisenumber = (Integer.parseInt(clusterInfo.praisenumber) - 1) + "";
				clusterInfo.ispraise = "0";
			}
			clusterViewAdapter.setLikePress(clusterInfo);
		} else if (id == R.id.function) {
			new FunctionDialog(mContext,clusterInfo.videoid, false, null).show();
		} else {
		}
	}

}
