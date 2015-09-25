package cn.com.mobnote.golukmobile.newest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class RecomVideoActivity extends Activity implements OnClickListener, VideoSuqareManagerFn{
	private EditText text;
	private TextView number;
	private CharSequence temp;
	private String videoid;
	private int limitnumber = 50;
	private CustomLoadingDialog mCustomProgressDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.edit_video_dialog);
		
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			mVideoSquareManager.addVideoSquareManagerListener("RecomVideoActivity", this);
		}
		
		videoid = getIntent().getStringExtra("videoid");
		text = (EditText)findViewById(R.id.text);
		number = (TextView)findViewById(R.id.number);
		findViewById(R.id.cancle).setOnClickListener(this);
		findViewById(R.id.tuijian).setOnClickListener(this);
		text.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
				String str = text.getText().toString();
	            int len=str.length();
                if (temp.length() > limitnumber) {
                	str = str.substring(0, limitnumber); 
                	text.setText(str);
                	len = limitnumber;
                }
                
                len = limitnumber - len;
                number.setText(len + "");
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				  temp = s;
			}      	
        }); 
		
	}
	
	public void showProgressDialog() {
		if (null == mCustomProgressDialog) {
			mCustomProgressDialog = new CustomLoadingDialog(this,null);
		}
		
		if (!mCustomProgressDialog.isShowing()) {
			mCustomProgressDialog.show();
		}
		
	}
	
	public void closeProgressDialog(){
		if(null != mCustomProgressDialog){
			mCustomProgressDialog.close();
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.cancle:
			UserUtils.hideSoftMethod(this);
			finish();
			break;
		case R.id.tuijian:
			UserUtils.hideSoftMethod(this);
			if (!isNetworkConnected() ) {
				GolukUtils.showToast(this, "网络异常，请检查网络");
				return;
			}
			
			String textStr = text.getText().toString().trim();
			if(TextUtils.isEmpty(textStr)) {
				GolukUtils.showToast(this, "请填写推荐理由！");
			}else {
				showProgressDialog();
				try {
					String encodeStr = URLEncoder.encode(textStr, "UTF-8");
					boolean a = GolukApplication.getInstance().getVideoSquareManager().recomVideo("1", videoid, encodeStr);
					GolukDebugUtils.e("", "TTTTT=====tuijian====a="+a);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			break;

		default:
			break;
		}
	}
	
	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,Object param2) {
		if(event == VSquare_Req_VOP_RecomVideo){
			if (RESULE_SUCESS == msg) {
				closeProgressDialog();
				String msgStr = "推荐失败";
				try {
					JSONObject json = new JSONObject((String)param2);
					if(null != json) {
						boolean success = json.optBoolean("success");
						msgStr = json.optString("msg");
						if(success) {
							JSONObject data = json.optJSONObject("data");
							if(null != data) {
								String result = data.optString("result");
								if("0".equals(result)) {
									msgStr = "推荐成功";
								}
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				GolukUtils.showToast(RecomVideoActivity.this, msgStr);
				finish();
			}else {
				GolukUtils.showToast(this, "网络异常，请检查网络");
			}
		}
	}
	
	/**
	 * 检查是否有可用网络
	 * @return
	 * @author xuhw
	 * @date 2015年6月5日
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	} 
	
}
