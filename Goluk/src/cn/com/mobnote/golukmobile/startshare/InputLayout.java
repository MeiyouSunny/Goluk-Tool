package cn.com.mobnote.golukmobile.startshare;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;

public class InputLayout implements OnClickListener, OnTouchListener, TextWatcher {

	private Context mContext = null;
	private ViewGroup mParentLayout = null;
	private LayoutInflater mLayoutFlater = null;
	private RelativeLayout mRootLayout = null;
	private TextView mCanTv = null;
	private TextView mOkTv = null;
	private EditText mShareInputEdit = null;
	private TextView mCountTv = null;

	private final int MAX_COUNT = 50;
	private int mWhiteColor = 0;
	private int mRedColor = 0;

	private boolean isInputValid = true;

	public InputLayout(Context context, ViewGroup parentLayout) {
		mContext = context;
		mParentLayout = parentLayout;
		mLayoutFlater = LayoutInflater.from(mContext);

		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.share_input_layout, null);
		mCanTv = (TextView) mRootLayout.findViewById(R.id.share_input_cancel);
		mOkTv = (TextView) mRootLayout.findViewById(R.id.share_input_ok);
		mShareInputEdit = (EditText) mRootLayout.findViewById(R.id.share_input_edit);
		mCountTv = (TextView) mRootLayout.findViewById(R.id.share_input_count);

		mShareInputEdit.addTextChangedListener(this);
		mCanTv.setOnClickListener(this);
		mOkTv.setOnClickListener(this);
		mRootLayout.setOnTouchListener(this);

		loadRes();

	}

	private void loadRes() {
		mWhiteColor = mContext.getResources().getColor(R.color.white);
		mRedColor = mContext.getResources().getColor(R.color.red);
	}

	public void show() {
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		mParentLayout.addView(mRootLayout, lp);

		mShareInputEdit.setFocusableInTouchMode(true);
		mShareInputEdit.requestFocus();
		showSoft(mShareInputEdit);
		isInputValid = true;
		mCountTv.setTextColor(mWhiteColor);
		mCountTv.setText("" + MAX_COUNT);
	}

	public void showSoft(final EditText edit) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) edit.getContext().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(edit, 0);
			}
		}, 500);
	}

	public void hideSoft() {
		// InputMethodManager inputManager = (InputMethodManager)
		// edit.getContext().getSystemService(
		// Context.INPUT_METHOD_SERVICE);

	}

	public void hide() {
		mShareInputEdit.setText("");
		mParentLayout.removeView(mRootLayout);
		UserUtils.hideSoftMethod((VideoEditActivity) mContext);
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		switch (id) {
		case R.id.share_input_cancel:
			hide();
			((VideoEditActivity) mContext).mTypeLayout.setEditContent(true, "");
			break;
		case R.id.share_input_ok:
			if (!isInputValid) {
				GolukUtils.showToast(mContext, "内容长度超出限制");
				return;
			}
			final String content = mShareInputEdit.getText().toString().trim();
			((VideoEditActivity) mContext).mTypeLayout.setEditContent(false, content);
			hide();
			break;
		default:
			break;
		}

	}

	@Override
	public boolean onTouch(View v, MotionEvent arg1) {
		return true;
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		int num = arg0.length();
		int number = MAX_COUNT - num;
		if (number >= 0) {
			// 白色
			isInputValid = true;
			mCountTv.setTextColor(mWhiteColor);
		} else {
			// 紅色
			isInputValid = false;
			mCountTv.setTextColor(mRedColor);
		}
		mCountTv.setText("" + number);

	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

}
