package cn.com.mobnote.golukmobile.photoalbum;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.mobnote.eventbus.EventDeletePhotoAlbumVid;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnRightClickListener;
import cn.com.mobnote.util.GolukUtils;
import de.greenrobot.event.EventBus;

public class PlayerMoreDialog extends Dialog implements android.view.View.OnClickListener {

	private Context mContext;
	private String mVidPath;
	private LinearLayout fun_dialog_del_layout;
	private TextView cancel;
	private int mType;
	private LinearLayout mShareVideoLL;
	private String mVideoFrom;

	private CustomDialog mCustomDialog;

	public PlayerMoreDialog(Context context, String path, int type, String videoFrom) {
		super(context, R.style.CustomDialog);
		setContentView(R.layout.show_delete_dialog);
		this.mVidPath = path;
		this.mType = type;
		mContext = context;
		mVideoFrom = videoFrom;
		intLayout();
	}

	private void intLayout() {
		fun_dialog_del_layout = (LinearLayout) findViewById(R.id.fun_dialog_del_layout);
		cancel = (TextView) findViewById(R.id.cancel);
		mShareVideoLL = (LinearLayout)findViewById(R.id.fun_dialog_share_layout);
		mShareVideoLL.setOnClickListener(this);

		fun_dialog_del_layout.setOnClickListener(this);
		cancel.setOnClickListener(this);

		if("local".equals(mVideoFrom)) {
			mShareVideoLL.setVisibility(View.VISIBLE);
		} else {
			mShareVideoLL.setVisibility(View.GONE);
		}
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
		case R.id.fun_dialog_share_layout:
			dismiss();
			GolukUtils.startVideoEditActivity(mContext, mType, mVidPath);
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
				((PhotoAlbumPlayer)mContext).finish();
			}
		});
		mCustomDialog.show();
	}

}
