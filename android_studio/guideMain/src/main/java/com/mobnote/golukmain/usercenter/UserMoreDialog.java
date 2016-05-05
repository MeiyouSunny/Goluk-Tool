package com.mobnote.golukmain.usercenter;

import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class UserMoreDialog extends Dialog implements android.view.View.OnClickListener {

	private Button mShareText, mBackText, mCancleText;
	private Context mContext;

	public UserMoreDialog(Context context) {
		super(context, R.style.CustomDialog);
		setContentView(R.layout.dialog_user_more);

		Window window = this.getWindow();
		window.setGravity(Gravity.BOTTOM);

		mContext = context;
		initLayout();
	}

	private void initLayout() {
		mShareText = (Button) findViewById(R.id.btn_user_more_share);
		mBackText = (Button) findViewById(R.id.btn_user_more_back);
		mCancleText = (Button) findViewById(R.id.btn_user_more_cancle);

		mShareText.setOnClickListener(this);
		mBackText.setOnClickListener(this);
		mCancleText.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_user_more_share) {
			dismiss();
			if (null != mContext && mContext instanceof NewUserCenterActivity) {
				((NewUserCenterActivity) mContext).shareHomePage();
			}
		} else if (id == R.id.btn_user_more_back) {
			dismiss();
			Intent it = new Intent(mContext, MainActivity.class);
			mContext.startActivity(it);
		} else if (id == R.id.btn_user_more_cancle) {
			dismiss();
		} else {
		}
	}

}
