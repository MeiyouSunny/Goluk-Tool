package cn.com.mobnote.golukmobile.comment;

import cn.com.mobnote.golukmobile.UserVersionActivity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickHeadListener implements OnClickListener {

	private Context mContext;
	
	public ClickHeadListener(Context context) {
		this.mContext = context;
	}
	
	@Override
	public void onClick(View arg0) {
		Intent itHead = new Intent(mContext,UserVersionActivity.class);
		mContext.startActivity(itHead);
	}

}
