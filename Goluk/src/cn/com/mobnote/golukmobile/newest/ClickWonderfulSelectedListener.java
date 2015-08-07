package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.golukmobile.special.ClusterListActivity;
import cn.com.mobnote.golukmobile.special.SpecialListActivity;
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
		if ("1".equals(mJXListItemDataInfo.ztype)) {//专题
			Intent intent = new Intent(mContext, SpecialListActivity.class);
			intent.putExtra("ztid", "");
			mContext.startActivity(intent);
		}else if ("2".equals(mJXListItemDataInfo.ztype)) {//tag
			Intent intent = new Intent(mContext, ClusterListActivity.class);
			intent.putExtra("ztid", "");
			mContext.startActivity(intent);
		}else if ("3".equals(mJXListItemDataInfo.ztype)) {//单视频
			
		}else if ("4".equals(mJXListItemDataInfo.ztype)) {//url
			
		}
		
	}
	
}
