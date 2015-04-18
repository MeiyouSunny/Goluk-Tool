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
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class VideoSquareOnClickListener implements OnClickListener ,VideoSuqareManagerFn {
	
	Context mcontext;
	List<VideoSquareInfo> mVideoSquareListData;
	VideoSquareInfo mVideoSquareInfo;
	SharePlatformUtil sharePlatform;
	
	private CustomProgressDialog mCustomProgressDialog=null;
	
	public VideoSquareOnClickListener(Context context, List<VideoSquareInfo> videoSquareListData,VideoSquareInfo videoSquareInfo){
		mcontext = context;
		mVideoSquareListData = videoSquareListData;
		mVideoSquareInfo = videoSquareInfo;
		sharePlatform = new SharePlatformUtil(mcontext);
		sharePlatform.configPlatforms();//设置分享平台的参数
		
		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener("videosharehotlist", this);
	}
	
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.share_btn:
			System.out.println("sss");
			if(null == mCustomProgressDialog){
				mCustomProgressDialog = new CustomProgressDialog(mcontext);
				//mCustomProgressDialog.setCancelable(false);
				//mCustomProgressDialog.show();
			}
			
			boolean result = GolukApplication.getInstance().getVideoSquareManager().getShareUrl(mVideoSquareInfo.mVideoEntity.videoid, mVideoSquareInfo.mVideoEntity.type);
			if(!result){
				mCustomProgressDialog.dismiss();
			}
			break;
		case R.id.like_btn:
			Button dz = (Button) view;
			int likenumber = Integer.parseInt(dz.getText().toString());
			dz.setText((likenumber+1)+"");
			dz.setBackgroundResource(R.drawable.livestreaming_heart_btn_down);
			String videoid = mVideoSquareInfo.mVideoEntity.videoid;
			for(int i = 0;i<mVideoSquareListData.size();i++){
				VideoSquareInfo vsi =  mVideoSquareListData.get(i);
				if(vsi.mVideoEntity.videoid.equals(videoid)){
					mVideoSquareInfo.mVideoEntity.praisenumber = (likenumber + 1)+"";
					mVideoSquareListData.get(i).mVideoEntity.praisenumber = (likenumber + 1)+"";
					mVideoSquareListData.get(i).mVideoEntity.ispraise = "1";
					break;
				}
			}

			if(null != VideoSquareListView.mHandler){
				Message msg = new Message();
				msg.what = 1;
				msg.obj = mVideoSquareInfo;
				VideoSquareListView.mHandler.sendMessage(msg);
			}
			
			break;
		default:
			break;
		}
	}
	
	/**
	 * 关闭加载中对话框
	 * @author xuhw
	 * @date 2015年4月15日
	 */
	private void closeProgressDialog(){
		if(null != mCustomProgressDialog){
			if(mCustomProgressDialog.isShowing()){
				System.out.println("FFFFFdialog");
				mCustomProgressDialog.dismiss();
			}
		}
	}
	
	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,
			Object param2) {
		System.out.println("YYYY==888888==getSquareList====event=" + event
				+ "===msg=" + msg + "==param2=" + param2);
		closeProgressDialog();
		if (event == SquareCmd_Req_GetShareUrl) {
			//mPdsave.dismiss();
			if (RESULE_SUCESS == msg) {
				try {
					JSONObject result = new JSONObject((String)param2);
					if(result.getBoolean("success")){
						JSONObject data =  result.getJSONObject("data");
						String shareurl = data.getString("shorturl");
						String coverurl = data.getString("coverurl");
						if("".equals(coverurl)){
							
						}
						//设置分享内容
						sharePlatform.setShareContent(shareurl, coverurl, "goluk分享");
						VideoSquareActivity vsa = (VideoSquareActivity) mcontext;
						CustomShareBoard shareBoard = new CustomShareBoard(vsa);
				        shareBoard.showAtLocation(vsa.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
}
