package cn.com.mobnote.golukmobile.videosuqare;
import java.text.SimpleDateFormat;
import java.util.List;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.util.GolukUtils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class VideoSquareOnClickListener implements OnClickListener{

	Context mcontext;
	List<VideoSquareInfo> mVideoSquareListData;
	public VideoSquareInfo mVideoSquareInfo;
	SharePlatformUtil sharePlatform;
	int form = 1;
	AlertDialog dialog;

	AlertDialog ad;

	AlertDialog confirmation;
	VideoSquareListViewAdapter mVideoSquareListViewAdapter;
	
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日 HH时mm分ss秒");

	public VideoSquareOnClickListener(Context context, List<VideoSquareInfo> videoSquareListData,
			final VideoSquareInfo videoSquareInfo, int plform, SharePlatformUtil spf,
			VideoSquareListViewAdapter _mVideoSquareListViewAdapter) {
		mcontext = context;
		mVideoSquareListData = videoSquareListData;
		mVideoSquareInfo = videoSquareInfo;
		sharePlatform = spf;
		mVideoSquareListViewAdapter = _mVideoSquareListViewAdapter;
		form = plform;

	}
	
	@Override
	public void onClick(View view) {
		mVideoSquareListViewAdapter.setOnClick(this);
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.share_btn:
			if (mcontext instanceof MainActivity) {
				MainActivity vsa = (MainActivity) mcontext;
				vsa.shareVideoId = mVideoSquareInfo.mVideoEntity.videoid;
				vsa.mCustomProgressDialog = new CustomLoadingDialog(vsa, null);
				vsa.mCustomProgressDialog.show();
				boolean result = GolukApplication.getInstance().getVideoSquareManager()
						.getShareUrl(mVideoSquareInfo.mVideoEntity.videoid, mVideoSquareInfo.mVideoEntity.type);
				if (!result) {
					vsa.mCustomProgressDialog.close();
					GolukUtils.showToast(mcontext, "网络异常，请检查网络");
				}
			} else if (mcontext instanceof VideoSquarePlayActivity) {
				VideoSquarePlayActivity vspa = (VideoSquarePlayActivity) mcontext;
				vspa.mCustomProgressDialog = new CustomLoadingDialog(vspa, null);
				vspa.mCustomProgressDialog.show();

				vspa.shareVideoId = mVideoSquareInfo.mVideoEntity.videoid;

				boolean result = GolukApplication.getInstance().getVideoSquareManager()
						.getShareUrl(mVideoSquareInfo.mVideoEntity.videoid, mVideoSquareInfo.mVideoEntity.type);
				if (!result) {
					vspa.mCustomProgressDialog.close();
				}
			}
			
			break;
		case R.id.like_btn:
			String praise = "0";
			Button dz = (Button) view;
			int likenumber = 0;
			if ("0".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {// 没有点过赞
				
				likenumber = Integer.parseInt(dz.getText().toString())+1;
				dz.setText(likenumber+"");
				
				Drawable drawable= mcontext.getResources().getDrawable(R.drawable.like_btn_press);  
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());  
				dz.setCompoundDrawables(drawable,null,null,null);  
				
				praise = "1";
				

				GolukApplication.getInstance().getVideoSquareManager()
						.clickPraise("1", mVideoSquareInfo.mVideoEntity.videoid, "1");

				
			}else{
				likenumber = Integer.parseInt(dz.getText().toString())-1;
				dz.setText(likenumber+"");
				
				Drawable drawable= mcontext.getResources().getDrawable(R.drawable.like_btn);  
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());  
				dz.setCompoundDrawables(drawable,null,null,null);  
				
				praise = "0";
			}
			
			
			String videoid = mVideoSquareInfo.mVideoEntity.videoid;
			
			for (int i = 0; i < mVideoSquareListData.size(); i++) {
				VideoSquareInfo vsi = mVideoSquareListData.get(i);
				if (vsi.mVideoEntity.videoid.equals(videoid)) {
					mVideoSquareInfo.mVideoEntity.praisenumber = likenumber + "";
					mVideoSquareListData.get(i).mVideoEntity.praisenumber = likenumber + "";
					mVideoSquareListData.get(i).mVideoEntity.ispraise = praise;
					break;
				}
			}
			
			if (null != VideoSquareListView.mHandler) {
				Message msg = new Message();
				msg.what = 1;
				msg.obj = mVideoSquareInfo;
				VideoSquareListView.mHandler.sendMessage(msg);
			}
			break;
		case R.id.report_icon:
			this.showDialog();
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
		dialog = new AlertDialog.Builder(mcontext,R.style.CustomDialog).create();
		dialog.show();
		dialog.getWindow().setContentView(R.layout.video_square_dialog_main);
		dialog.getWindow().findViewById(R.id.report).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				ad = new AlertDialog.Builder(mcontext,R.style.CustomDialog).create();
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
		});

		dialog.getWindow().findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	public void confirmation(final String reporttype) {
		ad.dismiss();
		confirmation = new AlertDialog.Builder(mcontext,R.style.CustomDialog).create();
		confirmation.show();
		confirmation.getWindow().setContentView(R.layout.video_square_dialog_confirmation);
		confirmation.getWindow().findViewById(R.id.sure).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean flog = GolukApplication.getInstance().getVideoSquareManager()
						.report("1", mVideoSquareInfo.mVideoEntity.videoid, reporttype);
				if (flog) {
					GolukUtils.showToast(mcontext, "举报成功，我们稍后会进行处理");
				}else{
					GolukUtils.showToast(mcontext, "网络异常，请检查网络");
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

	String getNikename() {
		return mVideoSquareInfo.mUserEntity.nickname;
	}


	/**
	 * 关闭之前请求的dialog
	 * 
	 * @Title: closeRqsDialog
	 * @Description: TODO
	 * @param context
	 *            void
	 * @author 曾浩
	 * @throws
	 */
	public void closeRqsDialog(Context context) {
		if (mcontext instanceof VideoSquarePlayActivity) {
			VideoSquarePlayActivity vspa = (VideoSquarePlayActivity) mcontext;
			if (vspa != null && !vspa.isFinishing()) {
				vspa.mCustomProgressDialog.close();
			}

		} else if (mcontext instanceof MainActivity) {
			MainActivity vsa = (MainActivity) mcontext;
			if (vsa != null && !vsa.isFinishing()) {
				if (vsa.mCustomProgressDialog != null) {
					vsa.mCustomProgressDialog.close();
				}
			}
		}
	}

	

}
