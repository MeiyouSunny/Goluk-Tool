package cn.com.mobnote.golukmobile.videodetail;

import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.usercenter.UCUserInfo;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity;
import cn.com.mobnote.logic.GolukModule;
import cn.com.tiros.debug.GolukDebugUtils;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickHeadListener implements OnClickListener {

	private Context mContext;
	
	public ClickHeadListener(Context context) {
		this.mContext = context;
	}
	
	@Override
	public void onClick(View arg0) {
		clickToUserCenter();
	}
	
	/**
	 *跳转到UserCenterActivity 
	 */
	private void clickToUserCenter() {
		String info = GolukApplication.getInstance().mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage, 0, "");
        GolukDebugUtils.i("lily", "---IndexMore--------" + info);
        UCUserInfo user = new UCUserInfo();
        try {
            JSONObject json = new JSONObject(info);
            user.uid = json.getString("uid");
            user.nickname = json.getString("nickname");
            user.headportrait = json.getString("head");
            user.introduce = json.getString("desc");
            user.sex = json.getString("sex");
            user.customavatar = "";
            user.praisemenumber = json.getInt("praisemenumber")+"";
            user.sharevideonumber = json.getInt("sharevideonumber")+"";
        } catch (Exception e) {
            e.printStackTrace();
        }
		Intent it = new Intent(mContext, UserCenterActivity.class);
		it.putExtra("userinfo",user);
		mContext.startActivity(it);
	}

}
