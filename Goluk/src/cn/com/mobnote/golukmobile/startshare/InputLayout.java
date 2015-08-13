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
	/** 上下文，此刻应该是 VideoEditActivity */
	private Context mContext = null;
	private LayoutInflater mLayoutFlater = null;
	/** InputLayout的父布局 */
	private ViewGroup mParentLayout = null;
	/** 当前布局 */
	private RelativeLayout mRootLayout = null;
	/** 取消按钮 */
	private TextView mCanTv = null;
	/** 确定按钮 */
	private TextView mOkTv = null;
	/** 输入框 */
	private EditText mShareInputEdit = null;
	/** 显示输入計数器 */
	private TextView mCountTv = null;
	/** 最大输入描述字符数 */
	private final int MAX_COUNT = 50;
	/** 字体颜色，用于计数器平时显示 */
	private int mWhiteColor = 0;
	/** 字体颜色，用户计数器超出显示 */
	private int mRedColor = 0;
	/** 用于标识用户输入描述是否超长 */
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

	/**
	 * 加载资源
	 * 
	 * @author jyf
	 * @date 2015年7月30日
	 */
	private void loadRes() {
		mWhiteColor = mContext.getResources().getColor(R.color.color_input_count);
		mRedColor = mContext.getResources().getColor(R.color.red);
	}

	/**
	 * 显示用户输入界面
	 * 
	 * @param msg
	 *            输入框默认显示
	 * @author jyf
	 * @date 2015年7月30日
	 */
	public void show(String msg) {
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		mParentLayout.addView(mRootLayout, lp);

		mShareInputEdit.setFocusableInTouchMode(true);
		mShareInputEdit.requestFocus();
		showSoft(mShareInputEdit);
		mShareInputEdit.setText(msg);
		if (msg.length() > MAX_COUNT) {
			isInputValid = false;
		} else {
			isInputValid = true;
		}

		mCountTv.setTextColor(mWhiteColor);
		mCountTv.setText("" + (MAX_COUNT - msg.length()));

		mShareInputEdit.setSelection(msg.length());
	}

	/**
	 * 显示軟键盘
	 * 
	 * @param edit
	 * @author jyf
	 * @date 2015年7月30日
	 */
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

	/**
	 * 隐藏当前界面
	 * 
	 * @author jyf
	 * @date 2015年7月30日
	 */
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

	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

	}

}
