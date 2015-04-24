package cn.com.mobnote.golukmobile.videosuqare;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.SharePlatformUtil;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomProgressDialog;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.umeng.widget.CustomShareBoard;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class VideoSquareOnClickListener implements OnClickListener,
		VideoSuqareManagerFn {

	Context mcontext;
	List<VideoSquareInfo> mVideoSquareListData;
	VideoSquareInfo mVideoSquareInfo;
	SharePlatformUtil sharePlatform;
	int form = 1;
	AlertDialog dialog;

	AlertDialog ad;

	AlertDialog confirmation;

	public static CustomProgressDialog mCustomProgressDialog = null;

	public VideoSquareOnClickListener(Context context,
			List<VideoSquareInfo> videoSquareListData,
			VideoSquareInfo videoSquareInfo, int plform) {
		mcontext = context;
		mVideoSquareListData = videoSquareListData;
		mVideoSquareInfo = videoSquareInfo;
		sharePlatform = new SharePlatformUtil(mcontext);
		// sharePlatform.configPlatforms();//设置分享平台的参数
		form = plform;
		GolukApplication.getInstance().getVideoSquareManager()
				.addVideoSquareManagerListener("videosharehotlist", this);
		if (null == mCustomProgressDialog) {
			mCustomProgressDialog = new CustomProgressDialog(mcontext);
		}
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.share_btn:
			System.out.println("sss");
			if (null != mCustomProgressDialog) {
				mCustomProgressDialog.show();
			}
			if (mcontext instanceof VideoSquareActivity) {
				VideoSquareActivity vsa = (VideoSquareActivity) mcontext;
				vsa.shareVideoId = mVideoSquareInfo.mVideoEntity.videoid;
				System.out.println("shareid=" + vsa.shareVideoId);
			} else if (mcontext instanceof VideoSquarePlayActivity) {
				VideoSquarePlayActivity vspa = (VideoSquarePlayActivity) mcontext;
				vspa.shareVideoId = mVideoSquareInfo.mVideoEntity.videoid;
				System.out.println("shareid=" + vspa.shareVideoId);
			}

			boolean result = GolukApplication
					.getInstance()
					.getVideoSquareManager()
					.getShareUrl(mVideoSquareInfo.mVideoEntity.videoid,
							mVideoSquareInfo.mVideoEntity.type);
			System.out.println("YYYY+RESULT3333333333" + result);
			if (!result) {
				mCustomProgressDialog.dismiss();
			}
			break;
		case R.id.like_btn:
			
			if ("0".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {//没有点过赞
				Button dz = (Button) view;
				int likenumber = Integer.parseInt(dz.getText().toString());
				dz.setText((likenumber + 1) + "");
				dz.setBackgroundResource(R.drawable.livestreaming_heart_btn_down);
				String videoid = mVideoSquareInfo.mVideoEntity.videoid;
				for (int i = 0; i < mVideoSquareListData.size(); i++) {
					VideoSquareInfo vsi = mVideoSquareListData.get(i);
					if (vsi.mVideoEntity.videoid.equals(videoid)) {
						mVideoSquareInfo.mVideoEntity.praisenumber = (likenumber + 1)
								+ "";
						mVideoSquareListData.get(i).mVideoEntity.praisenumber = (likenumber + 1)
								+ "";
						mVideoSquareListData.get(i).mVideoEntity.ispraise = "1";
						break;
					}
				}

				boolean flog = GolukApplication
						.getInstance()
						.getVideoSquareManager()
						.clickPraise("1",mVideoSquareInfo.mVideoEntity.videoid, "1");
				
				if (null != VideoSquareListView.mHandler) {
					Message msg = new Message();
					msg.what = 1;
					msg.obj = mVideoSquareInfo;
					VideoSquareListView.mHandler.sendMessage(msg);
				}
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
		dialog = new AlertDialog.Builder(mcontext).create();
		dialog.show();
		dialog.getWindow().setContentView(R.layout.video_square_dialog_main);
		dialog.getWindow().findViewById(R.id.report)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						ad = new AlertDialog.Builder(mcontext).create();
						ad.show();
						ad.getWindow().setContentView(
								R.layout.video_square_dialog_selected);
						ad.getWindow().findViewById(R.id.sqds)
								.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										confirmation("1");
									}
								});
						ad.getWindow().findViewById(R.id.yyhz)
								.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										confirmation("2");
									}
								});
						ad.getWindow().findViewById(R.id.zzmg)
								.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										confirmation("3");
									}
								});
						ad.getWindow().findViewById(R.id.qtyy)
								.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										confirmation("4");
									}
								});
						ad.getWindow().findViewById(R.id.qx)
								.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										ad.dismiss();
									}
								});
					}
				});

		dialog.getWindow().findViewById(R.id.exit)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
	}

	public void confirmation(final String reporttype) {
		ad.dismiss();
		confirmation = new AlertDialog.Builder(mcontext).create();
		confirmation.show();
		confirmation.getWindow().setContentView(
				R.layout.video_square_dialog_confirmation);
		confirmation.getWindow().findViewById(R.id.sure)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						boolean flog = GolukApplication
								.getInstance()
								.getVideoSquareManager()
								.report("1",
										mVideoSquareInfo.mVideoEntity.videoid,
										reporttype);
						if (flog) {
							Toast.makeText(mcontext, "举报成功，我们稍后会进行处理",
									Toast.LENGTH_SHORT).show();
						}
						confirmation.dismiss();
					}
				});
		confirmation.getWindow().findViewById(R.id.exit)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						confirmation.dismiss();
					}
				});
	}

	/**
	 * 关闭加载中对话框
	 * 
	 * @author xuhw
	 * @date 2015年4月15日
	 */
	private void closeProgressDialog() {
		if (null != mCustomProgressDialog) {
			if (mCustomProgressDialog.isShowing()) {
				System.out.println("FFFFFdialog");
				mCustomProgressDialog.dismiss();
			}
		}
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,
			Object param2) {
		System.out.println("YYYY==888888==getSquareList8888====form===" + form
				+ "event=" + event + "===msg=" + msg + "==param2=" + param2);

		System.out.println("YYYY+RESULT-2-2-2-2-2-2-2");
		if (event == SquareCmd_Req_GetShareUrl) {
			System.out.println("YYYY+RESULT-3-3-3-3-3-3-3");
			closeProgressDialog();
			if (RESULE_SUCESS == msg) {
				try {
					System.out.println("YYYY+RESULT-1-1-1-1-1-1-1");
					JSONObject result = new JSONObject((String) param2);
					System.out.println("YYYY+RESULT00000000");
					if (result.getBoolean("success")) {
						JSONObject data = result.getJSONObject("data");
						String shareurl = data.getString("shorturl");
						String coverurl = data.getString("coverurl");
						if ("".equals(coverurl)) {

						}
						System.out.println("YYYY+RESULT11111111");
						// 设置分享内容
						sharePlatform.setShareContent(shareurl, coverurl,
								"goluk分享");
						System.out.println("YYYY+RESULT22222222");

						if (form == 2) {
							VideoSquarePlayActivity vspa = (VideoSquarePlayActivity) mcontext;
							if (!vspa.isFinishing()) {
								CustomShareBoard shareBoard = new CustomShareBoard(
										vspa);
								shareBoard.showAtLocation(vspa.getWindow()
										.getDecorView(), Gravity.BOTTOM, 0, 0);
							}

						} else {
							VideoSquareActivity vsa = (VideoSquareActivity) mcontext;
							if (!vsa.isFinishing()) {
								CustomShareBoard shareBoard = new CustomShareBoard(
										vsa);
								shareBoard.showAtLocation(vsa.getWindow()
										.getDecorView(), Gravity.BOTTOM, 0, 0);
							}

						}

					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}
