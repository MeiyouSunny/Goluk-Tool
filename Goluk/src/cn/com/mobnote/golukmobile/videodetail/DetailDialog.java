package cn.com.mobnote.golukmobile.videodetail;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.newest.RecomVideoActivity;
import cn.com.mobnote.util.GolukUtils;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class DetailDialog extends Dialog implements android.view.View.OnClickListener {

	private TextView tuijian;
	private TextView jubao;
	private TextView mShare;
	private TextView back;
	private TextView cancle;
	private Context mContext;
	private VideoJson mVideoJson = null;
	private AlertDialog ad;
	private AlertDialog confirmation;

	public DetailDialog(Context context, VideoJson videoJson) {
		super(context, R.style.CustomDialog);
		setContentView(R.layout.video_detail_dialog);
		
		Window window = this.getWindow();
		window.setGravity(Gravity.BOTTOM);
		
		this.mVideoJson = videoJson;
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tuijian:
			dismiss();
			Intent intent = new Intent(mContext, RecomVideoActivity.class);
			intent.putExtra("videoid", mVideoJson.data.avideo.video.videoid);
			mContext.startActivity(intent);
			break;
		case R.id.jubao:
			dismiss();
			showDialog();
			break;
		case R.id.tv_dialog_item_share:
			dismiss();
			if(null != mContext) {
				if(mContext instanceof VideoDetailActivity) {
					((VideoDetailActivity)mContext).getShare();
				}
			}
			break;
		case R.id.back:
			dismiss();
			Intent it = new Intent(mContext,MainActivity.class);
			mContext.startActivity(it);
			break;
		case R.id.cancle:
			dismiss();
			break;
		default:
			break;
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
				boolean flog = GolukApplication.getInstance().getVideoSquareManager().report("1", mVideoJson.data.avideo.video.videoid, reporttype);
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
