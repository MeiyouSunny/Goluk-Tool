package cn.com.mobnote.golukmobile.startshare;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.newest.IDialogDealFn;

public class StartShareFunctionDialog extends Dialog implements android.view.View.OnClickListener {

	private TextView cancle;
	private Context mContext;
	private LinearLayout mStartLocationLayout = null;
	private LinearLayout mDelLocationLayout = null;
	private IDialogDealFn mListener = null;

	public StartShareFunctionDialog(Context context, IDialogDealFn fn) {
		super(context, R.style.CustomDialog);
		setContentView(R.layout.start_share_function_dialog);
		mContext = context;
		mListener = fn;
		initLayout();
	}

	private void initLayout() {
		this.cancle = (TextView) findViewById(R.id.cancle);
		mStartLocationLayout = (LinearLayout) findViewById(R.id.fun_dialog_start_layout);
		mDelLocationLayout = (LinearLayout) findViewById(R.id.fun_dialog_del_layout);
		mStartLocationLayout.setOnClickListener(this);
		mDelLocationLayout.setOnClickListener(this);
		cancle.setOnClickListener(this);
	}

	private void sendCallBack(int event, Object data) {
		if (null == mListener) {
			return;
		}
		mListener.CallBack_Del(event, data);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fun_dialog_start_layout:
			sendCallBack(1, null);
			dismiss();
			mListener = null;
			mContext = null;
			break;
		case R.id.fun_dialog_del_layout:
			sendCallBack(2, null);
			dismiss();
			mListener = null;
			mContext = null;
			break;
		case R.id.cancle:
			dismiss();
			mListener = null;
			mContext = null;
			break;
		default:
			break;
		}
	}

}
