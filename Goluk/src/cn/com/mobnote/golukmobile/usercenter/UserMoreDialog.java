package cn.com.mobnote.golukmobile.usercenter;

import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
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
		switch (v.getId()) {
		case R.id.btn_user_more_share:
			dismiss();
			if (null != mContext && mContext instanceof NewUserCenterActivity) {
				((NewUserCenterActivity) mContext).shareHomePage();
			}
			break;
		case R.id.btn_user_more_back:
			dismiss();
			Intent it = new Intent(mContext, MainActivity.class);
			mContext.startActivity(it);
			break;
		case R.id.btn_user_more_cancle:
			dismiss();
			break;
		default:
			break;
		}
	}

}
