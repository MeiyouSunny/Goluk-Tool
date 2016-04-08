package cn.com.mobnote.golukmobile.thirdshare.china;

import com.umeng.socialize.Config;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;

public class ChinaThirdShare extends AbsThirdShare {

	public ChinaThirdShare(Activity activity, SharePlatformUtil spf, String surl, String curl, String db, String tl,
			Bitmap bitmap, String realDesc, String videoId) {
		super(activity, spf, surl, curl, db, tl, bitmap, realDesc, videoId);
		initView(activity);
		modifyUMDialog();
	}

	private void initView(Activity activity) {

	}

	private void modifyUMDialog() {
		ProgressDialog dialog = new ProgressDialog(mActivity);
		dialog.setMessage(mActivity.getString(R.string.str_um_share_dialog_txt));
		dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {
				setCanJump();
			}

		});
		Config.dialog = dialog;
	}

	private void setCanJump() {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {

	}

	@Override
	public void click(String type) {

	}

	@Override
	public void setCurrentShareType(String type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void CallBack_Share(int event) {
		// TODO Auto-generated method stub

	}

}
