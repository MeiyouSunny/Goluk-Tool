package cn.com.mobnote.golukmobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import cn.com.mobnote.golukmobile.xdpush.StartAppBean;
import cn.com.tiros.debug.GolukDebugUtils;

public class ExternalStartActivity extends Activity {

	private StartAppBean mStartAppBean = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GolukDebugUtils.e("", "start App: ExternalStartActivity:------------: taskid: " + this.getTaskId());
		
		getWebStartData();
		startApp();
		finish();
	}

	private void startApp() {
		Intent intent = new Intent(this, GuideActivity.class);
		intent.putExtra(GuideActivity.KEY_WEB_START, mStartAppBean);
		startActivity(intent);
	}

	private void getWebStartData() {
		Intent intent = getIntent();
		final String scheme = intent.getScheme(); // golukapp
		GolukDebugUtils.e("", "start App: scheme:" + scheme);
		final Uri uri = intent.getData();
		final String dataStr = intent.getDataString(); // 获取整个字符串
		if (null != scheme && "golukapp".equals(scheme) && null != uri) {
			String host = uri.getHost(); // goluk.app
			String path = uri.getPath();

			String vid = uri.getQueryParameter("id");
			String title = uri.getQueryParameter("title");
			String type = uri.getQueryParameter("type");
			mStartAppBean = new StartAppBean();
			mStartAppBean.uri = uri.toString();
			mStartAppBean.dataStr = dataStr;
			mStartAppBean.host = host;
			mStartAppBean.path = path;

			mStartAppBean.type = type;
			mStartAppBean.id = vid;
			mStartAppBean.title = title;
		}
	}
}
