package cn.com.mobnote.golukmobile.special;

import cn.com.mobnote.golukmobile.R;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class ClusterPressListener implements OnClickListener {
	private ClusterInfo clusterInfo;
	private ClusterViewAdapter clusterViewAdapter;

	public ClusterPressListener(Context context, ClusterInfo info, ClusterViewAdapter cva) {
		this.clusterInfo = info;
		this.clusterViewAdapter = cva;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.praiseLayout:
			// 没有点赞过
			if ("0".equals(clusterInfo.ispraise)) {
				clusterInfo.praisenumber = (Integer.parseInt(clusterInfo.praisenumber) + 1) + "";
				clusterInfo.ispraise = "1";
			} else {
				clusterInfo.praisenumber = (Integer.parseInt(clusterInfo.praisenumber) - 1) + "";
				clusterInfo.ispraise = "0";
			}
			clusterViewAdapter.setLikePress(clusterInfo);

			break;
		default:
			break;
		}
	}

}
