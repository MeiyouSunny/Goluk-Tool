package com.mobnote.golukmain.newest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.util.GolukUtils;

public class FunctionDialog extends Dialog implements android.view.View.OnClickListener {
	private TextView tuijian;
	private TextView jubao;
	private TextView cancle;
	private Context mContext;
	private String videoid;
	private AlertDialog ad;
	private AlertDialog confirmation;
	private LinearLayout mDelLayout = null;
	private LinearLayout mConfirmLayout = null;
	private boolean mIsDel = false;
	private IDialogDealFn mListener = null;
	/** 是否可以举报 */
	private boolean isCanConfirm = true;

	public FunctionDialog(Context context, String vid, boolean isDel, IDialogDealFn fn) {
		super(context, R.style.CustomDialog);
		setContentView(R.layout.function_dialog);
		this.videoid = vid;
		mContext = context;
		mIsDel = isDel;
		mListener = fn;
		initLayout();
	}

	/**
	 * 设置是否允许举报
	 * 
	 * @param isConfirm
	 * @author jyf
	 */
	public FunctionDialog setConfirm(boolean isConfirm) {
		isCanConfirm = isConfirm;
		if (null != mConfirmLayout) {
			if (isCanConfirm) {
				mConfirmLayout.setVisibility(View.VISIBLE);
			} else {
				mConfirmLayout.setVisibility(View.GONE);
			}

		}
		return this;
	}

	private void initLayout() {
		this.tuijian = (TextView) findViewById(R.id.tuijian);
		this.jubao = (TextView) findViewById(R.id.jubao);
		this.cancle = (TextView) findViewById(R.id.cancle);
		mDelLayout = (LinearLayout) findViewById(R.id.fun_dialog_del_layout);
		mConfirmLayout = (LinearLayout) findViewById(R.id.fun_dialog_confirm_layout);
		tuijian.setOnClickListener(this);
		jubao.setOnClickListener(this);
		cancle.setOnClickListener(this);

		if (mIsDel) {
			mDelLayout.setVisibility(View.VISIBLE);
			mDelLayout.setOnClickListener(this);
		} else {
			mDelLayout.setVisibility(View.GONE);
		}

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.fun_dialog_del_layout) {
			click_Del();
		} else if (id == R.id.tuijian) {
		} else if (id == R.id.jubao) {
			dismiss();
			showDialog();
		} else if (id == R.id.cancle) {
			dismiss();
		} else {
		}
	}

	private void click_Del() {
		dismiss();
		if (null != mListener) {
			mListener.CallBack_Del(IDialogDealFn.OPERATOR_DEL, videoid);
		}
	}

	/**
	 * 弹出举报的窗口
	 * 
	 * @Title: showDialog
	 * @Description:
	 * @author 曾浩
	 * @throws
	 */
	public void showDialog() {
		ad = new AlertDialog.Builder(mContext, R.style.CustomDialog).create();
		ad.show();
		ad.getWindow().setContentView(R.layout.video_square_dialog_selected);
		ad.getWindow().findViewById(R.id.sqds).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmation("1");
			}
		});
		ad.getWindow().findViewById(R.id.yyhz).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmation("2");
			}
		});
		ad.getWindow().findViewById(R.id.zzmg).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmation("3");
			}
		});
		ad.getWindow().findViewById(R.id.qtyy).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmation("4");
			}
		});
		ad.getWindow().findViewById(R.id.qx).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ad.dismiss();
			}
		});
	}

	public void confirmation(final String reporttype) {
		ad.dismiss();
		confirmation = new AlertDialog.Builder(mContext, R.style.CustomDialog).create();
		confirmation.show();
		confirmation.getWindow().setContentView(R.layout.video_square_dialog_confirmation);
		confirmation.getWindow().findViewById(R.id.sure).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!GolukUtils.isNetworkConnected(mContext)) {
					GolukUtils.showToast(mContext, mContext.getString(R.string.network_error));
					confirmation.dismiss();
					return;
				}

				boolean flog = GolukApplication.getInstance().getVideoSquareManager().report("1", videoid, reporttype);
				if (flog) {
					GolukUtils.showToast(mContext, mContext.getString(R.string.str_report_success));
				} else {
					GolukUtils.showToast(mContext, mContext.getString(R.string.network_error));
				}
				confirmation.dismiss();
			}
		});
		confirmation.getWindow().findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmation.dismiss();
			}
		});
	}

}
