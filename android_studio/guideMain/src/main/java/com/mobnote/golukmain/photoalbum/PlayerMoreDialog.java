package com.mobnote.golukmain.photoalbum;

import java.util.List;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventDeletePhotoAlbumVid;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.CarRecorderActivity.VideoType;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnRightClickListener;
import com.mobnote.util.GolukUtils;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
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
	private int mVideoType;

	public PlayerMoreDialog(Context context, String path, int type, String videoFrom,int videotype) {
		super(context, R.style.CustomDialog);
		setContentView(R.layout.show_delete_dialog);
		this.mVidPath = path;
		this.mType = type;
		mContext = context;
		mVideoFrom = videoFrom;
		mVideoType = videotype;
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
			if(mVideoType == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP){
				mShareVideoLL.setVisibility(View.GONE);
			}else{
				mShareVideoLL.setVisibility(View.VISIBLE);
			}
			
		} else {
			mShareVideoLL.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.fun_dialog_del_layout) {
			dismiss();
			showConfimDeleteDialog();
		} else if (id == R.id.cancel) {
			dismiss();
		} else if (id == R.id.fun_dialog_share_layout) {
			dismiss();
			GolukUtils.startVideoEditActivity(mContext, mType, mVidPath);
		} else {
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
				if(!"local".equals(mVideoFrom)){
					if(isAllowedDelete()){
						if (!GolukApplication.getInstance().getIpcIsLogin()) {
							GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.str_photo_check_ipc_state));
						}else{
							EventBus.getDefault().post(new EventDeletePhotoAlbumVid(mVidPath,mType));
							GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.str_photo_delete_ok));
						}
						
						((PhotoAlbumPlayer)mContext).finish();
					}else{
						GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.str_photo_downing));
					}
				}else{
					EventBus.getDefault().post(new EventDeletePhotoAlbumVid(mVidPath,mType));
					GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.str_photo_delete_ok));
					((PhotoAlbumPlayer)mContext).finish();
				}
				
				
			}
		});
		mCustomDialog.show();
	}
	
	private boolean isAllowedDelete() {
		List<String> dlist = GolukApplication.getInstance().getDownLoadList();
		if (dlist.contains(mVidPath)) {
			return false;
		}else{
			return true;
		}

		
	}

}
