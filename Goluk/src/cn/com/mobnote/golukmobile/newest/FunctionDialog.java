package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.util.GolukUtils;
import android.app.AlertDialog;
import android.app.Dialog; 
import android.content.Context;
import android.content.Intent;
import android.view.View;  
import android.widget.TextView;

public class FunctionDialog extends Dialog implements android.view.View.OnClickListener{ 
	private TextView tuijian;
	private TextView jubao;
	private TextView cancle;
	private Context mContext;
	private String  videoid;
	private AlertDialog dialog;
	private AlertDialog ad;
	private AlertDialog confirmation;
	 
	public FunctionDialog(Context context, String vid) { 
		super(context, R.style.CustomDialog);  
		setContentView(R.layout.function_dialog);
		this.videoid = vid;
		mContext = context;
		initLayout();
	}
	
	private void initLayout(){ 
		this.tuijian = (TextView)findViewById(R.id.tuijian);
		this.jubao = (TextView)findViewById(R.id.jubao);
		this.cancle = (TextView)findViewById(R.id.cancle);
		tuijian.setOnClickListener(this);
		jubao.setOnClickListener(this);
		cancle.setOnClickListener(this);
	}
 
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tuijian:
			dismiss();
			Intent intent = new Intent(mContext, RecomVideoActivity.class);
			intent.putExtra("videoid", videoid);
			mContext.startActivity(intent);
			break;
		case R.id.jubao:
			dismiss();
			showDialog();
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
	 * @author 曾浩
	 * @throws
	 */
	public void showDialog() {
//		dialog = new AlertDialog.Builder(mContext, R.style.CustomDialog).create();
//		dialog.show();
//		dialog.getWindow().setContentView(R.layout.video_square_dialog_main);
//		dialog.getWindow().findViewById(R.id.report).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				dialog.dismiss();
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
//			}
//		});
//
//		dialog.getWindow().findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				dialog.dismiss();
//			}
//		});
	}

	public void confirmation(final String reporttype) {
		ad.dismiss();
		confirmation = new AlertDialog.Builder(mContext, R.style.CustomDialog).create();
		confirmation.show();
		confirmation.getWindow().setContentView(R.layout.video_square_dialog_confirmation);
		confirmation.getWindow().findViewById(R.id.sure).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean flog = GolukApplication.getInstance().getVideoSquareManager()
						.report("1", videoid, reporttype);
				if (flog) {
					GolukUtils.showToast(mContext, "举报成功，我们稍后会进行处理");
				} else {
					GolukUtils.showToast(mContext, "网络异常，请检查网络");
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

