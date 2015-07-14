package cn.com.mobnote.golukmobile.startshare;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.user.UserUtils;

public class InputLayout implements OnClickListener, OnTouchListener {

	private Context mContext = null;
	private ViewGroup mParentLayout = null;
	private LayoutInflater mLayoutFlater = null;
	private RelativeLayout mRootLayout = null;
	private Button mCanBtn = null;
	private Button mOkBtn = null;
	private EditText mShareInputEdit = null;

	public InputLayout(Context context, ViewGroup parentLayout) {
		mContext = context;
		mParentLayout = parentLayout;
		mLayoutFlater = LayoutInflater.from(mContext);

		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.share_input_layout, null);
		mCanBtn = (Button) mRootLayout.findViewById(R.id.share_input_cancel);
		mOkBtn = (Button) mRootLayout.findViewById(R.id.share_input_ok);

		mShareInputEdit = (EditText) mRootLayout.findViewById(R.id.share_input_edit);

		mCanBtn.setOnClickListener(this);
		mOkBtn.setOnClickListener(this);

		mRootLayout.setOnTouchListener(this);
	}

	public void show() {
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		mParentLayout.addView(mRootLayout, lp);

		mShareInputEdit.setFocusableInTouchMode(true);
		mShareInputEdit.requestFocus();
		showSoft(mShareInputEdit);
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

}
