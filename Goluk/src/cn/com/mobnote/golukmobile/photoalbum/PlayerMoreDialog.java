package cn.com.mobnote.golukmobile.photoalbum;

import cn.com.mobnote.eventbus.EventDeletePhotoAlbumVid;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.adas.AdasGuideActivity;
import cn.com.mobnote.golukmobile.adas.AdasVerificationActivity;
import cn.com.mobnote.golukmobile.carrecorder.settings.SettingsActivity;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnRightClickListener;
import cn.com.mobnote.golukmobile.newest.RecomVideoActivity;
import de.greenrobot.event.EventBus;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayerMoreDialog extends Dialog implements android.view.View.OnClickListener {

	private Context mContext;
	private String mVidPath;
	private LinearLayout fun_dialog_del_layout;
	private TextView delvideo;
	private TextView cancel;
	private int mType;

	private CustomDialog mCustomDialog;

	public PlayerMoreDialog(Context context, String path,int type) {
		super(context, R.style.CustomDialog);
		setContentView(R.layout.show_delete_dialog);
		this.mVidPath = path;
		this.mType = type;
		mContext = context;
		intLayout();
	}

	private void intLayout() {
		fun_dialog_del_layout = (LinearLayout) findViewById(R.id.fun_dialog_del_layout);
		delvideo = (TextView) findViewById(R.id.delvideo);
		cancel = (TextView) findViewById(R.id.cancel);

		fun_dialog_del_layout.setOnClickListener(this);
		cancel.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.fun_dialog_del_layout:

			dismiss();
			showConfimDeleteDialog();
			break;

		case R.id.cancel:
			dismiss();
			break;
		default:
			break;
		}
	}

	private void showConfimDeleteDialog() {

		if(mCustomDialog==null){
			mCustomDialog = new CustomDialog(mContext);
		}
		
		mCustomDialog.setMessage(mContext.getString(R.string.str_photo_delete_confirm), Gravity.CENTER);
		mCustomDialog.setLeftButton(mContext.getString(R.string.dialog_str_cancel), null);
		mCustomDialog.setRightButton(mContext.getString(R.string.str_button_ok), new OnRightClickListener() {

			@Override
			public void onClickListener() {
				// TODO Auto-generated method stub
				mCustomDialog.dismiss();
				EventBus.getDefault().post(new EventDeletePhotoAlbumVid(mVidPath,mType));
			}
			
		});
		mCustomDialog.show();
	}

}
