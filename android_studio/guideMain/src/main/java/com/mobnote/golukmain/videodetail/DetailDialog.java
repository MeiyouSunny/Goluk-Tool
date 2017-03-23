package com.mobnote.golukmain.videodetail;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.newest.RecomVideoActivity;
import com.mobnote.util.GolukUtils;

public class DetailDialog extends Dialog implements android.view.View.OnClickListener {

	private TextView tuijian;
	private TextView jubao;
	private TextView mShare;
	private TextView back;
	private TextView cancle;
	private Context mContext;
	private VideoDetailRetBean mVideoDetailRetBean = null;
	private AlertDialog ad;
	private AlertDialog confirmation;
	private boolean mIsMy = false;

	public DetailDialog(Context context, VideoDetailRetBean videoDetailRetBean, boolean isMy) {
		super(context, R.style.CustomDialog);
		mIsMy = isMy;
		LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.video_detail_dialog,
				null);
		if (isMy) {
			// 如果是自己发布的视频，则显示 “删除这个视频”
			TextView tv = (TextView) linearLayout.findViewById(R.id.jubao);
			tv.setText(context.getString(R.string.dialog_str_del));
		}
		setContentView(linearLayout);
		Window window = this.getWindow();
		window.setGravity(Gravity.BOTTOM);
		this.mVideoDetailRetBean = videoDetailRetBean;
		mContext = context;
		initLayout();
	}

	private void initLayout() {
		this.tuijian = (TextView) findViewById(R.id.tuijian);
		this.jubao = (TextView) findViewById(R.id.jubao);
		mShare = (TextView) findViewById(R.id.tv_dialog_item_share);
		this.cancle = (TextView) findViewById(R.id.cancle);
		this.back = (TextView) findViewById(R.id.back);
		tuijian.setOnClickListener(this);
		jubao.setOnClickListener(this);
		mShare.setOnClickListener(this);
		cancle.setOnClickListener(this);
		back.setOnClickListener(this);
	}

	// 删除自己发布的视频
	private void click_delVideo() {
		if (null != mContext) {
			if (mContext instanceof VideoDetailActivity) {
				((VideoDetailActivity) mContext).delVideo();
			}
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.tuijian) {
			dismiss();
			Intent intent = new Intent(mContext, RecomVideoActivity.class);
			intent.putExtra("videoid", mVideoDetailRetBean.data.avideo.video.videoid);
			mContext.startActivity(intent);
		} else if (id == R.id.jubao) {
			dismiss();
			if (this.mIsMy) {
				// 删除这个视频
				click_delVideo();
			} else {
				// 显示举报对话框
				showDialog();
			}
		} else if (id == R.id.tv_dialog_item_share) {
			dismiss();
			if (null != mContext) {
				if (mContext instanceof VideoDetailActivity) {
					((VideoDetailActivity) mContext).getShare();
				}
			}
		} else if (id == R.id.back) {
			dismiss();
			Intent it = new Intent(mContext, MainActivity.class);
			mContext.startActivity(it);
		} else if (id == R.id.cancle) {
			dismiss();
		} else {
		}
	}

	/**
	 * 弹出举报的窗口
	 * 
	 * @Title: showDialog
	 * @Description: TODO void
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
					Toast.makeText(mContext,mContext.getString(R.string.network_error),Toast.LENGTH_SHORT).show();
					confirmation.dismiss();
					return;
				}

				boolean flog = GolukApplication.getInstance().getVideoSquareManager()
						.report("1", mVideoDetailRetBean.data.avideo.video.videoid, reporttype);
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
